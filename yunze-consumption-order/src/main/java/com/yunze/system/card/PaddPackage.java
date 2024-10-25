package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.card.AddPacket;
import com.yunze.common.core.redis.RedisCache;
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
 * 轮询批量加包 消费 者
 */
@Slf4j
@Component
public class PaddPackage {

    @Resource
    private RedisCache redisCache;
    @Resource
    private AddPacket addPacket;



    /**
     * 轮询批量加包
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "p_addPackage_card")
    public void createListener(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            addExecution(msg);
        } catch (Exception e) {
            log.error(">>错误 - 轮询批量加包 消费者:{}<<", e.getMessage().toString());
        }
    }


    /**
     * 轮询批量加包
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_p_addPackage_card")
    public void dlxCreateListener(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            addExecution(msg);
        } catch (Exception e) {
            log.error(">>错误 - 轮询批量加包 消费者:{}<<", e.getMessage().toString());
        }
    }


        private void addExecution(String msg){
            Map<String,Object> map = JSON.parseObject(msg);
            //String oid = map.get("id").toString();//订单 id
            String ord_no = map.get("ord_no").toString();//订单 号
            String iccid = map.get("iccid").toString();//iccid
            //String cid = map.get("Bid").toString();//card id
            String validate_type = map.get("validate_type").toString();//生效类型
            String create_time = map.get("create_time").toString();//订单创建时间
            String activate_date = map.get("activate_date")!=null&&map.get("activate_date").toString().trim().length()>6?map.get("activate_date").toString():null;//激活时间

            Map<String,Object> add_parameter = JSON.parseObject(map.get("add_parameter").toString());//加包参数

            String prefix = "p_addPackage_card";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ iccid+":"+ord_no);
            if(isExecute==null){
                //System.out.println("SUCCESS");
                redisCache.setCacheObject(prefix+":"+ iccid+":"+ord_no, msg, 10, TimeUnit.SECONDS);//10 秒缓存 避免 重复消费
                addPacket.addFlow(ord_no,iccid,add_parameter,validate_type,create_time,activate_date);//执行导入
            }
        }



}
