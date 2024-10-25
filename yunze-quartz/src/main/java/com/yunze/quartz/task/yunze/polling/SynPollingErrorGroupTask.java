package com.yunze.quartz.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.card.YzCardApiOfferinginfoMapper;
import com.yunze.common.mapper.yunze.card.YzCardApiOfferinginfolistMapper;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorGroupMapper;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步  卡用量接口查询时错误记录-分类
 */
@Component("synPollingErrorGroupTask")
public class SynPollingErrorGroupTask {


    @Resource
    private RabbitTemplate rabbitTemplate;

    public void SynOfferinginfo() {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_SynOfferinginfo_queue";
        String polling_routingKey = "admin.SynOfferinginfo.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "备份 执行 SynOfferinginfo_queue ");//启动类型
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
