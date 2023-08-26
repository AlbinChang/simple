package spring.simple.sequence.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description 机器序号注册表n机器序号注册表需要初始化10000条序号：0-9999
 * @author spring
 * @date 2023-08-26
 */
@Data
public class SeqRegistry implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 机器序号
   */
  private Integer seqNo;

  /**
   * jvm 示例 ip:pid
   */
  private String jvmInstance;

  /**
   * 续期时间
   */
  private LocalDateTime renewalTime;

  /**
   * 版本号
   */
  private Long version;

  public SeqRegistry() {}
}
