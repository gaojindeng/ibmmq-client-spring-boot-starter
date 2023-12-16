package io.github.gaojindeng.ibm.mq.esb.pool;

import com.ibm.mq.MQException;

import java.util.Properties;

public class QueueConnection {
    private Integer ccsid;
    private String hostName;
    private Integer port;
    private String channel;
    private String queueManagerName;
    private String sslCipherSuite;

    public CustomMQQueueManager connect() throws MQException {
        Properties params = new Properties();
        params.put("CCSID", this.ccsid);
        params.put("hostname", this.hostName);
        params.put("port", this.port);
        params.put("channel", this.channel);
        return new CustomMQQueueManager(this.queueManagerName, params);
    }

    public Integer getCcsid() {
        return ccsid;
    }

    public void setCcsid(Integer ccsid) {
        this.ccsid = ccsid;
    }

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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getQueueManagerName() {
        return queueManagerName;
    }

    public void setQueueManagerName(String queueManagerName) {
        this.queueManagerName = queueManagerName;
    }

    public String getSslCipherSuite() {
        return sslCipherSuite;
    }

    public void setSslCipherSuite(String sslCipherSuite) {
        this.sslCipherSuite = sslCipherSuite;
    }
}
