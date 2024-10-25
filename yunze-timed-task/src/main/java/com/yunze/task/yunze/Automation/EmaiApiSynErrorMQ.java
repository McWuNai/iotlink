package com.yunze.task.yunze.Automation;


import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcUrlMapper;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorGroupMapper;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorMapper;
import com.yunze.common.utils.Email.EmailCc;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class EmaiApiSynErrorMQ {


    @Resource
    private YzAutomationCcHisMapper yzAutomationCcHisMapper;
    @Resource
    private EmailCc emailCc;
    @Resource
    private YzCardPollingErrorGroupMapper yzCardPollingErrorGroupMapper;
    @Resource
    private YzCardPollingErrorMapper yzCardPollingErrorMapper;
    @Resource
    private YzAutomationCcMapper yzAutomationCcMapper;
    @Resource
    private YzAutomationCcUrlMapper yzAutomationCcUrlMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardMapper yzCardMapper;


    String Outcolumns[] = {"id","错误代码","错误信息","通道编码","创建时间","最近一次修改时间","是否需要通知","卡总数"};
    String keys[] = {"id","codeOn","message","cd_code","create_date","upd_date","is_notice","ct_sum"};


    String Outcolumns_1[] = {"id","iccid","错误代码","错误信息","通道编码","cd_id","通道名称","通道别称","登录账号","创建时间","最近一次修改时间","是否需要通知","返回数据"};
    String keys_1[] = {"id","iccid","codeOn","message","cd_code","cd_id","cd_name","cd_alias","cd_useraccount","create_date","upd_date","is_notice","rt_map"};


    String Outcolumns_2[] = {"cd_id","通道名称","通道别称","登录账号"};
    String keys_2[] = {"cd_id","cd_name","cd_alias","cd_useraccount"};


    private int OutSize = 50;//每 50条数据输出一次



    public boolean inIt(List<String> arr, String eStr){
        boolean bool = false;
        for (int i = 0; i < arr.size(); i++) {
            String str = arr.get(i);
            if(str.equals(eStr)){
                bool = true;
                break;
            }
        }
        return bool;
    }

    /**
     * 邮件抄送
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_EmaiApiSynError_queue")
    public void ListenerSend(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        String trigger_type = Pmap.get("trigger_type").toString();
        Send(trigger_type);
    }


    public void Send(String trigger_type) {

        Map<String, Object> findMap = new HashMap<>();
        findMap.put("trigger_type", trigger_type); // 自动化触发类型 ： 9  API同步错误-运营类型 | 10  API同步错误-非运营类型
        Map<String, Object> findErrorMap = new HashMap<>();
        String type = "";
        if(trigger_type.equals("9")){
            type = "运营类型";
            findErrorMap.put("notIn",0);
        }else{
            type = "非运营类型";
            findErrorMap.put("notIn",1);
        }




        Map findStatusAbnormal_map = new HashMap();

        Map<String, Object> CcMessage_Map = new HashMap<>();


        findErrorMap.put("is_notice","1");
        findErrorMap.put("isToDay","1");

        Map<String,Object> findDictData_map = new HashMap();
        findDictData_map.put("dictType","api_businessError_code");

        List<Map<String,Object>> errorCode = yzCardMapper.selectDictDataByType(findDictData_map);//获取 ‘上游业务错误编码’

        Map<String, Object> codeOnMap = new HashMap<>();
        List<Map <String,Object>> errorList = new ArrayList<>();
        List<String> exTypeArr = new ArrayList<>();
        if(errorCode!=null && errorCode.size()>0){
            for (int i = 0; i < errorCode.size(); i++) {
                Map<String,Object> codeObj = errorCode.get(i);
                String dict_label = codeObj.get("dict_label").toString();
                String dict_value = codeObj.get("dict_value").toString();
                List<String> exDictValueArr = new ArrayList<>();
                if(inIt(exTypeArr,dict_label)){
                    exDictValueArr = (List<String>)codeOnMap.get(dict_label);
                }
                exDictValueArr.add(dict_value);
                codeOnMap.put(dict_label,exDictValueArr);
            }

            //循环 去查询 某个通道类型下 业务错误编码【通知运营去处理的错误问题 如 ：‘卡号不属于本企业’】
            for(String key:codeOnMap.keySet()){
                List<String> cdCodeArr = new ArrayList<>();
                cdCodeArr.add(key);
                if(key.equals("YiDong_EC")){
                    cdCodeArr.add("YiDong_EC_Combo");//同属于 移动ECv5 用来区分查询用量的时候是查询当月还是当前使用账期用量
                }else if(key.equals("YiDong_ECv2")){
                    cdCodeArr.add("YiDong_ECv2_Combo");//同属于 移动ECv2 用来区分查询用量的时候是查询当月还是当前使用账期用量
                }
                findErrorMap.put("cdCodeArr",cdCodeArr);
                findErrorMap.put("codeOnArr",codeOnMap.get(key));
                List<Map <String,Object>> errorList_child = yzCardPollingErrorGroupMapper.selMap(findErrorMap);
                if(errorList_child!=null && errorList_child.size()>0){
                    errorList.addAll(errorList_child);
                }
            }
        }else{
            errorList =  yzCardPollingErrorGroupMapper.selMap(findErrorMap);
        }
        Integer Count = errorList!=null?errorList.size():0;
        if (Count > 0) {

            List<Map<String, Object>> AutomationCcArr = yzAutomationCcMapper.findConcise(findMap);//获取自动化 抄送 组
            if (AutomationCcArr != null && AutomationCcArr.size() > 0) {
                String taskId = "";



                String create_by = " [平台] - " + " [自动化] ";
                String newName = UUID.randomUUID().toString().replace("-", "") + "_ErrorGroupApi";
                String newName_1 = UUID.randomUUID().toString().replace("-", "") + "_ApiError";
                String newName_2 = UUID.randomUUID().toString().replace("-", "") + "_ApiRout";
                String task_name = " IoTLink 上游数据同步错误  [" + type + "] [" + VeDate.getStringDate() + "]";
                String SaveUrl = "/getcsv/" + newName + ".csv,"+"/getcsv/" + newName_1 + ".csv,"+"/getcsv/" + newName_2 + ".csv";
                Map<String, Object> task_map = new HashMap<String, Object>();
                task_map.put("auth", create_by);
                task_map.put("task_name", task_name);
                task_map.put("url", SaveUrl);
                task_map.put("agent_id", "100");
                task_map.put("type", "32"); //对应 字典表的 执行日志类别
                yzExecutionTaskMapper.add(task_map);//添加执行 任务表


                writeCSV.OutCSVObj(errorList, newName,Outcolumns, keys,null,OutSize);
                yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                taskId = ""+task_map.get("id");

                int errorCardCount = 0;
                for (int i = 0; i < errorList.size(); i++) {
                    Map <String,Object> eMap = errorList.get(i);
                    Integer ct_sum = eMap.get("ct_sum")!=null?Integer.parseInt(eMap.get("ct_sum").toString()):0;
                    errorCardCount += ct_sum;
                }

                Integer rCount = 0;


                writeCSV.OutCSVObj(errorList, newName,Outcolumns, keys,null,OutSize);

                List<Map <String,Object>> errorDsList =  yzCardPollingErrorMapper.selMapRelevance(findErrorMap);
                if(errorDsList!=null && errorDsList.size()>0){
                    writeCSV.OutCSVObj(errorDsList, newName_1,Outcolumns_1, keys_1,null,OutSize);
                    findErrorMap.put("isGroupBy",1);
                    List<Map <String,Object>> errorisGroupBy =  yzCardPollingErrorMapper.selMapRelevance(findErrorMap);
                    if(errorisGroupBy!=null && errorisGroupBy.size()>0) {
                        rCount = errorisGroupBy.size();
                        writeCSV.OutCSVObj(errorisGroupBy, newName_2, Outcolumns_2, keys_2, null, OutSize);
                    }
                }

                CcMessage_Map.put("taskId",taskId);// 获取 执行任务表的ID
                CcMessage_Map.put("describe","IoTLink API同步错误 [ "+type+" ] 错误类型 [ "+errorList.size()+" ] 个，涉及卡号 [ "+errorCardCount+" ] 个！，涉及通道 [ "+rCount+" ] 个！");
                CcMessage_Map.put("heartext"," IoTLink API同步错误 ["+type+"]业务需要您及时处理！，涉及卡号 [ "+errorCardCount+" ] 个！，涉及通道 [ "+rCount+" ] 个！");



                for (int i = 0; i < AutomationCcArr.size(); i++) {
                    Map<String, Object> CCObj = AutomationCcArr.get(i);
                    List<Map<String, Object>> AutomationCcUrlArr = yzAutomationCcUrlMapper.findConcise(CCObj);//获取自动化 抄送 邮箱

                    if (AutomationCcUrlArr != null && AutomationCcUrlArr.size() > 0) {
                        String execution_template = CCObj.get("execution_template").toString();
                        CcMessage_Map.put("emailMap", findStatusAbnormal_map);

                        if (execution_template.equals("1")) {
                            for (int j = 0; j < AutomationCcUrlArr.size(); j++) {
                                Object eM = AutomationCcUrlArr.get(j).get("email");
                                if (eM != null && eM.toString().length() > 0) {
                                    String SendEmail = eM.toString();
                                    Map<String, Object> saveHisMap = new HashMap<>();
                                    saveHisMap.put("cc_id", CCObj.get("id"));
                                    saveHisMap.put("cc_url", "");
                                    saveHisMap.put("cc_email", SendEmail);
                                    saveHisMap.put("cc_state", "1");//已抄送

                                    saveHisMap.put("cc_parameter", JSON.toJSONString(CcMessage_Map));
                                    Map<String, Object> Rmap = null;
                                    Boolean bool = false;
                                    String remark = "", cc_result = "0";
                                    try {
                                        Rmap = emailCc.CardApiSynError_default(CcMessage_Map, SendEmail);
                                        bool = (Boolean) Rmap.get("bool");
                                        if (bool) {
                                            cc_result = "1";
                                        }
                                    } catch (Exception e) {
                                        remark = e.getMessage();
                                        remark = remark.length() > 240 ? remark.substring(0, 240) : remark;
                                    }
                                    saveHisMap.put("remark", remark);
                                    saveHisMap.put("cc_result", cc_result);
                                    boolean saveHis = yzAutomationCcHisMapper.save(saveHisMap) > 0;
                                    log.info(">>自动化 [NoTariffGroup] 已抄送邮箱{}  - 插入抄送记录 bool   {} <<", SendEmail, saveHis);
                                } else {
                                    log.error(">>自动化 [NoTariffGroup] 抄送邮箱未获取到  - 抄送取消  {} <<", eM);
                                }
                            }
                        } else {
                            log.error(">>自动化 [NoTariffGroup] 抄送模板不批对  - 抄送取消  {} <<");
                        }
                    } else {
                        log.error(">>自动化 [NoTariffGroup] 获取自动化 抄送 邮箱 未找到数据 - 抄送取消  {} <<");
                    }
                }
            }
        }
    }



    /**
     * 邮件抄送
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_RetryCC_queue")
    public void ListenerRetryCC(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer cc_count = Integer.parseInt(Pmap.get("cc_count").toString());
        String trigger_type = Pmap.get("trigger_type").toString();

        RetryCC(cc_count,trigger_type);
    }

    /**
     * 邮件抄送 重试
     * @param cc_count 重试次数
     */
    public void RetryCC(Integer cc_count,String trigger_type) {
        Map<String, Object> findNotPerformed_Map = new HashMap<>();
        findNotPerformed_Map.put("trigger_type", trigger_type);
        findNotPerformed_Map.put("cc_count", cc_count);//重试次数
        List<Map<String, Object>> CcArr = yzAutomationCcHisMapper.findNotPerformed(findNotPerformed_Map);
        if (CcArr != null && CcArr.size() > 0) {
            //2.获取
            for (int i = 0; i < CcArr.size(); i++) {
                Map<String, Object> Cc_Map = CcArr.get(i);
                Map<String, Object> MsgMap = JSON.parseObject(Cc_Map.get("cc_parameter").toString());

                String SendEmail = Cc_Map.get("cc_email").toString();
                Map<String, Object> updHisMap = new HashMap<>();
                updHisMap.put("id", Cc_Map.get("id"));
                Map<String, Object> Rmap = null;
                Boolean bool = false;
                String remark = "", cc_result = "0";
                try {
                    Rmap = emailCc.CardApiSynError_default(MsgMap, SendEmail);
                    bool = (Boolean) Rmap.get("bool");
                    if (bool) {
                        cc_result = "1";
                    }
                } catch (Exception e) {
                    remark = e.getMessage();
                    remark = remark.length() > 240 ? remark.substring(0, 240) : remark;
                }
                updHisMap.put("remark", remark);
                updHisMap.put("cc_result", cc_result);
                boolean updHis = yzAutomationCcHisMapper.updCcCount(updHisMap) > 0;
                System.out.println(">>自动化  [NoTariffGroup]  已抄送邮箱" + SendEmail + "  - 抄送 bool   {" + bool + "}  修改 bool {" + updHis + "}<<");
            }
        }
    }




}
