package com.yunze.quartz.task.yunze.polling;


import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


/**
 * 对比主表数据 与 上游成员 标记是否修改 和新增
 */
@Component("synCardDataComparisonTask")
public class SynCardDataComparisonTask {


    @Resource
    private RabbitTemplate rabbitTemplate;

    public void DataComparison() {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_CardDataComparison_queue";
        String polling_routingKey = "admin.CardDataComparison.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_type = new HashMap<>();
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("群组所属成员信息 指令发送 失败 " + e.getMessage().toString());
        }
    }


}
