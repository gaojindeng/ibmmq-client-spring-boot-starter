package io.github.gaojindeng.ibm.mq.esb.cat;//package com.csii.card.router.esb.config;
//
//import com.dianping.cat.status.StatusExtension;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * @author gaojd
// * @Description: cat数据库连接监控
// */
//public class MqPoolMonitor implements StatusExtension {
//
//
//    private QueueManager queueManager;
//
//    private String id;
//
//    public MqPoolMonitor(QueueManager queueManager, String id) {
//        this.queueManager = queueManager;
//        this.id = id;
//    }
//
//    @Override
//    public String getDescription() {
//        return this.id;
//    }
//
//    @Override
//    public String getId() {
//        return "mqPool." + id;
//    }
//
//    @Override
//    public Map<String, String> getProperties() {
//        Map<String, String> status = new LinkedHashMap<String, String>();
//        if (queueManager != null) {
//            status.put("Pool.total", Integer.toString(queueManager.getTotal().get()));
//            status.put("Pool.idle", Integer.toString(queueManager.getPool().size()));
//            AtomicInteger value = QueueManager.busyBusinessMap.get(id);
//            if (value != null) {
//                status.put("Pool.busy", Integer.toString(value.get()));
//            }
//        }
//        return status;
//    }
//}
