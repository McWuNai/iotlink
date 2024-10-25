package com.yunze.cn.mapper.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品分类表 信息
 */
public interface YzWxByProductCategoryMapper {


    /**
     * 获取商品分类 分类 1 级 状态可看
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);






}
