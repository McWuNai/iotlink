package com.yunze.common.mapper.yunze;

import java.util.List;
import java.util.Map;

public interface YzCategoryMapper {

    /**
     * 查询所有
     * */
    public List<Map<String,Object>> CategoryList(Map map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public int selMapCount(Map map);

}
