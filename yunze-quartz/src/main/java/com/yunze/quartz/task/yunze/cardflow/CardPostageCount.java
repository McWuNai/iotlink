package com.yunze.quartz.task.yunze.cardflow;

import com.alibaba.fastjson.JSON;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzAgentPackageMapper;
import com.yunze.common.mapper.yunze.YzCardPackageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 同步改字段数据【表1：yz_card_package 2：yz_agent_package】 卡总数
 */
@Slf4j
@Component("cardPostageCount")
public class CardPostageCount {

    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void synchronization(Integer Time){
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_synchronizationP_queue";
        String polling_routingKey = "admin.synchronizationP.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "备份 执行 synchronizationP_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (Time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }

    }

}


















