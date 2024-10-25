package com.yunze.cn.service;

import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/25/14:10
 * @Description: 微信用户
 */
public interface IYzWeChatUser {

    /**
     * 新增
     * @param map
     * @return
     * @throws Exception
     */
    public int save(Map map);

    /**
     * 修改
     * @param map
     * @return
     * @throws Exception
     */
    public int upd(Map map);

    /**
     * 查询单条
     * @param map
     * @return
     */
    public Map<String, Object> find(Map map);

}
