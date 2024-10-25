package com.yunze.common.mapper.yunze.cwgl;

import java.util.List;
import java.util.Map;

public interface CwglBillingApplicationMapper {

    /**
     * 新增 订单合同发货信息
     * @param map
     * @return
     */
    public int add_BAN(Map<String,Object> map);



    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


    /**
     * 查询开票申请信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_data(Map<String,Object> map);

    /**
     * 删除开票申请信息
     * @param map
     * @return
     */
    public int del_all(Map<String,Object> map);

    /**
     * 修改开票申请
     * @param map
     * @return
     */
    public int upd_BAN(Map<String,Object> map);

    /**
     * 查询所属开票申请信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_BAN_ID(Map<String,Object> map);

    /**
     * 查询合同所属开票总计
     * @param map
     * @return
     */
    public String find_Sum_TotalBilling(Map<String,Object> map);


    /**
     * 查询发票申请ID下的开票
     * @param map
     * @return
     */
    public List<String> find_BAN_ID_arr(Map<String,Object> map);



}
