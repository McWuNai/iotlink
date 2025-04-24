package com.yunze.cn.util.redis;

import com.yunze.cn.mapper.commodity.YzWxByProductAgentMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: zhang feng
 * @Date: 2022/05/30/10:41
 * @Description:
 */
@Component
public class FindSysConfig {

    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private RedisCache redisCache;


    /**
     * 获取系统参数
     * @param key
     * @param timeout
     * @return
     */
    public  String getSysConfig(String key,int timeout){
        String value = "";
        Object  openSelCardMax = redisCache.getCacheObject(key);
        if(openSelCardMax!=null && openSelCardMax.toString().length()>0){
            value = openSelCardMax.toString();
        }else{
            Map<String, Object> fMap = new HashMap<>();
            fMap.put("config_key",key);
            value = yzWxByProductAgentMapper.findConfig(fMap);
            if(value!=null && value.length()>0){
                redisCache.setCacheObject(key,value,timeout, TimeUnit.HOURS);//1 小时 缓存
            }
        }
        return value;
    }

}
