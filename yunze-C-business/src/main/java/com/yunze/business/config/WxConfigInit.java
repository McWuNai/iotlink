package com.yunze.business.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yunze.business.entity.WxConfig;
import com.yunze.business.service.impl.WxConfigServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WxConfigInit {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private WxConfigServiceImpl wxConfigService;

    /**
     * auther:
     * return:
     * 描述： 获取所有的公众号
     * 时间： 2021/12/14 9:40
     */
    public List<WxConfig> getConfigs(){
        List<WxConfig> configs= wxConfigService.list(new QueryWrapper<WxConfig>()
                .eq("status",1));
        if(configs.size()>0){
            return configs;
        }
        return null;
    }

    public WxMpService wxMpService() throws WxErrorException {
        final List<WxConfig> configs=this.getConfigs();
        if(null==configs){
            System.out.println("没有获取到公众号信息:");
            return new WxMpServiceImpl();
        }
        WxMpService wxMpService=new WxMpServiceImpl();
        wxMpService.setMultiConfigStorages(configs
                .stream().map(a -> {
                    WxMpDefaultConfigImpl configStorage;
                    configStorage = new WxMpDefaultConfigImpl();

                    configStorage.setAppId(a.getAppId());
                    configStorage.setSecret(a.getAppSecret());
                    configStorage.setToken(a.getAppToken());
                    configStorage.setAesKey(a.getPaternerKey());
                    log.info("当前configStorage里面有：{}",configStorage.getAppId());
                    return configStorage;
                }).collect(Collectors.toMap(WxMpDefaultConfigImpl::getAppId, a -> a, (o, n) -> o)));
        return wxMpService;
    }
}
