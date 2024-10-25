package com.yunze.common.mapper.yunze.sysgl.Dictionaries;

import java.util.List;
import java.util.Map;

public interface SysDictionaryDetailsMapper {

    /**查询一级分类*/
    public List<Map<String,Object>> classification(Map map);
}
