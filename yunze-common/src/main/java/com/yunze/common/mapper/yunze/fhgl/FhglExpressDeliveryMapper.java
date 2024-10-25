package com.yunze.common.mapper.yunze.fhgl;

import java.util.List;
import java.util.Map;

public interface FhglExpressDeliveryMapper {

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
     * 查询快递公司详情数据
     * @param map
     * @return
     */
    public Map <String,Object> find_data(Map<String,Object> map);


    /**
     * 修改快递公司数据
     * @param map
     * @return
     */
    public int upd_EDY(Map<String,Object> map);


    /**
     * 添加快递公司数据
     * @param map
     * @return
     */
    public int add_EDY(Map<String,Object> map);


    /**
     * 删除快递公司信息
     * @param map
     * @return
     */
    public int del_EDY(Map<String,Object> map);


    /**
     * 查询快递公司ID及名称
     * @return
     */
    public List<Map <String,Object>> sel_date();


}
