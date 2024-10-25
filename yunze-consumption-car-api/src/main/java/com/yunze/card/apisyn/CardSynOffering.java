package com.yunze.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
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
 * 同步上游 已订购资费信息
 */
@Slf4j
@Component
public class CardSynOffering {

    @Resource
    private InternalApiRequest internalApiRequest;


    /**
     * @param msg
     * @param channel_1
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_cardSynOffering_queue")
    public void SynOffering(String msg, Channel channel_1) {
        if(msg!=null && msg.length()>0){
            Offering(msg);
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_cardSynOffering_queue")
    public void SynOffering_1(String msg, Channel channel_1) {
        if(msg!=null && msg.length()>0){
            Offering(msg);
        }
    }

    /**
     * 死信队列
     * @param msg
     * @param channel_1
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_cardSynOffering_queue")
    public void dlxSynOffering(String msg, Channel channel_1)  {
        if(msg!=null && msg.length()>0){
            Offering(msg);
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_cardSynOffering_queue")
    public void dlxSynOffering_2(String msg, Channel channel_1)  {
        if(msg!=null && msg.length()>0){
            Offering(msg);
        }
    }


    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_cardSynOffering_queue")
    public void dlxSynOffering_3(String msg, Channel channel_1)  {
        if(msg!=null && msg.length()>0){
            Offering(msg);
        }
    }


    /**
     * 同步 上游套餐
     * @param msg
     * @throws IOException
     */
    public void Offering(String msg) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String iccid = map.get("iccid").toString();
                Map<String, Object> Parammap = new HashMap<>();
                Parammap.put("iccid", iccid);
                Map<String, Object> Rmap = internalApiRequest.queryOffering(Parammap, map);
                String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                if (code.equals("200")) {
                    //System.out.println(Rmap);
                } else {
                    log.info(">>API - 同步 上游套餐 消费者 未获取到 : {} | {}<<", iccid, Rmap);
                }
        } catch (Exception e) {
            log.error(">>错误 - 同步 上游套餐 消费者:{}<<", e.getMessage());
        }
    }


}
