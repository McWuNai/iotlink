package com.yunze.quartz.task.yunze.card;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/08/17/13:59
 * @Description: 短信任务下发队列
 */
@Component("cardSmsTask")
public class CardSmsTask {

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 短信抄送下发
     * @param type
     */
    public void SynPacketCount(String type) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_cardSmsExecution_queue";
        String polling_routingKey = "admin.cardSmsExecution.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("type",type);//启动抄送类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("   短信任务下发队列  失败 " + e.getMessage().toString());
        }
    }


}
