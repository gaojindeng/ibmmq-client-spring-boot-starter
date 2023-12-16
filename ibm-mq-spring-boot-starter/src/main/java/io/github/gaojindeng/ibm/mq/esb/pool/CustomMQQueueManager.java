package io.github.gaojindeng.ibm.mq.esb.pool;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomMQQueueManager extends MQQueueManager {

    /**
     * 最新访问时间
     */
    private long lastUsedTime;

    /**
     * 已经打开的发送队列,key为queue名称
     */
    public final Map<String, MQQueue> sendQueueMap = new ConcurrentHashMap<>();

    /**
     * 已经打开的接收队列,key为queue名称
     */
    public final Map<String, MQQueue> receiveQueueMap = new ConcurrentHashMap<>();

    public CustomMQQueueManager(String queueManagerName, Hashtable properties) throws MQException {
        super(queueManagerName, properties);
        this.lastUsedTime = System.currentTimeMillis();
    }

    public MQQueue getSendQueue(String queueName) {
        return sendQueueMap.get(queueName);
    }

    public MQQueue getReceiveQueue(String queueName) {
        return receiveQueueMap.get(queueName);
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }
}
