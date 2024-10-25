package com.yunze.common.mapper.yunze.xsgl.Contract;

import java.util.List;
import java.util.Map;

public interface YzXsglContractMapper {

    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map map);

    /**
     * 查询 总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);


    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> selMap(Map map);


    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public Map <String,Object> find(Map map);


    /**
     * 基础修改
     * @param map
     * @return
     */
    public int upd(Map map);



    /**
     * 合同状态 = 删除
     * @param map
     * @return
     */
    public int delId(Map map);


    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();



    /**
     * 日、月 折线图
     * @param map
     * @return
     */
    public List<Map<String,Object>> getPolyline(Map<String,Object> map);


    /**2022年8月18日15:46:24
     * 本月合同日入款金额
     */
    public Map<String,Object> getContractPaymentAmountOfThisMonth(Map<String,Object> map);




}
