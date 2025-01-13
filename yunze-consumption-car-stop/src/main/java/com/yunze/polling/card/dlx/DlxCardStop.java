package com.yunze.polling.card.dlx;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.config.MyDictionary;
import com.yunze.common.core.card.CardChange;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.CardFlowSyn;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
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
 * 达量停机轮询消费 者
 */
@Slf4j
@Component
public class DlxCardStop {


    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private CardFlowSyn cardFlowSyn;
    @Resource
    private MyDictionary myDictionary;
    @Resource
    private CardChange cardChange;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;



    @RabbitHandler
    @RabbitListener(queues = "polling_dlxcardCardStop_queue",containerFactory = "customContainerFactory")
    public void dlxcardCardStop_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            synCardStop(msg,channel_1);
        }
    }



    /**
     * 同步达量停机
     *
     * @param msg
     * @param channel
     * @throws IOException
     */
    public void synCardStop(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }

            Map<String, Object> map = JSON.parseObject(msg);
            String iccid = map.get("iccid").toString();
            boolean is_sel = (boolean) map.get("is_sel");

            Double Max = Double.parseDouble(map.get("Max").toString());


            String polling_id = map.get("polling_id").toString();//轮询任务编号
            String prefix = "polling_dlxcardCardStop_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            //Object isExecute = redisCache.getCacheObject(prefix + ":" + iccid);
            //if (isExecute == null) {
                //System.out.println("SUCCESS");
            //redisCache.setCacheObject(prefix + ":" + iccid, msg, 3, TimeUnit.MINUTES);//3 分钟缓存 避免 重复消费
                redisCache.setCacheObject(polling_id + ":" + iccid, msg, 1, TimeUnit.HOURS);//1 小时缓存 用来统计轮询进度

                Map<String, Object> Parammap = new HashMap<>();
                Parammap.put("iccid", iccid);
                Parammap.put("Is_Stop", "off");
                //不需要查询的 已经超出的直接执行停机
                if (is_sel) {
                    Map<String, Object> Rmap = internalApiRequest.queryFlow(Parammap, map);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        //获取 卡用量 开卡日期 更新 card info
                        if (Rmap.get("Use") != null && Rmap.get("Use") != "" && Rmap.get("Use").toString().trim().length() > 0) {
                            Double Use = Double.parseDouble(Rmap.get("Use").toString());
                            if (Use >= 0) {
                                try {
                                    Map<String, Object> RMap = cardFlowSyn.CalculationFlow(iccid, Use, map);
                                    log.info(">>cardFlowSyn - 死信 达量停机-卡用量轮询消费者 同步卡用量返回:{} | {}<<", iccid, JSON.toJSON(RMap));
                                    Double used = Double.parseDouble(RMap.get("used").toString());
                                    if (used >= Max) {
                                        stop(iccid, Parammap, map);
                                    }
                                } catch (Exception e) {
                                    log.error(">>cardFlowSyn - 死信 达量停机-卡用量轮询消费者 同步卡用量失败:{} | {}<<", iccid, e.getMessage().toString());
                                }
                            } else {
                                log.info(">>API - 死信 达量停机-卡用量轮询消费者 未获取到卡用量 statusCode = 0 :{} | {}<<", iccid, Rmap);
                            }
                        }
                    } else {
                        log.info(">>API - 死信 达量停机-卡用量轮询消费者 未获取到卡用量:{} | {}<<", iccid, Rmap);
                    }
                } else {
                    String status_id = map.get("status_id") != null ? map.get("status_id").toString() : "8";//未知
                    stop(iccid, Parammap, map, status_id);
                }
            // }
        } catch (Exception e) {
            log.error(">>错误 - 死信 达量停机轮询消费者:{}<<", e.getMessage().toString());
        }
    }


    /**
     * 停机
     *
     * @param iccid
     * @param Parammap
     * @param map
     */
    public void stop(String iccid, Map<String, Object> Parammap, Map<String, Object> map) {
        Map<String, Object> Rmap = internalApiRequest.changeCardStatus(Parammap, map);
        String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
        if (code.equals("200")) {
            //获取 达量停机 开卡日期 更新 card info
            Map<String, Object> Upd_Map = new HashMap<>();
            Upd_Map.put("status_id", "2");
            Upd_Map.put("iccid", iccid);
            Object status_id = Upd_Map.get("status_id");
            String status_ShowId = getShowStatIdArr.GetShowStatId(status_id.toString());
            Upd_Map.put("status_ShowId",status_ShowId);
            boolean bool = yzCardMapper.updStatusId(Upd_Map) > 0;//变更达量停机
            log.info(map.get("iccid") + " 修改死信 达量停机 bool ： " + bool);
            if (!bool) {
                log.error(">>DB - 死信 达量停机轮询消费者 同步达量停机失败:{} | {}<<", iccid, Upd_Map);
            }
        } else {
            log.info(">>API - 死信 达量停机轮询消费者 未获取到达量停机:{} | {}<<", iccid, Rmap);
        }
    }


    /**
     * 停机
     *
     * @param iccid
     * @param Parammap
     * @param map
     */
    public void stop(String iccid, Map<String, Object> Parammap, Map<String, Object> map, String status_id) {
        Map<String, Object> Add_Map = new HashMap<>();
        Add_Map.put("iccid", iccid);
        Add_Map.put("ctype", "2");//生命周期
        Add_Map.put("cbefore", myDictionary.getdictLabel("yunze_card_status_ShowId", status_id));
        Add_Map.put("cafterward", "停机");
        Add_Map.put("source_type", "1");// 来源 类型 ：1 达量停机
        Map<String, Object> Upd_Map = new HashMap<>();
        Upd_Map.put("status_id", "2");
        Upd_Map.put("iccid", iccid);
        cardChange.OnOff(Parammap, map, Add_Map, Upd_Map);
    }

}
