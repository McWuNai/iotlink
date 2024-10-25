package com.yunze.quartz.task.yunze;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.utils.yunze.JsonUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/31/12:13
 * @Description:
 */
@Component("initMQ")
public class InitMQ {
    @Resource
    private RabbitMQConfig rabbitMQConfig;

    /**
     * 初始化 Mq 读取JSON 文件版
     */
    public void initMQConfig(){
        //读取 JSON 配置文件
        String mqinitArr_jsonStr = JsonUtil.readJsonFile("json/MqinitArr.json"); //指定格式前缀 死信队列 或 无死信队列 需初始化 队列
        JSONArray MqinitArr = JSONObject.parseArray(mqinitArr_jsonStr);
        String mqinitOhthArr_jsonStr = JsonUtil.readJsonFile("json/MqinitOhthArr.json");//自定义 含 死信队列 需初始化 队列
        JSONArray MqinitOhthArr = JSONObject.parseArray(mqinitOhthArr_jsonStr);


        for (int i = 0; i < MqinitArr.size(); i++) {
            Map<String,Object> Obj = (Map<String, Object>) MqinitArr.get(i);
            String card_exchangeName = Obj.get("card_exchangeName").toString();
            String card_queueName = Obj.get("card_queueName").toString();
            String card_routingKey = Obj.get("card_routingKey").toString();
            String card_del_exchangeName = "dlx_"+card_exchangeName,card_del_queueName = "dlx_"+card_queueName, card_del_routingKey = "dlx_"+card_routingKey;
            try {
                if(Obj.get("is_noDxl")!=null){
                    rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, null, null, null,null);
                }else{
                    rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                }
            }catch (Exception e){
                System.out.println("exchangeName "+card_exchangeName+" Mqinit 初始化失败 ……"+e.getMessage());
            }
        }

        for (int i = 0; i < MqinitOhthArr.size(); i++) {
            Map<String,Object> Obj = (Map<String, Object>) MqinitOhthArr.get(i);
            String card_exchangeName = Obj.get("card_exchangeName").toString();
            String card_queueName = Obj.get("card_queueName").toString();
            String card_routingKey = Obj.get("card_routingKey").toString();
            String card_del_exchangeName = Obj.get("card_del_exchangeName").toString();
            String card_del_queueName = Obj.get("card_del_queueName").toString();
            String card_del_routingKey = Obj.get("card_del_routingKey").toString();
            try {
                rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
            }catch (Exception e){
                System.out.println("exchangeName "+card_exchangeName+" Mqinit 初始化失败 ……"+e.getMessage());
            }
        }
    }

}