package com.yunze.task.yunze.Automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcUrlMapper;
import com.yunze.common.utils.Email.EmailCc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class EmailCardConsumptionMQ {

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

    /**
     * 邮件抄送
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_CardConsumption_queue")
    public void CardConsumption() {
        Map<String, Object> findMap = new HashMap<>();
        findMap.put("trigger_type", "7"); //自动化触发类型 未划分资费组
        Map<String, Object> CcMessage_Map = new HashMap<>();

        Map<String, Object> findCardMap = new HashMap<>();
        List<String> agent_idArr = new ArrayList<>();
        agent_idArr.add("100");
        //findCardMap.put("agent_id",agent_idArr);//公司所属

        findCardMap.put("dimensionField", "1"); //
        findCardMap.put("dimensionType", "2"); //
        findCardMap.put("dimensionValue", "0.0"); //

        List<Map<String, Object>> AutomationCcArr = yzAutomationCcMapper.findConcise(findMap);//获取自动化 抄送 组
        if (AutomationCcArr != null && AutomationCcArr.size() > 0) {


            for (int i = 0; i < AutomationCcArr.size(); i++) {
                Map<String, Object> CCObj = AutomationCcArr.get(i);


                if (CCObj.get("dep_id") != null) {
                    String dep_id = CCObj.get("dep_id").toString();
                    if (!dep_id.equals("100")) {
                        agent_idArr.add(dep_id);
                    }
                }
                List<String> operationArr = new ArrayList<>();
                if (CCObj.get("operation_type") != null) {
                    String arr[] = CCObj.get("operation_type").toString().split(",");
                    for (int j = 0; j < arr.length; j++) {//java string数组 添加到list
                        String obj = arr[j];
                        if (obj.length() > 0) {
                            operationArr.add(obj);
                        }
                    }
                }

                List<String> channelArr = new ArrayList<>();
                if (CCObj.get("channel_id") != null) {
                    String lst[] = CCObj.get("channel_id").toString().split(",");
                    for (int j = 0; j < lst.length; j++) {//java string数组 添加到list
                        String obj = lst[j];
                        if (obj.length() > 0) {
                            channelArr.add(obj);
                        }
                    }
                }
                if (operationArr.size() > 0) {
                    findCardMap.put("operation_type", operationArr);
                    findCardMap.put("cd_operator_type", operationArr);
                }
                if (channelArr.size() > 0) {
                    findCardMap.put("channel_id", channelArr);
                    findCardMap.put("cArr", channelArr);
                }
                findCardMap.put("agent_id", agent_idArr);

                //删除 发送用不到的 key 赋值 前端 选中树控件key
                findCardMap.remove("operation_type");
                //findCardMap.remove("agent_id");

                findCardMap.put("trreAgent_id", agent_idArr);


                Integer Count = yzCardMapper.selMapCount(findCardMap);//查询总数

                if (Count != null && Count > 0) {
                    List<Map<String, Object>> AutomationCcUrlArr = yzAutomationCcUrlMapper.findConcise(CCObj);//获取自动化 抄送 邮箱
                    if (AutomationCcUrlArr != null && AutomationCcUrlArr.size() > 0) {
                        String execution_template = CCObj.get("execution_template").toString();

                        CcMessage_Map.put("Count", Count);
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
                                        CcMessage_Map.put("findCardMap", findCardMap);
                                        Rmap = emailCc.CardConsumption_default(CcMessage_Map, SendEmail);
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
                                    log.info(">>自动化 [CardConsumption] 已抄送邮箱{}  - 插入抄送记录 bool   {} <<", SendEmail, saveHis);
                                } else {
                                    log.error(">>自动化 [CardConsumption] 抄送邮箱未获取到  - 抄送取消  {} <<", eM);
                                }
                            }
                        } else {
                            log.error(">>自动化 [CardConsumption] 抄送模板不批对  - 抄送取消  {} <<");
                        }
                    } else {
                        log.error(">>自动化 [CardConsumption] 获取自动化 抄送 邮箱 未找到数据 - 抄送取消  {} <<");
                    }


                }
            }
        }


    }


}
