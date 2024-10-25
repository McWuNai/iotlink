package com.yunze.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunze.business.dao.WxUserMapper;
import com.yunze.business.entity.WxUser;
import com.yunze.business.service.WxUserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements WxUserService {

    @Resource
    private WxUserMapper wxUserMapper;

    @Override
    public int getCountOfWechatUser(@Param("openid") String openid) {
        QueryWrapper<WxUser> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("openid",openid);
        return wxUserMapper.selectCount(queryWrapper);
    }






}
