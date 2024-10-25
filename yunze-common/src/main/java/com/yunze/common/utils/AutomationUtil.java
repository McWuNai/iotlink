package com.yunze.common.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动化mQ 发送任务帮助类
 */
@Component
public class AutomationUtil {

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 请求发送
     * @param type
     * @param address
     * @param parameter
     * @param head
     * @param returnExecuteParameter
     * @param returnExecute
     * @param returnType
     */
    public void send(String type,String address,Map<String, Object> parameter,Map<String, Object> head,Map<String, Object> returnExecuteParameter,String returnExecute,String returnType){
        String polling_queueName = "admin_automationNetworkCallback_queue";
        String polling_routingKey = "admin.automationNetworkCallback.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("parameter", parameter);//参数
            start_type.put("returnExecuteParameter", returnExecuteParameter);//返回执行函数名参数
            start_type.put("head", head);//头部
            start_type.put("address", address);//请求地址
            start_type.put("type", type);//请求方式
            start_type.put("returnExecute", returnExecute);//返回执行函数名
            start_type.put("returnType", returnType);// 返回 类型

            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("充值资费 指令发送 " + e.getMessage());
        }
    }






}
