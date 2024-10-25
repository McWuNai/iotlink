package com.yunze.common.mapper.yunze.fhgl;

import java.util.List;
import java.util.Map;

public interface FhglExpressDeliveryStaffMapper {

    /**
     * 添加快递公司详情数据
     * @param map
     * @return
     */
    public int add_EDSF(Map<String,Object> map);

    /**
     * 删除快递公司详情信息
     * @param map
     * @return
     */
    public int del_EDSF(Map<String,Object> map);


    /**
     * 查询快递公司业务员详情数据
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_data(Map map);



}
