package com.yunze.common.mapper.yunze.fhgl.Ship;

import java.util.List;
import java.util.Map;

public interface FhglShippingApplicationFormalDetailsMapper {

    /**
     * 新增 发货申请正式合同发货信息详情
     * @param map
     * @return
     */
    public int add_SAFDS(Map<String,Object> map);

    /**
     *查询 发货申请正式合同发货信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_data(Map<String,Object> map);

    /**
     * 删除 正式合同发货信息详情
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
     *  出库  合同发货
     * */

    public List<Map<String,Object>> find_out (Map map);

    /**
     *  出库  普通发货
     * */

    public List<Map<String,Object>> find_arr (Map map);














}
