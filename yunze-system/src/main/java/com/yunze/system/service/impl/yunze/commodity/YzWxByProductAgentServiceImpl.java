package com.yunze.system.service.impl.yunze.commodity;

import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductCategoryMapper;
import com.yunze.system.service.yunze.commodity.IYzWxByProductAgentService;
import com.yunze.system.service.yunze.commodity.IYzWxByProductCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class YzWxByProductAgentServiceImpl implements IYzWxByProductAgentService {

    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;


    @Override
    public List<Map<String, Object>> find(Map<String, Object> map) {
        return yzWxByProductAgentMapper.getDepIDArr(map);
    }

    @Override
    @Transactional
    public boolean edit(Map<String, Object> map) {
        yzWxByProductAgentMapper.delPAgent();//删除现有数据
        return yzWxByProductAgentMapper.save(map)>0;
    }
}
