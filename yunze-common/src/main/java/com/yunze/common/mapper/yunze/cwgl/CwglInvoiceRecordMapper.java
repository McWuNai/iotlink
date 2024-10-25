package com.yunze.common.mapper.yunze.cwgl;

import java.util.List;
import java.util.Map;

public interface CwglInvoiceRecordMapper {

    /**
     * 新增发票记录
     * @param map
     * @return
     */
    public int add_IRD(Map<String,Object> map);


    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


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
     * 查询单条发票信息
     * @param map
     * @return
     */
    public Map <String,Object> find_data(Map<String,Object> map);

    /**
     * 修改发票信息
     * @param map
     * @return
     */
    public int upd_IRD(Map<String,Object> map);

    /**
     * 通过合同编号修改发票信息
     * @param map
     * @return
     */
    public int upd_Ird_CtID(Map<String,Object> map);


    /**
     * 通过合同id查询 发票信息的  已开金额
     * @param map
     * @return
     */
    public String find_Ird_InvoicedAmount(Map<String,Object> map);


    /**
     * 删除发票信息
     * @param map
     * @return
     */
    public int del_IRD(Map<String,Object> map);



}
