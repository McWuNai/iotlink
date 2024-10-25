package com.yunze.task.yunze.Automation.Link;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.utils.AutomationUtil;
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
 *
 */
@Slf4j
@Component
public class OrderCallbackMQ {

    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private AutomationUtil automationUtil;



    @RabbitHandler
    @RabbitListener(queues = "admin_updOrderAudit_queue")
    public void Listener(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> start_type = JSON.parseObject(msg);
        Map<String,Object> Pmap = (Map<String, Object>) start_type.get("Pmap");
        execution(Pmap);
    }


    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_updOrderAudit_queue")
    public void dlxListener(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> start_type = JSON.parseObject(msg);
        Map<String,Object> Pmap = (Map<String, Object>) start_type.get("Pmap");
        execution(Pmap);
    }



    public void execution(Map<String,Object> Pmap) {
        //查询有回调地址的订单信息
        List<Map<String, Object>> caArr =  yzOrderMapper.findCallbackAddress(Pmap);
        if(caArr!=null && caArr.size()>0){
            for (int i = 0; i < caArr.size(); i++) {
                Map<String, Object> caObj = caArr.get(i);
                String address = caObj.get("callback_address").toString();
                Map<String, Object> parameter = new HashMap<>();
                Map<String, Object> head = new HashMap<>();
                Map<String, Object> returnExecuteParameter = new HashMap<>();
                String returnExecute = "OrderCallback";
                String returnType = "String";

                parameter.put("iccid",caObj.get("iccid"));
                parameter.put("ordNo",caObj.get("ord_no"));
                parameter.put("auditStatus",caObj.get("is_audit"));
                address+="/iotlink/callback";//指定接口拼接
                returnExecuteParameter.put("id",caObj.get("id"));
                automationUtil.send("post",address,parameter,head,returnExecuteParameter,returnExecute,returnType);
//                if(address.indexOf("://127.0.0.1")==-1 && address.indexOf("://localhost")==-1){
////                address+="/iotlink/callback";//指定接口拼接
//                }else{
//                    log.error(" 请求地址包含 【://127.0.0.1】 或 【://localhost】 被拒接发送回调 {} , {}",caObj.get("ord_no"),address);
//                }

            }
        }
   }

}
