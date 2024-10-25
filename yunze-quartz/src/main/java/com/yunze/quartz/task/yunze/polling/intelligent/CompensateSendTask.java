package com.yunze.quartz.task.yunze.polling.intelligent;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 同步数据补偿 - 【指定时间内确保有一次正常数据获取】
 */
@Component("compensateSendTask")
public class CompensateSendTask {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 用量
     * @param time
     * @param day 同步时间 几天内
     * @param status_ShowId 指定状态
     */
    public void pollingFlow(Integer time,Integer day,String status_ShowId,Integer StarRow,Integer PageSize) {
        String polling_queueName = "admin_FlowCompensateSend_queue";
        String polling_routingKey = "admin.FlowCompensateSend.queue";
        String polling_exchangeName = "admin_exchange";//路由

        try {
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("day",day);
            start_type.put("StarRow",StarRow);
            start_type.put("PageSize",PageSize);
            status_ShowId = status_ShowId.length()==0?null:status_ShowId;
            start_type.put("status_ShowId",status_ShowId);
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("用量补偿【指定时间内确保有一次正常数据获取】指令发送失败 " + e.getMessage());
        }
    }


    /**
     * 生命周期补偿 指定日期内
     * @param time
     * @param day
     */
    public void pollingState(Integer time,Integer day) {
        String polling_queueName = "admin_StateCompensateSend_queue";
        String polling_routingKey = "admin.StateCompensateSend.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("day",day);
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("生命周期补偿【指定时间内确保有一次正常数据获取】指令发送失败 " + e.getMessage());
        }
    }

}
