package com.yunze.config;

import com.yunze.common.core.redis.RedisCache;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    public void exit(){
        //防止程序意外终止后 redis 还有记录正在监听 记录 下次程序启动不在创建 消费者
        System.out.println("cardStop 程序结束 执行删除redis Listener 记录key ");
        //获取现在数据库中 开启轮询 状态正常的通道进行监听
        List<String> IDArr =  yzCardRouteMapper.findIDArr();
        if(IDArr!=null && IDArr.size()>0) {
            //获取redis 中 记录的 已经在监听中的通道名称 避免重复监听
            for (int i = 0; i < IDArr.size(); i++) {
                String cd_id = IDArr.get(i);
                redisCache.redisTemplate.delete("polling_cardCardStop_queue_"+cd_id);
                redisCache.redisTemplate.delete("dlx_polling_cardCardStop_queue_"+cd_id);
            }
        }

    }
}
