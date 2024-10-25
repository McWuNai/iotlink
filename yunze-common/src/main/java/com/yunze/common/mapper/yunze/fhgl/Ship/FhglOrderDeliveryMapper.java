package com.yunze.common.mapper.yunze.fhgl.Ship;

import java.util.List;
import java.util.Map;

public interface FhglOrderDeliveryMapper {

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
     * 新增 订单合同发货信息
     * @param map
     * @return
     */
    public int add_ODY(Map<String,Object> map);


    /**
     * 修改 订单合同发货信息
     * @param map
     * @return
     */
    public int upd_ODY(Map<String,Object> map);

    /**
     * 删除 订单合同发货信息
     * @param map
     * @return
     */
    public int del_ODY(Map<String,Object> map);


    /**
     * 查询未完成的临时订单合同ID
     * @param map
     * @return
     */
    public List<Map <String,Object>> sel_TCT_ID(Map<String,Object> map);


    /**
     * 查询未完成的销售合同ID
     * @param map
     * @return
     */
    public List<Map <String,Object>> sel_CT_ID(Map<String,Object> map);

    /**
     * 修改 合同订单发货状态
     * @param map
     * @return
     */
    public int upd_Order_Delivery(Map<String,Object> map);































}
