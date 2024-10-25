package com.yunze.system.card;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *同步主表到期日期  消费者
 * */
@Slf4j
@Component
public class DueExpireTime {

    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    @Resource
    private YzCardMapper yzCardMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_DueExpireTime_queue")
    private void DueExpireTime(){

        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> FindArr = yzCardFlowMapper.sletume(map);
        Map<String,List<Map<String,Object>>> UpdMap = FindArr.stream().
                collect(Collectors.groupingBy((Map<String,Object> m) -> m.get("end_time").toString()));

        for(String key:UpdMap.keySet()){
            //System.out.println("key="+key);
            String due_expire_time = key ;
            due_expire_time = due_expire_time.indexOf(".0")!=-1?due_expire_time.replace(".0", ""):due_expire_time ;
            Map<String,Object> updInfo = new HashMap<>();
            updInfo.put("due_expire_time", due_expire_time);
            updInfo.put("iccids",UpdMap.get(key));
            //System.out.println(updInfo);
            yzCardMapper.updDueExpireTime(updInfo);

        }



    }


}




























