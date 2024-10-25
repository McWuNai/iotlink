package com.yunze.common.mapper.yunze.polling;

import java.util.List;
import java.util.Map;

public interface YzUpstreamGroupMapper {

    /**
     * 查询是否存在
     * @param map
     * @return
     */
    public Integer is_exist(Map map);


    public int save(Map map);


    public int update(Map map);

    public List<String> findGroupId(Map map);



}
