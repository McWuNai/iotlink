package com.yunze.quartz.task.yunze.sys;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统订单
 * @Auther: zhang feng
 * @Date: 2022/06/15/10:30
 * @Description:
 */
@Component("sysOrder")
public class SysOrder {

    @Resource
    private YzOrderMapper yzOrderMapper;



    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *  修改 info LIKE '未找到加%' and add_package = '1'  订单 (未找到加包数据订单修改状态)【可能是未给客户划分资费】【激活生效未获取到激活时间】等！
     * */
    public void UpdOrderStatus() {
        //1.创建路由 绑定 生产队列 发送消息
        String polling_queueName = "admin_UpdOrderStatus_queue";
        String polling_routingKey = "admin.UpdOrderStatus.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "重置订单状态");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("重置订单状态 失败 " + e.getMessage().toString());
        }

    }


}
