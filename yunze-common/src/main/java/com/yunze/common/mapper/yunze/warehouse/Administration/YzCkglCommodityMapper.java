package com.yunze.common.mapper.yunze.warehouse.Administration;

import java.util.List;
import java.util.Map;

public interface YzCkglCommodityMapper {

    /**查询商品信息*/
    public List<Map<String,Object>> commodity (Map map);

    /**载入查询*/
    public List<Map<String,Object>> queryloading (Map map);


    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map map);

    /**
     * 查询 总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> selMap(Map map);

    /**
     * 修改
     * @param map
     * @return
     */
    public int upd(Map map);


    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


    /**
     * 是否存在同名 型号的商品
     * @param map
     * @return
     */
    public Integer isExist(Map map);


    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public Map<String,Object> find (Map map);

    /**
     *  查询详情查看
     */
    public List<Map<String,Object>> selId (Map map);

}
