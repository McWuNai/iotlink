package com.yunze.quartz.task.yunze;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.utils.StringUtils;
import com.yunze.common.utils.yunze.ApiParamMap;
import com.yunze.apiCommon.utils.InternalApiRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 未执行加包订单执行 加包任务生产
 * @author root
 */
@Component("orderAddPackageProductionTask")
public class OrderAddPackageProductionTask
{

    @Resource
    private YzOrderMapper yzOrderMapper;
    @Autowired
    private RabbitMQConfig mQConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;




    //路由队列
    String addPackage_exchangeName = "polling_addPackage_card", addPackage_queueName = "p_addPackage_card", addPackage_routingKey = "p.addPackage.card",
        addPackage_del_exchangeName = "dlx_"+addPackage_exchangeName,addPackage_del_queueName = "dlx_"+addPackage_queueName, addPackage_del_routingKey = "dlx_"+addPackage_routingKey;


    // 除 生效类型为 [激活生效] 外 所有加包 增加到生产任务中去
    public void addPackageProduction(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_addPackageProduction_queue";
        String polling_routingKey = "admin.addPackageProduction.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 addPackageProduction_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }


    //  生效类型为 [激活生效] 且 已有生效时间 增加到生产任务中去
    public void activationAddPackageProductionAndActivateDate(Integer time) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_activationAddPackageProductionAndActivateDate_queue";
        String polling_routingKey = "admin.activationAddPackageProductionAndActivateDate.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("time",time);
            start_type.put("Masage", "备份 执行 activationAddPackageProductionAndActivateDate_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }




    public void AddPackag(List<Map<String,Object>> AddPackageArr,int time){
        if(AddPackageArr!=null && AddPackageArr.size()>0){
            //卡号放入路由
            for (int j = 0; j < AddPackageArr.size(); j++) {
                Map<String, Object> card = AddPackageArr.get(j);
                //生产任务
                try {
                    if (j == 0) {
                        //设置任务 路由器 名称 与队列 名称
                        mQConfig.creatExchangeQueue(addPackage_exchangeName, addPackage_queueName, addPackage_routingKey, addPackage_del_exchangeName, addPackage_del_queueName, addPackage_del_routingKey,null);
                    }
                    rabbitTemplate.convertAndSend(addPackage_exchangeName, addPackage_routingKey, JSON.toJSONString(card), message -> {
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





    //  生效类型为 [激活生效] 且 无生效时间 API更新激活时间 【不生产】让 activationAddPackageProductionAndActivateDate 去生产
    public void activationAddPackageProduction() {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_activationAddPackageProduction_queue";
        String polling_routingKey = "admin.activationAddPackageProduction.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("Masage", "备份 执行 activationAddPackageProduction_queue ");//启动类型
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("同步主表到期日期 失败 " + e.getMessage().toString());
        }
    }


    /**
     * 创建 生产者路由队列
     * @param time
     * @param size
     * @param polling_id
     * @param p_Map
     * @param type
     * @return
     */
    public boolean CreateExchange(int time, int size, String polling_id,Map<String, Object> p_Map,String type) {
        boolean bool = true;
        //卡状态  轮询
        try {
            mQConfig.creatExchangeQueue(addPackage_exchangeName, addPackage_queueName, addPackage_routingKey, null, null, null, BuiltinExchangeType.FANOUT);
            Map<String, Object> start_type = new HashMap<>();
            start_type.putAll(p_Map);
            start_type.put("type", type);
            rabbitTemplate.convertAndSend(addPackage_exchangeName, addPackage_queueName, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 time 分钟 过期
                message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            bool = false;
            System.out.println("轮询 生产激活生效包  [CreateExchange] 启动类型 失败 " + e.getMessage().toString());
        }
        return bool;
    }



}
