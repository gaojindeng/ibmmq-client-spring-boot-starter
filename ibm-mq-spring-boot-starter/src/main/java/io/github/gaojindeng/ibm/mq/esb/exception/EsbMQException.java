package io.github.gaojindeng.ibm.mq.esb.exception;


public class EsbMQException extends RuntimeException {

    public EsbMQException(String msg, Exception e) {
        super(msg, e);
    }

    public EsbMQException(String msg) {
        super(msg);
    }

}
