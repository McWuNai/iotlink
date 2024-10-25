package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.utils.yunze.BulkUtil;
import com.yunze.common.utils.yunze.PlOrder;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 平台批量充值 消费 者
 */
@Slf4j
@Component
public class addOrder {


    @Resource
    private RedisCache redisCache;
    @Resource
    private PlOrder plOrder;
    @Resource
    private BulkUtil bulkUtil;


    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_OrderImportRecharge_queue")
    public void dlxCreateListener(String msg, Channel channel)  {
        try {
            execution( msg);
        } catch (Exception e) {
            log.error(">>错误 - 轮询批量加包 消费者:{}<<", e.getMessage().toString());
        }
    }




    /**
     * 平台批量充值
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_OrderImportRecharge_queue")
    public void createListener(String msg, Channel channel) {
        try {
            execution( msg);
        } catch (Exception e) {
            log.error(">>错误 - 轮询批量加包 消费者:{}<<", e.getMessage().toString());
        }
    }




    /**
     * 平台批量充值 【文本域】
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_OrderTextRecharge_queue")
    public void OrderTextRechargeListener(String msg, Channel channel) {
        try {
            TextExecution( msg);
        } catch (Exception e) {
            log.error(">>错误 - dlx_ 平台批量充值 【文本域】 消费者:{}<<", e.getMessage().toString());
        }
    }


    /**
     * 平台批量充值 【文本域】
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_OrderTextRecharge_queue")
    public void DlxOrderTextRechargeListener(String msg, Channel channel) {
        try {
            TextExecution( msg);
        } catch (Exception e) {
            log.error(">>错误 - 平台批量充值 【文本域】 消费者:{}<<", e.getMessage().toString());
        }
    }


    /**
     * 文本域充值
     * @param msg
     */
    public void TextExecution(String msg){
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> map = JSON.parseObject(msg);
        String filePath = map.get("filePath").toString();//项目根目录
        String newName = map.get("newName").toString();//输出文件名

        Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
        Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息
        Map<String,Object> OrderMap =  ( Map<String,Object>)Pmap.get("OrderMap");//添加订单生成参数
        Map<String,Object>  bulkMap = ( Map<String,Object>)map.get("bulkMap");//批量任务主表 信息
        List<String> iccidArr = (List<String>) Pmap.get("iccidArr");//文本域卡号

        bulkMap.put("state_id","3");//状态  执行中 3
        bulkMap.put("start_time", VeDate.getStringDate());//赋值 开始时间
        bulkMap.put("url", "");//url

        bulkUtil.update(bulkMap);//消费者进入变更执行状态 执行中

        String prefix = "admin_OrderTextRecharge_queue";
        //执行前判断 redis 是否存在 执行数据 存在时 不执行
        Object  isExecute = redisCache.getCacheObject(prefix+":"+ newName);
        if(isExecute==null){
            //System.out.println("SUCCESS");
            redisCache.setCacheObject(prefix+":"+ newName, msg, 30, TimeUnit.SECONDS);//30 秒缓存 避免 重复消费
            plOrder.TextAddOrder(iccidArr,filePath,"",newName,Pmap,User,OrderMap,bulkMap,"");//生成订单
        }
    }










    public void execution(String msg){
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> map = JSON.parseObject(msg);
        String filePath = map.get("filePath").toString();//项目根目录
        String ReadName = map.get("ReadName").toString();//上传新文件名
        String newName = map.get("newName").toString();//输出文件名
        String SaveUrl = "/getOriginal"+ReadName;

        Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
        Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息
        Map<String,Object> OrderMap =  ( Map<String,Object>)Pmap.get("OrderMap");//添加订单生成参数
        Map<String,Object>  bulkMap = ( Map<String,Object>)map.get("bulkMap");//批量任务主表 信息

        bulkMap.put("state_id","3");//状态  执行中 3
        bulkMap.put("start_time", VeDate.getStringDate());//赋值 开始时间
        bulkMap.put("url", SaveUrl);//url 存储上传文件 原始地址

        bulkUtil.update(bulkMap);//消费者进入变更执行状态 执行中

        String prefix = "admin_OrderImportRecharge_queue";
        //执行前判断 redis 是否存在 执行数据 存在时 不执行
        Object  isExecute = redisCache.getCacheObject(prefix+":"+ newName);
        if(isExecute==null){
            //System.out.println("SUCCESS");
            redisCache.setCacheObject(prefix+":"+ newName, msg, 30, TimeUnit.SECONDS);//30 秒缓存 避免 重复消费
            plOrder.AddOrder(filePath,ReadName,newName,Pmap,User,OrderMap,bulkMap,SaveUrl);//生成订单
        }
    }











}
