package com.yunze.common.mapper.yunze.fhgl;

import java.util.List;
import java.util.Map;

public interface FhglExpressInquiryDetailsMapper {

    /**
     * 新增快递信息
     * @param map
     * @return
     */
    public int add_EIDS(Map<String,Object> map);


    /**
     * 查询快递查询详情
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_data(Map map);

    /**
     * 删除快递查询详情信息
     * @param map
     * @return
     */
    public int del_EIDS(Map<String,Object> map);

    /**
     * 查询 出库需导入的商品
     * @param map
     * @return
     */
    public List<String> find_Name_arr(Map map);

    /**
     * 出库 发货单 导入 普通发货 2
     * @param map
     * @return
     */
    public List<Map <String,Object>> Imput_PT(Map map);


    /**
     * 传入 快递名称  查询销售商品 信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> Imput_Spe(Map map);





}
