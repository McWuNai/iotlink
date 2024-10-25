package com.yunze.utils;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.mysql.YzOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 回调执行函数
 */
@Component
@Slf4j
public class ReturnExecuteFunction {


    @Resource
    private YzOrderMapper yzOrderMapper;

    /**
     * API充值》订单回调 通知 【回调结果解析】
     *
     * @param str
     */
    public void OrderCallback(String str, Map<String, Object> returnExecuteParameter) {
        try {
            int callback_status = 0;//回调是否成功 1 = 成功
            if (str != null) {
                Map<String, Object> pMap = JSON.parseObject(str);
                String status = pMap.get("status").toString();
                if (status.equals("0")) {//成功
                    callback_status = 1;
                }
            }
            String callback_info = str.length() > 500 ? str.substring(0, 500) : str;
            returnExecuteParameter.put("callback_status", callback_status);
            returnExecuteParameter.put("callback_info", callback_info);
            yzOrderMapper.updOrderCallback(returnExecuteParameter);
        } catch (Exception e) {
            log.error("API充值》订单回调 通知 【回调结果解析】 错误 {} , {}", str, returnExecuteParameter);
        }
    }


}
