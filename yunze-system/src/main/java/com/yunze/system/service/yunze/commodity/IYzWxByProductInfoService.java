package com.yunze.system.service.yunze.commodity;

import java.util.List;
import java.util.Map;

public interface IYzWxByProductInfoService {

    /**
     * 获取商品 名称
     * @return
     */
    public List<Map<String, Object>> findName(Map<String, Object> map);


    /**
     * 查询
     * @param map
     * @return
     */
    public Map<String, Object> selMap(Map<String, Object> map);

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
    public boolean edit(Map<String, Object> map);


    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public Map<String, Object> findData(Map<String, Object> map);



}
