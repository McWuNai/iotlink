package com.yunze.system.service.yunze.sysgl.Dictionaries;

import java.util.List;
import java.util.Map;

public interface ISysDictionaryDetailsService {
    /**查询一级分类*/

    public List<Map<String,Object>> classification(Map map);

}
