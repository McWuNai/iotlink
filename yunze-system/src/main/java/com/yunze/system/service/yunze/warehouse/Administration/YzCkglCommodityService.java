package com.yunze.system.service.yunze.warehouse.Administration;

import java.util.List;
import java.util.Map;

public interface YzCkglCommodityService {

    /**查询商品信息*/
    public List<Map<String,Object>> commodity (Map map);

    /**载入查询*/
    public List<Map<String,Object>> queryloading (Map map);


    /**
     * 查询
     * @param map
     * @return
     */
    public Map<String,Object> selMap(Map map);



    /**
     * 新增
     * @param map
     * @return
     */
    public Map<String, Object> save(Map map);



    /**
     * 修改
     * @param map
     * @return
     */
    public boolean upd(Map map);


    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public Map<String,Object> find (Map map);


}
