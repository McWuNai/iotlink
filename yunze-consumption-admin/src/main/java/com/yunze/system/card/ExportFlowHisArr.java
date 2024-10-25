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
 * 用量详情导出
 */

@Slf4j
@Component
public class ExportFlowHisArr {

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;


    @RabbitHandler
    @RabbitListener(queues = "admin_ExportFlowHis_queue")
    public void DbCard(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> Pmap = JSON.parseObject(msg);
            //System.out.println(Pmap);
            Map<String, Object> map = (Map<String, Object>) Pmap.get("map");
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");

            List<Map<String, Object>> outCardIccidArr = (List<Map<String, Object>>) Pmap.get("outCardIccidArr");
            List<String> HisIccid = (List<String>) Pmap.get("iccids");
            List<Map<String, Object>> HisArr = (List<Map<String, Object>>) Pmap.get("HisArr");
            boolean IsGeneralGroupbool = (boolean) Pmap.get("IsGeneralGroupbool");

            String newName = (String) Pmap.get("newName");
            Map<String, Object> task_map = (Map<String, Object>) Pmap.get("task_map");

            OutDataCard(map, User, outCardIccidArr, newName, task_map,  HisArr,HisIccid,IsGeneralGroupbool);
        } catch (Exception e) {
            log.error(">>错误 - 用量详情导出 消费者:{}<<", e.getMessage().toString());
        }
    }

    private void OutDataCard(Map<String, Object> map, Map<String, Object> user, List<Map<String, Object>> outCardIccidArr,
                             String newName, Map<String, Object> task_map, List<Map<String, Object>> hisArr, List<String> HisIccid, boolean isGeneralGroupbool) {


        yzExecutionTaskMapper.add(task_map);//添加执行 任务表
        Map<String, Object> Dept = (Map<String, Object>) user.get("dept");
        String agent_id = Dept.get("deptId").toString();
        if (agent_id.equals("100")) {
            yzCardFlowMapper.queryPackage_simple(map);
        } else {
            Map<String, Object> PackageMap = new HashMap<String, Object>();
            PackageMap.put("agent_id", agent_id);
            yzCardFlowMapper.queryAgentPackage_simple(PackageMap);
        }
        map.put("iccid_arrs", outCardIccidArr);

        try {
            WriteOutCard(outCardIccidArr, hisArr,  newName, task_map,HisIccid,isGeneralGroupbool);
        } catch (Exception e) {
            log.error("导出卡信息异常 [导出数据 Exception] " + e.getCause().toString());
        }

    }


    /**
     * 写入 分组 、备注、发货时间
     * */
    protected Map<String, Object> WriteCar(Map<String, Object> CarMap, List<Map<String, Object>> HisArr) {
        String user_id = CarMap.get("iccid") != null ? CarMap.get("iccid").toString() : null;
        boolean bool = false;
        if (HisArr != null && user_id != null) {
            for (int i = 0; i < HisArr.size(); i++) {
                Map<String, Object> UserMap = HisArr.get(i);
                String Fuser_id = UserMap.get("iccid").toString();
                if (Fuser_id.equals(user_id)) {
                    CarMap.put("deliver_date", UserMap.get("deliver_date"));
                    CarMap.put("customize_grouping", UserMap.get("customize_grouping"));
                    CarMap.put("remarks", UserMap.get("remarks"));
                    bool = true;
                    break;
                }
            }
        }
        if (!bool) {
            CarMap.put("deliver_date", "");
            CarMap.put("customize_grouping", "");
            CarMap.put("remarks", "");
        }
        return CarMap;
    }


    private String WriteOutCard(List<Map<String, Object>> outCardIccidArr, List<Map<String, Object>> hisArr,
                                String newName, Map<String, Object> task_map, List<String> HisIccid, boolean isGeneralGroupbool) {

        List<Map<String, Object>> out_list = new ArrayList<Map<String, Object>>();
        //循环添加单卡数据信息
        for (int i = 0; i < outCardIccidArr.size(); i++) {
            Map<String, Object> CarMap = outCardIccidArr.get(i);

            //写入 分组 、备注、发货时间
            CarMap = WriteCar(CarMap, hisArr);

            out_list.add(CarMap);
        }

        String Outcolumns[] = {"iccid", "年份", "月份", "日", "创建时间", "修改时间", "当天使用总量", "当月使用总量", "发货日期", "分组", "备注信息"};
        String keys[]  = {"iccid", "year", "month", "day", "create_time", "update_time", "total_show_flow_now", "total_show_flow", "deliver_date", "customize_grouping", "remarks"};

        if(isGeneralGroupbool){
             Outcolumns =  new String[]{"iccid", "年份", "月份", "日", "创建时间", "修改时间", "当天使用总量", "当月使用总量", "发货日期", "分组", "备注信息","API-当天使用","API-当月使用"};
             keys =  new String[]{"iccid", "year", "month", "day", "create_time", "update_time", "total_show_flow_now", "total_show_flow", "deliver_date", "customize_grouping", "remarks","total_flow_now","total_flow"};
        }

        writeCSV.Write(newName, out_list, Outcolumns, null, keys);
        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }

}
























