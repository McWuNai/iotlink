package com.yunze.web.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yunze.business.entity.WxConfig;
import com.yunze.business.service.impl.WxConfigServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.List;

@Component
public class WxfPayConfig {

    @Resource
    private WxConfigServiceImpl wxConfugService;

    public List<WxConfig> getWxConfigs(){
        QueryWrapper<WxConfig> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("status",1);
        List<WxConfig> configs=wxConfugService.list(queryWrapper);
        return configs;
    }
}
