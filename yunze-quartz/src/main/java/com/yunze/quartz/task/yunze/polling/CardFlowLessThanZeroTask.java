package com.yunze.quartz.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.mapper.yunze.YrootlowHisMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日用量记录为 负数 卡号 再次同步用量
 * @Auther: zhang feng
 * @Date: 2022/04/21/12:22
 * @Description:
 */
@Component("cardFlowLessThanZeroTask")
public class CardFlowLessThanZeroTask {


    @Resource
    private RabbitTemplate rabbitTemplate;

    public void pollingCardFlowLessThanZero(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_pollingCardFlowLessThanZero_queue";
        String polling_routingKey = "admin.pollingCardFlowLessThanZero.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 pollingCardFlowLessThanZero_queue ");//启动类型
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
