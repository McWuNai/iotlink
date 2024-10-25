package com.yunze.quartz.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
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
@Component("cardStatusTask")
public class CardStatusTask {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void pollingCardStatus(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_pollingCardStatusTest_queue";
        String polling_routingKey = "admin.pollingCardStatusTest.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 pollingCardStatusTest_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }


   /* *//**
     * 创建 卡状态 轮询 生产者路由队列 及 数据库 插入 轮询信息
     * @param time
     * @param size
     * @param polling_id
     * @param pollingPublic_Map
     * @return
     *//*
    public boolean CardStatus(int time, int size, String polling_id,Map<String, Object> pollingPublic_Map) {
        boolean bool = true;
        //卡状态  轮询
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.FANOUT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("type", "CardStatus");
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 time 分钟 过期
                message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                return message;
            });
            pollingPublic_Map.put("polling_type", "2");
            pollingPublic_Map.put("cd_count", size);
            pollingPublic_Map.put("polling_id", polling_id);
            yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表

        } catch (Exception e) {
            bool = false;
            System.out.println("生产 轮询 [CardStatus] 启动类型 失败 " + e.getMessage().toString());
        }
        return bool;
    }*/












}
