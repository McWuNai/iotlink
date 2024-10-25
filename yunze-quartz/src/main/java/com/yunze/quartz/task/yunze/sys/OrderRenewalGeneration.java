package com.yunze.quartz.task.yunze.sys;

import com.alibaba.fastjson.JSON;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业续费 审核成功之后触发 订单生成
 */
@Component("orderRenewalGeneration")
public class OrderRenewalGeneration {



    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *  企业续费 审核成功之后触发 订单生成
     * */
    public void RenewalToGenerateOrder(Integer day) {
        //1.创建路由 绑定 生产队列 发送消息
        String polling_queueName = "admin_RenewalToGenerateOrder_queue";
        String polling_routingKey = "admin.RenewalToGenerateOrder.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("day", day);
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("企业续费 审核成功之后触发 订单生成 失败 " + e.getMessage().toString());
        }

    }


}
