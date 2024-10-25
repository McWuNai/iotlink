package com.yunze.system.service.yunze;

import java.util.Map;

public interface IYzCardPollingErrorGroupService {

    /*查询列表*/
    public Map<String, Object> listGroup (Map map);

    /*单个删除轮询错误概要*/
    public boolean del(Map map);

    /*单个修改轮询错误概要*/
    public boolean upd(Map map);

    /*批量删除轮询错误概要*/
    public boolean delArr(Map map);

    /*批量修改轮询错误概要*/
    public boolean updArr(Map map);
}
