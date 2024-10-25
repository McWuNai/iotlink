package com.yunze.cn.mapper.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品图片信息表 信息
 */
public interface YzWxByProductPicInfoMapper {


    /**
     * 查询有效图片
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);





}
