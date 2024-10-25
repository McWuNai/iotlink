package com.yunze.task.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzAgentPacketMapper;
import com.yunze.common.mapper.yunze.YzCardPacketMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.CommonlyUsed;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 定时任务 资费计划 卡数量同步
 */
@Slf4j
@Component
public class CardPacketCountMQ {

    @Resource
    private YzCardPacketMapper yzCardPacketMapper;
    @Resource
    private YzAgentPacketMapper yzAgentPacketMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitHandler
    @RabbitListener(queues = "admin_cardPacketCount_queue")
    public void SynPacketCount(String msg) {
        Execution();
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_cardPacketCount_queue")
    public void DlxSynPacketCount(String msg) {
        Execution();
    }


    public void Execution(){
        List<Map<String,Object>> pList = yzCardPacketMapper.findPacketCount(null);
        if(pList!=null){
            List<Map<String,Object>> sysPList = new ArrayList<>();
            List<Map<String,Object>> undividedList = new ArrayList<>();//未划分资费

            Map<String,Integer> cMap = new HashMap<>();

            List<String> exPid = new ArrayList<>();
            for (int i = 0; i < pList.size(); i++) {
                Map<String,Object> packet = pList.get(i);
                Integer count = packet.get("count")!=null?Integer.parseInt(packet.get("count").toString()):0;
                String dept_id = packet.get("dept_id").toString();
                String packet_id = packet.get("packet_id").toString();
                String ord_no = packet.get("ord_no").toString();
                if(exPid.size()>0){
                    if(CommonlyUsed.Val_Is_Arr(exPid,packet_id)){
                        Integer countSum =  cMap.get(packet_id);
                        countSum += count;
                        cMap.put(packet_id,count);
                        for (int j = 0; j < sysPList.size(); j++) {
                            Map<String,Object> sysPacket = sysPList.get(j);
                            String sPacket_id = sysPacket.get("packet_id").toString();
                            if(sPacket_id.equals(packet_id)){//做count 加法
                                sysPacket.put("count",countSum);
                                sysPList.set(j,sysPacket);
                                break;
                            }
                        }
                    }else{
                        exPid.add(packet_id);
                        sysPList.add(packet);
                        cMap.put(packet_id,count);
                    }
                }else {
                    exPid.add(packet_id);
                    sysPList.add(packet);
                    cMap.put(packet_id,count);
                }


                // agent 表修改
                if(!dept_id.equals("100")){
                    Map<String,Object> updMap = new HashMap<>();
                    updMap.put("card_count",count);
                    updMap.put("packet_id",packet_id);
                    updMap.put("dept_id",dept_id);
                    updMap.put("ord_no",ord_no);
                    int upd = yzAgentPacketMapper.updCardCount(updMap);
                    if(upd==0){
                        undividedList.add(updMap);
                        log.error(">> 修改企业资费表失败，核对是否已划分资费 {} <<", JSON.toJSONString(updMap));
                    }
                }
            }


            if(sysPList.size()>0){
                for (int i = 0; i < sysPList.size(); i++) {
                    Map<String,Object> sysPacket = sysPList.get(i);
                    // card 表修改
                    Map<String,Object> updMap = new HashMap<>();
                    updMap.put("card_count",sysPacket.get("count"));
                    updMap.put("packet_id",sysPacket.get("packet_id"));
                    updMap.put("dept_id",sysPacket.get("dept_id"));
                    yzAgentPacketMapper.updCardCount(updMap);
                }
            }

            //未划分资费的数据存储
            if(undividedList.size()>0){
                String newName = UUID.randomUUID().toString().replace("-", "") + "_undividedOut";

                String create_by = " [ 总平台 ] - " + " [ 系统操作 ] ";
                String SaveUrl = "/getcsv/" + newName + ".csv";

                Map<String, Object> task_map = new HashMap<String, Object>();
                task_map.put("auth", create_by);
                task_map.put("task_name","未划分资费组企业");
                task_map.put("url", SaveUrl);
                task_map.put("agent_id", "100");
                task_map.put("type", "39");
                yzExecutionTaskMapper.add(task_map);//添加执行 任务表
                try {
                    String Outcolumns[] = {"资费计划ID","企业ID","卡号数量","订单号"};
                    String keys[] = {"packet_id","dept_id","card_count","ord_no"};
                    writeCSV.Write(newName,undividedList,Outcolumns,null,keys);
                }catch (Exception e){

                }
                yzExecutionTaskMapper.set_end_time(task_map);//任务结束
            }






        }
    }

}
