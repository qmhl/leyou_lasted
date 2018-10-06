package com.leyou.sms.mq;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import com.sun.rmi.rmid.ExecOptionPermission;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-05 16:52
 **/
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly.sms.queue", durable = "true"),
            exchange = @Exchange(value = "ly.sms.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}))
    public void listenSms(Map<String, String> msg) throws RuntimeException {
        if (msg == null || msg.size() <= 0) {
            // 放弃处理
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");

        if (StringUtils.isBlank(phone) || StringUtils.isBlank(code)) {
            // 放弃处理
            return;
        }
        try {
            // 发送消息
            SendSmsResponse resp = this.smsUtils.sendSms(phone, code,
                    prop.getSignName(),
                    prop.getVerifyCodeTemplate());
            if ("OK".equals(resp.getCode())) {
                // 发送成功
                return;
            }
        } catch (Exception e){
            sleep();
            throw new RuntimeException();
        }
        sleep();
        // 发送失败
        throw new RuntimeException();
    }

    private void sleep(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            throw new RuntimeException();
        }
    }
}
