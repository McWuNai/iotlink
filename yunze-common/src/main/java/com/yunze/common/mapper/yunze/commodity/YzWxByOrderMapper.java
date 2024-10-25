package com.yunze.common.mapper.yunze.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品购买 订单发货表 信息
 */
public interface YzWxByOrderMapper {





    /**
     * 新增
     * @return
     */
    public int save(Map<String,Object> map);


    /**
     * 获取 订单发货表 简要
     * @return
     */
    public Map<String, Object> findConcise(Map<String, Object> map);


    /**
     * 查询发货订单
     * @return
     */
    public Map<String, Object> findWxOrder(Map<String, Object> map);


    /**导出*/

    public List<Map<String,Object>> shopping_export(Map map);
}
