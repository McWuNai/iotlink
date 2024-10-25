package com.yunze.common.mapper.yunze.polling;

import java.util.List;
import java.util.Map;

public interface YzSynCardInfoMapper {

    /**
     * 查询是否存在
     * @param map
     * @return
     */
    public Integer is_exist(Map map);


    public int save(Map map);


    public int update(Map map);

    /**
     * 获取没有获取到iccid 的接入号数组
     * @param map
     * @return
     */
    public List<String> getNotSyncedInfo(Map map);


    /**
     * 获取需要同步到主表 card_info 的数据
     * @param map
     * @return
     */
    public List<Map<String,Object>> getSyncCardInfo(Map map);


    public int updateArr(Map map);



}
