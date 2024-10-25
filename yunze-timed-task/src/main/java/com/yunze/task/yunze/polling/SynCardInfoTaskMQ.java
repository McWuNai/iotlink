
package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.mapper.yunze.polling.YzSynCardInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据  群组所属成员信息 同步iccid 等详情
 */
@Slf4j
@Component
public class SynCardInfoTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzSynCardInfoMapper yzSynCardInfoMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitHandler
    @RabbitListener(queues = "admin_synCardIccid_queue")
    public void SynCardInfo() {
        //1.状态 正常 且开启了需要同步上游成员 时
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        /*findRouteID_Map.put("cd_lunxun","notChoose");//是否轮询 不选择（只要是状态正常就去获取）*/
        findRouteID_Map.put("sync_upstream","1");// 同步上游卡号数据
        String endTime = VeDate.getStringDateShort();
        Map<String,Object> fConfigMap = new HashMap<>();
        fConfigMap.put("config_key","iotlink.synChannel.updTime");
        String longDay = yzWxByProductAgentMapper.findConfig(fConfigMap);
        longDay = longDay!=null?longDay:"2";//默认为近三天的
        int day = Integer.parseInt("-"+longDay);
        String staTime =  VeDate.getBeforeAfterDate(endTime,day);
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> ChannelObj = channelArr.get(i);
                String channel_id = ChannelObj.get("cd_id").toString();
                ChannelObj.put("channel_id",channel_id);
                ChannelObj.put("staTime",staTime);
                ChannelObj.put("endTime",endTime);
                int updCount = 0;
                List<String> msisdnArr = yzSynCardInfoMapper.getNotSyncedInfo(ChannelObj);
                if (msisdnArr != null && msisdnArr.size() > 0) {
                    Map<String,Object> pMap = new HashMap<>();

                    for (int j = 0; j <  msisdnArr.size(); j++) {
                        String msisdn = msisdnArr.get(j);
                        Map<String,Object> map = new HashMap<>();
                        map.put("card_no",msisdn);
                        map.put("msisdn",msisdn);
                        ChannelObj.put("card_no",msisdn);
                        pMap.put("map",map);
                        pMap.put("ChannelObj",ChannelObj);

                        String msg = JSON.toJSONString(pMap);
                        //生产任务
                        try {
                            rabbitTemplate.convertAndSend("admin_exchange", "admin.synCardInfoSel.queue", msg, message -> {
                                // 设置消息过期时间 time 分钟 过期
                                message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                                return message;
                            });
                            updCount +=1;
                        } catch (Exception e) {
                            System.out.println(e.getMessage().toString());
                        }
                    }
                    log.info(" 通道ID "+channel_id+" 本次 因修改 【"+msisdnArr.size()+"】,实际 发送修改指令 " +updCount+" 条 。");
                }
            }
        }
    }




}
