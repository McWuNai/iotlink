package com.yunze.task.yunze.intelligent;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class CardFlowAiTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;

    @Resource
    private RabbitMQConfig rabbitMQConfig;


    String ad_exchangeName = null, ad_queueName = null, ad_routingKey = null,
            ad_del_exchangeName = null, ad_del_queueName = null, ad_del_routingKey = null;








    /**
     * 轮询 用量 [用量轮询-智能-已激活 ][用量轮询-智能-未激活-有用量] [用量轮询-智能-未激活-已发货]
     *  time 多少 分钟 后失效
     *  activate_dateIsNull 激活时间是否为空
     *  polling_type 轮询类型 【用量轮询-智能-有激活时间 6 】【用量轮询-智能-无激活时间-有用量 7 】
     *  usedIsNull 用量是否大于 0
     *  deliver_dateIsNull 发货时间是否为空
     *
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardFlow_queue")
    public void pollingCardFlow(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        String activate_dateIsNull = Pmap.get("activate_dateIsNull").toString();
        String polling_type = Pmap.get("polling_type").toString();
        String usedIsNull = Pmap.get("usedIsNull").toString();
        String deliver_dateIsNull = Pmap.get("deliver_dateIsNull").toString();

        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("cd_algorithm", "2");//智能轮询

        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            String CardFlow_routingKey = "polling.cardCardFlow.routingKey";
           /* try {
                //设置任务 路由器 名称 与队列 名称
                ad_exchangeName = "polling_cardCardFlow_exchange";
                ad_queueName = "polling_cardCardFlow_queue";

                ad_del_exchangeName = "polling_dlxcardCardFlow_exchange";
                ad_del_queueName = "polling_dlxcardCardFlow_queue";
                ad_del_routingKey = "polling.dlxcardCardFlow.routingKey";
               // rabbitMQConfig.creatExchangeQueue(ad_exchangeName, ad_queueName, CardFlow_routingKey, ad_del_exchangeName, ad_del_queueName, ad_del_routingKey, null);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }*/

            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                findMap.put("activate_date", activate_dateIsNull);//激活时间
                findMap.put("used", usedIsNull);//用量
                findMap.put("deliver_date", deliver_dateIsNull);//发货时间

                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    pollingExecute(cd_id,cardArr,channel_obj,CardFlow_routingKey,time,polling_type);
                }
            }
        }
    }





    /**
     * 轮询 用量 [用量轮询-智能-未激活-未发货-已到沉默期]
     *  time 多少 分钟 后失效
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardFlowSilencePeriod_queue")
    public void pollingCardFlowSilencePeriod(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("cd_algorithm", "2");//智能轮询


        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            String  CardFlow_routingKey = "polling.cardCardFlow.routingKey";

            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);

                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCarSilencePeriod(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    pollingExecute(cd_id,cardArr,channel_obj,CardFlow_routingKey,time,"8");
                }
            }
        }
    }





    /**
     * 轮询 用量 用量记录无记录
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardFlowNotRecord_queue")
    public void pollingCardFlowNotRecord(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", null);

        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            String CardFlow_routingKey = "polling.cardCardFlow.routingKey";

            String YyyyAndMmArr[] = VeDate.getYyyyAndMm();
            String year = YyyyAndMmArr[0];
            String month = YyyyAndMmArr[1];
            String day = YyyyAndMmArr[2];
            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                findMap.put("year", year);
                findMap.put("month", month);
                findMap.put("day", day);

                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCarNotRecord(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    pollingExecute(cd_id,cardArr,channel_obj,CardFlow_routingKey,time,"18");
                }
            }
        }
    }



    /**
     * 公共执行
     * @param cd_id
     * @param cardArr
     * @param channel_obj
     * @param CardFlow_routingKey
     * @param time
     * @param polling_type
     */
    public void pollingExecute(String cd_id,List<Map<String, Object>> cardArr,Map<String, Object> channel_obj,String CardFlow_routingKey,Integer time,String polling_type){
        //插入 通道轮询详情表
        Map<String, Object> pollingPublic_Map = new HashMap<>();
        pollingPublic_Map.put("cd_id", cd_id);
        pollingPublic_Map.put("cd_current", 0);
        //卡状态 用量 轮询
        String polling_id = VeDate.getNo(4);
        pollingPublic_Map.put("polling_type",polling_type );//轮询类型
        pollingPublic_Map.put("cd_count", cardArr.size());
        pollingPublic_Map.put("polling_id", polling_id);
        yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表
        //2.卡状态
        //卡号放入路由
        for (int j = 0; j < cardArr.size(); j++) {
            Map<String, Object> card = cardArr.get(j);
            Map<String, Object> Card = new HashMap<>();
            Card.putAll(channel_obj);
            Card.put("iccid", card.get("iccid"));
            Card.put("card_no", card.get("card_no"));
            Card.put("network_type", card.get("network_type"));
            Card.put("polling_id", polling_id);//轮询任务详情编号
            String msg = JSON.toJSONString(Card);
            //生产任务
            try {
                rabbitTemplate.convertAndSend("polling_cardCardFlow_exchange", CardFlow_routingKey, msg, message -> {
                    // 设置消息过期时间 time 分钟 过期
                    message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println(e.getMessage().toString());
            }
        }

    }

}
