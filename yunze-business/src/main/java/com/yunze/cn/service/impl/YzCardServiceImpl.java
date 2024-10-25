package com.yunze.cn.service.impl;




import com.yunze.cn.mapper.YzCardMapper;
import com.yunze.cn.service.IYzCardService;

import javax.annotation.Resource;
import java.util.*;

import org.springframework.stereotype.Component;

/**
 * 卡信息 业务实现类
 * 
 * @author root
 */
@Component
public class YzCardServiceImpl implements IYzCardService
{

    @Resource
    private YzCardMapper yzCardMapper;




    @Override
    public Map<String, Object> findIccid(Map<String, Object> map) {
        return  yzCardMapper.findIccid(map);
    }

    @Override
    public String findOperatorType(Map<String, Object> map) {
        return yzCardMapper.findOperatorType(map);
    }





}