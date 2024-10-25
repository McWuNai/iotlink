package com.yunze.common.mapper.yunze;

import java.util.List;
import java.util.Map;

public interface YzInfoChangeMapper {

    /**
     * 查询所有
     * */
    public List<Map<String,Object>> ListInfo(Map map);
    /**
     * 查询总数
     * @param map
     * @return
     */
    public int selMapCount(Map map);

    /**
     * 时间降序查询
     * */
    public List<Map<String,Object>> selTime(Map<String, Object> map);
}
