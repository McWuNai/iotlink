package com.yunze.system.service.yunze;


import java.util.List;
import java.util.Map;

/**
 * 资费组计划 业务层
 * @author root
 */
public interface IYzAgentPacketService
{


    /**
     * 新增资费组
     * @param map
     * @return
     */
    public boolean add(Map<String, Object> map);


    /**
     * 查询单条 资费计划
     * @param map
     * @return
     */
    public Map<String,Object> find(Map<String, Object> map);


    /**
     * 修改 资费计划
     * @param map
     * @return
     */
    public boolean update(Map<String, Object> map);


    /**
     *查询 资费计划 划分信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> findPacket(Map<String, Object> map);
}
