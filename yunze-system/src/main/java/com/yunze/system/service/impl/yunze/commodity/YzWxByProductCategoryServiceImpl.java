package com.yunze.system.service.impl.yunze.commodity;

import com.yunze.common.mapper.yunze.commodity.YzWxByProductCategoryMapper;
import com.yunze.system.service.yunze.commodity.IYzWxByProductCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class YzWxByProductCategoryServiceImpl implements IYzWxByProductCategoryService {

    @Resource
    private YzWxByProductCategoryMapper yzWxByProductCategoryMapper;


    @Override
    public List<Map<String, Object>> selMap(Map<String, Object> map) {
        return yzWxByProductCategoryMapper.selMap(map);
    }

    @Override
    public boolean save(Map<String, Object> map) {
        return yzWxByProductCategoryMapper.save(map)>0;
    }

    @Override
    public boolean upd(Map<String, Object> map) {
        return yzWxByProductCategoryMapper.upd(map)>0;
    }

    @Override
    public List<Map<String, Object>> getCategory(Map<String, Object> map) {
        return yzWxByProductCategoryMapper.getCategory(map);
    }

}
