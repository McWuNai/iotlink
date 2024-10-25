package com.yunze.task.yunze.sys;

import com.yunze.common.mapper.yunze.card.YzCardApiOfferinginfoMapper;
import com.yunze.common.mapper.yunze.card.YzCardApiOfferinginfolistMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class CardSynOfferinginfoTaskMQ {

    @Resource
    private YzCardApiOfferinginfolistMapper yzCardApiOfferinginfolistMapper;
    @Resource
    private YzCardApiOfferinginfoMapper yzCardApiOfferinginfoMapper;

    /**
     * 同步上游套餐
     * */
    @RabbitHandler
    @RabbitListener(queues = "admin_SynOfferinginfoMQ_queue")
    public void SynOfferinginfo(String msg) {
        try {
            List<Map<String,Object>> offeringinfolist =  yzCardApiOfferinginfolistMapper.groupOfferingId(null);
            List<Map <String,Object>> addlist = new ArrayList<>();
            List<Map <String,Object>> updlist = new ArrayList<>();
            if(offeringinfolist!=null && offeringinfolist.size()>0){
                for (int i = 0; i < offeringinfolist.size(); i++) {
                    Map <String,Object> offeringinfo = offeringinfolist.get(i);
                    Map <String,Object>  is_ex = yzCardApiOfferinginfoMapper.is_ex(offeringinfo);
                    if(is_ex!=null &&  is_ex.get("id")!=null && is_ex.get("id").toString().length()>0){
                        updlist.add(offeringinfo);
                    }else{
                        addlist.add(offeringinfo);
                    }
                }
                int saveCount = 0;
                int updCount = 0;
                if(addlist!=null && addlist.size()>0){
                    Map<String, Object> addMap = new HashMap<>();
                    addMap.put("card_arrs",addlist);
                    saveCount =  yzCardApiOfferinginfoMapper.save(addMap);
                }
                if(updlist!=null && updlist.size()>0){
                    for (int i = 0; i < updlist.size(); i++) {
                        Map<String, Object> updMap = updlist.get(i);
                        updCount +=  yzCardApiOfferinginfoMapper.upd(updMap);
                    }
                }
                System.out.println("新增套餐 {"+saveCount+"} 修改套餐 {"+updCount+"} ");
            }
        } catch (Exception e) {
            System.out.println(" cardSynOfferinginfoTask  同步上游套餐 失败 " + e.getMessage().toString());
        }
    }


}
