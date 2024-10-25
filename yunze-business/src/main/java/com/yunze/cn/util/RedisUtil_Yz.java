package com.yunze.cn.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil_Yz {

    @Resource
    public  RedisTemplate<String,Object> redisTemplate;




    /**
     * auther:
     * return:
     * 描述： 删除指定的key
     * 时间： 2021/12/30 15:54
     */
    public boolean delKey(String key){
        return redisTemplate.delete(key);
    }

    /**
     * auther:
     * return:
     * 描述： 设置key
     * 时间： 2021/12/25 10:02
     */
    public void setKey(String key,Object value){
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * auther:
     * return:
     * 描述： 查询当前key是否已经失效 true:失效
     * 时间： 2021/12/18 15:55
     */
    public boolean ttlKey(String key){
        return isExpire(key);
    }

    /**
     * auther:
     * return:
     * 描述： 查询key是否过期
     * 时间： 2021/12/18 15:59
     */
    public boolean isExpire(String key){
        long t=redisTemplate.opsForValue().getOperations()
                .getExpire(key);
        if(t>1){
            return false;
        }else{
            return true;
        }

    }

    /**
     * auther:
     * return:
     * 描述： 查询key是否存在
     * 时间： 2021/12/18 15:58
     */
    public boolean existsKey(String key){
        boolean f=redisTemplate.hasKey(key);
        if(f){
            return true;
        }else{
            return false;
        }

    }

    /**
     * auther:
     * return: 添加过期时间 秒
     * 描述：
     * 时间： 2021/11/23 13:42
     */
    public void setExpire(String key,Object
                          value,long time) {
        redisTemplate.opsForValue().set(key,value,time,TimeUnit.SECONDS);
    }
    /**
     * auther:
     * return:
     * 描述： 获取过期时间
     * 时间： 2021/11/23 13:43
     */
    public long getExpire(String key) {
        return redisTemplate.opsForValue().getOperations()
                .getExpire(key);
    }


    /**
     * map 设置 过期时间
     * @param key
     * @param map
     * @param time
     */
    public void setExpireMap(String key,Map<String,Object> map,long time) {
            redisTemplate.opsForHash().putAll(key,map);
            redisTemplate.expire(key, time, TimeUnit.MINUTES);
    }







    public  Object get(Object o){
        return redisTemplate.opsForValue().get(o);
    }

    public void setAccessToken(Object o){
        redisTemplate.opsForValue().set("accessToken",o,2,TimeUnit.HOURS);
    }

    public void set(String key,Object o){

        redisTemplate.opsForValue().set(key,o,4,TimeUnit.HOURS);
    }



    public static void main(String[] args) {
       RedisTemplate redisTemplate=new RedisTemplate();
       //redisTemplate.expire("",600,TimeUnit.SECONDS);

        System.out.println(redisTemplate.opsForValue().getOperations()
                .getExpire(""));

      // long t=redisTemplate.getExpire("");
        // System.out.println(t);
    }
}
