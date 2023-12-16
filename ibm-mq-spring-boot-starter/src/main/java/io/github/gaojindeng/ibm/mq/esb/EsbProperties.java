package io.github.gaojindeng.ibm.mq.esb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "esb")
public class EsbProperties {

    private String hostName;

    private Integer port;

    private Integer ccsid;

    private String channel;

    private Map<String, String> sendQueueNames = new HashMap<>();

    private Map<String, String> receiveQueueNames = new HashMap<>();

    private String queueManagerName;

    //超时等待时间
    private long timeout = 60000;

    //初始连接大小
    private int initPoolSize = 10;

    //连接满时，重试拿连接次数
    private int retryTimes = 3;

    //最大连接
    private int maxPoolSize = 20;

    //繁忙连接限制数
    private int busyLimit = 75;

    //连接空闲时间
    private long freeTime = 180000L;

    //心跳间隔
    private long periodTime = 1000L * 3;

    private Map<String, ESBConProperties> customPool;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getCcsid() {
        return ccsid;
    }

    public void setCcsid(Integer ccsid) {
        this.ccsid = ccsid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Map<String, String> getSendQueueNames() {
        return sendQueueNames;
    }

    public void setSendQueueNames(Map<String, String> sendQueueNames) {
        this.sendQueueNames = sendQueueNames;
    }

    public Map<String, String> getReceiveQueueNames() {
        return receiveQueueNames;
    }

    public void setReceiveQueueNames(Map<String, String> receiveQueueNames) {
        this.receiveQueueNames = receiveQueueNames;
    }

    public String getQueueManagerName() {
        return queueManagerName;
    }

    public void setQueueManagerName(String queueManagerName) {
        this.queueManagerName = queueManagerName;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getInitPoolSize() {
        return initPoolSize;
    }

    public void setInitPoolSize(int initPoolSize) {
        this.initPoolSize = initPoolSize;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getBusyLimit() {
        return busyLimit;
    }

    public void setBusyLimit(int busyLimit) {
        this.busyLimit = busyLimit;
    }

    public long getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(long freeTime) {
        this.freeTime = freeTime;
    }

    public long getPeriodTime() {
        return periodTime;
    }

    public void setPeriodTime(long periodTime) {
        this.periodTime = periodTime;
    }

    public Map<String, ESBConProperties> getCustomPool() {
        return customPool;
    }

    public void setCustomPool(Map<String, ESBConProperties> customPool) {
        this.customPool = customPool;
    }

    public static class ESBConProperties {
        private int initPoolSize = 10;
        private int maxPoolSize = 20;
        private int retryTimes = 3;
        private int busyLimit = 75;
        private int freeTime = 180000;

        public int getInitPoolSize() {
            return initPoolSize;
        }

        public void setInitPoolSize(int initPoolSize) {
            this.initPoolSize = initPoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getRetryTimes() {
            return retryTimes;
        }

        public void setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
        }

        public int getBusyLimit() {
            return busyLimit;
        }

        public void setBusyLimit(int busyLimit) {
            this.busyLimit = busyLimit;
        }

        public int getFreeTime() {
            return freeTime;
        }

        public void setFreeTime(int freeTime) {
            this.freeTime = freeTime;
        }
    }
}
