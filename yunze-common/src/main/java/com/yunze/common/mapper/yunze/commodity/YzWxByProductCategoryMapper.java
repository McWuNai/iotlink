package com.yunze.common.mapper.yunze.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品分类表 信息
 */
public interface YzWxByProductCategoryMapper {


    /**
     * 获取商品分类 分类 1 级 状态可看
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);


    /**
     * 查询商品分类 信息
     * @param map
     * @return
     */
    public List<Map<String, Object>> selMap(Map<String, Object> map);


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
    public int upd(Map<String, Object> map);


    /**
     * 获取所有 商品分类
     * @param map
     * @return
     */
    public List<Map<String, Object>> getCategory(Map<String, Object> map);


    /**
     * 获取分类 编码
     * @param map
     * @return
     */
    public String findCode(Map<String, Object> map);

    /**
     * 查询总数
     * */
    public int selMapCount(Map map);


    /**
     * 查询所有
     * */
    public List<Map<String,Object>> CategoryList(Map map);

}










