package com.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/05/10 17:09
 * @Description: 记录用量查询接口错误信息记录 【用来分析接口状态】
 */
@Slf4j
@Component
public class SynCardPollingError {

    @Resource
    private YzCardPollingErrorMapper yzCardPollingErrorMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_sendQuerFlowError_queue",containerFactory = "customContainerFactory")
    public void SynCardPollingError(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            CardSynEnforce(Pmap);
        }catch (Exception e){
            log.error(">>错误 - SynCardPollingError 消费者:{}<<", e.getMessage().toString());
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_sendQuerFlowError_queue",containerFactory = "customContainerFactory")
    public void dlxSynCardPollingError(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            CardSynEnforce(Pmap);
        }catch (Exception e){
            log.error(">>错误 - dlxSynCardPollingError 消费者:{}<<", e.getMessage().toString());
        }
    }



    public  void CardSynEnforce(Map<String,Object> Pmap){
            int saveCount = 0 ;
            int updCount = 0 ;
            try {
                String id =  yzCardPollingErrorMapper.findEx(Pmap);
                if(id!=null && id.length()>0){
                    Pmap.put("id",id);
                   updCount =   yzCardPollingErrorMapper.upd(Pmap);
                }else{
                   saveCount =   yzCardPollingErrorMapper.save(Pmap);
                }
            }catch (Exception e){
                log.info("Pmap {} : 新增 {} 修改 {} 异常信息 {}  ",JSON.toJSONString(Pmap),saveCount,updCount,e.getMessage());
            }
            log.info("iccid {} : 新增 {} 修改 {} ",Pmap.get("iccid"),saveCount,updCount);


    }




}
