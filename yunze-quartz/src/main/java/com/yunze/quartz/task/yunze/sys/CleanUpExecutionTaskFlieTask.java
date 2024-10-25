package com.yunze.quartz.task.yunze.sys;

import com.alibaba.fastjson.JSON;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.redis.FindSysConfig;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 清理文件 释放系统硬盘
 */
@Component("cleanUpExecutionTaskFlieTask")
public class CleanUpExecutionTaskFlieTask {


    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *  清理执行任务 生成文件 释放系统硬盘
     * */
    public void ExecutionTaskFlie() {
        //1.创建路由 绑定 生产队列 发送消息
        String polling_queueName = "admin_cleanExecutionTaskFlie_queue";
        String polling_routingKey = "admin.cleanExecutionTaskFlie.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "清理执行任务 admin_cleanExecutionTaskFlie_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("清理执行任务 失败 " + e.getMessage().toString());
        }

    }




}
