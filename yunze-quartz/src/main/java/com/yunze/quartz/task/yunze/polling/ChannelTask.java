package com.yunze.quartz.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;

import com.yunze.apiCommon.utils.VeDate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 通道轮询
 *
 * @author root
 */
@Component("channelTask")
public class ChannelTask {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void channelType(String type, Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_channelType_queue";
        String polling_routingKey = "admin.channelType.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("type",type);
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 channelType_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }

    public void pollingActivateDate(String type, Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_pollingActivateDateTest_queue";
        String polling_routingKey = "admin.pollingActivateDateTest.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("type",type);
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 pollingActivateDateTest_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }

    public void pollingCardStatus(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_pollingCardStatusTime_queue";
        String polling_routingKey = "admin.pollingCardStatusTime.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 pollingCardStatusTime_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }


    public void channel(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_channelTime_queue";
        String polling_routingKey = "admin.channelTime.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 channelTime_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }

    public void channelFindPercentage(String percentage) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_channelFindPercentage_queue";
        String polling_routingKey = "admin.channelFindPercentage.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("percentage",percentage);
            start_type.put("Masage", "备份 执行 channelFindPercentage_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }

    public void CardStatus(int time, int size, String polling_id,Map<String, Object> pollingPublic_Map) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_CardStatusTest_queue";
        String polling_routingKey = "admin.CardStatusTest.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("size",size);
            start_type.put("polling_id",polling_id);
            start_type.put("pollingPublic_Map",pollingPublic_Map);
            start_type.put("Masage", "备份 执行 CardStatusTest_queue ");//启动类型
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
