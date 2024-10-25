package com.yunze.common.mapper.yunze.commodity;


import java.util.List;
import java.util.Map;

/**
 * 商品图片信息表 信息
 */
public interface YzWxByProductPicInfoMapper {


    /**
     * 查询有效图片
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);


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
     * 获取图片组
     * @param map
     * @return
     */
    public List<Map<String, Object>> findList(Map<String, Object> map);


    /**
     * 修改 状态
     * @param map
     * @return
     */
    public int updPicStatus(Map<String, Object> map);

    /**
     * 批量新增
     * @param map
     * @return
     */
    public int saveArr(Map<String, Object> map);


}
