package spring.simple.sequence.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import spring.simple.sequence.service.SeqService;

import javax.sql.DataSource;

@Configuration
public class SequenceAutoConfiguration implements ApplicationContextAware {

  @Value("${sequence.datasourceid:primary}")
  private String dataSourceId;

  private ApplicationContext applicationContext;

//  @Bean("seqDataSource")
  public DataSource getDataSource()
  {
      if(StrUtil.equalsIgnoreCase("primary", dataSourceId ))
      {
        return applicationContext.getBean(DataSource.class);
      }
      else
      {
        return applicationContext.getBean(dataSourceId, DataSource.class);
      }
  }

  @Bean("seqJdbcTemplate")
  public JdbcTemplate getJdbcTemplate()
  {
      return new JdbcTemplate(getDataSource());
  }


  @Bean
  public SeqService getSeqService()
  {
      return new SeqService(getJdbcTemplate());
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

       this.applicationContext = applicationContext;

  }


}
