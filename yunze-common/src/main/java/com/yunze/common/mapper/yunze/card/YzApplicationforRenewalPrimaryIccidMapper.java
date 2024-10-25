package com.yunze.common.mapper.yunze.card;

import java.util.List;
import java.util.Map;

public interface YzApplicationforRenewalPrimaryIccidMapper {



    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> find(Map map);


    public int save(Map map);

    public int del(Map map);


    public Integer findCount(Map map);


}
