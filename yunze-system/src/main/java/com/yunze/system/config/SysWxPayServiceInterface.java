package com.yunze.system.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.yunze.common.mapper.yunze.wechat.YzWxConfigMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Configuration
@ConditionalOnClass(WxPayService.class)
public class SysWxPayServiceInterface {

    public static Map<String,WxPayService> wxPayServiceMap =new ConcurrentHashMap<>();

    @Resource
    private YzWxConfigMapper yzWxConfigMapper;

    /**
     * return: 获取所有配置
     */
    public List<WxPayConfig> queryConfigs(){
        List<Map <String,Object>> list = yzWxConfigMapper.getWxConfigs();
        List<WxPayConfig> configs =new ArrayList<>();
        for(int i=0;i<list.size();i++){
            Map <String,Object> wxConfig =list.get(i);
            WxPayConfig wxPayConfig = new WxPayConfig();
            wxPayConfig.setAppId(wxConfig.get("app_id").toString());
            wxPayConfig.setMchId(wxConfig.get("mch_id").toString());
            wxPayConfig.setMchKey(wxConfig.get("paterner_key").toString());
            wxPayConfig.setTradeType("NATIVE");
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
