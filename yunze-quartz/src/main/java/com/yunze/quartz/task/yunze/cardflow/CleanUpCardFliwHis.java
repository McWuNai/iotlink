package com.yunze.quartz.task.yunze.cardflow;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YrootlowHisMapper;
import com.yunze.common.utils.yunze.VeDate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 清理用量记录表
 * @Auther: zhang feng
 * @Date: 2022/03/23/14:12
 * @Description:
 */
@Component("cleanUpCardFliwHis")
public class CleanUpCardFliwHis {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void CleanUp(String day) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_CleanUp_queue";
        String polling_routingKey = "admin.CleanUp.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("day",day);
            start_type.put("Masage", "备份 执行 CleanUp_queue ");//启动类型
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
