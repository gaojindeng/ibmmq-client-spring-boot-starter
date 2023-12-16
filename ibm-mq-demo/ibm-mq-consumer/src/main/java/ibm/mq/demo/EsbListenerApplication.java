package ibm.mq.demo;

import io.github.gaojindeng.ibm.mq.esb.EnableEsbClientMQ;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableEsbClientMQ
public class EsbListenerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EsbListenerApplication.class, args);
    }

}
