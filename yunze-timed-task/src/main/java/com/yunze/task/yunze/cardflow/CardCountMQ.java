package com.yunze.task.yunze.cardflow;

import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CardCountMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;

    @Transactional
    @RabbitHandler
    @RabbitListener(queues = "admin_synchronization_queue")
    public void synchronization() {

        Map<String, Object> Rmap = new HashMap<>();

        List<Map<String, Object>> list = yzCardRouteMapper.countId(Rmap);//查询 通道卡总数
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> obj = list.get(i);
                if (obj.get("channel_id") != null) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.putAll(obj);
                    objectMap.put("count",null);
                    Integer integer = yzCardRouteMapper.upCount(objectMap); //进行修改 通道表 卡总数
                    log.info(integer + "清空总数");
                }
                if(obj.get("channel_id") != null){
                    Integer integer = yzCardRouteMapper.upCount(obj); //进行修改 通道表 卡总数
                    log.info(integer + "数据操作成功");
                }
            }
        }
    }
}
