package io.github.gaojindeng.ibm.mq.esb.handler;


import io.github.gaojindeng.ibm.mq.esb.message.Message;
import io.github.gaojindeng.ibm.mq.esb.message.TimeoutMessage;

public interface IQueueTransport {

    /**
     * 发送并接收消息
     *
     * @param requestMessage
     * @return
     */
    Message submit(Message requestMessage);

    /**
     * 仅发送消息
     *
     * @param requestMessage
     */
    void onlySend(Message requestMessage);

    /**
     * 丢弃消息
     *
     * @param timeoutMessage
     */
    void discard(TimeoutMessage timeoutMessage);
}
