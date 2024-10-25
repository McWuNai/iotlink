package com.yunze.apiCommon.mapper;


import java.util.List;
import java.util.Map;

public interface YzIpWhitelistMapper {



    /**
     * 查询 允许向内部发送token 的白名单
     *
     * @param map
     * @return
     */
    public Map<String, Object> findWIp(Map<String, Object> map);






}
