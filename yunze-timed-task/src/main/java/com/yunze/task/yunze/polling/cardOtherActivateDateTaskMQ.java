package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 卡状态类型为 字典 指定类型  pollingOther_cardCardActivateDate_queue 下数据时 同步激活时间
 *
 * @author root
 */
@Component
public class cardOtherActivateDateTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;


    String ad_exchangeName = null, ad_queueName = null, ad_routingKey = null;





    /**
     * 轮询 卡状态
     * @param time 多少 分钟 后失效
     */
    public void pollingCardActivateDate(Integer time) {
        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {

            try {
                //设置任务 路由器 名称 与队列 名称
                ad_exchangeName = "polling_cardCardActivateDate_exchange";
                ad_queueName = "pollingOther_cardCardActivateDate_queue" ;
                ad_routingKey = "pollingOther.cardCardActivateDate.queue";
            }catch (Exception e){
                System.out.println(e.getMessage());
            }


            List<String> status_ShowIdArr = new ArrayList<>();//需要轮询的 卡状态
            Map<String, Object> findDict = new HashMap<>();
            findDict.put("dict_type", "pollingOther_cardCardActivateDate");
            List<Map<String, Object>>  dict_dataArr = yzCardMapper.findDict(findDict);
            if(dict_dataArr!=null && dict_dataArr.size()>0){
                for (int i = 0; i < dict_dataArr.size(); i++) {
                    Map<String, Object> dict = dict_dataArr.get(i);
                    status_ShowIdArr.add(dict.get("dict_value").toString());
                }
            }
            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                findMap.put("status_ShowIdArr", status_ShowIdArr);
                findMap.put("activate_date", "Null");//没有激活时间的
                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    //插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);
                    //卡状态 用量 轮询
                    String polling_id_CardActivateDate = VeDate.getNo(4);
                    pollingPublic_Map.put("polling_type", "21");
                    pollingPublic_Map.put("cd_count", cardArr.size());
                    pollingPublic_Map.put("polling_id", polling_id_CardActivateDate);
                    yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表
                    //创建 路由 新增轮询详情 生产启动类型消息
                    for (int j = 0; j < cardArr.size(); j++) {
                        Map<String, Object> card = cardArr.get(j);
                        Map<String, Object> Card = new HashMap<>();
                        Card.putAll(channel_obj);
                        Card.put("iccid", card.get("iccid"));
                        Card.put("card_no", card.get("card_no"));
                        Card.put("polling_id", polling_id_CardActivateDate);//轮询任务详情编号
                        String msg = JSON.toJSONString(Card);
                        //生产任务
                        try {
                                rabbitTemplate.convertAndSend(ad_exchangeName, ad_routingKey, msg, message -> {
                                    // 设置消息过期时间 time 分钟 过期
                                    message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                                    return message;
                                });
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }












}
