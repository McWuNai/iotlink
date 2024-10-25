package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.mapper.yunze.YrootlowHisMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CardFlowLessThanZeroTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YrootlowHisMapper yrootlowHisMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardFlowLessThanZero_queue")
    public void pollingCardFlowLessThanZero(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String, Object> findRouteID_Map = new HashMap<>();
        List<String> lArr =  yrootlowHisMapper.lessThanZeroChannelId(null);
        if(lArr!=null && lArr.size()>0){
            findRouteID_Map.put("Cd_idArr", lArr);
            List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
            if (channelArr != null && channelArr.size() > 0) {
                String CardFlow_routingKey = "polling.cardCardFlowLessThanZero.routingKey";
                //2.获取 通道下卡号
                for (int i = 0; i < channelArr.size(); i++) {
                    Map<String, Object> channel_obj = channelArr.get(i);
                    Map<String, Object> findMap = new HashMap<>();
                    String cd_id = channel_obj.get("cd_id").toString();
                    findMap.put("channel_id", cd_id);

                    List<Map<String, Object>> cardArr = yrootlowHisMapper.findChannelIdLessThanZero(findMap);
                    if (cardArr != null && cardArr.size() > 0) {
                        pollingExecute(cardArr,channel_obj,CardFlow_routingKey,time);
                    }
                }
            }
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
                rabbitTemplate.convertAndSend("polling_cardCardFlow_exchange", CardFlow_routingKey, msg, message -> {
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
