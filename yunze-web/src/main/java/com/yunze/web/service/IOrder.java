package com.yunze.web.service;

import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/26/8:35
 * @Description:
 */
public interface IOrder {


    /**
     * 订单查询
     * @param Pmap
     * @return
     */
    public Map<String,Object> queryOrders(Map<String, Object> Pmap);


    /**
     * 查询 当前卡所属代理下 资费计划
     * @param Pmap
     * @return
     */
    public Map<String,Object> queryPackage(Map<String, Object> Pmap);


    /**
     * 查询订单详情
     * @param Pmap
     * @return
     */
    public Map<String,Object> WxfindOrderDs(Map<String, Object> Pmap);








}
