package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class RebootTaskMQ {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;




    /**
     * 重新启动 没有在Redis 记录 的轮询 通道 创建 消费者 轮询
     * @ time
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_RebootFlowPolling_queue")
    public void RebootFlowPolling(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());

        Map<String, Object> findRouteID_Map = new HashMap<>();

        List<String> Cd_idArr = yzCardRouteMapper.findCardChannelID();
        Cd_idArr = Cd_idArr != null && Cd_idArr.size() > 0 ? Cd_idArr : null;
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("Cd_idArr", Cd_idArr);
        List<Map<String, Object>> ChannelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);

        Map<String, Object> rebootStandardMap = new HashMap<>();

        Object rebootStandard = redisCache.getCacheObject("polling.rebootStandard");
        Integer rebootStandardint = null;
        if (rebootStandard != null && rebootStandard.toString().length() > 0) {
            rebootStandard = rebootStandard != null ? rebootStandard : "1000";//默认 1K
        } else {
            rebootStandardMap.put("config_key", "polling.rebootStandard");
            rebootStandard = yzWxByProductAgentMapper.findConfig(rebootStandardMap);
            rebootStandard = rebootStandard != null ? rebootStandard : "1000";//默认 1K
            redisCache.setCacheObject("polling.rebootStandard", rebootStandard, 6, TimeUnit.HOURS);//6 小时 缓存
        }

        rebootStandardint = Integer.parseInt(rebootStandard.toString());
        String Type = "direct";
        if (ChannelArr != null && ChannelArr.size() > 0) {
            for (int i = 0; i < ChannelArr.size(); i++) {
                Map<String, Object> ChannelObj = ChannelArr.get(i);
                Map<String, Object> msg_Map = new HashMap<>();
                String cd_id = ChannelObj.get("cd_id").toString();
                msg_Map.put("FindCd_id", ChannelObj.get("cd_id"));

                RebootFlow(cd_id,time,rebootStandardint,msg_Map,Type);
                RebootStatus(cd_id,time,rebootStandardint,msg_Map,Type);
                RebootActivateDate(cd_id,time,rebootStandardint,msg_Map,Type);

            }
        }
    }





    /**
     * 重新启动 没有在Redis 记录 的轮询 通道 创建 消费者 轮询
     * @ time
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_RebootCardStop_queue")
    public void RebootCardStop(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        Map<String, Object> findRouteID_Map = new HashMap<>();

        List<String> Cd_idArr = yzCardRouteMapper.findRemindRatioChannelID();
        Cd_idArr = Cd_idArr != null && Cd_idArr.size() > 0 ? Cd_idArr : null;
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("Cd_idArr", Cd_idArr);
        List<Map<String, Object>> ChannelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);

        Map<String, Object> rebootStandardMap = new HashMap<>();

        Object rebootStandard = redisCache.getCacheObject("polling.rebootCardStop");
        Integer rebootStandardint = null;
        if (rebootStandard != null && rebootStandard.toString().length() > 0) {
            rebootStandard = rebootStandard != null ? rebootStandard : "100";//默认 100
        } else {
            rebootStandardMap.put("config_key", "polling.rebootCardStop");
            rebootStandard = yzWxByProductAgentMapper.findConfig(rebootStandardMap);
            rebootStandard = rebootStandard != null ? rebootStandard : "100";//默认 100
            redisCache.setCacheObject("polling.rebootStandard", rebootStandard, 6, TimeUnit.HOURS);//6 小时 缓存
        }

        rebootStandardint = Integer.parseInt(rebootStandard.toString());
        String Type = "direct";
        if (ChannelArr != null && ChannelArr.size() > 0) {
            for (int i = 0; i < ChannelArr.size(); i++) {
                Map<String, Object> ChannelObj = ChannelArr.get(i);
                Map<String, Object> msg_Map = new HashMap<>();
                String cd_id = ChannelObj.get("cd_id").toString();
                msg_Map.put("cd_id", ChannelObj.get("cd_id"));

                RebootCardStop(cd_id,time,rebootStandardint,msg_Map,Type);


            }
        }
    }

    /**
     * 重新启动 没有在Redis 记录 的轮询 通道 创建 消费者 轮询
     * @ time
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_RebootCardDisconnected_queue")
    public void RebootCardDisconnected(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        Map<String, Object> findRouteID_Map = new HashMap<>();

        List<String> Cd_idArr = yzCardRouteMapper.findIsDisconnectedChannelID();
        Cd_idArr = Cd_idArr != null && Cd_idArr.size() > 0 ? Cd_idArr : null;
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("Cd_idArr", Cd_idArr);
        List<Map<String, Object>> ChannelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);

        Map<String, Object> rebootStandardMap = new HashMap<>();

        Object rebootStandard = redisCache.getCacheObject("polling.rebootCardDisconnected");
        Integer rebootStandardint = null;
        if (rebootStandard != null && rebootStandard.toString().length() > 0) {
            rebootStandard = rebootStandard != null ? rebootStandard : "100";//默认 100
        } else {
            rebootStandardMap.put("config_key", "polling.rebootCardDisconnected");
            rebootStandard = yzWxByProductAgentMapper.findConfig(rebootStandardMap);
            rebootStandard = rebootStandard != null ? rebootStandard : "100";//默认 100
            redisCache.setCacheObject("polling.rebootStandard", rebootStandard, 6, TimeUnit.HOURS);//6 小时 缓存
        }
        rebootStandardint = Integer.parseInt(rebootStandard.toString());
        String Type = "direct";
        if (ChannelArr != null && ChannelArr.size() > 0) {
            for (int i = 0; i < ChannelArr.size(); i++) {
                Map<String, Object> ChannelObj = ChannelArr.get(i);
                Map<String, Object> msg_Map = new HashMap<>();
                String cd_id = ChannelObj.get("cd_id").toString();
                msg_Map.put("cd_id", ChannelObj.get("cd_id"));
                RebootCardDisconnected(cd_id,time,rebootStandardint,msg_Map,Type);
            }
        }
    }







    /**
     * 重启 轮询 超停阈值
     * @param cd_id
     * @param time
     * @param rebootStandardint
     * @param msg_Map
     * @param Type
     */
    public void RebootCardStop(String cd_id,Integer time,Integer rebootStandardint,Map<String, Object> msg_Map,String Type){
        String QFlowName = "polling_cardCardStop_queue" ;
        String dlxQFlowName = QFlowName;//死信 redis

        String card_exchangeName = "polling_cardCardStop_exchange";
        String card_queueName = "polling_cardCardStop_queue_"+cd_id;
        String card_routingKey = "polling.cardCardStop.routingKey."+cd_id;
        String OneKey = "admin.CardStop.queue.One";

        String card_del_exchangeName = "polling_dlxcardCardStop_exchange";
        String card_del_queueName = "polling_dlxcardCardStop_queue";
        String card_del_routingKey = "polling.dlxcardCardStop.routingKey";
        String TowKey = "admin.CardStop.queue.Tow";
        msg_Map.put("Listener",QFlowName);
        RebootCommon( time, rebootStandardint, msg_Map, Type, QFlowName, dlxQFlowName,
                card_exchangeName,  card_queueName,  card_routingKey, OneKey, TowKey,
                card_del_exchangeName, card_del_queueName, card_del_routingKey, "超停阈值");
    }




    /**
     * 重启 轮询 未订购轮询停机
     * @param cd_id
     * @param time
     * @param rebootStandardint
     * @param msg_Map
     * @param Type
     */
    public void RebootCardDisconnected(String cd_id,Integer time,Integer rebootStandardint,Map<String, Object> msg_Map,String Type){
        String QFlowName = "polling_cardCardDisconnected_queue" ;
        String dlxQFlowName = QFlowName;//死信 redis

        String card_exchangeName = "polling_cardCardDisconnected_exchange";
        String card_queueName = "polling_cardCardDisconnected_queue";
        String card_routingKey = "polling.cardCardDisconnected.routingKey";
        String OneKey = "admin.CardDisconnected.queue.One";

        String card_del_exchangeName = "polling_dlxcardCardDisconnected_exchange";
        String card_del_queueName = "polling_dlxcardCardDisconnected_queue";
        String card_del_routingKey = "polling.dlxcardCardDisconnected.routingKey";
        String TowKey = "admin.CardDisconnected.queue.Tow";

        msg_Map.put("Listener",QFlowName);
        RebootCommon( time, rebootStandardint, msg_Map, Type, QFlowName, dlxQFlowName,
                card_exchangeName,  card_queueName,  card_routingKey, OneKey, TowKey,
                card_del_exchangeName, card_del_queueName, card_del_routingKey, "未订购停机");
    }






    /**
     * 重启 轮询用量
     * @param cd_id
     * @param time
     * @param rebootStandardint
     * @param msg_Map
     * @param Type
     */
    public void RebootFlow(String cd_id,Integer time,Integer rebootStandardint,Map<String, Object> msg_Map,String Type){
        String QFlowName = "polling-Listener-CardFlow-" + cd_id;
        String dlxQFlowName = "polling-dex-Listener-CardFlow-" + cd_id;//死信 redis

        String card_exchangeName = "polling_cardCardFlow_exchange";
        String card_queueName = "polling_cardCardFlow_queue";
        String card_routingKey = "polling.cardCardFlow.routingKey";
        String OneKey = "admin.CardFlow.queue.One";

        String card_del_exchangeName = "polling_dlxcardCardFlow_exchange";
        String card_del_queueName = "polling_dlxcardCardFlow_queue";
        String card_del_routingKey = "polling.dlxcardCardFlow.routingKey";
        String TowKey = "admin.CardFlow.queue.Tow";

        RebootCommon( time, rebootStandardint, msg_Map, Type, QFlowName, dlxQFlowName,
                card_exchangeName,  card_queueName,  card_routingKey, OneKey, TowKey,
                card_del_exchangeName, card_del_queueName, card_del_routingKey, "卡用量");
    }




    /**
     * 重启 轮询 状态
     * @param cd_id
     * @param time
     * @param rebootStandardint
     * @param msg_Map
     * @param Type
     */
    public void RebootStatus(String cd_id,Integer time,Integer rebootStandardint,Map<String, Object> msg_Map,String Type){
        String QFlowName = "polling-Listener-CardStatus-" + cd_id;
        String dlxQFlowName = "polling-dxl-Listener-CardStatus-" + cd_id;//死信队列 名称

        String  card_exchangeName = "polling_cardCardStatus_exchange";
        String  card_queueName = "polling_cardCardStatus_queue" ;
        String  card_routingKey = "polling.cardCardStatus.routingKey";
        String OneKey = "admin.CardStatus.queue.One";

        String card_del_exchangeName = "polling_dlxcardCardStatus_exchange";
        String card_del_queueName = "polling_dlxcardCardStatus_queue";
        String card_del_routingKey = "polling.dlxcardCardStatus.routingKey";
        String TowKey = "admin.CardStatus.queue.Tow";

        RebootCommon( time, rebootStandardint, msg_Map, Type, QFlowName, dlxQFlowName,
                card_exchangeName,  card_queueName,  card_routingKey, OneKey, TowKey,
                card_del_exchangeName, card_del_queueName, card_del_routingKey, "卡状态");
    }




    /**
     * 重启 轮询 激活时间
     * @param cd_id
     * @param time
     * @param rebootStandardint
     * @param msg_Map
     * @param Type
     */
    public void RebootActivateDate(String cd_id,Integer time,Integer rebootStandardint,Map<String, Object> msg_Map,String Type){
        String QFlowName = "polling-Listener-ActivateDate-" + cd_id;
        String dlxQFlowName = "polling-Listener-dlxActivateDate-" + cd_id;//死信队列 名称

        String  card_exchangeName = "polling_cardActivateDate_exchange";
        String  card_queueName = "polling_cardActivateDate_queue_"+cd_id;
        String  card_routingKey = "polling.cardActivateDate.routingKey."+cd_id;
        String OneKey = "admin.CardActivateDate.queue.One";

        String card_del_exchangeName = "polling_dlxcardActivateDate_exchange";
        String card_del_queueName = "polling_dlxcardActivateDate_queue";
        String card_del_routingKey = "polling.dlxcardActivateDate.routingKey";

        String TowKey = "admin.CardActivateDate.queue.Tow";

        RebootCommon( time, rebootStandardint, msg_Map, Type, QFlowName, dlxQFlowName,
                card_exchangeName,  card_queueName,  card_routingKey, OneKey, TowKey,
                card_del_exchangeName, card_del_queueName, card_del_routingKey, "激活时间");
    }



    /**
     * 重启 轮询 公共部分
     * @param time 消息进入死信时间
     * @param rebootStandardint 待销费数量大于多少时再次开启一个消费者
     * @param msg_Map 通道查询参数
     * @param Type RabbitMq type
     * @param QFlowName Redis 记录轮询类型前缀名称
     * @param dlxQFlowName Redis 记录轮询类型前缀名称 【死信】
     * @param card_exchangeName 队列路由名称
     * @param card_queueName 队列名称
     * @param card_routingKey 队列key
     * @param OneKey 重启队列key
     * @param TowKey 重启队列key 【死信】
     * @param card_del_exchangeName 队列路由名称【死信】
     * @param card_del_queueName  队列名称【死信】
     * @param card_del_routingKey 队列key【死信】
     * @param LogoTypeName sout 输出 重启类型名字
     */
    public void RebootCommon(Integer time,Integer rebootStandardint,Map<String, Object> msg_Map,String Type,String QFlowName,String dlxQFlowName,
                             String  card_exchangeName,String  card_queueName,String  card_routingKey,String OneKey,String TowKey,
                             String card_del_exchangeName,String card_del_queueName,String card_del_routingKey,String LogoTypeName){

        Collection<String> keys =  redisCache.scanKeys(QFlowName);
        //没有检测到消费者直接创建 消费者
        if(keys!=null && keys.size()>0){
            System.out.println("重启 "+LogoTypeName+" 轮询消费者 " + QFlowName);
            //生产任务
            Send(time,card_exchangeName,OneKey,JSON.toJSONString(msg_Map), QFlowName );
        }else{
            //有消费查询  队列 待销费数量 是否大于 系统参数
            try {
                Integer Count = rabbitMQConfig.getCount(card_exchangeName,card_queueName,card_routingKey,Type);
                System.out.println(card_exchangeName+" "+card_queueName+"  "+Count+" : "+rebootStandardint);
                if(Count!=null && rebootStandardint!=null){
                    //待销费 >= 设定值时 创建 发送重启消费者指令
                    if(Count>=rebootStandardint){
                        Send(time,card_exchangeName,OneKey,JSON.toJSONString(msg_Map), QFlowName );//生产任务
                    }
                }
            }catch (Exception e){
                System.out.println(" getCount 获取 Exception  "+card_exchangeName+" "+card_queueName+" "+e.getMessage());
            }
        }
        Collection<String> delkeys =  redisCache.scanKeys(dlxQFlowName);
        //没有检测到消费者直接创建 消费者
        if(delkeys!=null && delkeys.size()>0){
            System.out.println("重启  "+LogoTypeName+"  轮询消费者 " + dlxQFlowName);
            //生产任务
            Send(time,card_exchangeName,TowKey,JSON.toJSONString(msg_Map), QFlowName );
        }else{
            //有消费查询  队列 待销费数量 是否大于 系统参数
            try {
                Integer Count = rabbitMQConfig.getCount(card_del_exchangeName,card_del_queueName,card_del_routingKey,Type);
                System.out.println(card_del_exchangeName+" "+card_del_queueName+"  "+Count+" : "+rebootStandardint);
                if(Count!=null && rebootStandardint!=null){
                    //待销费 >= 设定值时 创建 发送重启消费者指令
                    if(Count>=rebootStandardint){
                        Send(time,card_exchangeName,TowKey,JSON.toJSONString(msg_Map), QFlowName );
                    }
                }
            }catch (Exception e){
                System.out.println(" getCount 获取 Exception  "+card_del_exchangeName+" "+card_del_queueName+" "+e.getMessage());
            }
        }
    }




    public void Send(Integer time,String exchange, String routingKey, Object Obj,String quenName){
        //生产任务
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, Obj, message -> {
                // 设置消息过期时间 time 分钟 过期
                message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("重启 用量轮询消费者 失败 === " +quenName);
            System.out.println(e.getMessage());
        }

    }


}
