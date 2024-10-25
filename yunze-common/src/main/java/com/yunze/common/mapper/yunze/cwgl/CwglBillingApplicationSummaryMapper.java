package com.yunze.common.mapper.yunze.cwgl;

import java.util.List;
import java.util.Map;

public interface CwglBillingApplicationSummaryMapper {

    /**
     * 新增开票申请_发票汇总信息
     * @param map
     * @return
     */
    public int add_CBASY(Map<String,Object> map);


    /**
     * 查询所属开票申请_发票汇总信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_data(Map<String,Object> map);

    /**
     * 删除开票申请_发票汇总信息
     * @param map
     * @return
     */
    public int del_all(Map<String,Object> map);




}
