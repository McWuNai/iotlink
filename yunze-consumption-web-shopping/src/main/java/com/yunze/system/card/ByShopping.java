package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByOrderMapper;
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
 * 微信商品购买下单 消费者
 */
@Slf4j
@Component
public class ByShopping {



    @Resource
    private RedisCache redisCache;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzWxByOrderMapper yzWxByOrderMapper;

    /**
     * 微信购物 订单 消费者
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_WxShopping_queue")
    public void createListener(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> MsgMap = JSON.parseObject(msg);
            Map<String,Object> OrderMap = (Map<String, Object>) MsgMap.get("OrderMap");
            Map<String,Object> byOrderMap = (Map<String, Object>) MsgMap.get("byOrderMap");

            String ord_no = OrderMap.get("ord_no").toString();
            String iccid = OrderMap.get("iccid").toString();
            String open_id = OrderMap.get("open_id").toString();
            String prefix = "admin_WxShopping_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ord_no+":"+iccid+":"+open_id);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ord_no+":"+iccid+":"+open_id, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                AddOrder(OrderMap,byOrderMap);//生成订单
            }
        } catch (Exception e) {
            log.error(">>错误 - 微信购物 订单  消费者:{}<<", e.getMessage());
        }
    }


    /**
     * 生成订单
     * @param OrderMap
     */
    public void AddOrder(Map<String,Object> OrderMap,Map<String,Object> byOrderMap){
        try {
            int saveCount = yzOrderMapper.ShoppingSave(OrderMap);
            int ByOrderSaveCount = yzWxByOrderMapper.save(byOrderMap);
            if(saveCount>0 && ByOrderSaveCount>0){
                log.info(">>成功 - 新增订单  受影响数据:{}  /n  微信购物新增订单  受影响数据:{} <<", saveCount, ByOrderSaveCount);
            }else{
                log.error(">>失败 - 新增订单  受影响数据:{} /n  微信购物新增订单  受影响数据:{} <<", saveCount, ByOrderSaveCount);
            }
        }catch (Exception e){
            log.error(">>异常 - 微信购物 OrderMap {} /n byOrderMap {} /n 信息 :{}<<",OrderMap,byOrderMap,e.getMessage());
        }

    }


}
