package com.yunze.system.service.yunze;



import java.util.List;
import java.util.Map;

public interface IYzCcUrlService {

    public Map<String,Object> ListCcUrl (Map map);


    public boolean insertCcUrl(Map<String, Object> map);

    public boolean updateCcUrl(Map<String, Object> map);

    public Map<String, Object> DeleteCcUrl(Map map);

}
