package com.yunze.common.mapper.yunze.card;

import java.util.List;
import java.util.Map;

public interface YzCardApiOfferinginfoMapper {

    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer MapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> getList(Map map);


    public Integer save(Map map);

    public Integer upd(Map map);

    /**
     * 是否存在
     * @param map
     * @return
     */
    public Map <String,Object> is_ex(Map map);


    public Integer deleteInfo(Map map);


    public Integer updateInfo(Map map);


}
