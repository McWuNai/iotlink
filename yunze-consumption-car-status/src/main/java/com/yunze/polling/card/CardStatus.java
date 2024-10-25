package com.yunze.polling.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import com.yunze.common.utils.yunze.YzPollingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 卡状态轮询消费 者
 */
@Slf4j
@Component
public class CardStatus {


    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzPollingUtil yzPollingUtil;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;






    @RabbitHandler
    @RabbitListener(queues = "polling_cardCardStatus_queue",containerFactory = "customContainerFactory")
    public void cardCardStatus_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synCardStatus(msg,channel_1);
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "polling_dlxcardCardStatus_queue",containerFactory = "customContainerFactory")
    public void dlxcardCardStatus_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synCardStatus(msg,channel_1);
        }
    }


    @RabbitHandler
    @RabbitListener(queues = "pollingOther_cardCardStatus_queue",containerFactory = "customContainerFactory")
    public void pollingOther_cardCardStatus_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synCardStatus(msg,channel_1);
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_pollingOther_cardCardStatus_queue",containerFactory = "customContainerFactory")
    public void dlx_pollingOther_cardCardStatus_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synCardStatus(msg,channel_1);
        }
    }



    /**
     * 同步卡状态
     *
     * @param msg
     * @param channel
     * @throws IOException
     */
    public void synCardStatus(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }

            Map<String, Object> map = JSON.parseObject(msg);
            String iccid = map.get("iccid").toString();
            String polling_id = map.get("polling_id").toString();//轮询任务编号
            String prefix = "polling_cardCardStatus_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            //Object isExecute = redisCache.getCacheObject(prefix + ":" + iccid);
            //if (isExecute == null) {
                //System.out.println("SUCCESS");
            //redisCache.setCacheObject(prefix + ":" + iccid, msg, 3, TimeUnit.MINUTES);//3 分钟缓存 避免 重复消费
                redisCache.setCacheObject(polling_id + ":" + iccid, msg, 1, TimeUnit.HOURS);//1 小时缓存 用来统计轮询进度

                Map<String, Object> Parammap = new HashMap<>();
                Parammap.put("iccid", iccid);
                Map<String, Object> Rmap = internalApiRequest.queryCardStatus(Parammap, map);
                String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                if (code.equals("200")) {
                    //获取 卡状态 开卡日期 更新 card info
                    if (Rmap.get("statusCode") != null && Rmap.get("statusCode") != "" && Rmap.get("statusCode").toString().trim().length() > 0) {
                        String statusCode = Rmap.get("statusCode").toString().trim();
                        if (!statusCode.equals("0")) {
                            Map<String, Object> Upd_Map = new HashMap<>();
                            Upd_Map.put("status_id", statusCode);
                            Upd_Map.put("iccid", iccid);
                            //1.创建路由 绑定 生产队列 发送消息
                            String card_exchangeName = "admin_card_exchange", card_queueName = "admin_CardUpd_queue", card_routingKey = "admin.CardUpd.queue",
                                    card_del_exchangeName = "dlx_" + card_exchangeName, card_del_queueName = "dlx_" + card_queueName, card_del_routingKey = "dlx_" + card_routingKey;
                            try {
                                //rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                                    /*Upd_Map.put("queueTypeName", "admin_CardUpdStatusId_queue");
                                    rabbitTemplate.convertAndSend(card_exchangeName, card_routingKey, JSON.toJSONString(Upd_Map), message -> {
                                        // 设置消息过期时间 30 分钟 过期
                                        message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                                        return message;
                                    });*/

                                Object status_id = Upd_Map.get("status_id");
                                String status_ShowId = getShowStatIdArr.GetShowStatId(status_id.toString());
                                Upd_Map.put("status_ShowId",status_ShowId);
                                int upd = yzCardMapper.updStatusId(Upd_Map);
                                log.info(">>发送 admin_CardUpdStatusId_queue 指令成功 ！ iccid:{} | upd:{} | Upd_Map {}<<", iccid,upd, Upd_Map);
                            } catch (Exception e) {
                                log.info(">>发送 admin_CardUpdStatusId_queue 失败 ！ iccid:{} | Upd_Map {}  e ： {}<<", iccid, Upd_Map, e.getMessage());
                            }
                        } else {
                            log.info(">>API - 卡状态轮询消费者 未获取到卡状态 statusCode = 0 :{} | {}<<", iccid, Rmap);
                        }
                    }
                } else {
                    log.info(">>API - 卡状态轮询消费者 未获取到卡状态:{} | {}<<", iccid, Rmap);
                    // System.out.println(map.get("iccid")+" 未获取到卡状态 ！");
                }
            //}
        } catch (Exception e) {
//            // 记录该消息日志形式  存放数据库db中、后期通过定时任务实现消息补偿、人工实现补偿
            log.error(">>错误 - 卡状态轮询消费者:{}<<", e.getMessage().toString());
//            //将该消息存放到死信队列中，单独写一个死信消费者实现消费。
        }
    }


}
