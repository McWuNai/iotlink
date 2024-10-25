package com.yunze.system.service.yunze;

import java.util.Map;

public interface IYzCardPollingErrorService {

    /*列表查询*/
    public Map<String, Object> listError (Map map);

    /*单个修改*/
    public boolean upd(Map map);

    /*单个删除*/
    public boolean del(Map map);

    /*批量删除*/
    public boolean delArr(Map map);

    /*批量修改*/
    public boolean updArr(Map map);

}
