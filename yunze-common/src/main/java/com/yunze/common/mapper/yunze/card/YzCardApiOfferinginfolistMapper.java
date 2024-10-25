package com.yunze.common.mapper.yunze.card;

import java.util.List;
import java.util.Map;

public interface YzCardApiOfferinginfolistMapper {

    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer MapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> getList(Map map);


    public Integer save(Map map);

    public Integer upd(Map map);

    /**
     * 是否存在
     * @param map
     * @return
     */
    public Map <String,Object> is_ex(Map map);


    /**
     * 获取上游资费分组
     * @param map
     * @return
     */
    public List<Map <String,Object>> groupOfferingId(Map map);
    /**
     * 获取 上游单卡订购套餐列表
     */
    public List<Map<String,Object>> apiIdList(Map map);




}
