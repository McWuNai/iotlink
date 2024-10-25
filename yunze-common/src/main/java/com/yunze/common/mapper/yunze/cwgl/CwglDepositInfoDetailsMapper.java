package com.yunze.common.mapper.yunze.cwgl;

import java.util.List;
import java.util.Map;

public interface CwglDepositInfoDetailsMapper {
    /**
     * 删除 入款信息ID 所属入款信息详情
     * @param map
     * @return
     */
    public int del_DIDS_all(Map<String,Object> map);

    /**
     * 查询单条入款记录详情信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> find_CDIDS(Map map);

    /**
     * 添加入款记录详情
     * @param map
     * @return
     */
    public int add_DIDS(Map<String,Object> map);


    /**
     * 修改入款记录信息
     * @param map
     * @return
     */
    public int upd_DIDS(Map<String,Object> map);




}
