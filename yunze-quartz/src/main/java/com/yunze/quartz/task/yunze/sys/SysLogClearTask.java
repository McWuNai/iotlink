package com.yunze.quartz.task.yunze.sys;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.VeDate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志清理
 */
@Component("sysLogClearTask")
public class SysLogClearTask {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *  日志清理
     * */
    public void Generate(Integer Time){
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_LogClear_queue";
        String polling_routingKey = "admin.LogClear.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_Map = new HashMap<>();
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_Map), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (Time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println(" 日志清理 指令发送失败 " + e.getMessage().toString());
        }

    }

}
