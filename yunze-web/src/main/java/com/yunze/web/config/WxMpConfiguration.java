package com.yunze.web.config;

import com.yunze.business.entity.WxConfig;
import com.yunze.business.service.impl.WxConfigServiceImpl;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.constant.WxMpEventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Configuration
@ConditionalOnClass(WxMpService.class)
public class WxMpConfiguration {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private WxConfigServiceImpl wxConfigService;


   @Bean
    public WxMpService wxMpService(){
       return new WxMpServiceImpl();
   }




}