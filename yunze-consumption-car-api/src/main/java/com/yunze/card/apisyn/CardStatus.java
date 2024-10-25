package com.yunze.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  APi实时 获取数据时同步 数据库数据 消费者 [状态]
 * */
@Slf4j
@Component
public class CardStatus {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynCardStatus_queue")
    private void Listener(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynCardStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynCardStatus_queue")
    private void Listener1(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynCardStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynCardStatus_queue")
    private void Listener2(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynCardStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynCardStatus_queue")
    private void DListener(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynCardStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynCardStatus_queue")
    private void DListener1(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynCardStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynCardStatus_queue")
    private void DListener2(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynCardStatus_queue");
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
                    Syn(Pmap);
                }catch (Exception e){
                    log.error(">>{}  Syn 消费者:{}<<",ListenePrefix, e.getMessage());
                    redisCache.deleteObject(recordKey);//删除正在执行key
                }
            }
        } catch (Exception e) {
            log.error(">>错误 - {} {} 消费者:{}<<",ListenePrefix, msg,e.getMessage());
        }
    }






    private void Syn(Map<String,Object> Upd_Map) {

        String statusCode = Upd_Map.get("statusCode").toString().trim();
        Upd_Map.put("status_ShowId",getShowStatIdArr.GetShowStatId(statusCode));
        int updcount = yzCardMapper.updStatusId(Upd_Map);//变更卡状态
        log.info(" Upd_Map {} updcount {}",Upd_Map,updcount);
    }




}






