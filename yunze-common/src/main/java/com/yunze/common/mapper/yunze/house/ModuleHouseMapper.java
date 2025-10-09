package com.yunze.common.mapper.yunze.house;

import com.yunze.common.core.domain.entity.ModuleHouse;
import com.yunze.common.core.domain.entity.SysAutoPolling;

import java.util.List;
import java.util.Map;

public interface ModuleHouseMapper {
    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);

    List<Map<String,Object>> list(Map<String,Object> map);

    List<Map<String,Object>> test(Map<String,Object> map);
    void ins(ModuleHouse moduleHouse);


    void del(Map<String, Object> map);

    Map<String, Object> selectModuleBySerialNumber(ModuleHouse moduleHouse);


    Map<String, Object> selectModuleBySN(ModuleHouse moduleHouse);

    List<String> isExistence(Map<String, Object> map);
}
