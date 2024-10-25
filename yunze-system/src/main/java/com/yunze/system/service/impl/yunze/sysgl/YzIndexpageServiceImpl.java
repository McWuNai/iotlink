package com.yunze.system.service.impl.yunze.sysgl;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.sysgl.YzAgentAssetDetailsMapper;
import com.yunze.common.mapper.yunze.sysgl.YzAssetDetailsMapper;
import com.yunze.common.mapper.yunze.sysgl.YzIndexpageMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.system.service.yunze.sysgl.IYzIndexpageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class YzIndexpageServiceImpl implements IYzIndexpageService {

    @Resource
    private YzIndexpageMapper yzIndexpageMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;


    @Resource
    private YzAssetDetailsMapper yzAssetDetailsMapper;
    @Resource
    private YzAgentAssetDetailsMapper yzAgentAssetDetailsMapper;

    @Override
    public Map<String, Object> findToDay(Map<String, Object> map) {
        HashMap<String, Object> RMap = new HashMap<>();
        HashMap<String, Object> findToDayMap = new HashMap<>();
        String dept_id = map.get("dept_id").toString();
        String Message = "获取数据成功！";
        Map<String, Object> data  = null;
        String record_date = VeDate.getNextDay(VeDate.getStringDateShort(),"-1");
        map.put("record_date", record_date );//获取前一天的数据
        Integer id = yzIndexpageMapper.findExist(map);
        // id = null;//强制生成首页数据  测试必须生成数据 【正式打包需注释！！】
        if(id!=null){
            String rKey = "IndexpageCacheTime";
            Object  isExecute = redisCache.getCacheObject(rKey);
            String Xminute = "15";
            if(isExecute==null){
                HashMap<String, Object> configMap = new HashMap<>();
                configMap.put("config_key", "IndexpageCacheTime");
                Xminute =  yzWxByProductAgentMapper.findConfig(configMap);// 首页数据缓存时间
                redisCache.setCacheObject(rKey, Xminute, 16*60, TimeUnit.SECONDS);// 16 分钟 【缓存 系统参数】
            }else{
                Xminute = redisCache.getCacheObject(rKey).toString();
            }
            map.put("Xminute",Xminute);
            //获取修改时间是否在规定时间内
            Integer Xid = yzIndexpageMapper.findEffectiveTime(map);
            if(Xid!=null){
                findToDayMap.put("id",Xid);
            }else{
                //发送生成任务
                findToDayMap.put("id",id);
                GenerateTask(60,dept_id,record_date,id.toString());
            }
            String indexCacheKey = "index_"+dept_id+"_"+record_date;
            Object  indexCache = redisCache.getCacheObject(indexCacheKey);//查询生成用户数据是否在Redis 缓存中
            if(indexCache==null){
                data = yzIndexpageMapper.findToDay(findToDayMap);
                redisCache.setCacheObject(indexCacheKey, data, Integer.parseInt(Xminute)*60, TimeUnit.SECONDS);// Redis 缓存
            }else{
                data = redisCache.getCacheObject(indexCacheKey);
            }
        }else{
            //生成数据
            Message = "首页数据生成中请稍后前往首页查看！";
            GenerateTask(60,dept_id,record_date,null);
        }
        RMap.put("data",data);
        RMap.put("Message",Message);
        return RMap;
    }



    /**
     * 首页数据生成
     * */
    public void GenerateTask(Integer Time,String dept_id,String record_date,String id) {
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "admin_IndexDataGenerate_queue";
        String polling_routingKey = "admin.IndexDataGenerate.queue";
        String polling_exchangeName = "admin_exchange";//路由

        try {
            rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_Map = new HashMap<>();
            start_Map.put("dept_id", dept_id);//生成企业ID
            start_Map.put("record_date",record_date );//获取前一天的数据
            start_Map.put("id",id );//数据修改
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_Map), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (Time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println(" 首页数据生成 指令发送失败 " + e.getMessage());
        }
    }




    /**
     * 资产明细数据加载
     * */
    @Override
    public Map<String, Object> assetDetails(Map<String, Object> map) {
        HashMap<String, Object> RMap = new HashMap<>();
        String dept_id = map.get("dept_id").toString();
        String Message ="获取数据成功！";
        Map<String,Object> data = null;
        String record_date = VeDate.getNextDay(VeDate.getStringDateShort(),"-1");//获取前一天的数据
        map.put("record_date",record_date);
        String indexCacheKey = "indexAsset_"+dept_id+"_"+record_date;

        Map<String,Object> RdsData = redisCache.getCacheObject(indexCacheKey);
        if(RdsData==null){

            boolean internal = dept_id.equals("100")?true:false;

            if(internal){
                data = yzAssetDetailsMapper.findExist(map);
            }else{
                data = yzAgentAssetDetailsMapper.findExist(map);
            }
            if(data!=null){
                redisCache.setCacheObject(indexCacheKey, data, 8, TimeUnit.HOURS);// Redis 缓存
            }else {
                //生成数据
                Message = "资产明细数据生成中请稍后前往资产明细查看";
                Generate(60,dept_id,record_date,internal);
            }
        }else{
            data = RdsData;
        }
        RMap.put("data",data);
        RMap.put("Message",Message);
        return RMap;
    }

    /**
     * 资产明细数据生成
     * */
    public void Generate(Integer Time,String dept_id,String record_date, boolean internal){
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        String polling_queueName = "";
        String polling_routingKey = "";
        String polling_exchangeName = "admin_exchange";//路由
        if(internal){
            polling_queueName = "admin_AssetDetails_queue";
            polling_routingKey = "admin.AssetDetails.queue";
        }else{
            polling_queueName = "admin_AgentAssetDetails_queue";
            polling_routingKey = "admin.AgentAssetDetails.queue";
        }


        try {
            rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
            Map<String, Object> start_Map = new HashMap<>();
            start_Map.put("dept_id", dept_id);//生成企业ID
            start_Map.put("record_date",record_date );//获取前一天的数据
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_Map), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (Time * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("资产明细数据生成 指令发送失败 " + e.getMessage());
        }
    }


}
