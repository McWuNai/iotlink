package com.yunze.quartz.task.yunze.mouthfirst;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@Component("month")
public class Month {

    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 月初有订购期内流量卡 进行 复机
     * */
    public void gentable(Integer Time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_MonthFirstday_queue";
        String polling_routingKey = "admin.MonthFirstday.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
           // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "备份 执行 admin_MonthFirstday_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (Time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("导入 卡号 失败 " + e.getMessage().toString());
        }
    }
}
