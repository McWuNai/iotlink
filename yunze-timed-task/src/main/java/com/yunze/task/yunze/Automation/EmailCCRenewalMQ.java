package com.yunze.task.yunze.Automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcUrlMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryMapper;
import com.yunze.common.utils.Email.EmailCc;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 自动化 邮件抄送 [续费申请]
 *
 * @author userzf
 */
@Slf4j
@Component
public class EmailCCRenewalMQ {

    @Resource
    private YzAutomationCcHisMapper yzAutomationCcHisMapper;
    @Resource
    private EmailCc emailCc;
    @Resource
    private YzApplicationforRenewalPrimaryMapper yzApplicationforRenewalPrimaryMapper;
    @Resource
    private YzAutomationCcMapper yzAutomationCcMapper;

    @Resource
    private YzAutomationCcUrlMapper yzAutomationCcUrlMapper;



    @RabbitHandler
    @RabbitListener(queues = "admin_emailCCRenewal_queue")
    public void CCRenewal(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer cc_count = Integer.parseInt(Pmap.get("cc_count").toString());
        String type = Pmap.get("type").toString();

        if(type.equals("CCExecution")){
            CCExecution(cc_count);
        }else if(type.equals("RetryCC")){
            RetryCC(cc_count);
        }
    }









    public void CCExecution(Integer parameter) {

        Map<String, Object> findMap = new HashMap<>();
        findMap.put("trigger_type", "11"); //自动化触发类型 续费通知

        List<Map<String, Object>> sendInfoList = yzApplicationforRenewalPrimaryMapper.findEmailCC(null);

        List<Map<String, Object>> AutomationCcArr = yzAutomationCcMapper.findConcise(findMap);//获取自动化 抄送 组
        if (sendInfoList != null && sendInfoList.size() > 0 &&  AutomationCcArr != null && AutomationCcArr.size() > 0) {

            for (int k = 0; k < sendInfoList.size(); k++) {//抄送需要抄送的续费申请数组
                Map<String, Object> sendInfo = sendInfoList.get(k);

                Map<String, Object> CcMessage_Map = new HashMap<>();
                Map<String, Object> parMap = new HashMap<>();
                parMap.put("id",sendInfo.get("id"));

                CcMessage_Map.put("parMap",parMap);
                CcMessage_Map.put("card_sumCount",sendInfo.get("card_sumCount"));
                CcMessage_Map.put("amount",sendInfo.get("amount"));
                CcMessage_Map.put("info",sendInfo.get("info"));
                CcMessage_Map.put("create_time",sendInfo.get("create_time"));
                CcMessage_Map.put("dept_name",sendInfo.get("dept_name"));
                CcMessage_Map.put("title","点击查看详情");


                for (int i = 0; i < AutomationCcArr.size(); i++) {//抄送认循环
                    Map<String, Object> CCObj = AutomationCcArr.get(i);
                    List<Map<String, Object>> AutomationCcUrlArr = yzAutomationCcUrlMapper.findConcise(CCObj);//获取自动化 抄送 邮箱

                    if (AutomationCcUrlArr != null && AutomationCcUrlArr.size() > 0) {
                        String execution_template = CCObj.get("execution_template").toString();

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

                                    String cc_parameter = JSON.toJSONString(CcMessage_Map);
                                    cc_parameter = cc_parameter.length() > 500 ? cc_parameter.substring(0, 500) : cc_parameter;
                                    saveHisMap.put("cc_parameter", cc_parameter);
                                    Map<String, Object> Rmap = null;
                                    Boolean bool = false;
                                    String remark = "", cc_result = "0";
                                    try {
                                        Rmap = emailCc.ApplicationForRenewal_default(CcMessage_Map, SendEmail);
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
                                    log.info(">>自动化 [EmailCCRenewal] 已抄送邮箱{}  - 插入抄送记录 bool   {} <<", SendEmail, saveHis);
                                } else {
                                    log.error(">>自动化 [EmailCCRenewal] 抄送邮箱未获取到  - 抄送取消  {} <<", eM);
                                }
                            }
                        } else {
                            log.error(">>自动化 [EmailCCRenewal] 抄送模板不批对  - 抄送取消  {} <<");
                        }
                    } else {
                        log.error(">>自动化 [EmailCCRenewal] 获取自动化 抄送 邮箱 未找到数据 - 抄送取消  {} <<");
                    }
                }

            }


        }
    }


    /**
     * 邮件抄送 重试
     * @param cc_count 重试次数
     */
    public void RetryCC(Integer cc_count) {
        Map<String, Object> findNotPerformed_Map = new HashMap<>();
        findNotPerformed_Map.put("trigger_type", "11");
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
                    Rmap = emailCc.ApplicationForRenewal_default(MsgMap, SendEmail);
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
                System.out.println(">>自动化  [EmailCCRenewal]  已抄送邮箱" + SendEmail + "  - 抄送 bool   {" + bool + "}  修改 bool {" + updHis + "}<<");
            }
        }
    }




}
