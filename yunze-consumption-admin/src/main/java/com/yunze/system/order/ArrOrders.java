package com.yunze.system.order;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
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
import java.util.concurrent.TimeUnit;

/**
 * 全部订单导出
 * */

@Slf4j
@Component
public class ArrOrders {


    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;



    @RabbitHandler
    @RabbitListener(queues = "admin_Exportallorders_queue")
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
            List<Map<String,Object>> userArr = (List<Map<String, Object>>) Pmap.get("userArr");
            List<Map<String,Object>> arrorders = (List<Map<String, Object>>) Pmap.get("arrorders");
            List<Map<String, Object>> stateOptions = (List<Map<String, Object>>) Pmap.get("stateOptions");
            List<Map<String, Object>> pay_type = (List<Map<String, Object>>) Pmap.get("pay_type");
            List<Map<String, Object>> customize_whether = (List<Map<String, Object>>) Pmap.get("customize_whether");
            List<Map<String, Object>> cardConnection_type = (List<Map<String, Object>>) Pmap.get("cardConnection_type");


            String newName = (String) Pmap.get("newName");
            Map<String,Object> task_map = (Map<String, Object>) Pmap.get("task_map");

            String prefix = "admin_Exportallorders_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            OutDataCard( map,User,outCardIccidArr,userArr,arrorders,stateOptions,pay_type,customize_whether, newName,task_map,cardConnection_type);

        }catch (Exception e){
            log.error(">>错误 - 全部的订单导出 消费者:{}<<", e.getMessage().toString());
        }

    }

    private void OutDataCard(Map<String, Object> map, Map<String, Object> user, List<Map<String, Object>> outCardIccidArr, List<Map<String, Object>> userArr, List<Map<String, Object>> arrorders, List<Map<String, Object>> stateOptions,
                             List<Map<String, Object>> pay_type, List<Map<String, Object>> customize_whether, String newName, Map<String, Object> task_map, List<Map<String, Object>> cardConnection_type) {

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
            WriteOutCard(outCardIccidArr,stateOptions,pay_type,userArr,arrorders,customize_whether,newName,task_map,cardConnection_type);
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

        Map<String,Object> Rmap = new HashMap<String,Object>();
        String user_id = CarMap.get("agent_id")!=null?CarMap.get("agent_id").toString():null;
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



    private String WriteOutCard(List<Map<String, Object>> outCardArr,
                 List<Map<String, Object>> stateOptions, List<Map<String, Object>> pay_type,
                 List<Map<String, Object>> deptArr, List<Map<String, Object>> arrorders, List<Map<String, Object>> customize_whether,
                 String newName, Map<String, Object> task_map, List<Map<String, Object>> cardConnection_type) {
        boolean outFlowInfo = false;
        outFlowInfo = arrorders!=null && arrorders.size()>0?true:false;

        List<Map<String,Object>> out_list = new ArrayList<Map<String,Object>>();
        //循环添加单卡数据信息
        for (int i = 0; i < outCardArr.size(); i++) {
            Map<String,Object> CarMap =  outCardArr.get(i);

            //写入 支付方式 pay_type_value
            CarMap  =  WriteDic(CarMap,pay_type,"pay_type","pay_type_value");
            //写入 支付状态 status_value
            CarMap  =  WriteDic(CarMap,stateOptions,"status","status_value");
            //写入 自定义是否 pay_type_value
            CarMap  =  WriteDic(CarMap,customize_whether,"add_package","add_package_value");
            //写入 生效类型 pay_type_value
            CarMap  =  WriteDic(CarMap,cardConnection_type,"validate_type","validate_type_value");

            //写入 所属企业信息
            CarMap  =  WriteCarUser(CarMap,deptArr);

            //写入 资费计划 信息
           if(outFlowInfo){
               CarMap  =  WriteFlow(CarMap,arrorders);
           }


            out_list.add(CarMap);
        }

        String Outcolumns[] = {"订单号","交易名称","串号","微信订单号","支付状态","交易金额","支付方式","是否已加包","创建时间","生效类型","备注","加包执行时间"};
        String keys[] = {"ord_no","ord_name","iccid","wx_ord_no","status_value","price","pay_type_value","add_package_value","create_time","validate_type_value","info","add_package_time"};

        if(outFlowInfo){
            Outcolumns = new String[]{"订单号", "交易名称", "串号", "微信订单号", "支付状态", "交易金额", "支付方式", "是否已加包", "创建时间", "生效类型", "备注", "加包执行时间","资费计划名称","资费计划规则 月 年","资费计划规格","套餐包流量","所属公司"};
            keys = new String[]{"ord_no", "ord_name", "iccid", "wx_ord_no", "status_value", "price", "pay_type_value", "add_package_value", "create_time", "validate_type_value", "info", "add_package_time","packet_name_value","packet_valid_name_value","packet_valid__value","packet_flow_value","dept_name"};
        }


        writeCSV.Write(newName,out_list,Outcolumns,null,keys);

        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";


    }


}




























































































































