package com.yunze.cn.mapper;


import java.util.List;
import java.util.Map;

/***
 * 资费组 数据层
 */
public interface YzVWhiteMapper {

    /**
     *查询
     * @param map
     * @return
     */
    public Map<String,Object> selRoute(Map<String, Object> map);
    public Map<String,Object> selRouteCreate(Map<String, Object> map);
    public Map<String,Object> getPacketForIccid(Map<String, Object> map);
    public Map<String,Object> getPreForPacket(Map<String, Object> map);




}
