package com.yunze.system.service.yunze.sysgl;

import com.yunze.common.core.domain.entity.SysUser;

import java.util.Map;

/**
 * 系统首页数据
 */
public interface IMySysDeptService {

    /**
     * 授信修改
     * @param map
     * @return
     */
    public String updCredit(Map<String, Object> map, SysUser operateMap);


    /**
     * 企业预存 【微信支付】
     * @param map
     * @return
     */
    public Map<String, Object> initiatePreSave(Map<String, Object> map);

    /**
     * 微信支付回调 预存
     * @param xmlData
     * @param ip
     * @return
     */
    public String weChatNotify(String xmlData,String ip);


    /**
     * 企业预存 订单查看
     * @param map
     * @return
     */
    public Map<String, Object> queryWxOrder(Map<String, Object> map);


    /**
     * 企业预存 【平台操作】
     * @param map
     * @return
     */
    public Map<String, Object> sysPreSaved(Map<String, Object> map);


    /**
     * 预存抵扣
     * @param map
     * @return
     */
    public Map<String, Object> sysDeduct(Map<String, Object> map);


}
