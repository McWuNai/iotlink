package com.yunze.system.service.yunze.commodity;

import java.util.List;
import java.util.Map;

public interface IYzWxByProductCategoryService {

    /**
     * 查询商品分类 信息
     * @param map
     * @return
     */
    public List<Map<String, Object>> selMap(Map<String, Object> map);


    /**
     * 新增
     * @param map
     * @return
     */
    public boolean save(Map<String, Object> map);



    /**
     * 修改
     * @param map
     * @return
     */
    public boolean upd(Map<String, Object> map);



    /**
     * 获取所有 商品分类
     * @param map
     * @return
     */
    public List<Map<String, Object>> getCategory(Map<String, Object> map);



}
