package com.yunze.common.mapper.yunze.fhgl.Ship;

import java.util.List;
import java.util.Map;

public interface FhglShippingApplicationInvoiceMapper {

    /**
     * 新增 发货申请发票发货信息
     * @param map
     * @return
     */
    public int add_SAIE(Map<String,Object> map);






    /**
     *查询 发票发货信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_data(Map<String,Object> map);

    /**
     * 删除 临时订单发货信息详情
     * @param map
     * @return
     */
    public int del_date(Map<String,Object> map);


    /**
     * 修改  发票发货信息
     * @param map
     * @return
     */
    public int upd_date(Map<String,Object> map);


    /**
     * 修改所属发货申请ID
     * @param map
     * @return
     */
    public int upd_SANID(Map<String,Object> map);





}
