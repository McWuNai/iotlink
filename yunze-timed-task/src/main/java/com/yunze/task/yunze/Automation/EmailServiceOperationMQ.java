package com.yunze.task.yunze.Automation;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcUrlMapper;
import com.yunze.common.mapper.yunze.card.YzCardDueSoonMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.Email.EmailCc;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Array;
import java.util.*;

/**
 * 2022年8月15日16:14:15
 * 邮件抄送:根据服务快到期通知前台运维人员
 */
@Slf4j
@Component
public class EmailServiceOperationMQ {

    @Resource
    private YzAutomationCcHisMapper yzAutomationCcHisMapper;
    @Resource
    private EmailCc emailCc;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzAutomationCcMapper yzAutomationCcMapper;
    @Resource
    private YzAutomationCcUrlMapper yzAutomationCcUrlMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private YzCardDueSoonMapper yzCardDueSoonMapper;

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;

    @Resource
    private WriteCSV writeCSV;

    String Outcolumns[] = {"id", "企业名称", "邮箱", "企业Id", "快到期张数", "快到期最小时间", "快到期最大时间"};
    String keys[] = {"id", "dept_name", "email", "dept_id", "count", "record_date", "nextDay"};


    /**
     * 2022年8月15日16:15:32
     * 邮件抄送(根据服务快到期进行查询 并抄送给前端运维人员)
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_emailServiceOperation_queue")
    public void emailServiceOperation() {

        log.info("消费者开始:admin_emailServiceOperation_queue");
        Map<String, Object> findMap = new HashMap<>();
        findMap.put("trigger_type", "12"); //自动化触发类型 服务快到期

        Map<String, Object> channel_idMap = new HashMap<>();

        List<String> channel_id = new ArrayList<String>();
        channel_id.add("IsNull");
        channel_idMap.put("channel_id", channel_id);

        Map<String, Object> CcMessage_Map = new HashMap<>();

        Integer Count = yzCardMapper.selMapCount(channel_idMap);//查询总数

        /*这里获取所有服务快到期的内容*/
        HashMap<String, Object> configMap = new HashMap<>();
        configMap.put("config_key", "card.expiringSoonCount");
        String delay = yzWxByProductAgentMapper.findConfig(configMap);// 快到期 天数
        Map<String, Object> Pmap_expiringSoonCount = new HashMap<>();
        String record_date = VeDate.getStringDate();
        Pmap_expiringSoonCount.put("timetype", "5");
        Pmap_expiringSoonCount.put("staTime", record_date);
        String nextDay = VeDate.getNextDay(record_date, delay);
        nextDay = nextDay + " 23:59:59";
        Pmap_expiringSoonCount.put("endTime", nextDay);
        List<Map> maps = yzCardDueSoonMapper.SelectCardDueSoon(Pmap_expiringSoonCount);
        /*类型转化*/
        HashMap<String, Object> h = new HashMap<>();
        ArrayList<Map<String, Object>> Q = new ArrayList<>();
        for (Map s : maps) {
            h = (HashMap<String, Object>) s;
            Q.add(h);
        }

        String create_by = " [平台] - " + " [自动化] ";
        String newName = UUID.randomUUID().toString().replace("-", "") + "_Duesoon";
        String task_name = "  [服务快到期] [" + record_date + " ~ " + nextDay + "]";
        String SaveUrl = "/getcsv/" + newName + ".csv";
        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth", create_by);
        task_map.put("task_name", task_name);
        task_map.put("url", SaveUrl);
        task_map.put("agent_id", "100");
        task_map.put("type", "32"); //对应 字典表的 执行日志类别
        yzExecutionTaskMapper.add(task_map);//添加执行 任务表
        Map<String, Object> defOutcolumns = new HashMap<>();
        defOutcolumns.put("record_date", record_date);
        defOutcolumns.put("nextDay", nextDay);

        writeCSV.OutCSVObj(Q, newName, Outcolumns, keys, defOutcolumns, 100);
        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        String taskId = "" + task_map.get("id");



