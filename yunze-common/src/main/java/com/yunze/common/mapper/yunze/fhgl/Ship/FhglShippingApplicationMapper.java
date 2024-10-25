package com.yunze.common.mapper.yunze.fhgl.Ship;

import java.util.List;
import java.util.Map;

public interface FhglShippingApplicationMapper {

    /**
     * 新增 发货申请
     * @param map
     * @return
     */
    public int add_SAN(Map<String,Object> map);


    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String,Object> map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> selMap(Map<String,Object> map);


    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


    /**
     * 查询 发货申请
     * @param map
     * @return
     */
    public Map <String,Object> find_data(Map<String,Object> map);

    /**
     * 修改 发货申请
     * @param map
     * @return
     */
    public int upd_SAN(Map<String,Object> map);


    /**
     * 删除发货申请
     * @param map
     * @return
     */
    public int del_SAN(Map<String,Object> map);


    /**
     * 删除一批发货申请
     * @param map
     * @return
     */
    public int del_SANID(Map<String,Object> map);


    /**
     * 查询 单号
     * */
    public List<Map<String,Object>> sleNumber(Map map);


    /**
     * 出库修改状态 已出库
     * */
    public int normalUpdate(Map<String,Object> map);




}
