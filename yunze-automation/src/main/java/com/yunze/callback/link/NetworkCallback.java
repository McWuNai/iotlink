package com.yunze.callback.link;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.utils.ReturnExecuteFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 网络连接回调
 */
@Slf4j
@Component
public class NetworkCallback {


    @Resource
    private ReturnExecuteFunction returnExecuteFunction;

    @RabbitHandler
    @RabbitListener(queues = "admin_automationNetworkCallback_queue", containerFactory = "customContainerFactory")
    public void AutomationNetworkCallback(String msg, Channel channel_1) {
        if (msg != null && msg.length() > 0) {
            execution(msg);
        }
    }


    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_automationNetworkCallback_queue", containerFactory = "customContainerFactory")
    public void DlxAutomationNetworkCallback(String msg, Channel channel_1) {
        if (msg != null && msg.length() > 0) {
            execution(msg);
        }
    }

    public void execution(String msg) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> pMap = JSON.parseObject(msg);
            Map<String, Object> parameter = (Map<String, Object>) pMap.get("parameter");// 参数
            Map<String, Object> returnExecuteParameter = (Map<String, Object>) pMap.get("returnExecuteParameter");// 返回执行函数名参数
            Map<String, String> head = (Map<String, String>) pMap.get("head");// 头部
            String address = pMap.get("address").toString();// 请求地址
            String type = pMap.get("type").toString();// 请求方式
            String returnExecute = pMap.get("returnExecute").toString();// 返回执行函数名
            String returnType = pMap.get("returnType").toString();// 返回 类型

            if (type.equalsIgnoreCase("get")) {
                if (returnType.equalsIgnoreCase("String")) {
                    String Str = HttpUtil.get(address);
                    if (Str != null && Str.length() > 0) {
                        if (returnExecute.equalsIgnoreCase("OrderCallback")) {
                            returnExecuteFunction.OrderCallback(Str, returnExecuteParameter);
                        }
                    }
                }
            } else if (type.equalsIgnoreCase("post")) {
                if (returnType.equalsIgnoreCase("String")) {
                    String Str = HttpUtil.post(address, JSON.toJSONString(parameter), head);
                    if (Str != null) {
                        if (returnExecute.equalsIgnoreCase("OrderCallback")) {
                            returnExecuteFunction.OrderCallback(Str, returnExecuteParameter);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(">>错误 - admin_automationNetworkCallback_queue 消费者:{} , {}<<", e.getMessage(), msg);

        }
    }
}
