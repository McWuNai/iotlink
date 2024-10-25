package com.yunze.system.service.yunze;

import java.util.HashMap;
import java.util.Map;

public interface IYzLogsService {

    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String,Object> selMap(Map map);

    public Map<String,Object> getById(HashMap<String, Object> map);






}
