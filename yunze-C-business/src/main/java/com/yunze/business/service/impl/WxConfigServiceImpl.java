package com.yunze.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunze.business.dao.WxConfigMapper;
import com.yunze.business.entity.WxConfig;
import com.yunze.business.service.WxConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class WxConfigServiceImpl extends ServiceImpl<WxConfigMapper, WxConfig> implements WxConfigService {

    @Resource
    private WxConfigMapper wxConfigMapper;


}
