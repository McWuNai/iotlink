package com.yunze.task.yunze.polling;

import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorGroupMapper;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorMapper;
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
public class SynPollingErrorGroupTask {

    @Resource
    private YzCardPollingErrorGroupMapper yzCardPollingErrorGroupMapper;
    @Resource
    private YzCardPollingErrorMapper yzCardPollingErrorMapper;

    /**
     * 同步上游套餐String msg
     * */
    @RabbitHandler
    @RabbitListener(queues = "admin_SynOfferinginfo_queue")
    public void SynOfferinginfo() {
        try {
            List<Map<String,Object>> erroeList =  yzCardPollingErrorMapper.findGroup(null);
            List<Map <String,Object>> addlist = new ArrayList<>();
            List<Map <String,Object>> updlist = new ArrayList<>();
            if(erroeList!=null && erroeList.size()>0){
                for (int i = 0; i < erroeList.size(); i++) {
                    Map <String,Object> erroeMap = erroeList.get(i);
                    String  is_ex = yzCardPollingErrorGroupMapper.findEx(erroeMap);
                    if(is_ex!=null &&  is_ex!=null && is_ex.toString().length()>0){
                        erroeMap.put("id",is_ex);
                        updlist.add(erroeMap);
                    }else{
                        addlist.add(erroeMap);
                    }
                }
                int saveCount = 0;
                int updCount = 0;
                if(addlist!=null && addlist.size()>0){
                    Map<String, Object> addMap = new HashMap<>();
                    addMap.put("msg_arrs",addlist);
                    saveCount =  yzCardPollingErrorGroupMapper.save(addMap);
                }
                if(updlist!=null && updlist.size()>0){
                    for (int i = 0; i < updlist.size(); i++) {
                        Map<String, Object> updMap = updlist.get(i);
                        updCount +=  yzCardPollingErrorGroupMapper.upd(updMap);
                    }
                }
                System.out.println("新增套餐 {"+saveCount+"} 修改套餐 {"+updCount+"} ");
            }
        } catch (Exception e) {
            System.out.println(" cardSynOfferinginfoTask  同步上游套餐 失败 " + e.getMessage().toString());
        }
    }



}
