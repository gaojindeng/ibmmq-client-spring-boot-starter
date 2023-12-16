package ibm.mq.demo.executor;

import ibm.mq.demo.enums.EsbServiceIdEnum;
import io.github.gaojindeng.ibm.mq.esb.QueueTransportSupport;
import io.github.gaojindeng.ibm.mq.esb.handler.IQueueTransport;
import io.github.gaojindeng.ibm.mq.esb.message.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Component
public class EsbMQExecutor {

    @Resource
    private QueueTransportSupport queueTransportSupport;

   /* public <T extends EsbBaseResponse> T execute(EsbBaseRequest esbRequest, Class<T> responseClass, EsbServiceIdEnum esbServiceIdEnum) {
        // 1.获取报文转换器和esb通讯处理器
        IEsbMsgConverter esbMsgConverter = esbMsgConverterSupport.get(esbServiceIdEnum.getCode());
        IQueueTransport queueTransport = queueTransportSupport.getTransport("core", esbServiceIdEnum.getSystemCode().getSysCode());

        // 2.请求报文转换
        Message reqMsg = esbMsgConverter.convertMessage(esbRequest);

        // 3.esb通讯处理
        Message resMsg = queueTransport.submit(reqMsg);
        queueTransport.onlySend(resMsg);

        // 4.对结果进行转换
        T esbResponse = esbMsgConverter.convertObj(resMsg, responseClass);

        // 5.对结果进行校验
        esbMsgConverter.validator(esbResponse);

        return esbResponse;
    }*/

    public String testString(String xmlRequestMsg, EsbServiceIdEnum esbServiceIdEnum) {
        // 1.获取报文转换器和esb通讯处理器
        IQueueTransport queueTransport = queueTransportSupport.getTransport("card", esbServiceIdEnum.getSystemCode().getSysName());

        // 2.请求报文转换
        Message reqMsg = new Message((byte[]) null, xmlRequestMsg.getBytes(StandardCharsets.UTF_8), null, 1208);

        // 3.esb通讯处理
        Message resMsg = queueTransport.submit(reqMsg);

        // 4.对结果进行转换

        // 5.对结果进行校验

        return new String(resMsg.getData());
    }
}
