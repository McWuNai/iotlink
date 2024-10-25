package com.yunze.quartz.task.yunze.card;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据  yz_syn_card_info 上游返回数据标记 执行 批量修改 或 新增指令执行
 */
@Component("cardSynInfoTask")
public class CardSynInfoTask {


    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 修改执行 发送
     */
    public void updDetect() {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_cardSynInfoUpdDetect_queue";
        String polling_routingKey = "admin.cardSynInfoUpdDetect.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("yz_syn_card_info 修改执行 发送 失败 " + e.getMessage().toString());
        }
    }



    /**
     * 修改执行 发送
     */
    public void addDetect() {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_cardSynInfoAddDetect_queue";
        String polling_routingKey = "admin.cardSynInfoAddDetect.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("yz_syn_card_info 修改执行 发送 失败 " + e.getMessage().toString());
        }
    }

}
