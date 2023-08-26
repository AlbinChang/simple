package spring.simple.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import spring.simple.sequence.service.SeqService;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {

    ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);

    SeqService seqService = applicationContext.getBean(SeqService.class);

    for (int i = 0; i < 1000000; i++) {
      System.out.println(seqService.getNextNumber());
    }

  }
}
