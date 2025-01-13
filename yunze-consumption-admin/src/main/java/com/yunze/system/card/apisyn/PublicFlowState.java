package com.yunze.system.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.config.MyDictionary;
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
 * 勾选操作 同步状态，用量 和同步状态和用量
 */
@Slf4j
@Component
public class PublicFlowState {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private CardFlowSyn cardFlowSyn;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;
    @Resource
    private MyDictionary myDictionary;

    private String Outcolumns[] = {"iccid", "执行人", "返回消息 [状态]", "同步后状态", "描述 [状态]", "返回消息 [用量]", "描述 [用量]", "同步后已用用量MB", "同步后剩余用量MB", "执行结果"};
    private String keys[] = {"iccid", "agentName", "StateMessage", "status_ShowId_text", "Statedescribe", "FlowMessage", "Flowdescribe", "used", "remaining", "result"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 同步用量
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_Consumption_queue")
    public void ConsumptionArr(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Rmap = (Map<String, Object>) map.get("map");//参数
            execution(Rmap, "Consumption");//同步用量
        } catch (Exception e) {
            log.error(">>错误 - 批量 【同步用量】 消费者:{}<<", e.getMessage());
        }
    }

    /**
     * 同步状态
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_PublicMethod_queue")
    public void PublicMethodArr(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Rmap = (Map<String, Object>) map.get("map");//参数
            execution(Rmap, "PublicMethod");//同步状态
        } catch (Exception e) {
            log.error(">>错误 - 批量 【同步状态】 消费者:{}<<", e.getMessage());
        }
    }

    /**
     * 同步状态和用量
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_ConsumptionAndState_queue")
    public void ConsumptionAndStateArrss(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Rmap = (Map<String, Object>) map.get("map");//参数
            execution(Rmap, "ConsumptionAndState");//同步状态
        } catch (Exception e) {
            log.error(">>错误 - 批量 【同步状态和用量】 消费者:{}<<", e.getMessage());
        }
    }

    /**
     * 批量执行 公共函数
     */
    private void execution(Map<String, Object> Rmap, String OptionType) {
        String StateMessage = "";

        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = (List<Map<String, Object>>) Rmap.get("iccids");

        String task_nameApend = "";//执行日志类型 拼接
        String newNameApend = "";//执行日志 文件名 后缀
        String TaskType = "";//执行日志 类型

        // 批量 同步用量
        switch (OptionType) {
            case "Consumption":
                task_nameApend = "批量 同步用量";
                newNameApend = "_Consumption";
                TaskType = "18";
                break;
            case "PublicMethod":
                task_nameApend = "批量 同步状态";
                newNameApend = "_PublicMethod";
                TaskType = "19";
                break;
            case "ConsumptionAndState":
                task_nameApend = "批量 同步状态和用量";
                newNameApend = "_ConsumptionAndState";
                TaskType = "20";
                break;
        }
        Map<String, Object> User = (Map<String, Object>) Rmap.get("User");
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
                        defOutcolumns.put("Statedescribe", "未划分通道，取消操作！");
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

                                defOutcolumns.put("Statedescribe", "通道状态 为停用或已删除 取消操作");
                                defOutcolumns.put("agentName", create_by);
                                defOutcolumns.put("result", "查询失败");
                                writeCSV.OutCSVObj(Out_list_Different, newName, Outcolumns, keys, defOutcolumns, OutSize);
                            }
                        }
                        map.put("card_arrs", list);//更新 list


                        List<Map<String, Object>> SuccessArr = new ArrayList<>();
                        List<Map<String, Object>> failArr = new ArrayList<>();

                        List<Map<String, Object>> findRouteArr = yzCardMapper.findRouteArr(map);//查询 卡号 分配 通道
                        for (int i = 0; i < findRouteArr.size(); i++) {
                            Map<String, Object> Route = findRouteArr.get(i);
                            String iccid = Route.get("iccid").toString();//获取

                            Map<String, Object> Obj = new HashMap<>();// 定义 Obj
                            Obj.put("iccid", iccid);
                            Obj.put("card_arrs", iccid);
                            String used = "", remaining = "";//CalculationFlow-->返回数据
                            String FlowMessage = "", Flowdescribe = "", Statedescribe = "";
                            Map<String, Object> Add_Map = new HashMap<>();
                            String status_ShowId_text = "";

                            if (OptionType.equals("PublicMethod") || OptionType.equals("ConsumptionAndState")) {
                                Map<String, Object> StateRmap = internalApiRequest.queryCardStatus(Obj, findRouteArr.get(i));//同步状态
                                String code = StateRmap.get("code") != null ? StateRmap.get("code").toString() : "500";
                                if (code.equals("200")) {
                                    //获取 卡状态
                                    if (StateRmap.get("statusCode") != null && StateRmap.get("statusCode") != "" && StateRmap.get("statusCode").toString().trim().length() > 0) {
                                        String statusCode = StateRmap.get("statusCode").toString().trim();
                                        if (!statusCode.equals("0")) {
                                            Map<String, Object> Upd_Map = new HashMap<>();
                                            String status_ShowId = getShowStatIdArr.GetShowStatId(statusCode);
                                            Upd_Map.put("status_id", statusCode);
                                            Upd_Map.put("status_ShowId", status_ShowId);
                                            Upd_Map.put("iccid", Obj.get("iccid").toString());
                                            try {
                                                boolean bool = yzCardMapper.updStatusId(Upd_Map) > 0;//变更卡状态
                                                if (bool) {
                                                    log.info("已成功同步卡状态！");
                                                    StateMessage = "成功";
                                                    Statedescribe = "已成功同步卡状态！";
                                                    // status_ShowId_text 匹对 字典表 卡状态 8 个的 前端显示用的
                                                    status_ShowId_text = status_ShowId != null && status_ShowId.length() > 0 ? status_ShowId.toString() : "未知";
                                                    if (!status_ShowId.equals("未知")) {
                                                        status_ShowId_text = myDictionary.getdictLabel("yunze_card_status_ShowId", status_ShowId);
                                                    }

                                                } else {
                                                    StateMessage = "失败";
                                                    Statedescribe = "保存状态异常！";
                                                    log.error("保存状态异常:False！");
                                                }
                                            } catch (Exception e) {
                                                StateMessage = "失败";
                                                Statedescribe = "DB保存状态异常！";
                                                log.error("DB保存状态异常！e:{} " + e.getMessage());
                                            }
                                        } else {
                                            StateMessage = "失败";
                                            Statedescribe = "接口超频返回暂无数据返回，请稍后重试！";
                                            log.error("接口超频返回暂无数据返回，请稍后重试！");
                                        }
                                    }
                                } else {
                                    StateMessage = "失败";
                                    Statedescribe = "接口超频返回暂无数据返回，请稍后重试！";
                                    log.error("同步数据失败 " + JSON.toJSONString(StateRmap));
                                }
                            }


                            if (OptionType.equals("Consumption") || OptionType.equals("ConsumptionAndState")) {
                                Map<String, Object> FlowRmap = internalApiRequest.queryFlow(Obj, findRouteArr.get(i));//同步用量

                                String Flowcode = FlowRmap.get("code") != null ? FlowRmap.get("code").toString() : "500";
                                if (Flowcode.equals("200")) {
                                    //获取 卡用量 开卡日期 更新 card info
                                    if (FlowRmap.get("Use") != null && FlowRmap.get("Use") != "" && FlowRmap.get("Use").toString().trim().length() > 0) {
                                        Double Use = Double.parseDouble(FlowRmap.get("Use").toString());
                                        if (Use >= 0) {
                                            try {
                                                Map<String, Object> FlowSynRmap = cardFlowSyn.CalculationFlow(Obj.get("iccid").toString(), Use, findRouteArr.get(i));
                                                boolean bool_info = (boolean) FlowSynRmap.get("bool_info");
                                                if (bool_info) { //为true成功
                                                    used = FlowSynRmap.get("used").toString();
                                                    remaining = FlowSynRmap.get("remaining").toString();
                                                    log.info("已成功同步用量");
                                                    FlowMessage = "成功";
                                                    Flowdescribe = "已成功同步卡用量！";
                                                } else {
                                                    FlowMessage = "失败";
                                                    Add_Map.put("used", used);
                                                    Add_Map.put("remaining", remaining);
                                                    failArr.add(Add_Map);
                                                    log.error("同步用量异常");
                                                }
                                                log.info(JSON.toJSONString(FlowRmap));
                                            } catch (Exception e) {
                                                FlowMessage = "失败";
                                                Flowdescribe = "用量内部计算错误！";
                                                log.error("用量内部计算错误！" + e.getMessage().toString());
                                            }
                                        } else {
                                            FlowMessage = "失败";
                                            Flowdescribe = "同步数据失败！";
                                            log.error("同步数据失败 " + JSON.toJSONString(FlowRmap));
                                        }
                                    }
                                } else {
                                    FlowMessage = "失败";
                                    Flowdescribe = "同步数据失败！";
                                    log.error("同步数据失败 " + JSON.toJSONString(FlowRmap));
                                }
                            }

                            Add_Map.put("iccid", iccid);//iccid
                            Add_Map.put("agentName", create_by);
                            Add_Map.put("FlowMessage", FlowMessage);//返回消息 [用量]
                            Add_Map.put("Flowdescribe", Flowdescribe);//描述 [用量]
                            Add_Map.put("used", used);// CalculationFlow-->返回数据
                            Add_Map.put("remaining", remaining);// CalculationFlow-->返回数据
                            Add_Map.put("StateMessage", StateMessage);//返回消息 [状态]
                            Add_Map.put("Statedescribe", Statedescribe);//描述 [状态]
                            Add_Map.put("status_ShowId_text", status_ShowId_text);//描述 [前端显示用状态 值状态]
                            Add_Map.put("result", "已执行");

                            SuccessArr.add(Add_Map);

                        }//for end


                        //执行 CSV 文件 输出
                        if (SuccessArr.size() > 0) {
                            writeCSV.OutCSVObj(SuccessArr, newName, Outcolumns, keys, null, OutSize);
                        } else if (failArr.size() > 0) {
                            writeCSV.OutCSVObj(failArr, newName, Outcolumns, keys, null, OutSize);
                        }
                        yzExecutionTaskMapper.set_end_time(task_map);
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
    }
}


