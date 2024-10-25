
package com.yunze.quartz.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 上游通道配置 了 需要 同步上游卡号数据 的进行同步成员信息（通道正常的）
 */
@Component("synUpstreamMemberTask")
public class SynUpstreamMemberTask {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void SynOfferinginfo() {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_SynUpstreamMember_queue";
        String polling_routingKey = "admin.SynUpstreamMember.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_type = new HashMap<>();
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步上游卡号数据 指令发送 失败 " + e.getMessage().toString());
        }
    }



}
