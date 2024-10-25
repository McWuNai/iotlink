package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.config.RabbitMQConfig;
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
public class CardActivateDateTaskMQ {

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


    String ad_exchangeName = null, ad_queueName = null, ad_routingKey = null,
            ad_del_exchangeName = null,ad_del_queueName = null, ad_del_routingKey = null;





    /**
     * 轮询 激活时间
     * type Slight .还没有用量的数据; 2.超出用量的 Serious
     *  type
     *  time 多少 分钟 后失效
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_CardActivate_queue")
    public void pollingActivateDate(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        String type = Pmap.get("type").toString();
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
                findMap.put("type", type);
                List<Map<String, Object>> cardArr = yzCardMapper.channelCardSeriousAndSlight(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    //插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);
                    //激活时间 任务生产
                    findMap.put("Arr", cardArr);
                    List<Map<String, Object>> ActivateArr = yzCardMapper.activate_dateNull(findMap);// 获取激活时间为空 iccid
                    if (ActivateArr != null && ActivateArr.size() > 0) {
                        String polling_id = VeDate.getNo(4);
                        //创建 路由 新增轮询详情 生产启动类型消息
                        ActivateDateRun(ActivateArr,channel_obj,polling_id,cd_id,time,pollingPublic_Map);
                    }

                }
            }
        }
    }


    /**
     * 轮询 激活时间
     * time
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingActivateTime_queue")
    public void pollingActivateDateIntelligent(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());

        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        findRouteID_Map.put("cd_algorithm", "1");//高频轮询
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                findMap.put("activate_date", "Null");//激活时间 为空时才获取

                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    String polling_id = VeDate.getNo(4);
                    //插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);
                    //创建 路由 新增轮询详情 生产启动类型消息
                    ActivateDateRun(cardArr,channel_obj,polling_id,cd_id,time,pollingPublic_Map);
                }

            }
        }
    }






    /**
     * 激活时间 任务发送消息
     * @param ActivateArr
     * @param channel_obj
     * @param polling_id
     * @param cd_id
     * @param time
     */
    public void ActivateDateRun(List<Map<String, Object>> ActivateArr, Map<String, Object> channel_obj, String polling_id,String cd_id,int time,Map<String, Object> pollingPublic_Map) {

        try {
            //设置任务 路由器 名称 与队列 名称
            ad_exchangeName = "polling_cardActivateDate_exchange";
            ad_queueName = "polling_cardActivateDate_queue";
            ad_routingKey = "polling.cardActivateDate.routingKey";
            ad_del_exchangeName = "polling_dlxcardActivateDate_exchange";
            ad_del_queueName = "polling_dlxcardActivateDate_queue" ;
            ad_del_routingKey = "polling.dlxcardActivateDate.routingKey";
            // rabbitMQConfig.creatExchangeQueue(ad_exchangeName, ad_queueName, ad_routingKey, ad_del_exchangeName, ad_del_queueName, ad_del_routingKey,null);

        } catch (Exception e) {
            System.out.println(e.getMessage().toString());
        }

        pollingPublic_Map.put("polling_type", "1");
        pollingPublic_Map.put("cd_count", ActivateArr.size());
        pollingPublic_Map.put("polling_id", polling_id);
        yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表


        for (int j = 0; j < ActivateArr.size(); j++) {
            Map<String, Object> card = ActivateArr.get(j);
            Map<String, Object> Card = new HashMap<>();
            Card.putAll(channel_obj);
            Card.put("iccid", card.get("iccid"));
            Card.put("card_no", card.get("card_no"));
            Card.put("polling_id", polling_id);//轮询任务详情编号
            String msg = JSON.toJSONString(Card);
            //1. 更新激活时间 [生产任务]
            try {
                rabbitTemplate.convertAndSend(ad_exchangeName, ad_routingKey, msg, message -> {
                    // 设置消息过期时间 time 分钟 过期
                    message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println(e.getMessage().toString());
            }
        }
    }




    /**
     * 创建 激活时间 轮询 生产者路由队列 及 数据库 插入 轮询信息
     * @param time
     * @param size
     * @param polling_id
     * @param pollingPublic_Map
     * @return
     */
    /*public boolean ActivateDate(int time, int size, String polling_id,Map<String, Object> pollingPublic_Map) {
        boolean bool = true;
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.FANOUT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("type", "ActivateDate");
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 time 分钟 过期
                message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                return message;
            });
            pollingPublic_Map.put("polling_type", "1");
            pollingPublic_Map.put("cd_count", size);
            pollingPublic_Map.put("polling_id", polling_id);
            yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表
        } catch (Exception e) {
            System.out.println("生产 轮询 [ActivateDate] 启动类型 失败 " + e.getMessage().toString());
        }
        return bool;
    }*/


}
