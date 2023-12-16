package io.github.gaojindeng.ibm.mq.esb.pool;

import io.github.gaojindeng.ibm.mq.esb.exception.EsbMQException;
import com.ibm.mq.MQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class QueueConnectionPool {
    private static final Logger log = LoggerFactory.getLogger(QueueConnectionPool.class);

    private String id;
    private int initPoolSize;
    private int retryTimes;
    private int maxPoolSize;
    private int busyLimit;
    private boolean defaultFlag = false;                                                //是否是默认连接池
    private QueueConnection queueConnection;                                            //连接参数
    public static final Map<String, AtomicInteger> busyBusinessMap = new HashMap<>(); //繁忙连接数
    private BlockingQueue<CustomMQQueueManager> pool = new LinkedBlockingQueue<>();     //连接池
    private AtomicInteger total = new AtomicInteger(0);                       //总连接数
    private ReentrantLock lock = new ReentrantLock();

    public QueueConnectionPool(QueueConnection queueConnection) {
        this.queueConnection = queueConnection;
    }

    /**
     * 初始化连接池
     */
    public void init() {
        if (this.maxPoolSize < this.initPoolSize) {
            this.maxPoolSize = this.initPoolSize;
        }
        try {
            for (int i = 0; i < this.initPoolSize; ++i) {
                CustomMQQueueManager mqQueueManager = queueConnection.connect();
                if (!this.pool.offer(mqQueueManager)) {
                    this.closeQueueManagerQuietly(mqQueueManager, "init");
                    throw new IllegalStateException("can't offer queueManager to pool");
                }
                this.total.incrementAndGet();
            }
            log.info("QueueManager {} started. size: {}", this.id, this.pool.size());
        } catch (Exception e) {
            this.closeAllQueueManager();
            log.error("cannot initialize QueueManager {}", this.id, e);
            throw new IllegalStateException(e);
        }
    }

    public void close() {
        this.closeAllQueueManager();
        log.info("QueueManager " + this.id + " stoped. size: " + this.pool.size() + " " + this.total.get());
    }

    public void closeQueueManagerQuietly(CustomMQQueueManager qm, String source) {
        log.info("closeQueueManagerQuietly_" + source);
        //Cat.logEvent("esb.submit", "close." + source);
        if (qm == null) {
            return;
        }

        qm.sendQueueMap.values().forEach(value -> {
            try {
                value.close();
            } catch (MQException e) {
                log.error("closeSendQueue", e);
            }
        });

        qm.receiveQueueMap.values().forEach(value -> {
            try {
                value.close();
            } catch (MQException e) {
                log.error("closeReceive", e);
            }
        });

        try {
            qm.disconnect();
        } catch (MQException e) {
            log.error("closeQManager", e);
        }

        this.total.decrementAndGet();
    }

    /**
     * 新增繁忙连接数
     *
     * @param receiveSystemName
     */
    public void businessBusyIncrement(String receiveSystemName) {
        AtomicInteger atomicInteger = busyBusinessMap.get(receiveSystemName);
        if (atomicInteger == null) {
            return;
        }
        atomicInteger.incrementAndGet();
    }

    /**
     * 减少繁忙连接数
     *
     * @param receiveSystemName
     */
    public void businessBusyDecrement(String receiveSystemName) {
        AtomicInteger atomicInteger = busyBusinessMap.get(receiveSystemName);
        if (atomicInteger == null) {
            return;
        }
        atomicInteger.decrementAndGet();
    }

    public CustomMQQueueManager getQueueManager(String sendQueueName, String receiveQueueName, String receiveSystemName) throws MQException {
        CustomMQQueueManager queueManager = this.getQueueManager(receiveSystemName);
        try {
            accessQueue(sendQueueName, receiveQueueName, queueManager);
            businessBusyIncrement(receiveSystemName);
            return queueManager;
        } catch (MQException e) {
            this.closeQueueManagerQuietly(queueManager, "accessQueue");
            throw e;
        }

    }

    private CustomMQQueueManager getQueueManager(String receiveSystemName) {
        // 判断是否繁忙
        if (isBusinessBusy(receiveSystemName)) {
            throw new EsbMQException(receiveSystemName + " connection busy");
        }

        // 从连接池中拿到一个连接管理器
        int poolSize = 0;
        for (int i = 0; i < this.retryTimes; ++i) {
            poolSize = this.pool.size();
            CustomMQQueueManager mqQueueManager = this.pool.poll();
            if (mqQueueManager != null) {
                mqQueueManager.setLastUsedTime(System.currentTimeMillis());
                return mqQueueManager;
            }

            if (this.total.get() < this.maxPoolSize) {
                CustomMQQueueManager customMQQueueManager = this.expandPool();
                if (customMQQueueManager != null) {
                    return customMQQueueManager;
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                log.error("getQueueManager InterruptedException", e);
            }

        }

        log.error("system is busy or MQ server is broken. maybe make the pool size more bigger.pool size: " + poolSize);
        throw new EsbMQException(receiveSystemName + " connection busy");
    }


    /**
     * 扩容连接
     *
     * @return
     */
    private CustomMQQueueManager expandPool() {
        //Cat.logEvent("esb.submit", "expand");
        try {
            lock.lock();
            if (this.total.get() < this.maxPoolSize) {
                CustomMQQueueManager qm = queueConnection.connect();
                this.total.incrementAndGet();
                return qm;
            } else {
                return null;
            }
        } catch (MQException e) {
            log.error("expandPool fail:", e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 连接是否繁忙
     *
     * @param receiveSystemName
     * @return
     */
    private boolean isBusinessBusy(String receiveSystemName) {
        //非默认连接池，不需要判断
        if (StringUtils.isEmpty(receiveSystemName) || !defaultFlag) {
            return false;
        }

        AtomicInteger atomicInteger = busyBusinessMap.get(receiveSystemName);
        if (atomicInteger == null) {
            return false;
        }

        int busyCount = atomicInteger.get();
        boolean flag = busyCount >= busyLimit;
        if (flag) {
            log.error(receiveSystemName + " busy pool size: " + busyCount);
        }
        return flag;
    }

    public void offerPool(CustomMQQueueManager queueManager, String receiveSystemName) {
        businessBusyDecrement(receiveSystemName);
        this.pool.offer(queueManager);
    }

    /**
     * 访问queue
     *
     * @param sendQueueName
     * @param receiveQueueName
     * @param queueManager
     * @throws MQException
     */
    private void accessQueue(String sendQueueName, String receiveQueueName, CustomMQQueueManager queueManager) throws MQException {
        if (sendQueueName != null && queueManager.sendQueueMap.get(sendQueueName) == null) {
            queueManager.sendQueueMap.put(sendQueueName, queueManager.accessQueue(sendQueueName, 8208));
        }
        if (receiveQueueName != null && queueManager.receiveQueueMap.get(receiveQueueName) == null) {
            queueManager.receiveQueueMap.put(receiveQueueName, queueManager.accessQueue(receiveQueueName, 8226));
        }
    }

    private void closeAllQueueManager() {
        CustomMQQueueManager qm = null;
        while ((qm = this.pool.poll()) != null) {
            this.closeQueueManagerQuietly(qm, "closeAll");
        }
    }

    public boolean isExpand() {
        return total.get() > initPoolSize;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setInitPoolSize(int initPoolSize) {
        this.initPoolSize = initPoolSize;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setBusyLimit(int busyLimit) {
        this.busyLimit = busyLimit;
    }

    public void setDefaultFlag(boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
    }

    public BlockingQueue<CustomMQQueueManager> getPool() {
        return pool;
    }
}
