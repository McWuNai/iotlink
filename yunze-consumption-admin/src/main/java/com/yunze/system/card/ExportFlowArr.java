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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订购资费导出
 * */

@Slf4j
@Component
public class ExportFlowArr {


    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;



    @RabbitHandler
    @RabbitListener(queues = "admin_ExportFlow_queue")
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
            List<Map<String,Object>> deptArr = (List<Map<String, Object>>) Pmap.get("deptArr");
            List<Map<String,Object>> arrorders = (List<Map<String, Object>>) Pmap.get("arrorders");
            List<Map<String,Object>> Cardstatus = (List<Map<String, Object>>) Pmap.get("Cardstatus");
            List<Map<String,Object>> packet_type = (List<Map<String, Object>>) Pmap.get("packet_type");
            List<Map<String,Object>> takeEffect_type = (List<Map<String, Object>>) Pmap.get("takeEffect_type");
            List<Map<String,Object>> simpleArr = (List<Map<String, Object>>) Pmap.get("simpleArr");
            List<Map<String,Object>> PacketArr = (List<Map<String, Object>>) Pmap.get("PacketArr");

            String newName = (String) Pmap.get("newName");
            Map<String,Object> task_map = (Map<String, Object>) Pmap.get("task_map");

            OutDataCard( map,User,outCardIccidArr,deptArr,arrorders,Cardstatus,packet_type,takeEffect_type, newName,task_map,simpleArr,PacketArr);
        }catch (Exception e){
            log.error(">>错误 - 订购资费导出 消费者:{}<<", e.getMessage().toString());
        }
    }

    private void OutDataCard(Map<String, Object> map, Map<String, Object> user, List<Map<String, Object>> outCardIccidArr,
                             List<Map<String, Object>> userArr, List<Map<String, Object>> arrorders, List<Map<String, Object>> cardstatus,
                             List<Map<String, Object>> packet_type, List<Map<String, Object>> takeEffect_type, String newName, Map<String,
            Object> task_map,List<Map<String, Object>> simpleArr,List<Map<String, Object>> PacketArr) {


        yzExecutionTaskMapper.add(task_map);//添加执行 任务表
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
            WriteOutCard(outCardIccidArr,userArr,arrorders,cardstatus,packet_type,takeEffect_type,newName,task_map,outCardIccidArr,simpleArr,PacketArr);
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

    /**
     * 写入 所属企业信息
     * @param CarMap
     * @param deptArr
     * @return
     */
    protected Map<String,Object> WriteCarUser (Map<String,Object> CarMap,List<Map<String,Object>> deptArr){

        String user_id = CarMap.get("dept_id")!=null?CarMap.get("dept_id").toString():null;
        boolean bool = false;
        if(deptArr!=null && user_id !=null){
            for (int i = 0; i < deptArr.size(); i++) {
                Map<String,Object> UserMap =  deptArr.get(i);
                String Fuser_id = UserMap.get("dept_id").toString();
                if(Fuser_id.equals(user_id)){
                    CarMap.put("dept_name",UserMap.get("dept_name"));
                    bool = true;
                    break;
                }
            }
        }
        if(!bool){
            CarMap.put("dept_name","");
        }
        return CarMap;
    }

    /**
     * 写入 资费计划信息
     * @param CarMap
     * @param PacketArr
     * @return
     */
    protected Map<String,Object> WriteCarPacket (Map<String,Object> CarMap,List<Map<String,Object>> PacketArr){

        String user_id = CarMap.get("dictValue")!=null?CarMap.get("dictValue").toString():null;
        boolean bool = false;
        if(PacketArr!=null && user_id !=null){
            for (int i = 0; i < PacketArr.size(); i++) {
                Map<String,Object> UserMap =  PacketArr.get(i);
                String Fuser_id = UserMap.get("dictValue").toString();
                if(Fuser_id.equals(user_id)){
                    CarMap.put("dictLabel",UserMap.get("dictLabel"));
                    bool = true;
                    break;
                }
            }
        }
        if(!bool){
            CarMap.put("dictLabel","");
        }
        return CarMap;
    }

    /**
     * 写入 资费组
     * @param CarMap
     * @param simpleArr
     * @return
     */
    protected Map<String,Object> WriteCarsimple (Map<String,Object> CarMap,List<Map<String,Object>> simpleArr){

        String user_id = CarMap.get("package_id")!=null?CarMap.get("package_id").toString():null;
        boolean bool = false;
        if(simpleArr!=null && user_id !=null){
            for (int i = 0; i < simpleArr.size(); i++) {
                Map<String,Object> UserMap =  simpleArr.get(i);
                String Fuser_id = UserMap.get("package_id").toString();
                if(Fuser_id.equals(user_id)){
                    CarMap.put("package_agentname",UserMap.get("package_agentname"));
                    bool = true;
                    break;
                }
            }
        }
        if(!bool){
            CarMap.put("package_agentname","");
        }
        return CarMap;
    }

    /**
     * 写入 年月日
     * @param CarMap
     * @param deptArr
     * @return
     */
    protected Map<String,Object> WriteFlow (Map<String,Object> CarMap,List<Map<String,Object>> deptArr){

        String user_id = CarMap.get("packet_id")!=null?CarMap.get("packet_id").toString():null;
        boolean bool = false;
        if(deptArr!=null && user_id !=null){
            for (int i = 0; i < deptArr.size(); i++) {
                Map<String,Object> UserMap =  deptArr.get(i);
                String packet_id = UserMap.get("packet_id").toString();
                if(packet_id.equals(user_id)){
                    CarMap.put("packet_name_value",UserMap.get("packet_wx_name"));
                    CarMap.put("packet_valid_name_value",UserMap.get("packet_valid_name"));
                    CarMap.put("packet_valid__value",UserMap.get("packet_valid_time"));
                    CarMap.put("packet_flow_value",UserMap.get("packet_flow"));

                    bool = true;
                    break;
                }
            }
        }
        if(!bool){
            CarMap.put("packet_name_value","");
            CarMap.put("packet_valid_name_value","");
            CarMap.put("packet_valid__value","");
            CarMap.put("packet_flow_value","");
        }
        return CarMap;
    }

    private String WriteOutCard(List<Map<String, Object>> outCardIccidArr, List<Map<String, Object>> deptArr,
                                List<Map<String, Object>> arrorders, List<Map<String, Object>> cardstatus,
                                List<Map<String, Object>> packet_type, List<Map<String, Object>> takeEffect_type,
                                String newName, Map<String, Object> task_map, List<Map<String, Object>> outCardArr,List<Map<String, Object>> simpleArr,List<Map<String, Object>> PacketArr
                               ) {

        boolean outFlowInfo = false;
        outFlowInfo = arrorders!=null && arrorders.size()>0?true:false;

        List<Map<String,Object>> out_list = new ArrayList<Map<String,Object>>();
        //循环添加单卡数据信息
        for (int i = 0; i < outCardArr.size(); i++) {
            Map<String,Object> CarMap =  outCardArr.get(i);

            //写入 卡状态
            CarMap  =  WriteDic(CarMap,cardstatus,"status","status_value");
            //写入 资费计划类别
            CarMap  =  WriteDic(CarMap,packet_type,"packet_type","packet_type_value");
            //写入 生效类别
            CarMap  =  WriteDic(CarMap,takeEffect_type,"validate_type","validate_type_value");

            //写入 所属企业信息
            CarMap  =  WriteCarUser(CarMap,deptArr);
            //写入 资费组信息
            CarMap  =  WriteCarsimple(CarMap,simpleArr);
            //写入 资费计划信息
            CarMap  =  WriteCarPacket(CarMap,PacketArr);
            //写入 资费计划 信息
            if(outFlowInfo){
                CarMap  =  WriteFlow(CarMap,arrorders);
            }
            out_list.add(CarMap);
        }


        String Outcolumns[] = {"id","资费组","资费计划","订单号","流量","生效时间","到期时间","创建时间","状态","资费计划类别","使用流量","对应串号","生效类别","更新累计包时间","天包记录已用流量"};
        String keys[] = {"id","package_agentname","packet_name_value","ord_no","true_flow","start_time","end_time","create_time","status_value","packet_type_value","use_flow","iccid","validate_type_value","validate_time","use_so_flow"};

        if(outFlowInfo){
            Outcolumns = new String[]{"id","资费组","资费计划","订单号","流量","生效时间","到期时间","创建时间","状态","资费计划类别","使用流量","对应串号","生效类别","更新累计包时间","天包记录已用流量","资费计划规则 月 年","资费计划规格","套餐包流量"};
            keys = new String[]{"id","package_agentname","packet_name_value","ord_no","true_flow","start_time","end_time","create_time","status_value","packet_type_value","use_flow","iccid","validate_type_value","validate_time","use_so_flow","packet_valid_name_value","packet_valid__value","packet_flow_value"};
        }

        writeCSV.Write(newName,out_list,Outcolumns,null,keys);
        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";

    }


}




























































































































