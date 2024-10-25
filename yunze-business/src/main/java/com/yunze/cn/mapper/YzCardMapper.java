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
}
