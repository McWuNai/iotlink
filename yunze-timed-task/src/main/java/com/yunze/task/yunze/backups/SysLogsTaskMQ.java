package com.yunze.task.yunze.backups;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzSysLogsMapper;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 清空日志 yz_sys_logs 消费 者
 */
@Slf4j
@Component
public class SysLogsTaskMQ {

    @Resource
    private RedisCache redisCache;
    @Resource
    private YzSysLogsMapper yzSysLogsMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_BackupsyzSysLogs_queue")
    public void BkSysLogs(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> Bmap = JSON.parseObject(msg);
            //System.out.println(Bmap);
            String create_by = Bmap.get("Masage").toString();//上传新文件名
            String prefix = "admin_BackupsyzSysLogs_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + create_by);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + create_by, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                int deleteById = yzSysLogsMapper.deleteById(null);
            }
        } catch (Exception e) {
            log.error(">>错误 - 连接管理导出 消费者:{}<<", e.getMessage().toString());
        }
    }




    }



