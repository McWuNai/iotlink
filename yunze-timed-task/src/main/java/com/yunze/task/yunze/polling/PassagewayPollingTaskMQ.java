package com.yunze.task.yunze.polling;

import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class PassagewayPollingTaskMQ {


    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;
    @Resource
    private RedisCache redisCache;



    /**
     * 获取正在执行的轮询任务进度获取  通过 redis 模糊key 查询 获取 轮询任务编号下的卡号
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_SynChannel_queue")
    public void SynChannel()
    {
        //1. 查询  一天内 正在执行的轮询任务 并且 当前执行日志数 小于 任务总数 （三天以上的可能读取数据误差或其他原因不在同步数据了）
        List<Map<String, Object>> channelArr = yzPassagewayPollingMapper.find_execute();
        if(channelArr!=null && channelArr.size()>0){

            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> obj = channelArr.get(i);
                String polling_id = obj.get("polling_id").toString();
                Integer cd_current = Integer.parseInt(obj.get("cd_current").toString());
                Integer cd_count = Integer.parseInt(obj.get("cd_count").toString());

                Collection<String> keys =  redisCache.scanKeys(polling_id);
                if(keys!=null && keys.size()>0){
                    //redis 缓存数据大于数据库当前轮询数量时 修改
                    if(keys.size()>cd_current){
                        Map<String, Object> updMap = new HashMap<String, Object>();
                        updMap.put("polling_id",polling_id);
                        updMap.put("cd_current",keys.size());
                        yzPassagewayPollingMapper.update_cd_current(updMap);
                    }
                    if(keys.size()==cd_count){
                        try {
                            redisCache.deleteObject(keys);
                            System.out.println("删除redis 记录成功！");
                        }catch (Exception e){
                            System.out.println("删除redis 记录异常");
                        }
                    }
                }
            }

        }
    }


    /**
     * 清除 今天 已经完成的 通道进度
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_cleanRedisChannel_queue")
    public void cleanRedisChannel()
    {
        //1. 查询  一天内 正在执行的轮询任务 并且 当前执行日志数 小于 任务总数 （三天以上的可能读取数据误差或其他原因不在同步数据了）
        List<Map<String, Object>> channelArr = yzPassagewayPollingMapper.find_completed();
        if(channelArr!=null && channelArr.size()>0){

            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> obj = channelArr.get(i);
                String polling_id = obj.get("polling_id").toString();

                Collection<String> keys =  redisCache.scanKeys(polling_id);
                if(keys!=null && keys.size()>0) {
                    int delCount = 0;
                    Object [] Objkey = keys.toArray();
                    for (int j = 0; j < Objkey.length; j++) {
                        try {
                            boolean del = redisCache.deleteObject(Objkey[j].toString());
                            if(del){
                                delCount+=1;
                            }
                        } catch (Exception e) {
                            System.out.println("删除redis 记录异常");
                        }
                    }
                    System.out.println("polling_id "+polling_id+" delCount "+delCount);
                }
            }

        }
    }





}
