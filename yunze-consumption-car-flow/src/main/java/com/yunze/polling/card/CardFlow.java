package com.yunze.polling.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.apiCommon.utils.RateLimiterUtil;
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
    RateLimiterUtil rateLimiterUtil;



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
                Map<String, Object> Rmap = internalApiRequest.queryFlow(Parammap, map);
                String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                if (code.equals("200")) {
                    //获取 卡用量 开卡日期 更新 card info
                    if (Rmap.get("Use") != null && Rmap.get("Use") != "" && Rmap.get("Use").toString().trim().length() > 0) {
                        Double Use = Double.parseDouble(Rmap.get("Use").toString());
                        if (Use >= 0) {
                            try {
                                Map<String, Object> RMap = cardFlowSyn.CalculationFlow(iccid, Use, map);
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


}
