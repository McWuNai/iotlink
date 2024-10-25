package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardInfoChangeMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Console;
import java.io.IOException;
import java.util.*;

/**
 * 灵活变更状态
 **/
@Slf4j
@Component
public class CardFleState {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzCardInfoChangeMapper yzCardInfoChangeMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;

    private String Outcolumns[] = {"iccid", "返回消息", "执行描述", "执行人", "执行结果"};
    private String keys[] = {"iccid", "Message", "describe", "agentName", "result"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 灵活变更状态
     *
     * @param msg
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_fileFlexible_queue")
    private void SetCardInfo(String msg ) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            Map<String, Object> Rmap = (Map<String, Object>) map.get("map");//参数
            Map<String,Object> User =  ( Map<String,Object>)map.get("User");//登录用户信息

            executionel(Rmap,User);//灵活变更状态"map" -> {JSONObject@11031}  size = 8
        } catch (Exception e) {
            log.error(">>错误 - 特殊操作 灵活变更状态 :{}<<", e.getMessage().toString());
        }
    }

        private void executionel(Map<String, Object> Rmap,Map<String, Object> User) {
        String Message = "";
        HashMap<String, Object> map = new HashMap<>();

        List<String> list = (List<String>) Rmap.get("iccids");

        List<Map<String,Object>> mapList = new  ArrayList<>();

        // 把List<String> 转换成 List<Map<String,Object>>
        for (int i = 0; i <list.size() ; i++) {
            HashMap<String, Object> HaAap = new HashMap<>();
            HaAap.put("iccid", list.get(i));
            mapList.add(HaAap);
        }

        Map<String, String> Dept = (Map<String, String>)User.get("dept");
        String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";

        String newName = UUID.randomUUID().toString().replace("-", "") + "_FleStat";////对应 执行任务导出类别 CSV 前缀
        String failAdd = UUID.randomUUID().toString().replace("-","")+"_failAdd";

        String task_name =  "勾选 操作 [" + "灵活变更状态" + "] ";
        String SaveUrl = "/getcsv/" + newName + ".csv";
        String Url = "";
        SaveUrl = "/getcsv/" + newName + ".csv";
        Url += SaveUrl + ",/getcsv/"+failAdd+".csv"; // 进行追加
        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth", create_by);
        task_map.put("task_name", task_name);
        task_map.put("url", SaveUrl);
        task_map.put("agent_id", Dept.get("deptId"));
        task_map.put("type", "30"); //对应 字典表的 任务类别

        yzExecutionTaskMapper.add(task_map);//添加执行 任务表

        map.put("card_arrs", mapList);//更新 list
        map.put("type", "3"); //获取 iccid

        if (mapList != null && mapList.size() > 0) {
            //筛选出未划分通道的
            map.put("channel_idType", "notNull");
            List<String> Channel_iccidarr = yzCardMapper.isExistence(map); //查询单卡信息
            if (Channel_iccidarr != null && Channel_iccidarr.size() > 0) {
                if (!(Channel_iccidarr.size() == mapList.size())) {
                    // 获取 数组去重数据 和 重复值
                    Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(mapList, Channel_iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                    mapList = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
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
                map.put("card_arrs", mapList);//更新 list
                List<Map<String, Object>> find_crArr = yzCardRouteMapper.find_cr();//查询 通道简要信息  状态为 正常
                List<Object> channel_idArr = new ArrayList<>();
                if (find_crArr != null && find_crArr.size() > 0) {//查询 通道简要信息  状态为 正常
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
                        if (!(normalChannel_iccidarr.size() == mapList.size())) {
                            //上传数据>数据库查询 赛选出
                            List<String> list1 = new ArrayList<>();
                            for (int i = 0; i < mapList.size(); i++) {
                                list1.add(mapList.get(i).get("iccid").toString());
                            }
                            // 获取 数组去重数据 和 重复值
                            Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(mapList, normalChannel_iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                            mapList = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
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
                        map.put("card_arrs", mapList);//更新 list


                        List<Map<String, Object>> SuccessArr = new ArrayList<>();
                        List<Map<String, Object>> failArr = new ArrayList<>();

                        List<Map<String, Object>> findRouteArr = yzCardMapper.findRouteArr(map);//查询 卡号 分配 通道
                        for (int i = 0; i < findRouteArr.size(); i++) {
                            Map<String, Object> Route = findRouteArr.get(i);
                            String iccid = Route.get("iccid").toString();//获取

                            Map<String, Object> Obj = new HashMap<>();// 定义 Obj
                            Obj.put("iccid", iccid);
                            Obj.put("card_arrs", iccid);
                            Object ShowId = Rmap.get("status_ShowId");
                            Obj.put("operType",ShowId);//API 状态


                            Map<String, Object> CsFble = internalApiRequest.changeCardStatusFlexible(Obj, findRouteArr.get(i));//获取接口
                            String code = CsFble.get("code") != null ? CsFble.get("code").toString() : "500";
                            if (code.equals("200")) {
                                Object status = Rmap.get("status_ShowId");
                                if (status != null && status != "" && status.toString().trim().length() > 0) {
                                    Message = Rmap.get("Message") != null ? Rmap.get("Message").toString() : "操作成功！";
                                    Obj.put("status", status);
                                    Obj.put("Message", Message);
                                    Obj.put("agentName", create_by);
                                    Obj.put("describe", "成功");
                                    Obj.put("result", "操作成功");
                                    SuccessArr.add(Obj);
                                }
                            } else {
                                Obj.put("result", "操作失败");
                                Obj.put("describe", "失败");
                                Message = CsFble.get("Message") != null ? CsFble.get("Message").toString() : "失败！";
                                Obj.put("Message", Message);
                                failArr.add(Obj);
                            }


                            // 成功
                            if (SuccessArr.size() > 0) {
                                writeCSV.OutCSVObj(SuccessArr, newName, Outcolumns, keys, null, OutSize);
                                // 变更主表信息
                                try {
                                    String State ="";
                                    String status_ShowId = Rmap.get("status_ShowId").toString();
                                    if(status_ShowId.equals("0")){ // 已激活转已停机
                                        State = "2";//卡状态 停机
                                    }
                                    // 1:已停机转已激活 2:库存转已激活 5:可测试转已激活 6:待激活转已激活
                                    if(status_ShowId.equals("1") || status_ShowId.equals("2") || status_ShowId.equals("5") || status_ShowId.equals("6")){
                                        State = "1";//卡状态 正常
                                    }
                                    if(status_ShowId.equals("3")){// 可测试转库存
                                        State = "19";//卡状态 库存
                                    }
                                    if(status_ShowId.equals("4")){// 可测试转待激活
                                        State = "7";//卡状态 待激活
                                    }
                                    updStatus(SuccessArr, State);//下发变更 卡状态 队列

                                }catch (Exception e){
                                    log.info(">>错误 - 变更主表信息 :{} | {}<<", e.getMessage());
                                }

                                List<Map<String, Object>> AddArr = new ArrayList<>();
                                for (int j = 0; j < SuccessArr.size(); j++) {
                                    Map<String, Object> Add_Map = SuccessArr.get(j);
                                    String cbefore =""; //变更前 状态
                                    String cafterward = ""; //变更后状态
                                    Add_Map.put("ctype", "1"); //变更类型
                                    Object ShowIds = Rmap.get("status_ShowId");
                                    if(ShowIds.equals("0")){
                                        cbefore="1";
                                        cafterward="2";
                                    }
                                    if(ShowIds.equals("1")){
                                        cbefore="2";
                                        cafterward="1";
                                    }
                                    if(ShowIds.equals("2")){
                                        cbefore="19";
                                        cafterward="1";
                                    }
                                    if(ShowIds.equals("3")){
                                        cbefore="17";
                                        cafterward="19";
                                    }
                                    if(ShowIds.equals("4")){
                                        cbefore="17";
                                        cafterward="7";
                                    }
                                    if(ShowIds.equals("5")){
                                        cbefore="17";
                                        cafterward="1";
                                    }
                                    if(ShowIds.equals("6")){
                                        cbefore="7";
                                        cafterward="1";
                                    }
                                    Add_Map.put("cbefore", cbefore);
                                    Add_Map.put("cafterward", cafterward);
                                    Add_Map.put("remark", "执行成功！");
                                    Add_Map.put("source_type", "9");// 卡变更类型来源
                                    Add_Map.put("execution_status", "1");//执行状态
                                    AddArr.add(Add_Map);
                                }
                                Map<String, Object> map2 = new HashMap<>();
                                map2.put("CardInfoMapList", AddArr);
                                Integer addBool = yzCardInfoChangeMapper.addinfo(map2);
                                log.info("Dlx - " + map.get("iccid") + " 卡信息变更表 插入 ： " + SuccessArr + " 新增卡信息变更记录 addBool :" + addBool);
                            }


                            // 失败 newName
                            if (failArr.size() > 0) {
                                writeCSV.OutCSVObj(failArr, newName, Outcolumns, keys, null, OutSize);

                                task_map.put("url",Url);
                                task_map.get("id");
                                yzExecutionTaskMapper.upd(task_map);//修改 任务表
                                writeCSV.OutCSVObj(failArr, failAdd, Outcolumns, keys, null, OutSize); //拼接

                                List<Map<String, Object>> AddArr = new ArrayList<>();
                                for (int j = 0; j < failArr.size(); j++) {
                                    Map<String, Object> Add_Map = failArr.get(j);
                                    Add_Map.put("ctype", "1");
                                    Object ShowIds = Rmap.get("status_ShowId");
                                    Add_Map.put("cbefore", ShowIds);
                                    Add_Map.put("cafterward", ShowIds);
                                    Add_Map.put("remark", "执行失败！");
                                    Add_Map.put("source_type", "9");// 卡变更类型来源
                                    Add_Map.put("execution_status", "2");
                                    AddArr.add(Add_Map);
                                }
                                    Map<String, Object> map3 = new HashMap<>();
                                    map3.put("CardInfoMapList", AddArr);
                                    Integer addBool = yzCardInfoChangeMapper.addinfo(map3);
                                    log.info("Dlx - " + map.get("iccid") + " 卡信息变更表 插入 ： " + failArr + " 新增卡信息变更记录 addBool :" + addBool);
                            }


                        }

                    } else {
                        Map<String, Object> defOutcolumns = new HashMap<>();
                        defOutcolumns.put("describe", "通道状态 为停用或已删除 取消操作！");
                        defOutcolumns.put("agentName", create_by);
                        defOutcolumns.put("result", "查询失败");
                        writeCSV.OutCSVObj(mapList, newName, Outcolumns, keys, defOutcolumns, OutSize);
                    }
                } else {
                    Map<String, Object> defOutcolumns = new HashMap<>();
                    defOutcolumns.put("describe", "未划分通道，取消操作！");
                    defOutcolumns.put("agentName", create_by);
                    defOutcolumns.put("result", "查询失败");
                    writeCSV.OutCSVObj(mapList, newName, Outcolumns, keys, defOutcolumns, OutSize);
                }
                yzExecutionTaskMapper.set_end_time(task_map);//需改任务 结束 时间
            }
            yzExecutionTaskMapper.set_end_time(task_map);//需改任务 结束 时间

        }
    }

    /**
     * 修改卡状态
     *
     * @param SuccessArr
     */
    public void updStatus(List<Map<String, Object>> SuccessArr, String statusCode) {
        //1. 修改卡状态 || 修改停复机状态
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




}









