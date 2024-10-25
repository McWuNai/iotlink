package com.yunze.config;

import com.yunze.common.core.redis.RedisCache;
import com.yunze.apiCommon.mapper.mysql.YzCardRouteMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @Auther: zhang feng
 * @Date: 2021/07/10/14:24
 * @Description:
 */
@Component
public class DisposableBean {


    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private RedisCache redisCache;


    @PreDestroy
    public void exit() {
        System.out.println("YzAutomationApplication 程序结束 ");

    }
}
