package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信订单 消费
 * @Auther: zhang feng
 * @Date: 2021/09/02/11:55
 * @Description:
 */
@Slf4j
@Component
public class WxAddOrder {

    @Resource
    private RedisCache redisCache;
    @Resource
    private YzOrderMapper yzOrderMapper;


    /**
     * 微信充值 订单 消费者
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_WxWebOrder_queue")
    public void createListener(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> MsgMap = JSON.parseObject(msg);
            Map<String,Object> OrderMap = (Map<String, Object>) MsgMap.get("OrderMap");
            String ord_no = OrderMap.get("ord_no").toString();
            String iccid = OrderMap.get("iccid").toString();
            String prefix = "admin_WxWebOrder_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ord_no+":"+iccid);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ord_no+":"+iccid, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                AddOrder(OrderMap);//生成订单
            }
        } catch (Exception e) {
            log.error(">>错误 - 微信充值 订单  消费者:{}<<", e.getMessage());
        }
    }


    /**
     * 生成订单
     * @param OrderMap
     */
    public void AddOrder(Map<String,Object> OrderMap){
        try {
            int saveCount = yzOrderMapper.save(OrderMap);
            if(saveCount>0){
                log.info(">>成功 - 微信充值新增订单  受影响数据:{}<<", saveCount);
            }else{
                log.error(">>失败 - 微信充值 新增订单  受影响数据:{}<<", saveCount);
            }
        }catch (Exception e){
            log.error(">>异常 - 微信充值 AddOrder {} /n 信息 :{}<<",OrderMap,e.getMessage());
        }

    }



}
