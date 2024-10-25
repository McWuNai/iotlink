package com.yunze.cn.config;

import com.yunze.cn.entity.WxConfig;
import com.yunze.cn.mapper.YzWxConfigMapper;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class YzWxConfigInit {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private YzWxConfigMapper yzWxConfigMapper;

    /**
     * auther:
     * return:
     * 描述： 获取所有的公众号
     * 时间： 2021/12/14 9:40
     */
    public List<WxConfig> getConfigs(){

        List<Map<String, Object>> list = yzWxConfigMapper.getList();
        List<WxConfig> configs = new ArrayList<>();
        if(list!=null && list.size()>0){
            for (int i = 0; i <list.size() ; i++) {
                configs.add(new WxConfig(list.get(i)));
            }
            if(configs.size()>0){
                return configs;
            }
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
