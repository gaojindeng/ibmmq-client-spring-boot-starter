package io.github.gaojindeng.ibm.mq.esb.message;


import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 超时消息
 */
public class TimeoutMessage implements Delayed {
    private byte[] messageId;
    private String key;
    private long expired = System.currentTimeMillis() + 60 * 1000 * 10; //10分钟后过期

    public TimeoutMessage(byte[] messageId, String key) {
        this.messageId = messageId;
        this.key = key;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expired - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        TimeoutMessage o1 = (TimeoutMessage) o;
        return this.expired - o1.expired <= 0 ? -1 : 1;
    }

    public byte[] getMessageId() {
        return messageId;
    }

    public String getKey() {
        return key;
    }

    public long getExpired() {
        return expired;
    }
}
