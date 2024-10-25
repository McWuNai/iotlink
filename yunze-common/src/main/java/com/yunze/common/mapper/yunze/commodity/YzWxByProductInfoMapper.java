package com.yunze.common.mapper.yunze.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品信息表 信息
 */
public interface YzWxByProductInfoMapper {


    /**
     * 获取商品 上架、已审核
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);

    /**
     * 验证商品价格查询
     * @param map
     * @return
     */
    public Map<String, Object> findId(Map<String, Object> map);




    /**
     * 获取商品 名称
     * @return
     */
    public List<Map<String, Object>> findName(Map<String, Object> map);

    /**
     * 查询
     * @param map
     * @return
     */
    public List<Map<String, Object>> selMap(Map<String, Object> map);


    /**
     * 查询 总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String, Object> map);


    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map<String, Object> map);


    /**
     * 修改
     * @param map
     * @return
     */
    public int edit(Map<String, Object> map);


    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public Map<String, Object> findData(Map<String, Object> map);


    /**
     * 获取 分类下 最大商品编码
     * @param map
     * @return
     */
    public String getMaxCore(Map<String, Object> map);




}
