package io.github.gaojindeng.ibm.mq.esb;

import io.github.gaojindeng.ibm.mq.esb.handler.AbstractQueueTransport;
import io.github.gaojindeng.ibm.mq.esb.handler.IQueueTransport;
import io.github.gaojindeng.ibm.mq.esb.pool.QueueConnectFactory;
import io.github.gaojindeng.ibm.mq.esb.pool.QueueConnectionPool;
import io.github.gaojindeng.ibm.mq.esb.constant.EsbConstant;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ComponentScan(
        basePackages = {"io.github.gaojindeng.ibm.mq.esb"}
)
public class QueueTransportSupport {
    private final Map<String, IQueueTransport> cacheMap = new ConcurrentHashMap<>();

    @Resource
    private EsbProperties esbProperties;

    @Resource
    private QueueConnectFactory queueConnectFactory;

    public IQueueTransport getDefaultTransport() {
        return getTransport(EsbConstant.DEFAULT, EsbConstant.DEFAULT);
    }

    public IQueueTransport get(String key) {
        return cacheMap.get(key);
    }

    public IQueueTransport getTransport(String submitSystemName, String receiveSystemName) {
        String key = submitSystemName.concat(receiveSystemName);
        return getAndInit(key, submitSystemName, receiveSystemName);
    }

    private IQueueTransport getAndInit(String key, String submitSystemName, String receiveSystemName) {
        IQueueTransport queueTransport = cacheMap.get(key);
        if (queueTransport != null) {
            return queueTransport;
        }

        synchronized (this) {
            queueTransport = cacheMap.get(key);
            if (queueTransport != null) {
                return queueTransport;
            }
            String sendQueueName = esbProperties.getSendQueueNames().get(submitSystemName);
            if (StringUtils.isEmpty(sendQueueName)) {
                throw new IllegalArgumentException("submitSystemName is empty");
            }
            String receiveQueueName = esbProperties.getReceiveQueueNames().get(submitSystemName);
            QueueConnectionPool queueConnectionPool = queueConnectFactory.get(receiveSystemName);
            queueConnectionPool.setId(receiveSystemName);
            // 初始化连接繁忙数为0
            QueueConnectionPool.busyBusinessMap.putIfAbsent(receiveSystemName, new AtomicInteger(0));

            // 创建对应处理器
            queueTransport = new AbstractQueueTransport(key,
                    sendQueueName,
                    receiveQueueName,
                    receiveSystemName,
                    queueConnectionPool,
                    (int) queueConnectFactory.getSyncTimeout()) {
            };
            cacheMap.put(key, queueTransport);
        }
        return queueTransport;
    }
}
