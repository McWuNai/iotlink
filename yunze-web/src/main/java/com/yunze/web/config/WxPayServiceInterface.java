package com.yunze.web.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.yunze.business.entity.WxConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Configuration
@ConditionalOnClass(WxPayService.class)
public class WxPayServiceInterface  {

    public static Map<String,WxPayService> wxPayServiceMap =new ConcurrentHashMap<>();

    @Resource
    private WxfPayConfig wxfPayConfig;

    /**
     * return: 获取所有配置
     */
    public List<WxPayConfig> queryConfigs(){
        List<WxConfig> list=wxfPayConfig.getWxConfigs();
        List<WxPayConfig> configs =new ArrayList<>();
        for(int i=0;i<list.size();i++){
            WxConfig wxConfig =list.get(i);
            WxPayConfig wxPayConfig =new WxPayConfig();
            wxPayConfig.setAppId(wxConfig.getAppId());
            wxPayConfig.setMchId(wxConfig.getMchId());
            wxPayConfig.setMchKey(wxConfig.getPaternerKey());
            wxPayConfig.setTradeType("JSAPI");
            configs.add(wxPayConfig);
        }
        return configs;
    }

    /**
     * return: 获取支付的weixinpay
     */
    public WxPayService getWxPayService(String appId){
        return wxPayServiceMap.get(appId);
    }

    public void afterPropertiesSet() throws Exception {
        List<WxPayConfig> configs= queryConfigs();
        for(int i=0;i<configs.size();i++){
            WxPayService wxPayService=new WxPayServiceImpl();
            wxPayService.setConfig(configs.get(i));
            wxPayServiceMap.put(configs.get(i).getAppId(),wxPayService);
        }
        System.out.println(wxPayServiceMap);
    }
}
