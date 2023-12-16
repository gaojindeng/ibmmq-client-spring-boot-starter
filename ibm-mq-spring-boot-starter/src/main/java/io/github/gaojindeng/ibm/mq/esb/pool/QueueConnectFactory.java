package io.github.gaojindeng.ibm.mq.esb.pool;

import io.github.gaojindeng.ibm.mq.esb.EsbProperties;
import io.github.gaojindeng.ibm.mq.esb.QueueTransportSupport;
import io.github.gaojindeng.ibm.mq.esb.constant.EsbConstant;
import io.github.gaojindeng.ibm.mq.esb.handler.AbstractQueueTransport;
import io.github.gaojindeng.ibm.mq.esb.message.TimeoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.DelayQueue;

@Component
public class QueueConnectFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractQueueTransport.class);

    /**
     * 连接池列表
     */
    private final List<QueueConnectionPool> connectionList = new ArrayList<>();
    private final Map<String, QueueConnectionPool> connectionPoolMap = new HashMap<>();

    /**
     * 默认的连接池
     */
    private final QueueConnectionPool defaultConnectionPool;

    /**
     * 连接空闲时间
     */
    private final long freeTime;

    /**
     * 心跳间隔
     */
    private final long periodTime;

    /**
     * 同步等待消息超时时间
     */
    private final long syncTimeout;

    /**
     * 超时消息队列
     */
    public static final DelayQueue<TimeoutMessage> delayQueue = new DelayQueue<>();

    @Resource
    private QueueTransportSupport queueTransportSupport;

    public QueueConnectFactory(@Autowired EsbProperties esbProperties) {
        this.freeTime = esbProperties.getFreeTime();
        this.syncTimeout = esbProperties.getTimeout();
        this.periodTime = esbProperties.getPeriodTime();
        QueueConnectionPool queueConnectionPool = getQueueConnection(esbProperties);
        queueConnectionPool.setDefaultFlag(true);
        defaultConnectionPool = queueConnectionPool;

        //添加默认连接池
        addConnectionPool(EsbConstant.DEFAULT, queueConnectionPool);

        //获取自定义连接池配置
        Map<String, EsbProperties.ESBConProperties> customPoolProperties = esbProperties.getCustomPool();
        if (customPoolProperties == null) {
            return;
        }

        //添加自定义连接池
        customPoolProperties.forEach((name, properties) -> {
            QueueConnectionPool customPool = getQueueConnection(esbProperties);
            customPool.setMaxPoolSize(properties.getMaxPoolSize());
            customPool.setInitPoolSize(properties.getInitPoolSize());
            addConnectionPool(name, customPool);
        });

        addMonitor();
    }


    public QueueConnectionPool get(String key) {
        QueueConnectionPool connectionPool = connectionPoolMap.get(key);
        if (connectionPool == null) {
            return defaultConnectionPool;
        }
        return connectionPool;
    }

    @PostConstruct
    public void init() {

        //初始化连接池
        for (QueueConnectionPool connectionPool : connectionList) {
            connectionPool.init();
        }

        //创建定时任务
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (QueueConnectionPool connectionPool : connectionList) {
                    CustomMQQueueManager queueManager = connectionPool.getPool().poll();
                    if (queueManager == null) {
                        continue;

                    }

                    //心跳测试连通性
                    try {
                        queueManager.getMaximumMessageLength();
                    } catch (Exception mqe) {
                        log.error("manager pool clear because of getMaximumMessageLength Error", mqe);
                        connectionPool.closeQueueManagerQuietly(queueManager, "testConnect");
                        continue;
                    }

                    //扩容的连接要进行回收
                    long lastUsedTime = queueManager.getLastUsedTime();
                    if (connectionPool.isExpand() && (System.currentTimeMillis() - lastUsedTime) > freeTime) {
                        connectionPool.closeQueueManagerQuietly(queueManager, "recycle");
                    } else {
                        connectionPool.getPool().offer(queueManager);
                    }

                }

                //获取超时消息
                while (true) {
                    TimeoutMessage poll = delayQueue.poll();
                    if (poll == null) {
                        break;
                    }
                    queueTransportSupport.get(poll.getKey()).discard(poll);
                }
            }
        }, periodTime, periodTime);
    }


    @PreDestroy
    public void destroy() {
        for (QueueConnectionPool QueueConnection : connectionList) {
            QueueConnection.close();
        }
    }

    public void addMonitor() {
        connectionPoolMap.forEach((key, value) -> {
//            try {
//                MqPoolMonitor monitor = new MqPoolMonitor(value, key);
//                StatusExtensionRegister.getInstance().register(monitor);
//            } catch (Throwable ignore) {
//            }
        });
    }

    public void addConnectionPool(String name, QueueConnectionPool connectionPool) {
        connectionPool.setId(name);
        connectionList.add(connectionPool);
        connectionPoolMap.put(name, connectionPool);
    }

    private QueueConnectionPool getQueueConnection(EsbProperties esbProperties) {
        QueueConnection queueConnection = new QueueConnection();
        QueueConnectionPool queueConnectionPool = new QueueConnectionPool(queueConnection);
        queueConnection.setQueueManagerName(esbProperties.getQueueManagerName());
        queueConnection.setChannel(esbProperties.getChannel());
        queueConnection.setHostName(esbProperties.getHostName());
        queueConnection.setPort(esbProperties.getPort());
        queueConnection.setCcsid(esbProperties.getCcsid());
        queueConnectionPool.setInitPoolSize(esbProperties.getInitPoolSize());
        queueConnectionPool.setMaxPoolSize(esbProperties.getMaxPoolSize());
        queueConnectionPool.setRetryTimes(esbProperties.getRetryTimes());
        queueConnectionPool.setBusyLimit(esbProperties.getBusyLimit());
        return queueConnectionPool;
    }

    public long getSyncTimeout() {
        return syncTimeout;
    }
}
