package com.yunze.polling.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.config.MyDictionary;
import com.yunze.common.core.card.CardChange;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.utils.yunze.CardFlowSyn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 未订购资费 轮询断网 消费者
 */
@Slf4j
@Component
public class CardBreakNetwork {

    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private RedisCache redisCache;
    @Resource
    private CardFlowSyn cardFlowSyn;
    @Resource
    private MyDictionary myDictionary;
    @Resource
    private CardChange cardChange;


    /**
     * 监听器
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "polling_cardCardBreakNetwork_queue")
    public void pollingStart(String msg, Channel channel) throws IOException {
        if (msg != null && msg.length() > 0) {
            synCardBreakNetwork(msg, channel);
        }
    }

    /**
     * 执行 未订购 断网
     *
     * @param msg
     * @param channel
     * @throws IOException
     */
    private void synCardBreakNetwork(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String iccid = map.get("iccid").toString();

            String polling_id = map.get("polling_id").toString();//轮询任务编号

            redisCache.setCacheObject(polling_id+":"+ iccid, msg, 1, TimeUnit.HOURS);//1 小时缓存 用来统计轮询进度

            Map<String,Object> Parammap=new HashMap<>();
            Parammap.put("iccid",iccid);
            Parammap.put("Is_Break","1");
            Map<String,Object> Rmap =  internalApiRequest.queryFlow(Parammap,map);
            String code =  Rmap.get("code")!=null?Rmap.get("code").toString():"500";
            if(code.equals("200")){
                //获取 卡用量 开卡日期 更新 card info
                if(Rmap.get("Use")!=null &&  Rmap.get("Use")!="" &&  Rmap.get("Use").toString().trim().length()>0){
                    Double Use = Double.parseDouble(Rmap.get("Use").toString());
                    if(Use>=0){
                        try {
                            Map<String,Object> cardFlowSynRMap = cardFlowSyn.CalculationFlow(iccid,Use);
                            log.info(">>cardFlowSyn - 未订购资费断网轮询-卡用量轮询消费者 同步卡用量返回:{} | {}<<",iccid,JSON.toJSON(cardFlowSynRMap));
                            Double remaining =  Double.parseDouble(cardFlowSynRMap.get("remaining").toString());
                            if(remaining<=0 && remaining!=-1){
                                String status_id = map.get("status_id")!=null && map.get("status_id")!="" && map.get("status_id").toString().length()>0?map.get("status_id").toString():"未知";
                                log.info(">>Disconnected iccid:{} | cardFlowSynRMap {} | Parammap {} | map {} | status_id {}<<",iccid,JSON.toJSON(cardFlowSynRMap),Parammap,map,status_id);
                                BreakNetwork(iccid,Parammap,map,status_id);
                            }
                        }catch (Exception e){
                            log.error(">>cardFlowSyn - 未订购资费断网轮询-卡用量轮询消费者 同步卡用量失败:{} | {}<<",iccid,e.getMessage());
                        }
                    }else{
                        log.info(">>API -  未获取到卡用量 Use>=0 esle :{} | {}<<",iccid,Rmap);
                    }
                }

            }else {
                log.info(">>API - 未订购资费断网轮询-卡用量轮询消费者 未获取到卡用量:{} | {}<<",iccid,Rmap);
            }
        } catch (Exception e) {
            log.error(">>错误 - 未订购资费断网轮询轮询消费者:{}<<", e.getMessage().toString());
        }

    }


    /**
     * 断网
     * @param iccid
     * @param Parammap
     * @param map
     */
    public void BreakNetwork (String iccid,Map<String,Object> Parammap,Map<String,Object> map,String status_id){
        String cbefore = "";
        if(!status_id.equals("未知")){
            cbefore =  myDictionary.getdictLabel("yunze_card_status_ShowId",status_id);
        }else{
            cbefore = status_id;
        }
        cbefore = cbefore !=null && cbefore != "" && cbefore.length()>0?cbefore:"未知";
        Map<String,Object> Add_Map = new HashMap<>();
        Add_Map.put("iccid",iccid);
        Add_Map.put("ctype","2");//生命周期
        Add_Map.put("cbefore",cbefore);
        Add_Map.put("cafterward","断网");
        Add_Map.put("source_type","10");// 来源类型

        Map<String,Object> Upd_Map = new HashMap<>();
        Upd_Map.put("execution_status","1");//卡状态
        Upd_Map.put("iccid",iccid);
        Upd_Map.put("connection_status","2");//断网
        cardChange.ChangeConnection(Parammap,map,Add_Map,Upd_Map);
    }

}



























































































