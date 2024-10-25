package com.yunze.quartz.task.yunze.sys;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.VeDate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component("cardStatisticalTask")
public class CardStatisticalTask {

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     *  资产明细数据生成
     * */
    public void Generate(Integer Time,String dept_id){
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_AssetDetails_queue";
        String polling_routingKey = "admin.AssetDetails.queue";
        String polling_exchangeName = "admin_exchange";//路由

        try {
            Map<String, Object> start_Map = new HashMap<>();
            start_Map.put("dept_id", dept_id);//生成企业ID
            start_Map.put("record_date", VeDate.getNextDay(VeDate.getStringDateShort(),"-1") );//获取前一天的数据
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_Map), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (Time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println(" 资产明细数据生成 指令发送失败 " + e.getMessage().toString());
        }

    }

}
