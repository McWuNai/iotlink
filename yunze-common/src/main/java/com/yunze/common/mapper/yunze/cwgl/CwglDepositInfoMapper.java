package com.yunze.common.mapper.yunze.cwgl;

import java.util.List;
import java.util.Map;

public interface CwglDepositInfoMapper {

    /**
     * 新增 入款信息(默认：未到款，入款合计为 ：0)
     * @param map
     * @return
     */
    public int add_CDIO(Map<String,Object> map);

    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


    /**
     * 修改入款合计
     * @param map
     * @return
     */
    public int upd_Total(Map<String,Object> map);

    /**
     * 通过合同编号 →删除入款信息
     * @param map
     * @return
     */
    public int del_CDIO_Contract(Map<String,Object> map);


    /**
     * 通过合同ID查询 入款记录ID
     * @param map
     * @return
     */
    public List<Map <String,Object>> fnid_Dio_ID(Map<String,Object> map);

    /**
     * 查询销售合同入款记录基本信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> sel_CT_data(Map<String,Object> map);


    /**
     *查询销售合同入款记录基本信息总数
     * @param map
     * @return
     */
    public Integer sel_CT_data_Count(Map<String,Object> map);

    /**
     * 查询单条正式合同入款记录信息
     * @param map
     * @return
     */
    public Map <String,Object> find_Ct(Map<String,Object> map);

    /**
     * 修改未到款 ，与入款状态
     * @param map
     * @return
     */
    public int upd_Unpaid(Map<String,Object> map);


    /**
     * 传入 未到款 ，所属合同ID 查询 当前的入款与未到款比较【用于未到款更新查询】
     * @param map
     * @return
     */
    public Map <String,Object> sel_Dio_DsID(Map<String,Object> map);


    /**
     * 修改入款信息
     * @param map
     * @return
     */
    public int upd_CDIO(Map<String,Object> map);


    /**
     * 入款金额年份数据查询[销售合同]
     * @param map
     * @return
     */
    public List<Map <String,Object>> sel_Year_data_CT(Map<String,Object> map);

    /**
     * 入款金额年份中月份数据查询[销售合同]
     * @param map
     * @return
     */
    public List<Map <String,Object>> sel_Month_data_CT(Map<String,Object> map);


    /**
     * 查询销售合同入款记录基本信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> sel_CT_data_DS(Map<String,Object> map);


    /**
     * 查询销售合同入款记录基本信息总数
     * @param map
     * @return
     */
    public Integer sel_CT_data_Count_DS(Map map);

    /**
     *  修改状态
     * */
    public int updateID(Map<String,Object> map);


    /**
     * 入款详情查看
     * */
    public List<Map<String,Object>> sleDetails(Map map);

    /**
     * 详情总数 查询
     * */
    public int countSel(Map map);


    /**
     * 导出  入款信息
     */
    public List<Map<String, Object>> ExportRemittance(Map map);

    /**
     * 导出  入款详情
     */
    public List<Map<String, Object>> ExportIncome(Map map);


    /**
     * 按条件查询 未到款 sum
     * @param map
     * @return
     */
    public String getSumDio_Unpaid(Map map);




}
