package com.yunze.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.CardFlowSyn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  APi实时 获取数据时同步 数据库数据 消费者 [用量]
 * */
@Slf4j
@Component
public class CardUsed {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private CardFlowSyn cardFlowSyn;


    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynCardUsed_queue")
    private void Listener(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynCardUsed_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynCardUsed_queue")
    private void Listener1(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynCardUsed_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynCardUsed_queue")
    private void Listener2(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynCardUsed_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynCardUsed_queue")
    private void DListener(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynCardUsed_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynCardUsed_queue")
    private void DListener1(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynCardUsed_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynCardUsed_queue")
    private void DListener2(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynCardUsed_queue");
    }

    /**
     * 监听公用
     * @param msg
     * @param channel
     */
    private void ListeneCommonr(String msg, Channel channel,String ListenePrefix){
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            String iccid = Pmap.get("iccid").toString();
            Double Use = Double.parseDouble(Pmap.get("Use").toString());
            String prefix = ListenePrefix;
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            String recordKey = prefix+":"+ iccid;
            Object  isExecute = redisCache.getCacheObject(recordKey);
            if(isExecute==null){
                redisCache.setCacheObject(recordKey, msg, 10, TimeUnit.SECONDS);// 10 秒 缓存 避免 重复消费
                try {
                    Syn(iccid,Use);
                }catch (Exception e){
                    log.error(">>{}  Syn 消费者:{}<<",ListenePrefix, e.getMessage());
                    redisCache.deleteObject(recordKey);//删除正在执行key
                }
            }
        } catch (Exception e) {
            log.error(">>错误 - {} 消费者:{}<<",ListenePrefix, e.getMessage());
        }
    }






    private void Syn(String iccid,Double Use) {
        Map<String,Object> RMap = cardFlowSyn.CalculationFlow(iccid,Use,null);
        log.info("RMap {}",RMap);
    }




}






