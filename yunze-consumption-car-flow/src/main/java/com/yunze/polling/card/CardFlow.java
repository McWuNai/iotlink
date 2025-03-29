package com.yunze.polling.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.apiCommon.utils.RateLimiterUtil;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 卡用量轮询消费 者
 */
@Slf4j
@Component
public class CardFlow {


    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private RedisCache redisCache;
    @Resource
    private CardFlowSyn cardFlowSyn;

    @Resource
    GetShowStatIdArr getShowStatIdArr;

    @Resource
    RateLimiterUtil rateLimiterUtil;

    @Resource
    YzCardMapper yzCardMapper;

    /*@RabbitHandler
    @RabbitListener(queues = "api_pushApiCardData_queue",containerFactory = "customContainerFactory")
    public void pushApiCardData_exchange(String msg) {

    }*/

    private Map<String, Object> extracted(Map<String, Object> map, String iccid, Map<String, Object> Parammap, double Use) {
        Map<String, Object> RMap = cardFlowSyn.CalculationFlow(iccid, Use, map);
        if (Double.parseDouble(RMap.get("remaining").toString()) <= 0.00) {
            // 提前获取status_ShowId
            Object statusShowIdObj = yzCardMapper.find(Parammap).get("status_ShowId");
            Integer status_ShowId = statusShowIdObj instanceof Integer ? (Integer) statusShowIdObj : null;
            if (status_ShowId != null && status_ShowId != 5) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("iccid", Parammap.get("iccid").toString());
                map1.put("status_ShowId", "0");
                singleState(map1, map);
            }
        }
        return RMap;
    }


    /**
     *
     * @param msg
     * @param channel_1
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "polling_cardCardFlow_queue",containerFactory = "customContainerFactory")
    public void cardCardFlow_exchange(String msg, Channel channel_1) {
        if(msg!=null && msg.length()>0){
            synCardFlow(msg,true);
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "polling_cardBatchCardFlow_queue",containerFactory = "customContainerFactory")
    public void cardBatchCardFlow_exchange(String msg, Channel channel_1) {
        if(msg!=null && msg.length()>0){
            synBatchCardFlow(msg,true);
        }
    }

    /**
     * 死信队列
     * @param msg
     * @param channel_1
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "polling_dlxcardCardFlow_queue",containerFactory = "customContainerFactory")
    public void dlxcardCardFlow_exchange(String msg, Channel channel_1)  {
        if(msg!=null && msg.length()>0){
            synCardFlow(msg,true);
        }
    }



    /**
     *日用量记录为 负数 卡号 再次同步用量
     * @param msg
     * @param channel_1
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "polling_cardCardFlowLessThanZero_queue",containerFactory = "customContainerFactory")
    public void CardCardFlowLessThanZero(String msg, Channel channel_1) {
        if(msg!=null && msg.length()>0){
            synCardFlow(msg,false);
        }
    }

    /**
     * 批量同步卡用量
     *
     * @param msg
     * @throws IOException
     */
    public void synBatchCardFlow(String msg, boolean is_Record) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            List<String> iccids = (List<String>) map.get("iccids");
            String polling_id = "";
            for (String iccid : iccids) {
                if (is_Record) {
                    polling_id = map.get("polling_id").toString();//轮询任务编号
                    String prefix = "polling_cardBatchCardFlow_queue";
                    //执行前判断 redis 是否存在 执行数据 存在时 不执行
                    //Object isExecute = redisCache.getCacheObject(prefix + ":" + iccid);
                    // if (isExecute == null) {
                    //System.out.println("SUCCESS");
                    // redisCache.setCacheObject(prefix + ":" + iccid, msg, 3, TimeUnit.MINUTES);//3 分钟缓存 避免 重复消费
                    redisCache.setCacheObject(polling_id + ":" + iccid, msg, 1, TimeUnit.HOURS);//1 小时缓存 用来统计轮询进度
                }
            }


            Map<String, Object> Parammap = new HashMap<>();
            Parammap.put("iccids", iccids);
            //批量查询
            Map<String, Object> Rmap = internalApiRequest.autocompleteBatchCard(Parammap, map);
            String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
            if (code.equals("200")) {
                //获取 卡用量 开卡日期 更新 card info
                List<Map<String, Object>> cardInfo = (List<Map<String, Object>>) Rmap.get("card_info");
                for (Map<String, Object> info: cardInfo) {
                    if (info.get("used") != null && info.get("used") != "" && info.get("used").toString().trim().length() > 0) {
                        double Use = Double.parseDouble(info.get("used").toString());
                        if (Use >= 0) {
                            try {
                                yzCardMapper.updSingleCardData(info);
                                Map<String, Object> RMap = cardFlowSyn.CalculationBatchFlow(info.get("iccid").toString(), Use, map, info.get("realNameStatus").toString());
                                Object status_id = info.get("status_id").toString().trim();
                                int status_ShowId = Integer.parseInt(getShowStatIdArr.GetShowStatId(status_id.toString()));
                                info.put("status_ShowId",status_ShowId);
                                yzCardMapper.updStatusId(info);
                                if (Double.parseDouble(RMap.get("remaining").toString()) <= 0.00) {
                                    if (status_ShowId != 5) {
                                        Map<String, Object> map1 = new HashMap<>();
                                        map1.put("iccid", Parammap.get("iccid").toString());
                                        map1.put("status_ShowId", "0");
                                        singleState(map1,map);
                                    }
                                }
                                log.info(">>cardFlowSyn - 卡用量轮询消费者 同步卡用量返回:{} | {} | {} | {} <<", polling_id, info.get("iccid"), JSON.toJSON(RMap), JSON.toJSON(Rmap));
                            } catch (Exception e) {
                                log.error(">>cardFlowSyn - 卡用量轮询消费者 同步卡用量失败:{} | {} | {}  | {}<<", polling_id, info.get("iccid"), JSON.toJSON(Rmap) , e.getMessage().toString());
                            }
                        } else {
                            log.info(">>API - 卡用量轮询消费者 未获取到卡用量 {} |  statusCode = 0 :{} | {}<<", polling_id, info.get("iccid"), Rmap);
                        }
                    }
                }

                /*if (Rmap.get("used") != null && Rmap.get("used") != "" && Rmap.get("used").toString().trim().length() > 0) {
                    Double Use = Double.parseDouble(Rmap.get("used").toString());
                    if (Use >= 0) {
                        try {
                            yzCardMapper.updSingleCardData(Rmap);
                            Map<String, Object> RMap = cardFlowSyn.CalculationFlow(iccid, Use, map);
                            if (Double.parseDouble(RMap.get("remaining").toString()) <= 0.00) {
                                // 提前获取status_ShowId
                                Object statusShowIdObj = yzCardMapper.find(Parammap).get("status_ShowId");
                                Integer status_ShowId = statusShowIdObj instanceof Integer ? (Integer) statusShowIdObj : null;
                                if (status_ShowId != null && status_ShowId != 5) {
                                    Map<String, Object> map1 = new HashMap<>();
                                    map1.put("iccid", Parammap.get("iccid").toString());
                                    map1.put("status_ShowId", "0");
                                    singleState(map1);
                                }
                            }
                            log.info(">>cardFlowSyn - 卡用量轮询消费者 同步卡用量返回:{} | {} | {} | {} <<", polling_id, iccid, JSON.toJSON(RMap), JSON.toJSON(Rmap));
                        } catch (Exception e) {
                            log.error(">>cardFlowSyn - 卡用量轮询消费者 同步卡用量失败:{} | {} | {}  | {}<<", polling_id, iccid, JSON.toJSON(Rmap) , e.getMessage().toString());
                        }
                    } else {
                        log.info(">>API - 卡用量轮询消费者 未获取到卡用量 {} |  statusCode = 0 :{} | {}<<", polling_id, iccid, Rmap);
                    }
                }*/
            } else {
                log.info(">>API - 卡用量轮询消费者 未获取到批量卡用量:{} | {}<<", polling_id, Rmap);
                // System.out.println(map.get("iccid")+" 未获取到卡用量 ！");
            }
            // }
        } catch (Exception e) {
//            // 记录该消息日志形式  存放数据库db中、后期通过定时任务实现消息补偿、人工实现补偿
            log.error(">>错误 - 卡用量轮询消费者:{}<<", e.getMessage());
//            //将该消息存放到死信队列中，单独写一个死信消费者实现消费。
        }
    }

    /**
     * 同步卡用量
     *
     * @param msg
     * @throws IOException
     */
    public void synCardFlow(String msg, boolean is_Record) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String iccid = map.get("iccid").toString();
            String polling_id = "";
            if(is_Record){
                 polling_id = map.get("polling_id").toString();//轮询任务编号
                String prefix = "polling_cardCardFlow_queue";
                //执行前判断 redis 是否存在 执行数据 存在时 不执行
                //Object isExecute = redisCache.getCacheObject(prefix + ":" + iccid);
                // if (isExecute == null) {
                //System.out.println("SUCCESS");
                // redisCache.setCacheObject(prefix + ":" + iccid, msg, 3, TimeUnit.MINUTES);//3 分钟缓存 避免 重复消费
                redisCache.setCacheObject(polling_id + ":" + iccid, msg, 1, TimeUnit.HOURS);//1 小时缓存 用来统计轮询进度
            }


                Map<String, Object> Parammap = new HashMap<>();
                Parammap.put("iccid", iccid);
                Map<String, Object> Rmap = internalApiRequest.autocompleteCard(Parammap, map);
                String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                if (code.equals("200")) {
                    //获取 卡用量 开卡日期 更新 card info
                    if (Rmap.get("used") != null && Rmap.get("used") != "" && Rmap.get("used").toString().trim().length() > 0) {
                        Double Use = Double.parseDouble(Rmap.get("used").toString());
                        if (Use >= 0) {
                            try {
                                yzCardMapper.updSingleCardData(Rmap);
                                Map<String, Object> RMap = extracted(map, iccid, Parammap, Use);
                                log.info(">>cardFlowSyn - 卡用量轮询消费者 同步卡用量返回:{} | {} | {} | {} <<", polling_id, iccid, JSON.toJSON(RMap), JSON.toJSON(Rmap));
                            } catch (Exception e) {
                                log.error(">>cardFlowSyn - 卡用量轮询消费者 同步卡用量失败:{} | {} | {}  | {}<<", polling_id, iccid, JSON.toJSON(Rmap) , e.getMessage().toString());
                            }
                        } else {
                            log.info(">>API - 卡用量轮询消费者 未获取到卡用量 {} |  statusCode = 0 :{} | {}<<", polling_id, iccid, Rmap);
                        }
                    }
                } else {
                    log.info(">>API - 卡用量轮询消费者 未获取到卡用量:{} | {} | {}<<", polling_id, iccid, Rmap);
                    // System.out.println(map.get("iccid")+" 未获取到卡用量 ！");
                }
            // }
        } catch (Exception e) {
//            // 记录该消息日志形式  存放数据库db中、后期通过定时任务实现消息补偿、人工实现补偿
            log.error(">>错误 - 卡用量轮询消费者:{}<<", e.getMessage());
//            //将该消息存放到死信队列中，单独写一个死信消费者实现消费。
        }
    }

    public Map<String, Object> singleState(Map<String, Object> map,Map<String, Object> Route) {
        Map<String, Object> rMap = new HashMap<>();
        boolean bool = false;
        String message = "单卡灵活变更状态 操作失败";
        //Map<String, Object> Route = yzCardMapper.findRoute(map);
        if (Route != null) {
            String cd_status = Route.get("cd_status").toString();
            String iccid = Route.get("iccid").toString();
            Map<String, Object> Obj = new HashMap<>();
            Object ShowId = map.get("status_ShowId");
            Obj.put("operType", ShowId);//API 状态
            Obj.put("iccid", iccid);
            if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                Map<String, Object> CsFble = internalApiRequest.changeCardStatusFlexible(Obj, Route);
                String code = CsFble.get("code") != null ? CsFble.get("code").toString() : "500";
                if (code.equals("200")) {
                    String statusCode = map.get("status_ShowId").toString();
                    Map<String, Object> Upd_Map = new HashMap<>();
                    Upd_Map.put("status_id", statusCode);
                    Upd_Map.put("status_ShowId", getShowStatIdArr.GetShowStatId(statusCode));
                    Upd_Map.put("iccid", map.get("iccid").toString());
                    try {
                        yzCardMapper.updStatusId(Upd_Map);//变更卡状态
                        bool = true;
                        message = "操作成功！";
                    } catch (Exception e) {
                        message = "DB保存状态操作失败！" + e.getMessage();
                    }
                } else {
                    if (CsFble.get("Message") == null) {
                        message = "网络繁忙稍后重试！";
                    } else {
                        message = CsFble.get("Message").toString();
                    }
                }
            } else {
                message = "未知状态！";
            }
        } else {
            message = " iccid [" + map.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！";
        }
        rMap.put("bool",bool);
        rMap.put("message",message);
        return rMap;
    }
}
