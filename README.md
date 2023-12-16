### 介绍

在 IBM MQ 中，队列模型是典型的点对点模型（Point-to-Point），而非发布-订阅模型（Publish-Subscribe）。每个系统（客户端和服务端）在 IBM MQ 中都会有分配的队列用于发送和接收消息。通常情况下，发送消息的队列称为发送队列（Outbound Queue），接收消息的队列称为接收队列（Inbound Queue）。这种模式确保了消息的可靠性传递，每个消息都被发送到指定的队列，然后被接收方从相应的队列中读取。
### 背景
之前我们线上遇到过一个问题：部分 HTTP 请求超时或报错。经排查发现是由于通过 IBM MQ 发往下游系统处理缓慢所致。连接池是共用的，导致大部分连接被该系统占用，其他系统的消息发送请求无法获取连接。因此，我决定自己开发一套 IBM MQ 连接池的组件，其中有几个优化点：

- 系统独立连接池： 为各个系统配置独立的连接池，确保每个系统都能够有效地管理连接资源。
- 动态连接熔断机制： 对于公共连接池，在某个系统的连接使用量达到阈值时，触发熔断效果，防止影响其他系统的连接获取。这样可以保证各系统之间的连接资源相对均衡，提高整体系统的稳定性。
- 连接定时保持心跳
- 超时未获取到返回的消息，可以自定义处理
### 使用
**源码地址：**
需要自己把ibm-mq-spring-boot-starter项目install到自己的本地仓库，然后引用：
```xml
<dependency>
  <groupId>io.github.gaojindeng.ibm.mq</groupId>
  <artifactId>ibm-mq-spring-boot-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```
**配置文件：**
```yaml

esb:
  hostName: 10.0.111.97
  port: 40000
  ccsid: 1208
  channel: SVRCONN_GW_IN
  queueManagerName: MQ_Transaction_Server
  timeout: 60000
  periodTime: 3000    #连接心跳间隔
  initPoolSize: 15    #公共池核心连接数
  retryTimes: 3       #获取连接失败重试次数
  maxPoolSize: 200    #公共池最大连接数
  busyLimit: 50       #公共池单系统繁忙连接数，超过该阀值熔断
  customPool:
    loan:              #自定义连接池-信贷
      initPoolSize: 15
      maxPoolSize: 100
      freeTime: 180000 #连接空闲时间

    core:              #自定义连接池-核心
      initPoolSize: 15
      maxPoolSize: 100
  sendQueueNames:      #发送队列，card为本系统名称
    card: IBM.SERVICE.XXXXXX.REQUESTER.IN
  receiveQueueNames:   #接收队列，card为本系统名称
    card: IBM.SERVICE.XXXXXX.REQUESTER.OUT

```
**示例代码：**
```java
@SpringBootApplication
@EnableEsbClientMQ
public class EsbListenerApplication {
    @Resource
    private QueueTransportSupport queueTransportSupport;
    
    public static void main(String[] args) {
        SpringApplication.run(EsbListenerApplication.class, args);
    }

    //测试发送xml报文
    public String testString(String xmlRequestMsg, EsbServiceIdEnum esbServiceIdEnum) {
        // 1.获取esb通讯处理器，card为自身的系统，esbServiceIdEnum为接收方的系统
        IQueueTransport queueTransport = queueTransportSupport.getTransport("card", esbServiceIdEnum.getSystemCode().getSysName());

        // 2.报文转换成Message
        Message reqMsg = new Message((byte[]) null, xmlRequestMsg.getBytes(StandardCharsets.UTF_8), null, 1208);

        // 3.发送请求，并等待结果
        Message resMsg = queueTransport.submit(reqMsg);

        return new String(resMsg.getData());
    }
}
```