        /*判断查询出来的服务快到期的集合是不是空或者null*/
        if (maps != null && maps.size() > 0) {
            /*说明有数据 查询运维邮箱那些*/
            List<Map<String, Object>> emailDept = yzAutomationCcMapper.findConcise(findMap);
            /*是否有地址或者是不是null*/
            if (emailDept != null && emailDept.size() > 0) {
                for (Map map : emailDept) {
                    //将map参数放入 cc_parameter

                    List emailDeptUrl = yzAutomationCcUrlMapper.findConcise(map);
                    if (emailDeptUrl != null && emailDeptUrl.size() > 0) {
                        String execution_template = map.get("execution_template").toString();
                        CcMessage_Map.put("count", Count);
                        CcMessage_Map.put("taskId", taskId);
                        if (execution_template.equals("1")) {
                            for (Object eM : emailDeptUrl) {
                                if (eM != null && eM.toString().length() > 0) {

                                    Map sendEmail = (Map<String, Object>) eM;
                                    String email = sendEmail.get("email").toString();
                                    Map<String, Object> saveHisMap = new HashMap<>();
                                    saveHisMap.put("cc_id", map.get("id"));
                                    saveHisMap.put("cc_url", "");
                                    saveHisMap.put("cc_email", email);
                                    saveHisMap.put("cc_state", "1");//已抄送

//                                    /*测试map放入 count   ui task_id 能否在 重试发送邮件中存在*/

//                                    HashMap<String, Object> reNeed = new HashMap<>();
//                                    reNeed.put("count",Count);
//                                    reNeed.put("taskId",taskId);
//                                    String cc_parameter = JSON.toJSONString(reNeed);
                                    String cc_parameter = JSON.toJSONString(map);
                                    //String cc_parameter = JSON.toJSONString(CcMessage_Map);
                                    cc_parameter = cc_parameter.length() > 500 ? cc_parameter.substring(0, 500) : cc_parameter;
                                    saveHisMap.put("cc_parameter", cc_parameter);
                                    Map<String, Object> Rmap = null;
                                    Boolean bool = false;
                                    String remark = "", cc_result = "0";
                                    try {
                                        Rmap = emailCc.ServiceOperation(CcMessage_Map, email);
                                        bool = (Boolean) Rmap.get("bool");
                                        if (bool) {
                                            cc_result = "1";
                                            log.info("消费者:邮件发送完毕");
                                        }
                                    } catch (Exception e) {
                                        remark = e.getMessage();
                                        remark = remark.length() > 240 ? remark.substring(0, 240) : remark;
                                    }
                                    saveHisMap.put("remark", remark);
                                    saveHisMap.put("cc_result", cc_result);
                                    boolean saveHis = yzAutomationCcHisMapper.save(saveHisMap) > 0;
                                    log.info(">>自动化 [EmailPassageway] 已抄送邮箱{}  - 插入抄送记录 bool   {} <<", sendEmail, saveHis);
                                } else {
                                    log.error(">>自动化 邮件抄送给运维===>>>失败:邮箱未获取到");
                                }
                            }
                        } else {
                            log.error("[自动化:邮件抄送给运维]==>>失败:信息模板不批对");
                        }
                    }

                }
            }
        }

    }


    /**
     * 邮件抄送 重试
     * <p>
     * cc_count 重试次数
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_emailServiceOperationCount_queue")
    public void RetryCC(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        System.out.println("测试===>>>参数msg:"+msg);
        Map<String, Object> Pmap = JSON.parseObject(msg);
        Integer cc_count = Integer.parseInt(Pmap.get("cc_count").toString());

        Map<String, Object> findNotPerformed_Map = new HashMap<>();
        findNotPerformed_Map.put("trigger_type", "12");
        findNotPerformed_Map.put("cc_count", cc_count);//重试次数
        List<Map<String, Object>> CcArr = yzAutomationCcHisMapper.findNotPerformed(findNotPerformed_Map);
        System.out.println("测试===>>>CcArr:" + CcArr);
        if (CcArr != null && CcArr.size() > 0) {
            //2.获取
            for (int i = 0; i < CcArr.size(); i++) {
                Map<String, Object> Cc_Map = CcArr.get(i);
                System.out.println("测试===>>>Cc_map:" + Cc_Map);

                Map<String, Object> MsgMap = JSON.parseObject(Cc_Map.get("cc_parameter").toString());
                System.out.println("测试===>>>MsgMap:" + MsgMap);

                String SendEmail = Cc_Map.get("cc_email").toString();
                Map<String, Object> updHisMap = new HashMap<>();
                updHisMap.put("id", Cc_Map.get("id"));
                Map<String, Object> Rmap = null;
                Boolean bool = false;
                String remark = "", cc_result = "0";
                try {
                    Rmap = emailCc.ServiceOperation(MsgMap, SendEmail);
                    System.out.println("测试===>>>进入try");
                    bool = (Boolean) Rmap.get("bool");
                    if (bool) {
                        cc_result = "1";
                    }
                } catch (Exception e) {
                    remark = e.getMessage();
                    System.out.println("测试===>>>进入catch");

//                    remark = remark.length() > 240 ? remark.substring(0, 240) : remark;
                }
                updHisMap.put("remark", remark);
                updHisMap.put("cc_result", cc_result);
                boolean updHis = yzAutomationCcHisMapper.updCcCount(updHisMap) > 0;
                System.out.println(">>自动化  [EmailPassageway]  已抄送邮箱" + SendEmail + "  - 抄送 bool   {" + bool + "}  修改 bool {" + updHis + "}<<");
            }
        }
    }

}
