package com.yunze.system.service.yunze;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IYzInfoChangeService {

    public Map<String,Object> selMap(Map map);


    public List<Map<String,Object>> selTime(HashMap<String, Object> map);

}
