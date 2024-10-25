package com.yunze.task.yunze.sys;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CardSynOfferinginfolistTaskMQ {


    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 同步上游套餐
     * */
    @RabbitHandler
    @RabbitListener(queues = "admin_SynOfferinginfoTest_queue")
    public void SynOfferinginfo(String msg) {
        try {
            Map<String, Object> findRouteID_Map = new HashMap<>();
            Map<String, Object> findDict = new HashMap<>();
            findDict.put("dict_type", "yz_card_api_synOfferinginfo");
            List<Map<String,Object>> dict_dataArr = yzCardMapper.findDict(findDict);
            if(dict_dataArr!=null && dict_dataArr.size()>0){
                List<String> cd_codeArr = new ArrayList<>();
                for (int i = 0; i < dict_dataArr.size(); i++) {
                    cd_codeArr.add(dict_dataArr.get(i).get("dict_value").toString());
                }
                findRouteID_Map.put("cd_codeArr", cd_codeArr);//有哪些已经对接的需要 轮询 通道编码
            }
            List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
            if (channelArr != null && channelArr.size() > 0) {
                String CardFlow_routingKey = "admin.cardSynOffering.queue";
                //2.获取 通道下卡号
                for (int i = 0; i < channelArr.size(); i++) {
                    Map<String, Object> channel_obj = channelArr.get(i);
                    Map<String, Object> findMap = new HashMap<>();
                    String cd_id = channel_obj.get("cd_id").toString();
                    findMap.put("channel_id", cd_id);
                    List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);
                    if (cardArr != null && cardArr.size() > 0) {
                        pollingExecute(cardArr,channel_obj,CardFlow_routingKey,180);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(" 修改 测试期 已有激活时间状态 为 已激活 失败 " + e.getMessage().toString());
        }
    }

    /**
     * 公共执行
     * @param cardArr
     * @param channel_obj
     * @param CardFlow_routingKey
     * @param time
     */
    public void pollingExecute(List<Map<String, Object>> cardArr,Map<String, Object> channel_obj,String CardFlow_routingKey,Integer time){
        //卡号放入路由
        for (int j = 0; j < cardArr.size(); j++) {
            Map<String, Object> card = cardArr.get(j);
            Map<String, Object> Card = new HashMap<>();
            Card.putAll(channel_obj);
            Card.put("iccid", card.get("iccid"));
            Card.put("card_no", card.get("card_no"));
            Card.put("network_type", card.get("network_type"));
            String msg = JSON.toJSONString(Card);
            //生产任务
            try {
                rabbitTemplate.convertAndSend("admin_exchange", CardFlow_routingKey, msg, message -> {
                    // 设置消息过期时间 time 分钟 过期
                    message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println(e.getMessage().toString());
            }
        }

    }

}
