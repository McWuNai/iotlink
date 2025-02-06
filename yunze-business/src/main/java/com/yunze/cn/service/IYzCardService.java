package com.yunze.cn.service;



import java.util.Map;

/**
 * 卡板信息 业务层
 * @author root
 */
public interface IYzCardService
{
    /**
     *查询
     * @param map
     * @return
     */
    public Map<String,Object> findIccid(Map<String, Object> map);



    /**
     * 查询 通道运商类型
     * @param map
     * @return
     */
    public String findOperatorType(Map<String, Object> map);


    void singleState(Map<String, Object> map);
}
