package com.yunze.quartz.task.yunze.card;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务处理 卡轮询
 */
@Component("autoPolling")
public class AutoPolling {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void autoPollingCzc(){
        String polling_queueName = "admin_autoPollingCzc_queue";
        String polling_routingKey = "admin.autoPollingCzc.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "autoPollingCzc ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("自动轮询Czc失败: " + e.getMessage().toString());
        }
    }

}
