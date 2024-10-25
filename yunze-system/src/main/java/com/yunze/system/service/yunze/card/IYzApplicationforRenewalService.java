package com.yunze.system.service.yunze.card;

import com.yunze.common.core.domain.entity.SysUser;

import java.util.List;
import java.util.Map;

public interface IYzApplicationforRenewalService {

    public Map<String,Object> selMap(Map map);

    public Map<String,Object>  save(Map map, SysUser operateMap);

    public boolean  upd(Map map);


    public Map<String,Object> find(Map map);


    public Map<String, Object> findIccid(Map map);

}
