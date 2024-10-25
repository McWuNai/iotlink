package com.yunze.common.mapper.yunze.sysgl;


import java.util.Map;

public interface YzIndexpageMapper {


    /**
     * 查询首页数据
     * @param map
     * @return
     */
    public Map <String,Object> findToDay(Map<String,Object> map);

    /**
     * 判断修改时间是否在 Xminute 分钟内
     * @param map
     * @return
     */
    public Integer findEffectiveTime(Map<String,Object> map);

    /**
     * 判断 代理下是否已生成 首页数据
     * @param map
     * @return
     */
    public Integer findExist(Map<String,Object> map);

    /**
     * 新增首页数据
     * @param map
     * @return
     */
    public int addIndexpage(Map<String,Object> map);

    /**
     * 更新首页数据
     * @param map
     * @return
     */
    public int updInfo(Map<String,Object> map);
    public String findBusinessVolumeFlow(Map<String,Object> map);

}
