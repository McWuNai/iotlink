package com.yunze.quartz.task.yunze.backups;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzSysLogsMapper;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.common.utils.yunze.WriteCSV;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 定时任务 通道轮询 达量停机
 *
 * @author root
 */
@Component("sysLogsTask")
public class SysLogsTask {


    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 备份 SysLogsTask
     */
    public void backups() {

        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_BackupsyzSysLogs_queue";
        String polling_routingKey = "admin.BackupsyzSysLogs.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "备份 执行 GenTableTask ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("导入 卡号 失败 " + e.getMessage().toString());
        }




   }













}
