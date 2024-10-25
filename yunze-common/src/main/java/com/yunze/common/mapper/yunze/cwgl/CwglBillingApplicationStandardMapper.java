package com.yunze.common.mapper.yunze.cwgl;

import java.util.List;
import java.util.Map;

public interface CwglBillingApplicationStandardMapper {

    /**
     * 新增开票申请_标配
     * @param map
     * @return
     */
    public int add_CBASD(Map<String,Object> map);

    /**
     * 查询开票申请信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_data(Map<String,Object> map);


    /**
     * 删除开票申请_标配
     * @param map
     * @return
     */
    public int del_all(Map<String,Object> map);





}
