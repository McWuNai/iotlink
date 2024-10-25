package com.yunze.system.moneyChange;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.sysgl.YzMoneyChangeRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * 金额变更记录表
 */
@Slf4j
@Component
public class MoneyChangeRecord {


    @Resource
    private YzMoneyChangeRecordMapper yzMoneyChangeRecordMapper;


    /**
     * 新增 金额变更记录表
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_insertMoneyChangeRecord_queue", containerFactory = "customContainerFactory")
    public void CardUpd(String msg, Channel channel)   {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> Pmap = JSON.parseObject(msg);
            int saveCount = yzMoneyChangeRecordMapper.saveMoneyChange(Pmap);
            log.info(">> {} 执行结果 - saveCount:{}<<",Pmap,saveCount);
        } catch (Exception e) {
            log.error(">>Exception admin_insertMoneyChangeRecord_queue 消费者:{}<<", e.getMessage());
        }
    }


    /**
     * 新增 金额变更记录表
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_insertMoneyChangeRecord_queue", containerFactory = "customContainerFactory")
    public void dlx_CardUpd(String msg, Channel channel) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> Pmap = JSON.parseObject(msg);
            int saveCount = yzMoneyChangeRecordMapper.saveMoneyChange(Pmap);
            log.info(">> {} 执行结果 - saveCount:{}<<",Pmap,saveCount);
        } catch (Exception e) {
            log.error(">>Exception dlx_admin_insertMoneyChangeRecord_queue 消费者:{}<<", e.getMessage());
        }
    }




}



