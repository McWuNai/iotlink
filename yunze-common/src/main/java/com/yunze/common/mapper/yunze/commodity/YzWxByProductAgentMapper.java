package com.yunze.common.mapper.yunze.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品购买代理关联表 信息
 */
public interface YzWxByProductAgentMapper {


    /**
     * 获取 商品购买代理关联
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);


    /**
     * 查询运费配置
     * @return
     */
    public String findByCardFare();


    /**
     * 查询系统配置
     * @param map
     * @return
     */
    public String findConfig(Map<String, Object> map);


    /**
     * 查询 商品代理
     * @param map
     * @return
     */
    public List<Map<String, Object>> getDepIDArr(Map<String, Object> map);


    /**
     * 新增商品代理
     * @param map
     * @return
     */
    public int save(Map<String, Object> map);

    /**
     * 删除商品代理表
     * @param map
     * @return
     */
    public int delPAgent();


    public int SendCount(Map map);
}
