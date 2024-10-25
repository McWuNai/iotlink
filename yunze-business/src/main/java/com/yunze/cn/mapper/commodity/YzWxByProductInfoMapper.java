package com.yunze.cn.mapper.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品信息表 信息
 */
public interface YzWxByProductInfoMapper {


    /**
     * 获取商品 上架、已审核
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);

    /**
     * 验证商品价格查询
     * @param map
     * @return
     */
    public Map<String, Object> findId(Map<String, Object> map);



}
