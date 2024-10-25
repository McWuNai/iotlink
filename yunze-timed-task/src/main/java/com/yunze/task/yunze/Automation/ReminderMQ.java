package com.yunze.task.yunze.Automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcUrlMapper;
import com.yunze.common.mapper.yunze.card.YzCardUsageReminderMapper;
import com.yunze.common.utils.Email.EmailCc;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class ReminderMQ {


    @Resource
    private YzCardUsageReminderMapper yzCardUsageReminderMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private EmailCc emailCc;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzAutomationCcMapper yzAutomationCcMapper;
    @Resource
    private YzAutomationCcUrlMapper yzAutomationCcUrlMapper;
    @Resource
    private YzAutomationCcHisMapper yzAutomationCcHisMapper;


    String Outcolumns[] = {"iccid", "资费id", "资费激活名称", "APN 名称", "总量(MB)", "使用量(MB)", "剩余量(MB)", "已用百分比(%)", "创建时间"};
    String keys[] = {"iccid", "offeringId", "offeringName", "apnName", "totalAmount", "useAmount", "remainAmount", "percentage", "create_date"};
    private int OutSize = 50;//每 50条数据输出一次

    @RabbitHandler
    @RabbitListener(queues = "admin_Reminder_queue")
    public void Reminder(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer parameter = Integer.parseInt(Pmap.get("parameter").toString()) ;

        Map<String, Object> findMap = new HashMap<>();
        findMap.put("trigger_type", "8"); //自动化触发类型 未划分资费组

        List<Map<String,Object>> out_list = new ArrayList<>();
        Map<String, Object> CcMessage_Map = new HashMap<>();
        String taskId = "";

        Map<String, Object> map = new HashMap<>();
        map.put("dimensionField","4");
        map.put("dimensionType","1");
        map.put("percentage",parameter);
        List<Map<String, Object>> list = yzCardUsageReminderMapper.mailId(map);

        if(list != null){
            String create_by = " [平台] - " + " [自动化] ";
            String newName = UUID.randomUUID().toString().replace("-", "") + "_Reminder";
            String task_name = " 上游套餐超过百分比 抄送 [" + VeDate.getStringDate() + "] ";
            String SaveUrl = "/getcsv/" + newName + ".csv";
            SaveUrl = "/getcsv/" + newName + ".csv";
            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth", create_by);
            task_map.put("task_name", task_name);
            task_map.put("url", SaveUrl);
            task_map.put("agent_id", "100");
            task_map.put("type", "32"); //对应 字典表的 执行日志类别
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表

            for (int j = 0; j < list.size(); j++) {
                Map<String,Object> CarMap = new HashMap<>();
                CarMap.putAll(list.get(j));
                out_list.add(CarMap);
            }
            writeCSV.OutCSVObj(out_list, newName,Outcolumns, keys,null,OutSize);
            yzExecutionTaskMapper.set_end_time(task_map);//任务结束
            taskId = ""+task_map.get("id");
            CcMessage_Map.put("taskId",taskId);// 获取 执行任务表的ID
            CcMessage_Map.put("Count",""+out_list.size());
        }else {
            log.info("usageMailReminder 自动化任务邮件抄送【上游套餐超过百分比】未获取到 数据 抄送取消");
        }

        List<Map<String, Object>> AutomationCcArr = yzAutomationCcMapper.findConcise(findMap);//获取自动化 抄送 组
        if (AutomationCcArr != null && AutomationCcArr.size() > 0) {
            for (int i = 0; i < AutomationCcArr.size(); i++) {
                Map<String, Object> CCObj = AutomationCcArr.get(i);
                List<Map<String, Object>> AutomationCcUrlArr = yzAutomationCcUrlMapper.findConcise(CCObj);//获取自动化 抄送 邮箱

                if (AutomationCcUrlArr != null && AutomationCcUrlArr.size() > 0) {
                    String execution_template = CCObj.get("execution_template").toString();
                    CCObj.put("taskId",taskId);
                    CCObj.put("Count",out_list.size());
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

                                String cc_parameter = JSON.toJSONString(CCObj);
                                cc_parameter = cc_parameter.length() > 500 ? cc_parameter.substring(0, 500) : cc_parameter;
                                saveHisMap.put("cc_parameter", cc_parameter);
                                Map<String, Object> Rmap = null;
                                Boolean bool = false;
                                String remark = "", cc_result = "0";
                                try {

                                    Rmap = emailCc.usageMailReminder_default(CcMessage_Map, SendEmail);
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
                                log.info(">>自动化 [usageMailReminder] 已抄送邮箱{}  - 插入抄送记录 bool   {} <<", SendEmail, saveHis);
                            } else {
                                log.error(">>自动化 [usageMailReminder] 抄送邮箱未获取到  - 抄送取消  {} <<", eM);
                            }
                        }
                    } else {
                        log.error(">>自动化 [usageMailReminder] 抄送模板不批对  - 抄送取消  {} <<");
                    }
                } else {
                    log.error(">>自动化 [usageMailReminder] 获取自动化 抄送 邮箱 未找到数据 - 抄送取消  {} <<");
                }
            }
        }
    }


    /**
     * 邮件抄送 重试
     * @param msg 重试次数
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_ReminderCount_queue")
    public void RetryCC(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        RetryExecution(msg);
    }


    /**
     * 邮件抄送 重试
     * @param msg 重试次数
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ReminderCount_queue")
    public void DlxRetryCC(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        RetryExecution(msg);
    }


    public void RetryExecution(String msg){
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer cc_count = Integer.parseInt(Pmap.get("cc_count").toString());

        Map<String, Object> findNotPerformed_Map = new HashMap<>();
        findNotPerformed_Map.put("trigger_type", "8");
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
                    Rmap = emailCc.usageMailReminder_default(MsgMap, SendEmail);
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
                System.out.println(">>自动化  [Reminder]  已抄送邮箱" + SendEmail + "  - 抄送 bool   {" + bool + "}  修改 bool {" + updHis + "}<<");
            }
        }
    }

}
