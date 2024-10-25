package com.yunze.cn.mapper;


import java.util.List;
import java.util.Map;

/**
 * 微信公众号配置 信息
 */
public interface YzWxConfigMapper {

    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String, Object> find(Map<String, Object> map);


    /**
     * 查询所有正在启用的公众号
     * @return
     */
    public List<Map<String, Object>> getList();






}
