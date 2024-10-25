package com.yunze.system.service.yunze.xsgl.Contract;

import java.util.List;
import java.util.Map;

public interface IYzXsglContractService {


    /**
     * 查询
     * @param map
     * @return
     */
    public Map<String,Object> selMap(Map map);
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
    public Map<String,Object> upd(Map map);



    /**
     * 新增
     * @param map
     * @return
     */
    public Map<String,Object> save(Map map);


    /**
     * 合同作废
     * @param map
     * @return
     */
    public Map<String,Object> delId(Map map);


    /**
     * 发货申请查询 合同 数据
     * @param map
     * @return
     */
    public List<Map <String,Object>> find_FH_data(Map map);



}
