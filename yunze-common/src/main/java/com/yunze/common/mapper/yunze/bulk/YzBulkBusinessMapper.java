package com.yunze.common.mapper.yunze.bulk;

import java.util.List;
import java.util.Map;

public interface YzBulkBusinessMapper {


    public int add(Map map);

    public int update(Map map);

    /**
     * 查询
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> selMap(Map map);

    /**
     * 查询总数
     *
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String, Object> map);

    /**
     *
     * @param map
     * @return
     */
    public List<String> findIdArr(Map map);


}
