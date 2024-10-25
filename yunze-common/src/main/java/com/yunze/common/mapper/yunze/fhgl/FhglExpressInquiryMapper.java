package com.yunze.common.mapper.yunze.fhgl;

import java.util.List;
import java.util.Map;

public interface FhglExpressInquiryMapper {

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
     * 修改时间、状态
     * @param map
     * @return
     */
    public int upd_EIY(Map<String,Object> map);


    /**
     * 查询快递查询详情
     * @param map
     * @return
     */
    public Map <String,Object> find_data(Map<String,Object> map);


    /**
     * 新增快递查询信息
     * @param map
     * @return
     */
    public int add_EIY(Map<String,Object> map);

    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


    /**
     * 修改快递查询详情发货方式数据
     * @param map
     * @return
     */
    public int upd_EIY_data(Map<String,Object> map);

    /**
     * 删除快递查询信息
     * @param map
     * @return
     */
    public int del_EIY(Map<String,Object> map);


    /**
     * 修改所属发货申请ID
     * @param map
     * @return
     */
    public int upd_SANID(Map<String,Object> map);


    /**
     * 按时间段查询 快递ID
     * @param map
     * @return
     */
    public List<String> find_ID(Map<String,Object> map);


    /**
     * 查询快递 类型分组
     * @param map
     * @return
     */
    public List<Map <String,Object>> groupCount(Map<String,Object> map);













}
