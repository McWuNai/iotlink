package com.yunze.quartz.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务 重新启动 》大于系统参数 多少条数据的  轮询通道队列
 *
 * @author root
 */
@Component("rebootTask")
public class RebootTask {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 轮询 用量 指定单个通道创建 消费者 【用来补救 消费者灵活监听时 没有创建 成功 消费者 进行 手动 补救创建 消费者】
     *
     * @param time 多少 分钟 后失效
     */
    public void CardFlowCreateListener(Integer time, String cd_id) {
        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", cd_id);
        //生产任务
        try {
            rabbitTemplate.convertAndSend("polling_cardCardFlow_exchange", "admin.CardFlow.queue.One", JSON.toJSONString(findRouteID_Map), message -> {
                // 设置消息过期时间 time 分钟 过期
                message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void RebootFlowPolling(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_RebootFlowPolling_queue";
        String polling_routingKey = "admin.RebootFlowPolling.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 RebootFlowPolling_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }


    public void RebootCardStop(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_RebootCardStop_queue";
        String polling_routingKey = "admin.RebootCardStop.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 RebootCardStop_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }

    public void RebootCardDisconnected(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_RebootCardDisconnected_queue";
        String polling_routingKey = "admin.RebootCardDisconnected.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 RebootCardDisconnected_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }



}
