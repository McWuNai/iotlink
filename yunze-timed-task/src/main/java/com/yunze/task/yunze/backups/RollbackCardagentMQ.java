package com.yunze.task.yunze.backups;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RollbackCardagentMQ {

    @Resource
    private YzCardMapper yzCardMapper;


    @RabbitHandler
    @RabbitListener(queues = "admin_rollbackError_queue")
    public void rollbackError(String msg){
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);

        String Url = Pmap.get("Url").toString();
        Integer error_agent_id = Integer.parseInt(Pmap.get("error_agent_id").toString());

        Integer error_user_id = Integer.parseInt(Pmap.get("error_user_id").toString());

        List<Map<String,Object>> dataList = WriteCSV.readCSV(Url,null);

        //分组 更具 dept_id-user_id
        Map<String, List<Map<String,Object>>> collect = dataList.stream().collect(Collectors.groupingBy(scope->scope.get("dept_id").toString()+'-'+scope.get("user_id").toString()));
        //System.out.println(JSON.toJSONString(collect));
       // System.out.println("=====");
        int UpdCpunt = 0 ;
        for(Map.Entry<String, List<Map<String, Object>>> entry:collect.entrySet()){
            String Keys[] = entry.getKey().split("-");
            List<Map<String, Object>>  idArr = entry.getValue();
            String dept_id = Keys[0];
            String user_id = Keys[1];
            Map<String,Object> setMap = new HashMap<>();
            setMap.put("dept_id",dept_id);
            setMap.put("user_id",user_id);
            setMap.put("error_agent_id",error_agent_id);
            setMap.put("error_user_id",error_user_id);
            setMap.put("idArr",idArr);
            int CUpdCpunt = yzCardMapper.dividCardBk(setMap);
            UpdCpunt += CUpdCpunt;
           log.info(" 总修改 ："+UpdCpunt+" ，本次 【"+ VeDate.getStringDate()+"】 ["+entry.getKey()+"] 修改 "+CUpdCpunt+" idArr.size() "+idArr.size());
        }
        log.info(" 修改完成 "+VeDate.getStringDate()+"  UpdCpunt "+UpdCpunt);
    }

    public static void main(String[] args) {
        RollbackCardagentMQ init = new RollbackCardagentMQ();
        //init.initMQConfig();
    }

}
