package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CardBreakNetworkTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;

    /**
     * 轮询 未订购断网
     * time 多少 分钟 后失效
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardBreakNetwork_queue")
    public void pollingCardBreakNetwork(String msg2) {
        if (StringUtils.isEmpty(msg2)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg2);
        Integer time = Integer.parseInt(Pmap.get("time").toString());

        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {

            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdBreakNetwork(findMap);
                if (cardArr != null && cardArr.size()>0) {
                    //插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);

                    //卡状态 用量 轮询
                    String polling_id_CardBreakNetwork = VeDate.getNo(4);

                    pollingPublic_Map.put("polling_type", "5");//轮询类型  未订购资费断网
                    pollingPublic_Map.put("cd_count", cardArr.size());
                    pollingPublic_Map.put("polling_id", polling_id_CardBreakNetwork);
                    yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表

                    //2.卡状态
                    //卡号放入路由
                    for (int j = 0; j < cardArr.size(); j++) {
                        Map<String, Object> card = cardArr.get(j);
                        Map<String, Object> Card = new HashMap<>();
                        Card.putAll(channel_obj);
                        Card.put("iccid", card.get("iccid"));
                        Card.put("card_no", card.get("card_no"));
                        Card.put("status_id", card.get("status_id"));
                        Card.put("polling_id", polling_id_CardBreakNetwork);//轮询任务详情编号

                        String msg = JSON.toJSONString(Card);
                        String addOrder_exchangeName = "admin_exchange";
                        String addOrder_routingKey = "polling.cardCardBreakNetwork.queue";
                        //生产任务
                        try {
                            rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, msg, message -> {
                                // 设置消息过期时间 time 分钟 过期
                                message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                                return message;
                            });
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }



}
