package spring.simple.sequence.service;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.simple.sequence.model.SeqRegistry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 章文斌
 */
@Slf4j
public class SeqService {

    private JdbcTemplate jdbcTemplate;

    private static AtomicInteger atomicInteger = new AtomicInteger(0);


    private static ExecutorService exec = new ThreadPoolExecutor(1, 1,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(),
        r -> new Thread(r, "Sequence-Task-" + atomicInteger.incrementAndGet()));


    private final RegisterTask registerTask;

    public SeqService(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;

        registerTask = new RegisterTask();

        exec.submit(registerTask);
    }

    private final AtomicInteger seq = new AtomicInteger(0);

    private String getNextSeq()
    {

         int temp = seq.get();
         if( temp >= 10000 )
         {
            //防止多次重置为 0
            seq.compareAndSet( temp , 0  );
         }

        return StrUtil.padPre(  String.valueOf( seq.incrementAndGet())  , 4, "0" );

    }


    public String getNextNumber()
    {
        while (registerTask.seqNoStr == null )
        {
            Thread.yield();
        }

        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd") )
             + System.currentTimeMillis() % 1_0000_0000 + getNextSeq() + registerTask.seqNoStr;

    }

    private class RegisterTask implements Runnable{


      private final String QUERY_SQL =
          "select * from seq_registry where renewal_time is null or renewal_time < date_add(now(), interval -1 minute) ";

      private final String GET_SQL =
          "select * from seq_registry where `seq_no` = ? AND `version` = ? ";


      private final String UPDATE_SQL =
          "UPDATE seq_registry SET `jvm_instance` = ? , `renewal_time` = ? , `version` = ? WHERE `seq_no` = ? AND `version` = ? ";

      private boolean registered = false;

      private final String ip = NetUtil.getLocalhostStr();

      private final long pid =  SystemUtil.getCurrentPID();

      private final String ipPid = ip + ":" + pid;

      private Integer seqNo;

      private volatile String seqNoStr;

      private Long version;

      private BeanPropertyRowMapper<SeqRegistry> rowMapper =  new BeanPropertyRowMapper(SeqRegistry.class);

      @Override
      public void run() {

        while (true) {

          try {

            if (!registered) {
              List<SeqRegistry> registryList = jdbcTemplate.query(QUERY_SQL,  rowMapper );

              for (SeqRegistry seqRegistry : registryList) {
                if (jdbcTemplate.update(UPDATE_SQL,
                    ipPid, LocalDateTime.now(), seqRegistry.getVersion() + 1L,
                    seqRegistry.getSeqNo(), seqRegistry.getVersion()
                ) == 1) {

                  seqNo = seqRegistry.getSeqNo();
                  seqNoStr = StrUtil.padPre(seqNo.toString(), 4, "0");
                  version = seqRegistry.getVersion() + 1L;
                  registered = true;
                  log.info("机器序号{}注册成功，本次版本号{}", seqNo  ,version );
                  //如果注册成功，退出循环
                  break;
                }
              }

            } else {

               try {
                 SeqRegistry seqRegistry = jdbcTemplate.queryForObject(GET_SQL, rowMapper, seqNo, version);

                 if( jdbcTemplate.update(UPDATE_SQL,
                     ipPid, LocalDateTime.now(), seqRegistry.getVersion() + 1L,
                     seqRegistry.getSeqNo(), seqRegistry.getVersion()
                    )==1  )
                 {
                   version = seqRegistry.getVersion() + 1L;
                   log.info("机器序号{}续期成功，本次版本号{}",seqNo  ,version );
                 }
                 else
                 {
                   //机器序号续期失败，需要重新注册机器序号
                   registered = false;
                   log.warn("机器序号{}续期失败：上次版本号{}",seqNo , version );
                 }

               }
               catch (DataAccessException dataAccessException)
               {
                 //机器序号续期失败，需要重新注册机器序号
                 registered = false;
                 log.warn("机器序号{}续期失败：上次版本号{}", seqNo , version );
                 log.error("机器序号续期失败：", dataAccessException);
               }
            }

            Thread.sleep(10000);

          }catch (Exception ex)
          {
             log.warn("序号注册任务发生异常:",ex);
          }

        }

      }
    }




}
