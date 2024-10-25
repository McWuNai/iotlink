package com.yunze.common.mapper.yunze.xsgl.Contract;

import java.util.List;
import java.util.Map;

public interface YzXsglContractStandardMapper {

    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map map);



    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public List<Map <String,Object>> find(Map map);


    /**
     * 基础修改
     * @param map
     * @return
     */
    public int upd(Map map);



    /**
     * 单个状态 = 删除
     * @param map
     * @return
     */
    public int delId(Map map);


    /**
     * 所属合同 删除
     * @param map
     * @return
     */
    public int delCsd_CtID(Map map);


    /**
     * 发货申请查询 合同 数据
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_FH_data(Map map);



}
