package com.yunze.business.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunze.business.entity.WxUser;

public interface WxUserService extends IService<WxUser> {

    int getCountOfWechatUser(String openid);


}
