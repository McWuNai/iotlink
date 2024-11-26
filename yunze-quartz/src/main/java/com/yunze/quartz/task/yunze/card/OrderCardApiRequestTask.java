package com.yunze.quartz.task.yunze.card;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务 订单表 检查需要执行API请求执行失败 重复执行【系统最大重试执行次数 默认 100 次】 该任务默认每分钟触发一次
 */
@Component("orderCardApiRequestTask")
public class OrderCardApiRequestTask {


    @Resource
    private RabbitTemplate rabbitTemplate;

    public void Execution() {
        String polling_queueName = "admin_orderCardApiRequest_queue";
        String polling_routingKey = "admin.orderCardApiRequest.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_type = new HashMap<>();
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("定时删除 orderCardApiRequestTask 指令发送失败 " + e.getMessage());
        }
    }

}
