package com.yunze.task.yunze.polling;

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

@Component
@Slf4j
public class CompensateSendMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;

    /**
     *  用量补偿 - 【指定时间内确保有一次正常数据获取】
     *
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_FlowCompensateSend_queue")
    public void pollingFlow(String msg2) {
        if (StringUtils.isEmpty(msg2)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg2);
        Integer time = Integer.parseInt(Pmap.get("time").toString());

        String day = Pmap.get("day").toString();
        String status_ShowId = Pmap.get("status_ShowId")!=null?Pmap.get("status_ShowId").toString():null;

        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            String  srTime = VeDate.getNextDay(VeDate.getStringDateShort(),"-"+day);
            String addOrder_exchangeName = "admin_exchange";
            String addOrder_routingKey = "polling.FlowCompensate.queue";
            String queue = "polling_FlowCompensate_queue";

            Purgr(addOrder_exchangeName,queue,addOrder_routingKey,true);//清空队列积压

            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                findMap.put("status_ShowId", status_ShowId);
                findMap.put("srTime", srTime+" 00:00:00");
                if(Pmap.get("StarRow")!=null && Pmap.get("PageSize")!=null){
                    findMap.put("StarRow", Pmap.get("StarRow"));
                    findMap.put("PageSize", Pmap.get("PageSize"));
                }

                List<Map<String, Object>> cardArr = yzCardMapper.findFlowCompensate(findMap);
                if (cardArr != null && cardArr.size()>0) {
                    //插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);
                    //卡状态 用量 轮询
                    String polling_id_CardBreakNetwork = VeDate.getNo(4);
                    pollingPublic_Map.put("polling_type", "22");//轮询类型  用量同步补偿
                    pollingPublic_Map.put("cd_count", cardArr.size());
                    pollingPublic_Map.put("polling_id", polling_id_CardBreakNetwork);
                    yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表

                    //2.卡状态
                    //卡号放入路由
                    for (int j = 0; j < cardArr.size(); j++) {
                        Map<String, Object> card = cardArr.get(j);
                        Map<String, Object> Card = new HashMap<>();
                        Card.putAll(channel_obj);
                        Card.put("iccid", card.get("iccid"));
                        Card.put("card_no", card.get("card_no"));
                        Card.put("status_id", card.get("status_id"));
                        Card.put("polling_id", polling_id_CardBreakNetwork);//轮询任务详情编号
                        Card.put("network_type", card.get("network_type"));
                        String msg = JSON.toJSONString(Card);

                        //生产任务
                        try {
                            rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, msg, message -> {
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


    /**
     * 清空 队列
     * @param exchangeName
     * @param queue
     * @param Key
     * @param isDlx
     */
    public void Purgr(String exchangeName,String queue,String Key,boolean isDlx){
        boolean bool = false;
        boolean dlx_bool = false;
        try {
            bool = rabbitMQConfig.purgeMessage(exchangeName,queue,Key,"direct");
        }catch (Exception e){
            log.error("rabbitMQConfig.purgeMessage 异常 exchangeName {} , queue {} , Key {}",exchangeName,queue,Key);
        }
        if(isDlx){
            try {
                dlx_bool = rabbitMQConfig.purgeMessage("dlx_"+exchangeName,"dlx_"+queue,"dlx_"+Key,"direct");
            }catch (Exception e){
                log.error("dlx rabbitMQConfig.purgeMessage 异常 exchangeName {} , queue {} , Key {}",exchangeName,queue,Key);
            }
        }
        log.error("Purgr  exchangeName={} , queue={} , Key={},isDlx={} ,bool={} ,dlx_bool = {}",exchangeName,queue,Key,isDlx,bool,dlx_bool);
    }





    /**
     *  生命周期补偿 - 【指定时间内确保有一次正常数据获取】
     *
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_StateCompensateSend_queue")
    public void pollingState(String msg2) {
        if (StringUtils.isEmpty(msg2)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg2);
        Integer time = Integer.parseInt(Pmap.get("time").toString());

        String day = Pmap.get("day").toString();

        //1.状态 正常 轮询开启 时 获取  每个 通道下卡号 加入队列
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            String  srTime = VeDate.getNextDay(VeDate.getStringDateShort(),"-"+day);
            String addOrder_exchangeName = "admin_exchange";
            String addOrder_routingKey = "polling.StateCompensate.queue";
            String queue = "polling_StateCompensate_queue";

            Purgr(addOrder_exchangeName,queue,addOrder_routingKey,true);//清空队列积压


            //2.获取 通道下卡号
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> channel_obj = channelArr.get(i);
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                findMap.put("srTime", srTime+" 00:00:00");
                List<Map<String, Object>> cardArr = yzCardMapper.findStatusCompensate(findMap);
                if (cardArr != null && cardArr.size()>0) {
                    //插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);
                    //卡状态 用量 轮询
                    String polling_id_CardBreakNetwork = VeDate.getNo(4);
                    pollingPublic_Map.put("polling_type", "23");//轮询类型  生命周期同步补偿
                    pollingPublic_Map.put("cd_count", cardArr.size());
                    pollingPublic_Map.put("polling_id", polling_id_CardBreakNetwork);
                    yzPassagewayPollingMapper.add(pollingPublic_Map);//新增 轮询详情表

                    //2.卡状态
                    //卡号放入路由
                    for (int j = 0; j < cardArr.size(); j++) {
                        Map<String, Object> card = cardArr.get(j);
                        Map<String, Object> Card = new HashMap<>();
                        Card.putAll(channel_obj);
                        Card.put("iccid", card.get("iccid"));
                        Card.put("card_no", card.get("card_no"));
                        Card.put("status_id", card.get("status_id"));
                        Card.put("polling_id", polling_id_CardBreakNetwork);//轮询任务详情编号
                        Card.put("network_type", card.get("network_type"));
                        String msg = JSON.toJSONString(Card);

                        //生产任务
                        try {
                            rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, msg, message -> {
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
