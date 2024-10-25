package com.yunze.polling.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/07/09/12:43
 * @Description:
 */
@Slf4j
@Component
public class ActivateDate {


    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;


    @RabbitHandler
    @RabbitListener(queues = "polling_cardActivateDate_queue",containerFactory = "customContainerFactory")
    public void cardActivateDate_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synActivateDate(msg,channel_1);
        }
    }


    @RabbitHandler
    @RabbitListener(queues = "polling_dlxcardActivateDate_queue",containerFactory = "customContainerFactory")
    public void dlxcardActivateDate_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synActivateDate(msg,channel_1);
        }
    }



    @RabbitHandler
    @RabbitListener(queues = "pollingOther_cardCardActivateDate_queue",containerFactory = "customContainerFactory")
    public void cardCardActivateDate_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synActivateDate(msg,channel_1);
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_pollingOther_cardCardActivateDate_queue",containerFactory = "customContainerFactory")
    public void dlxCardCardActivateDate_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synActivateDate(msg,channel_1);
        }
    }

    public void synActivateDate(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }

            Map<String,Object> map = JSON.parseObject(msg);
            String iccid = map.get("iccid").toString();
            String polling_id = map.get("polling_id").toString();//轮询任务编号
            String prefix = "polling_cardActivateDate_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            //Object  isExecute = redisCache.getCacheObject(prefix+":"+ iccid);
            //if(isExecute==null){
                //System.out.println("SUCCESS");
            // redisCache.setCacheObject(prefix+":"+ iccid, msg, 3, TimeUnit.MINUTES);//3 分钟缓存 避免 重复消费
            //  redisCache.setCacheObject(polling_id+":"+ iccid, msg, 3, TimeUnit.MINUTES);//3 分钟缓存 用来统计轮询进度

                Map<String,Object> Parammap=new HashMap<>();
                Parammap.put("iccid",iccid);
                Map<String,Object> Rmap =  internalApiRequest.queryCardActiveTime(Parammap,map);
                String code =  Rmap.get("code")!=null?Rmap.get("code").toString():"500";
                if(code.equals("200")){
                    //获取 激活日期 开卡日期 更新 card info
                    if(Rmap.get("activateDate")!=null &&  Rmap.get("activateDate")!="" &&  Rmap.get("activateDate").toString().trim().length()>0 ||
                            Rmap.get("openDate")!=null &&  Rmap.get("openDate")!="" &&  Rmap.get("openDate").toString().trim().length()>0){
                        Object activateDate_obj = Rmap.get("activateDate")!=null &&  Rmap.get("activateDate")!="" &&  Rmap.get("activateDate").toString().trim().length()>0?
                                Rmap.get("activateDate").toString().trim():"";
                        String activateDate = activateDate_obj.toString().trim();
                        activateDate = activateDate.length()>10?activateDate.substring(0,10):activateDate;
                        Object openDate = Rmap.get("openDate")!=null &&  Rmap.get("openDate")!="" &&  Rmap.get("openDate").toString().trim().length()>0?
                                Rmap.get("openDate").toString().trim():null;
                        Map<String,Object> Upd_Map = new HashMap<>();
                        if(activateDate!=null && activateDate.length()>0 ){
                            Upd_Map.put("activate_date",activateDate);
                        }
                        if(openDate!=null){
                            Upd_Map.put("open_date",openDate);
                        }
                        Upd_Map.put("iccid",iccid);
                        //1.创建路由 绑定 生产队列 发送消息
                        String card_exchangeName = "admin_card_exchange", card_queueName = "admin_CardUpd_queue", card_routingKey = "admin.CardUpd.queue",
                                card_del_exchangeName = "dlx_"+card_exchangeName,card_del_queueName = "dlx_"+card_queueName, card_del_routingKey = "dlx_"+card_routingKey;
                        try {
                            //rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                           /* Upd_Map.put("queueTypeName","admin_CardUpdActivate_queue");
                            rabbitTemplate.convertAndSend(card_exchangeName, card_routingKey, JSON.toJSONString(Upd_Map), message -> {
                                // 设置消息过期时间 30 分钟 过期
                                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                                return message;
                            });*/
                            int upd = yzCardMapper.updActivate(Upd_Map);
                            //log.info("发送 变更激活时间 指令成功 ！{} ",upd);
                        } catch (Exception e) {
                            System.out.println("变更激活时间 队列 创建 失败 " + e.getMessage());
                        }
                    }
                }else{
                    log.info(">>API - 未获取到:{} | {} | Parammap {} | map {}<<",iccid,Rmap,Parammap,map);
                    // System.out.println(map.get("iccid")+" 未获取到激活日期 ！");
                }
            // }
        } catch (Exception e) {
//            // 记录该消息日志形式  存放数据库db中、后期通过定时任务实现消息补偿、人工实现补偿
            log.error(">>错误 - 激活时间轮询消费者:{}<<", e.getMessage().toString());
//            //将该消息存放到死信队列中，单独写一个死信消费者实现消费。
        }
    }

}
