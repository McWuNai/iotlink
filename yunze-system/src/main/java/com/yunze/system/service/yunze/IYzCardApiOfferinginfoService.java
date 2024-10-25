package com.yunze.system.service.yunze;

import java.util.Map;

public interface IYzCardApiOfferinginfoService {

    public Map<String,Object> getList(Map map);

    public boolean deleteInfo(Map map);

    public boolean updateInfo(Map map);

}
