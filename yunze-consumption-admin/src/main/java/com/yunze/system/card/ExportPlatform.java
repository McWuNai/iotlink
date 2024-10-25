package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
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

/**
 * 平台资费 导出
 * */
@Slf4j
@Component
public class ExportPlatform {

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_ExportPlatform_queue")
    public void DbCard(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            //System.out.println(Pmap);
            Map<String, Object> map = (Map<String, Object>) Pmap.get("map");
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");
            List<Map<String, Object>> outCardIccidArr = (List<Map<String, Object>>) Pmap.get("outCardIccidArr");
            String newName = (String) Pmap.get("newName");
            Map<String,Object> task_map = (Map<String, Object>) Pmap.get("task_map");

            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            OutDataCard( map,User,outCardIccidArr,newName,task_map);

        }catch (Exception e){
            log.error(">>错误 - 平台资费导出 消费者:{}<<", e.getMessage().toString());
        }

    }

    private void OutDataCard(Map<String, Object> map, Map<String, Object> user, List<Map<String, Object>> outCardIccidArr, String newName, Map<String, Object> task_map) {

        map.put("iccid_arrs",outCardIccidArr);
        try {
            WriteOutCard(outCardIccidArr,newName,task_map);
        }catch (Exception e){
            log.error("导出平台资费异常 [导出数据 Exception] "+e.getCause().toString() );
        }
    }

    private String WriteOutCard(List<Map<String, Object>> outCardIccidArr, String newName, Map<String, Object> task_map) {

        String Outcolumns[] = {"资费组编号","资费组名称","资费组别称","所属公司","所属人","创建时间","卡总数"};
        String keys[] = {"package_id","package_name","package_agentname","dept_name","nick_name","create_time","card_count"};


        writeCSV.Write(newName,outCardIccidArr,Outcolumns,null,keys);
        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }

}
