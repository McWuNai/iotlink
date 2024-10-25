package com.yunze.system.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.config.MyDictionary;
import com.yunze.common.mapper.yunze.YzCardInfoChangeMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * 勾选操作 停复机，断开网操作
 */
@Slf4j
@Component
public class PublicMethod {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardInfoChangeMapper yzCardInfoChangeMapper;
    @Resource
    private MyDictionary myDictionary;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;

    private String Outcolumns[] = {"iccid", "返回消息", "执行描述", "执行人", "执行结果"};
    private String keys[] = {"iccid", "Message", "describe", "agentName", "result"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 批量停机
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_Stopped_queue")
    public void StoppedArr(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");//参数
            execution(Pmap, "Stop");//批量 停机
        } catch (Exception e) {
            log.error(">>错误 - 批量 【停机】 消费者:{}<<", e.getMessage());
        }
    }
    /**
     * 批量复机
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_Machine_queue")
    public void MachineArr(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");//参数
            execution(Pmap, "Machine");//批量 停机
        } catch (Exception e) {
            log.error(">>错误 - 批量 【复机】 消费者:{}<<", e.getMessage());
        }
    }
    /**
     * 批量断网
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_DisconnectNetwork_queue")
    public void DisconnectNetworkArr(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");//参数
            execution(Pmap, "DisconnectNetwork");//批量 停机
        } catch (Exception e) {
            log.error(">>错误 - 批量 【断网】 消费者:{}<<", e.getMessage());
        }
    }
    /**
     * 批量开网
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_OpenNetwork_queue")
    public void OpenNetworkArr(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");//参数
            execution(Pmap, "OpenNetwork");//批量 停机
        } catch (Exception e) {
            log.error(">>错误 - 批量 【开网】 消费者:{}<<", e.getMessage());
        }
    }


    /**
     * 批量执行 公共函数
     * @param Pmap 请求参数
     * @param OptionType 操作类型  Stop 》 停机
     */
    private void execution(Map<String, Object> Pmap, String OptionType) {
        String Message = "";
        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = (List<Map<String, Object>>) Pmap.get("iccids");
        //执行API 调用 参数 Map
        Map<String, Object> ApiPmap = new HashMap<>();
        String task_nameApend = "";//执行日志类型 拼接
        String newNameApend = "";//执行日志 文件名 后缀
        String TaskType = "";//执行日志 类型
        String cafterward = "";//变更后状态
        String ctype = "";
        String dict_type = "";
        String key = "";
        // 批量 停机
        switch (OptionType) {
            case "Stop":
                task_nameApend = "批量 停机";
                newNameApend = "_Stopped";
                TaskType = "14";
                ApiPmap.put("Is_Stop", "off");//上游 API 执行 必要参数
                ApiPmap.put("status_id", "5");// Cupdate 修改队列 修改 card_info 表 字段 名 和 字段值 如 修改为 已停机 status_id = 5
                cafterward = "已停机";
                key = "Sel_status_ShowId";// 获取 变更状态 值
                dict_type = "yunze_card_status_ShowId";//卡状态描述 字典表
                ctype = "1";//变更类型为  生命周期
                break;
            // 批量 复机
            case "Machine":
                task_nameApend = "批量 复机";
                newNameApend = "_Machine";
                TaskType = "15"; //执行日志类别
                ApiPmap.put("Is_Stop", "on");//复机
                ApiPmap.put("status_id", "4");
                cafterward = "已复机";
                key = "Sel_status_ShowId"; //卡状态
                dict_type = "yunze_card_status_ShowId";//卡状态描述 字典表
                ctype = "1";//变更类型为  生命周期
                break;
            // 批量 断网
            case "DisconnectNetwork":
                task_nameApend = "批量 断网";
                newNameApend = "_DisconnectNetwork";
                TaskType = "16"; //执行日志类别
                ApiPmap.put("Is_Break", "1");//断网
                ApiPmap.put("status_id", "5");
                cafterward = "已断网";
                key = "connection_status"; //卡状态
                dict_type = "yz_cardConnection_type";//卡状态描述 字典表
                ctype = "2";//变更类型为  生命周期
                break;
            // 批量 开网
            case "OpenNetwork":
                task_nameApend = "批量 开网";
                newNameApend = "_OpenNetwork";
                TaskType = "17"; //执行日志类别
                ApiPmap.put("Is_Break", "0");//开网
                ApiPmap.put("status_id", "4");
                cafterward = "已开网";
                key = "connection_status"; //卡状态
                dict_type = "yz_cardConnection_type";//卡状态描述 字典表
                ctype = "2";//变更类型为  生命周期
                break;
        }


        Map<String, Object> User = (Map<String, Object>) Pmap.get("User");
        Map<String, String> Dept = (Map<String, String>) User.get("dept");
        String create_by = " [ " + Dept.get("deptName") + " ] - " + " [ " + User.get("userName") + " ] ";
        String newName = UUID.randomUUID().toString().replace("-", "") + newNameApend;//这里 _Stopped
        String task_name = "-批量 操作 [" + task_nameApend + "] ";
        String SaveUrl = "/getcsv/" + newName + ".csv";
        SaveUrl = "/getcsv/" + newName + ".csv";
        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth", create_by);
        task_map.put("task_name", task_name);
        task_map.put("url", SaveUrl);
        task_map.put("agent_id", User.get("deptId"));
        task_map.put("type", TaskType); //对应 字典表的 执行日志类别



        yzExecutionTaskMapper.add(task_map);//添加执行 任务表
        map.put("card_arrs", list);//更新 list
        map.put("type", "3");

        if (list != null && list.size() > 0) {
            //筛选出未划分通道的
            map.put("channel_idType", "notNull");
            List<String> Channel_iccidarr = yzCardMapper.isExistence(map); //查询单卡信息
            if (Channel_iccidarr != null && Channel_iccidarr.size() > 0) {
                if (!(Channel_iccidarr.size() == list.size())) {
                    // 获取 数组去重数据 和 重复值
                    Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, Channel_iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                    list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                    List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                    //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                    if (Out_list_Different.size() > 0) {
                        Map<String, Object> defOutcolumns = new HashMap<>();
                        defOutcolumns.put("describe", "未划分通道，取消操作！");
                        defOutcolumns.put("agentName", create_by);
                        defOutcolumns.put("result", "查询失败");
                        writeCSV.OutCSVObj(Out_list_Different, newName, Outcolumns, keys, defOutcolumns, OutSize);
                    }
                }
                map.put("card_arrs", list);//更新 list

                List<Map<String, Object>> find_crArr = yzCardRouteMapper.find_cr();//查询 通道简要信息  状态为 正常
                List<Object> channel_idArr = new ArrayList<>();
                if (find_crArr != null && find_crArr.size() > 0) {
                    for (int i = 0; i < find_crArr.size(); i++) {
                        channel_idArr.add(find_crArr.get(i).get("dictValue"));
                    }
                } else {
                    channel_idArr.add("0");
                }
                //筛选出通道正常的进行 查询
                map.put("channel_idArr", channel_idArr);
                List<String> normalChannel_iccidarr = yzCardMapper.isExistence(map);
                if (normalChannel_iccidarr != null && normalChannel_iccidarr.size() > 0) {
                    if (!(normalChannel_iccidarr.size() == list.size())) {
                        //上传数据>数据库查询 赛选出
                        List<String> list1 = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            list1.add(list.get(i).get("iccid").toString());
                        }
                        // 获取 数组去重数据 和 重复值
                        Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, normalChannel_iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                        list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                        List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                        //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                        if (Out_list_Different.size() > 0) {
                            Map<String, Object> defOutcolumns = new HashMap<>();
                            defOutcolumns.put("describe", "通道状态 为停用或已删除 取消操作");
                            defOutcolumns.put("agentName", create_by);
                            defOutcolumns.put("result", "查询失败");
                            writeCSV.OutCSVObj(Out_list_Different, newName, Outcolumns, keys, defOutcolumns, OutSize);
                        }
                    }
                    map.put("card_arrs", list);//更新 list

                    List<Map<String, Object>> failArr = new ArrayList<>();
                    String describe = "";
                    List<Map<String, Object>> SuccessArr = new ArrayList<>();

                    List<Map<String, Object>> findRouteArr = yzCardMapper.findRouteArr(map);//查询 卡号 分配 通道 及变更前 卡状态 断开网状态
                    for (int i = 0; i < findRouteArr.size(); i++) {
                        Map<String, Object> Obj = new HashMap<>();
                        Map<String, Object> Route = findRouteArr.get(i);
                        String iccid = Route.get("iccid").toString();
                        Object Sel_status_ShowId = Route.get("Sel_status_ShowId") != null ? Route.get("Sel_status_ShowId") : "8";//默认'未知'
                        Object Sel_connection_status = Route.get("Sel_connection_status") != null ? Route.get("Sel_connection_status") : "未知";

                        Obj.putAll(ApiPmap);//将 执行 PAI 的 参数 放入执行请求 参数中
                        Obj.put("iccid", iccid);
                        //放入变更前 卡状态 和 断开网状态
                        Obj.put("Sel_status_ShowId", Sel_status_ShowId);
                        Obj.put("Sel_connection_status", Sel_connection_status);




                        Map<String, Object> Rmap = null;
                        switch (OptionType) {
                            case "Stop":
                                Rmap = internalApiRequest.changeCardStatus(Obj, findRouteArr.get(i));
                                break;
                            case "Machine":
                                Rmap = internalApiRequest.changeCardStatus(Obj, findRouteArr.get(i));
                                break;
                            case "DisconnectNetwork":
                                Rmap = internalApiRequest.FunctionApnStatus(Obj, findRouteArr.get(i));
                                break;
                            case "OpenNetwork":
                                Rmap = internalApiRequest.FunctionApnStatus(Obj, findRouteArr.get(i));
                                break;
                        }


                        String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                        if (code.equals("200")) {
                            //停机操作
                            Object status = Rmap.get("status");
                            if (status != null && status != "" && status.toString().trim().length() > 0) {
                                //Obj.put("status", status.toString().trim());
                                String RMessage = Rmap.get("Message") != null ? Rmap.get("Message").toString() : "操作成功！";
                                Obj.put("Message", RMessage);
                                Obj.put("describe", describe);
                                Obj.put("agentName", create_by);
                                Obj.put("result", "操作成功");
                                SuccessArr.add(Obj);
                                log.info(">>API - 变更操作  成功 = 0 :{} | {}<<", iccid, Rmap);
                            }
                        } else {
                            Obj.put("agentName", create_by);
                            Obj.put("result", "操作失败");
                            Message = Rmap.get("Message") != null ? Rmap.get("Message").toString() : "API异常！";
                            Obj.put("Message", Message);
                            failArr.add(Obj);
                        }
                    }
                    //2. 卡信息 变更表 插入数据
                    if (SuccessArr.size() > 0) {
                        writeCSV.OutCSVObj(SuccessArr, newName, Outcolumns, keys, null, OutSize);
                        yzExecutionTaskMapper.set_end_time(task_map);
                        switch (OptionType) {
                            case "Stop":
                                //变更主表信息
                                try {
                                    updStatus(SuccessArr, "1");//下发变更 卡状态 队列
                                } catch (Exception e) {
                                    log.info(">>错误 - 变更主表信息 :{} | {}<<", e.getMessage());
                                }
                                break;
                            case "Machine":
                                //变更主表信息
                                try {
                                    updStatus(SuccessArr, "2");//下发变更 卡状态 队列
                                } catch (Exception e) {
                                    log.info(">>错误 - 变更主表信息 :{} | {}<<", e.getMessage());
                                }
                                break;
                            case "DisconnectNetwork":
                                //变更主表信息
                                try {
                                    updConnectionStatus(SuccessArr, "1");//下发变更 卡状态 队列
                                } catch (Exception e) {
                                    log.info(">>错误 - 变更主表信息 :{} | {}<<", e.getMessage());
                                }
                            case "OpenNetwork":
                                //变更主表信息
                                try {
                                    updConnectionStatus(SuccessArr, "0");//下发变更 卡状态 队列
                                } catch (Exception e) {
                                    log.info(">>错误 - 变更主表信息 :{} | {}<<", e.getMessage());
                                }
                                break;
                        }
                        //变更 信息 遍历放入 插入 list
                        List<Map<String, Object>> AddArr = new ArrayList<>();
                        for (int i = 0; i < SuccessArr.size(); i++) {
                            Map<String, Object> Obj = SuccessArr.get(i);
                            String iccid = Obj.get("iccid").toString();
                            Map<String, Object> Add_Map = new HashMap<>();
                            String value = Obj.get(key) != null && Obj.get(key).toString().length() > 0 ? Obj.get(key).toString() : "未知";
                            String cbefore = "";//变更前
                            if (!value.equals("未知")) {
                                cbefore = myDictionary.getdictLabel(dict_type, value);
                            } else {
                                cbefore = value;
                            }
                            Add_Map.put("iccid", iccid);//查询 卡号 分配 通道
                            Add_Map.put("ctype", ctype);//卡信息变更类型
                            Add_Map.put("cbefore", cbefore);//变更前
                            Add_Map.put("cafterward", cafterward);//变更后
                            Add_Map.put("remark", "执行成功！");
                            Add_Map.put("source_type", "5");// 来源 类型 ：5 批量操作
                            Add_Map.put("execution_status", "1");
                            AddArr.add(Add_Map);
                        }
                        Map<String, Object> map1 = new HashMap<>();
                        map1.put("CardInfoMapList", AddArr);
                        int addBool = yzCardInfoChangeMapper.addinfo(map1);//插入变更 信息
                        log.info("成功信息插入 - " + map.get("iccid") + " 卡信息变更表 插入 ： " + OptionType + " 新增卡信息变更记录 addBool :" + addBool);

                    }
                    if (failArr.size() > 0) {
                        writeCSV.OutCSVObj(failArr, newName, Outcolumns, keys, null, OutSize);
                        yzExecutionTaskMapper.set_end_time(task_map);
                        List<Map<String, Object>> AddArr = new ArrayList<>();
                        for (int j = 0; j < failArr.size(); j++) {
                            Map<String, Object> ObjMap = failArr.get(j);
                            String iccid = ObjMap.get("iccid").toString();
                            String value = ObjMap.get(key) != null && ObjMap.get(key).toString().length() > 0 ? ObjMap.get(key).toString() : "未知";
                            String cbefore = "";//变更前
                            if (!value.equals("未知")) {
                                cbefore = myDictionary.getdictLabel(dict_type, value);
                            } else {
                                cbefore = value;
                            }
                            Map<String, Object> Add_Map = new HashMap<>();
                            Add_Map.put("iccid", iccid);//查询 卡号 分配 通道
                            Add_Map.put("ctype", ctype);//变更类型
                            Add_Map.put("cbefore", cbefore);//变更前
                            Add_Map.put("cafterward", cafterward);//变更后
                            Add_Map.put("describe", describe);//执行描述
                            Add_Map.put("agentName", create_by);
                            Add_Map.put("remark", "执行失败！");
                            Add_Map.put("source_type", "5");// 来源 类型 ：5 批量操作
                            Add_Map.put("execution_status", "2");//失败
                            AddArr.add(Add_Map);
                        }
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("CardInfoMapList", AddArr);
                        int addBool = yzCardInfoChangeMapper.addinfo(map2);
                        log.info("失败信息插入 - " + map.get("iccid") + " 卡信息变更表 插入 ： " + SuccessArr + " 新增卡信息变更记录 addBool :" + addBool);
                    }

                } else {
                    Map<String, Object> defOutcolumns = new HashMap<>();
                    defOutcolumns.put("describe", "通道状态 为停用或已删除 取消操作！");
                    defOutcolumns.put("agentName", create_by);
                    defOutcolumns.put("result", "查询失败");
                    writeCSV.OutCSVObj(list, newName, Outcolumns, keys, defOutcolumns, OutSize);
                }
            } else {
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("describe", "未划分通道，取消操作！");
                defOutcolumns.put("agentName", create_by);
                defOutcolumns.put("result", "查询失败");
                writeCSV.OutCSVObj(list, newName, Outcolumns, keys, defOutcolumns, OutSize);
            }
        }


    }

    /**
     * 修改卡状态
     * @param SuccessArr
     */
    public void updStatus(List<Map<String, Object>> SuccessArr, String statusCode) {
        //1. 修改卡状态 || 修改停机复机状态
        List<String> iccidArr = new ArrayList<String>();
        Map<String, Object> Upd_Map = new HashMap<>();
        for (int i = 0; i < SuccessArr.size(); i++) {
            iccidArr.add(SuccessArr.get(i).get("iccid").toString());
        }
        Upd_Map.put("iccidArr", iccidArr);
        Upd_Map.put("status_id", statusCode);
        Upd_Map.put("status_ShowId", getShowStatIdArr.GetShowStatId(statusCode));

        yzCardMapper.updStatusIdArr(Upd_Map);
    }


    public void updConnectionStatus (List<Map<String, Object>> SuccessArr,String Connection) {
        //1. 修改卡状态 || 修改断开网状态
        List<String> iccidArr = new ArrayList<String>();
        Map<String,Object> Upd_Map = new HashMap<>();
        for (int i = 0; i < SuccessArr.size(); i++) {
            iccidArr.add(SuccessArr.get(i).get("iccid").toString());
        }
        Upd_Map.put("iccidArr",iccidArr);
        Upd_Map.put("connection_status",Connection);
        yzCardMapper.updction(Upd_Map);

    }


}






