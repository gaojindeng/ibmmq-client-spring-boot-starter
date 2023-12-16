package io.github.gaojindeng.ibm.mq.esb.handler;

import io.github.gaojindeng.ibm.mq.esb.exception.EsbMQException;
import io.github.gaojindeng.ibm.mq.esb.pool.CustomMQQueueManager;
import io.github.gaojindeng.ibm.mq.esb.pool.QueueConnectFactory;
import io.github.gaojindeng.ibm.mq.esb.pool.QueueConnectionPool;
import io.github.gaojindeng.ibm.mq.esb.message.Message;
import io.github.gaojindeng.ibm.mq.esb.message.TimeoutMessage;
import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueueTransport implements IQueueTransport {

    private static final Logger log = LoggerFactory.getLogger(AbstractQueueTransport.class);

    /**
     * queue通道的唯一key
     */
    private String key;
    /**
     * 发送队列的名称
     */
    private String sendQueueName;
    /**
     * 接收队列的名称
     */
    private String receiveQueueName;
    /**
     * 消费端系统名称
     */
    private String receiveSystemName;

    /**
     * 连接池
     */
    private QueueConnectionPool queueConnectionPool;

    /**
     * 同步等待超时时间
     */
    private int syncTimeout;

    public AbstractQueueTransport(String key, String sendQueueName, String receiveQueueName, String receiveSystemName, QueueConnectionPool queueConnectionPool, int syncTimeout) {
        this.key = key;
        this.sendQueueName = sendQueueName;
        this.receiveQueueName = receiveQueueName;
        this.receiveSystemName = receiveSystemName;
        this.queueConnectionPool = queueConnectionPool;
        this.syncTimeout = syncTimeout;
    }

    @Override
    public Message submit(Message requestMessage) {
        CustomMQQueueManager queueManager = null;
        byte[] msgId = null;
        try {
            //获取queueManger
            queueManager = queueConnectionPool.getQueueManager(sendQueueName, receiveQueueName, receiveSystemName);

            //发送消息
            MQMessage send = send(queueManager, requestMessage);
            msgId = send.messageId;

            //返回消息
            return receive(queueManager, send.messageId, requestMessage.getTimeWaitInterval());
        } catch (Exception e) {
            if (e instanceof MQException) {
                MQException mqe = (MQException) e;
                // 超时放到延迟队列中
                if (mqe.getReason() == 2033 && msgId != null) {
                    QueueConnectFactory.delayQueue.offer(new TimeoutMessage(msgId, key));
                }
                throw new EsbMQException("message timeout", e);
            } else {
                throw new EsbMQException("submit message error", e);
            }
        } finally {
            if (queueManager != null) {
                queueConnectionPool.offerPool(queueManager, receiveSystemName);
            }
        }

    }

    @Override
    public void onlySend(Message requestMessage) {
        CustomMQQueueManager queueManager = null;
        try {
            queueManager = queueConnectionPool.getQueueManager(sendQueueName, receiveQueueName, receiveSystemName);
            send(queueManager, requestMessage);
        } catch (Exception e) {
            throw new EsbMQException("send message error", e);
        } finally {
            if (queueManager != null) {
                queueConnectionPool.offerPool(queueManager, receiveSystemName);
            }
        }
    }

    @Override
    public void discard(TimeoutMessage timeoutMessage) {
        CustomMQQueueManager queueManager = null;
        try {
            queueManager = queueConnectionPool.getQueueManager(sendQueueName, receiveQueueName, receiveSystemName);
            receive(queueManager, timeoutMessage.getMessageId(), 10);
            log.info("discard messageId:{}", new String(timeoutMessage.getMessageId()));
        } catch (Exception e) {
            log.error("discard error messageId:{}", new String(timeoutMessage.getMessageId()), e);
        } finally {
            if (queueManager != null) {
                queueConnectionPool.offerPool(queueManager, receiveSystemName);
            }
        }
    }

    public MQMessage send(CustomMQQueueManager queueManager, Message requestMessage) throws Exception {
        String format = requestMessage.getFormat();
        int characterSet = requestMessage.getCharacterSet();
        byte[] messageId = requestMessage.getMessageId();
        MQMessage message = new MQMessage();
        if (format != null) {
            message.format = format;
        }

        if (characterSet != -1) {
            message.characterSet = characterSet;
        }

        if (messageId == null) {
            message.messageId = CMQC.MQMI_NONE;
        } else {
            message.messageId = messageId;
        }

        message.write(requestMessage.getData());
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        MQQueue sendQueue = queueManager.getSendQueue(sendQueueName);
        sendQueue.put(message, pmo);
        return message;
    }

    public Message receive(CustomMQQueueManager queueManager, byte[] messageId, int timeWaitInterval) throws Exception {
        if (timeWaitInterval <= 0) {
            timeWaitInterval = syncTimeout;
        }
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        gmo.options |= 8193;
        gmo.waitInterval = timeWaitInterval;
        MQMessage message = new MQMessage();
        if (messageId == null) {
            message.messageId = CMQC.MQMI_NONE;
        } else {
            gmo.matchOptions |= 1;
            message.messageId = messageId;
            message.correlationId = CMQC.MQMI_NONE;
        }

        queueManager.getReceiveQueue(receiveQueueName).get(message, gmo);
        int length = message.getMessageLength();
        byte[] buffer = new byte[length];
        message.readFully(buffer);
        return new Message(messageId, buffer);
    }
}
