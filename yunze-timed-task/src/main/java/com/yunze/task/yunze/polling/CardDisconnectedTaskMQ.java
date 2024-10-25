package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;
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
public class CardDisconnectedTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;

    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RedisCache redisCache;
    //卡轮询 路由队列
    String polling_queueName = "polling_card_disconnected";
    String polling_routingKey = "polling.card.disconnected";
    String polling_exchangeName = "polling_card";

    //
    String ad_exchangeName = null, ad_queueName = null, ad_routingKey = null,
            ad_del_exchangeName = null,ad_del_queueName = null, ad_del_routingKey = null;





    /**
     * 轮询 卡状态
     * time 多少 分钟 后失效
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardDisconnected_queue")
    public void pollingCardDisconnected(String msg2) {
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
            String CardDisconnected_routingKey = "";
            try {
                ad_exchangeName = "polling_cardCardDisconnected_exchange";
                ad_queueName = "polling_cardCardDisconnected_queue";
                CardDisconnected_routingKey = "polling.cardCardDisconnected.routingKey";
                ad_del_exchangeName = "polling_dlxcardCardDisconnected_exchange";
                ad_del_queueName = "polling_dlxcardCardDisconnected_queue";
                ad_del_routingKey = "polling.dlxcardCardDisconnected.routingKey";
                // rabbitMQConfig.creatExchangeQueue(ad_exchangeName, ad_queueName, CardDisconnected_routingKey, ad_del_exchangeName, ad_del_queueName, ad_del_routingKey,null);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }



            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdDisconnected(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    //插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);

                    //卡状态 用量 轮询
                    String polling_id_CardDisconnected = VeDate.getNo(4);

                    pollingPublic_Map.put("polling_type", "5");
                    pollingPublic_Map.put("cd_count", cardArr.size());
                    pollingPublic_Map.put("polling_id", polling_id_CardDisconnected);
                    yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表
                    //创建 路由 新增轮询详情 生产启动类型消息
                    Map<String, Object> start_type = new HashMap<String, Object>();
                    start_type.put("Listener","polling_cardCardDisconnected_queue");
                    start_type.put("cd_id", cd_id);

                    //2.卡状态
                    //卡号放入路由
                    for (int j = 0; j < cardArr.size(); j++) {
                        Map<String, Object> card = cardArr.get(j);
                        Map<String, Object> Card = new HashMap<>();
                        Card.putAll(channel_obj);
                        Card.put("iccid", card.get("iccid"));
                        Card.put("card_no", card.get("card_no"));
                        Card.put("status_id", card.get("status_id"));
                        Card.put("polling_id", polling_id_CardDisconnected);//轮询任务详情编号

                        String msg = JSON.toJSONString(Card);
                        //生产任务
                        try {
                            rabbitTemplate.convertAndSend("polling_cardCardDisconnected_exchange", CardDisconnected_routingKey, msg, message -> {
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


    /**
     * 创建 卡状态 轮询 生产者路由队列 及 数据库 插入 轮询信息
     *  time
     *  size
     *  polling_id
     *  pollingPublic_Map
     *  start_type
     * @return
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_CardDisconnectedTest_queue")
    public boolean CardDisconnected(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return true;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        Integer size = Integer.parseInt(Pmap.get("size").toString());
        String polling_id = Pmap.get("polling_id").toString();
        Map<String,Object> pollingPublic_Map = (Map<String, Object>) Pmap.get("pollingPublic_Map");
        Map<String,Object> start_type = (Map<String, Object>) Pmap.get("start_type");
        boolean bool = true;
        //卡状态  轮询
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.FANOUT);
            start_type.put("type", "CardDisconnected");
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 time 分钟 过期
                message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                return message;
            });
            pollingPublic_Map.put("polling_type", "5");
            pollingPublic_Map.put("cd_count", size);
            pollingPublic_Map.put("polling_id", polling_id);
            yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表

        } catch (Exception e) {
            bool = false;
            System.out.println("生产 轮询 [CardDisconnected] 启动类型 失败 " + e.getMessage().toString());
        }
        return bool;
    }


}
