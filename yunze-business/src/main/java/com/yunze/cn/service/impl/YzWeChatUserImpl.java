package com.yunze.cn.service.impl;

import com.yunze.cn.mapper.YzWeChatUserMapper;
import com.yunze.cn.service.IYzWeChatUser;

import javax.annotation.Resource;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/25/14:11
 * @Description:
 */
@Component
public class YzWeChatUserImpl implements IYzWeChatUser {

    @Resource
    private  YzWeChatUserMapper yzWeChatUserMapper;

    @Override
    public int save(Map map) {
        return yzWeChatUserMapper.save(map);
    }

    @Override
    public int upd(Map map) {
        return yzWeChatUserMapper.upd(map);
    }

    @Override
    public Map<String, Object> find(Map map) {
        return yzWeChatUserMapper.find(map);
    }
}
