package com.yunze.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.config.MyDictionary;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardInfoChangeMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  APi实时 获取数据时同步 数据库数据 消费者 [修改断开网状态]
 * */
@Slf4j
@Component
public class UpdCardConnectionStatus {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;
    @Resource
    private MyDictionary myDictionary;
    @Resource
    private YzCardInfoChangeMapper yzCardInfoChangeMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynUpdCardConnectionStatus_queue")
    private void Listener(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynUpdCardConnectionStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynUpdCardConnectionStatus_queue")
    private void Listener1(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynUpdCardConnectionStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_ApiSynUpdCardConnectionStatus_queue")
    private void Listener2(String msg, Channel channel){
        ListeneCommonr(msg,channel,"admin_ApiSynUpdCardConnectionStatus_queue");
    }


    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynUpdCardConnectionStatus_queue")
    private void DListener(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynUpdCardConnectionStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynUpdCardConnectionStatus_queue")
    private void DListener1(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynUpdCardConnectionStatus_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ApiSynUpdCardConnectionStatus_queue")
    private void DListener2(String msg, Channel channel){
        ListeneCommonr(msg,channel,"dlx_admin_ApiSynUpdCardConnectionStatus_queue");
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
                    log.error(">>{}  Syn 消费者:{}<<", ListenePrefix,e.getMessage());
                    redisCache.deleteObject(recordKey);//删除正在执行key
                }
            }
        } catch (Exception e) {
            log.error(">>错误 - {} {} 消费者:{}<<",ListenePrefix, msg,e.getMessage());
        }
    }






    private void Syn(Map<String,Object> Upd_Map) {

        String Is_Break = Upd_Map.get("Is_Break").toString();

        Map<String,Object> Obj = yzCardMapper.findRoute(Upd_Map);
        if(Obj!=null){
            String key = "status_id";
            String dict_type = "yunze_card_status_ShowId";
            String ctype = "1";
            String cbefore = "";//变更前
            String cafterward = "";//变更后
            String connection_status = "";//断开网状态
            String value = Obj.get(key) != null && Obj.get(key).toString().length() > 0 ? Obj.get(key).toString() : "未知";
            if (!value.equals("未知")) {
                cbefore = myDictionary.getdictLabel(dict_type, value);
            } else {
                cbefore = value;
            }
            Map<String, Object> Add_Map = new HashMap<String,Object>();

            if(Is_Break.equals("1")){
                cafterward ="断网";
                connection_status ="2";
            }else if(Is_Break.equals("0")){
                cafterward ="开网";
                connection_status ="1";
            }
            Add_Map.put("ctype", ctype);
            Add_Map.put("cbefore", cbefore);
            Add_Map.put("cafterward", cafterward);
            Add_Map.put("remark", "执行成功！");
            Add_Map.put("source_type", "8");// 来源 类型 ：8 Api变更
            Add_Map.put("execution_status", "2");

            Upd_Map.put("connection_status", connection_status);

            int  updCount = yzCardMapper.updConnectionStatus(Upd_Map);
            int addChange = 0;
            if(updCount>0){
                addChange = yzCardInfoChangeMapper.save(Add_Map);
            }
            log.info("Upd_Map {} updCount {} addChange {}",Upd_Map,updCount,addChange);
        }
    }




}






