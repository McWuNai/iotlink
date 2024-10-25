package com.yunze.web.service;

import com.yunze.cn.entity.WxConfig;

import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/26/8:35
 * @Description:
 */
public interface IWeixinPacket {


    /**
     * 用量查询
     * @param Pmap
     * @return
     */
    public Map<String,Object> queryFlowInfo (Map<String,Object> Pmap);


    /**
     * 获取充值 资费计划
     * @param Pmap
     * @return
     */
    public Map<String,Object> getWxPackets (Map<String,Object> Pmap);


    /**
     * 查询支付类型
     * @param Pmap
     * @return
     */
    public Map<String,Object> querPayType (Map<String,Object> Pmap);

    /**
     * 微信支付
     * @param Pmap
     * @return
     */
    public Map<String,Object> weixinPay (Map<String,Object> Pmap);


    /**
     * 微信回调业务处理
     * @param xmlData
     * @param appId
     * @return
     */
    public Map<String,Object> weChatNotify (String xmlData,String appId);


    /**
     * 绑定
     * @param Pmap
     * @return
     */
    public Map<String, Object> bindCard (Map<String,Object> Pmap);


    /**
     * 解绑
     * @param Pmap
     * @return
     */
    public Map<String, Object> unbind (Map<String,Object> Pmap);




    /**
     * 获取当前卡的信息,进入相应的界面
     * @param Pmap
     * @return
     */
    public Map<String, Object> goWebIndex (Map<String,Object> Pmap);


    /**
     * 操作检测是否绑定
     * @param Pmap
     * @return
     */
    public Map<String, Object> checkBind (Map<String,Object> Pmap);


    /**
     * 获取字典数据
     * @param Pmap
     * @return
     */
    public Map<String, Object> getDictionary (Map<String,Object> Pmap);


    /**
     * 获取订购资费计划
     * @param Pmap
     * @return
     */
    public Map<String, Object> getOrderFlow (Map<String,Object> Pmap);






}
