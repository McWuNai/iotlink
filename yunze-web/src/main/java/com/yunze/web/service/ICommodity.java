package com.yunze.web.service;

import java.util.Map;

/**
 * 商品业务层
 * @Auther: zhang feng
 * @Date: 2021/09/22/10:40
 * @Description:
 */
public interface ICommodity {

    /**
     * 商品分类查询
     * @param Pmap
     * @return
     */
    public Map<String,Object> QuerCategory (Map<String,Object> Pmap);



    /**
     * 获取商品图片
     * @param Pmap
     * @return
     */
    public Map<String,Object> QuerImg (Map<String,Object> Pmap);


    /**
     * 支付验证
     * @param Pmap
     * @return
     */
    public Map<String,Object> PayCheck (Map<String,Object> Pmap);


    /**
     * web端商品购买订单生成
     * @param Pmap
     * @return
     */
    public Map<String,Object> ShoppingPay (Map<String,Object> Pmap);



}
