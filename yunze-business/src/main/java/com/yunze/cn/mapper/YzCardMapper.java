package com.yunze.cn.mapper;


import java.util.List;
import java.util.Map;


/**
 * 卡板信息 数据层
 * 
 * @author root
 */

public interface YzCardMapper
{


    /**
     * 查询总数
     * @param map
     * @return
     */
    public Map<String, Object> findIccid(Map<String, Object> map);


    /**
     * 查询 通道运商类型
     * @param map
     * @return
     */
    public String findOperatorType(Map<String, Object> map);


    /**
     * 查询字典信息
     * @param map
     * @return
     */
    public List<Map<String, Object>> findDict(Map<String, Object> map);

    /**
     * 修改 卡用量
     * @param map
     * @return
     */
    public int updUsed(Map<String, Object> map);


    /**
     * 修改卡状态
     * @param map
     * @return
     */
    public int updStatusId(Map<String, Object> map);

    /**
     * 查询单卡信息
     * @param map
     * @return
     */
    public Map<String,Object> find(Map<String, Object> map);

    /**
     * 查询 卡号 分配 通道 状态
     * @param map
     * @return
     */
    public Map<String, Object> findRoute(Map<String, Object> map);
}
