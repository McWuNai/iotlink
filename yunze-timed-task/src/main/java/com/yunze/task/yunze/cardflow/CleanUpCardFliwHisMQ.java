package com.yunze.task.yunze.cardflow;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YrootlowHisMapper;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CleanUpCardFliwHisMQ {

    @Resource
    private YrootlowHisMapper yrootlowHisMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_CleanUp_queue")
    public void CleanUp(String msg){
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        String day =  Pmap.get("day").toString();

        if(day!=null && day.length()>0){
            Integer datint = Integer.parseInt(day);
            if(datint>=31){
                Map<String, Object> map = new HashMap<>();
                String yyyy_MM_dd_HH_mm_ss = VeDate.getStringDate();
                String create_time = VeDate.getNextDay(yyyy_MM_dd_HH_mm_ss.split(" ")[0],"-"+day);
                map.put("create_time", create_time);
                List<String> delArr = yrootlowHisMapper.getOneThousand(map);
                if(delArr!=null){
                    del(map,delArr);
                }
            }else{
                System.out.println("day "+day+" <31 不允许进行删除！记录" );
            }
        }
    }
    public void del(Map<String, Object> map,List<String> delArr){
        Map<String, Object> delMap = new HashMap<>();
        delMap.put("idArr", delArr);
        int delCount = yrootlowHisMapper.delFlow(delMap);
        System.out.println("已删除 delArr[0] "+delArr.get(0)+" 至  delArr.get(delArr.size()-1) "+delArr.get(delArr.size()-1)+"  "+delCount+"条");
        List<String> delArr_1  = yrootlowHisMapper.getOneThousand(map);
        if(delArr_1!=null){
            del(map,delArr_1);
        }
    }


}
