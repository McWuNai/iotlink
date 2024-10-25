package com.yunze.quartz.task.yunze.Automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByOrderMapper;
import com.yunze.common.utils.Email.EmailCc;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 自动化 邮件抄送 [商品购物]
 *
 * @author root
 */
@Component("emailCCShopping")
public class EmailCCShopping {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void CCShopping(Integer cc_count) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_CCShopping_queue";
        String polling_routingKey = "admin.CCShopping.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("cc_count",cc_count);
            start_type.put("Masage", "备份 执行 CCShopping_queue ");//启动类型
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
