package com.yunze.common.mapper.yunze;

import java.util.List;
import java.util.Map;

public interface YzCardIsStopMapper {

    /**
     * 50条查询
     * */
    public List<Map<String,Object>> SleOrcount (Map map);
    /**
     * 成功的数据
     * */
    public List<Map<String,Object>> SleOrderno (Map map);

    /**
     * 新增
     * */
    public int AddStop(Map map);

}
