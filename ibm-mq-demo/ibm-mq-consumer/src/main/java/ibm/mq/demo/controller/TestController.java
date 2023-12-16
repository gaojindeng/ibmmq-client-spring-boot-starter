package ibm.mq.demo.controller;

import ibm.mq.demo.enums.EsbServiceIdEnum;
import ibm.mq.demo.executor.EsbMQExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class TestController {

    @Resource
    private EsbMQExecutor routerEsbExecutor;

    @GetMapping("/test")
    public void test() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        String msg = "xml content....";
        String test = routerEsbExecutor.testString(msg, EsbServiceIdEnum._000000);
        System.out.println(test);
    }

}
