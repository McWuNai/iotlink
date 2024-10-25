package com.yunze.system.task;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务受理详情导出 成功 失败 待处理
 * */
@Slf4j
@Component
public class ExportBusiness {

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;


    @RabbitHandler
    @RabbitListener(queues = "admin_ExportaBusiness_queue")
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
            List<Map<String, Object>> state_id = (List<Map<String, Object>>) Pmap.get("state_id");
            String newName = (String) Pmap.get("newName");
            Map<String,Object> task_map = (Map<String, Object>) Pmap.get("task_map");

            String prefix = "admin_Exportallorders_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            OutDataCard( map,User,outCardIccidArr,state_id, newName,task_map);

        }catch (Exception e){
            log.error(">>错误 - 业务受理详情导出 消费者:{}<<", e.getMessage().toString());
        }

    }

    private void OutDataCard(Map<String, Object> map, Map<String, Object> user, List<Map<String, Object>> outCardIccidArr,
                             List<Map<String, Object>> state_id, String newName, Map<String, Object> task_map) {

        Map<String, Object> Dept = (Map<String, Object>) user.get("dept");
        String  agent_id = Dept.get("deptId").toString();
        if(agent_id.equals("100")){
            yzCardFlowMapper.queryPackage_simple(map);
        }else{
            Map<String,Object> PackageMap = new HashMap<String,Object>();
            PackageMap.put("agent_id",agent_id);
            yzCardFlowMapper.queryAgentPackage_simple(PackageMap);
        }
        map.put("iccid_arrs",outCardIccidArr);

        try {
            WriteOutCard(outCardIccidArr,newName,task_map,state_id);
        }catch (Exception e){
            log.error("导出卡信息异常 [导出数据 Exception] "+e.getCause().toString() );
        }
    }

    /**
     * 字典获取
     * @param CarMap 写入Map
     * @param DictDataArr 字典 list
     * @param basis 获取依据 字段 与 dictValue 判断
     * @param fieldName 返回字段名称
     * @return
     */
    public Map<String,Object> WriteDic (Map<String,Object> CarMap,List<Map<String, Object>> DictDataArr,String basis,String fieldName){

        Map<String,Object> Rmap = new HashMap<String,Object>();
        String status_id = CarMap.get(basis)!=null?CarMap.get(basis).toString():null;
        boolean bool = false;
        if(status_id!=null){
            for (int i = 0; i < DictDataArr.size(); i++) {
                Map<String, Object>  dictData =  DictDataArr.get(i);
                String dictValue = dictData.get("dictValue").toString();
                if(dictValue.equals(status_id)){
                    CarMap.put(fieldName,dictData.get("dictLabel"));
                    bool = true;
                    break;
                }
            }
        }
        //字段 默认值
        if(!bool){
            CarMap.put(fieldName,"");
        }
        return CarMap;
    }

    private String WriteOutCard(List<Map<String, Object>> outCardArr, String newName, Map<String, Object>
            task_map, List<Map<String, Object>> state_id) {

        List<Map<String,Object>> out_list = new ArrayList<Map<String,Object>>();
        //循环添加单数据信息
        for (int i = 0; i < outCardArr.size(); i++) {
            Map<String,Object> CarMap =  outCardArr.get(i);

            //写入 状态
            CarMap  =  WriteDic(CarMap,state_id,"state_id","state_id_value");


            out_list.add(CarMap);
        }

        String Outcolumns[] = {"号码","创建时间","修改时间","结束时间","描述","状态"};
        String keys[] = {"card_number","create_time","update_time","end_time","mydescribe","state_id_value"};


        writeCSV.Write(newName,out_list,Outcolumns,null,keys);
        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";

    }


}
