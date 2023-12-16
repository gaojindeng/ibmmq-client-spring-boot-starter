package io.github.gaojindeng.ibm.mq.esb;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({QueueTransportSupport.class})
public @interface EnableEsbClientMQ {
}
