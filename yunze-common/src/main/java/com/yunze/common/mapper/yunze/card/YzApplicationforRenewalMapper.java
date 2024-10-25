package com.yunze.common.mapper.yunze.card;

import java.util.List;
import java.util.Map;

public interface YzApplicationforRenewalMapper {


    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> find(Map map);


    public int save(Map map);

    public int upd(Map map);

    public int del(Map map);



}
