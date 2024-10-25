package com.yunze.quartz.task.yunze.Automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcUrlMapper;
import com.yunze.common.utils.Email.EmailCc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 定时任务 自动化 邮件抄送 [未划分资费组]
 *
 * @author root
 */
@Slf4j
@Component("emailPayGroup")
public class EmailPayGroup {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void PayGroup() {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_PayGroup_queue";
        String polling_routingKey = "admin.PayGroup.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "备份 执行 PayGroup_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }


    public void RetryCC(Integer cc_count) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_PayGroupCount_queue";
        String polling_routingKey = "admin.PayGroupCount.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("cc_count",cc_count);
            start_type.put("Masage", "备份 执行 PayGroupCount_queue ");//启动类型
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
