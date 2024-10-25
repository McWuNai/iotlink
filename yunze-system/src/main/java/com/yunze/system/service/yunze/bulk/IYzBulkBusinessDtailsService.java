package com.yunze.system.service.yunze.bulk;

import com.yunze.common.core.domain.entity.SysUser;

import java.util.Map;

public interface IYzBulkBusinessDtailsService {

    /**查询列表*/
    public Map<String,Object> getList(Map map);

    /**查询状态 成功*/
    public Map<String,Object> successArr(Map map);

    /**导出*/
    public String exportallorders(Map<String, Object> map, SysUser User);
}
