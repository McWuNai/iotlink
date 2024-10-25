package com.yunze.cn.mapper;

import java.util.List;
import java.util.Map;

/**
 * 订单 数据层
 * 
 * @author yunze
 */
public interface YzOrderMapper
{

    /**
     * 获取执行的加包订单 去生产任务
     * @return
     */
    public List<Map<String,Object>> findAddPackage(Map<String, Object> map);


    /**
     *查询
     * @param map
     * @return
     */
    public List<Map<String,Object>> selMap(Map<String, Object> map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String, Object> map);





    /**
     * 修改已经加包的订单 加包状态
     * @param map
     * @return
     */
    public int updAddPackage(Map<String, Object> map);


    /**
     * 自动 修改已经加包的订单加包状态
     * @return
     */
    public int updAutomatic();

    /**
     * 更新 激活时间
     * @param map
     * @return
     */
    public int updActivate(Map<String, Object> map);


    /**
     * 修改 加包执行状态
     * @param map
     * @return
     */
    public int updAddPackageInfo(Map<String, Object> map);


    /**
     * 查询订单加包参数
     * @param map
     * @return
     */
    public Map<String,Object> findOrder(Map<String, Object> map);


    /**
     * 批量插入订单
     * @param map
     * @return
     */
    public int importOrder(Map<String, Object> map);


    /**
     * 修改 微信订单号订单支付状态
     * @param map
     * @return
     */
    public int updStatus(Map<String, Object> map);


    /**
     * 获取 订单简要 信息
     * @param map
     * @return
     */
    public Map<String,Object> getOrderBriefly(Map<String, Object> map);


    /**
     * C 端 查询订单 简要信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> WxfindOrder(Map<String, Object> map);


    /**
     * C 端 查询订单 详情
     * @param map
     * @return
     */
    public Map<String,Object> WxfindOrderDs(Map<String, Object> map);





    /**
     * 重置 加包执行状态 【未找到加包】的 已经执行过的
     * @param map
     * @return
     */
    public int resetAddPackage(Map<String, Object> map);

}
