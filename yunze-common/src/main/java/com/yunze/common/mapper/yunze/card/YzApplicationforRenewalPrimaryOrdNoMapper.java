package com.yunze.common.mapper.yunze.card;

import java.util.List;
import java.util.Map;

public interface YzApplicationforRenewalPrimaryOrdNoMapper {



    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> find(Map map);


    public int save(Map map);

    public int del(Map map);

    public String findPid(Map map);

    /**
     * 获取系统订单关联信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> findOrder(Map map);


}
