package com.yunze.cn.mapper.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品购买代理关联表 信息
 */
public interface YzWxByProductAgentMapper {


    /**
     * 获取 商品购买代理关联
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);


    /**
     * 查询运费配置
     * @return
     */
    public String findByCardFare();



}
