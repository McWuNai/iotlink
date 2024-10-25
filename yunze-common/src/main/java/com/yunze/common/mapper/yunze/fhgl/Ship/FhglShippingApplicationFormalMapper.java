package com.yunze.common.mapper.yunze.fhgl.Ship;

import java.util.List;
import java.util.Map;

public interface FhglShippingApplicationFormalMapper {

    /**
     * 新增 发货申请正式合同发货信息
     * @param map
     * @return
     */
    public int add_SAFL(Map<String,Object> map);






    /**
     *查询 发货申请正式合同发货信息
     * @param map
     * @return
     */
    public Map <String,Object> find_data(Map<String,Object> map);

    /**
     * 删除 临时订单发货信息详情
     * @param map
     * @return
     */
    public int del_date(Map<String,Object> map);


    /**
     * 修改  正式合同发货信息
     * @param map
     * @return
     */
    public int upd_date(Map<String,Object> map);

    /**
     * 查询商品已发货数量
     * @param map
     * @return
     */
    public List<Map <String,Object>> sel_SUM_Sent_Quantity(Map<String,Object> map);

    /**
     * 查询 合同编号下的 所属发货申请 ID
     * @param map
     * @return
     */
    public List<String> sel_SAN_ID(Map<String,Object> map);


    public List<Map <String,Object>> Sle_ODYContractID(Map map);





























}
