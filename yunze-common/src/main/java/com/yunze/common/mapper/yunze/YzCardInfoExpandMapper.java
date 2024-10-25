package com.yunze.common.mapper.yunze;


import java.util.List;
import java.util.Map;

/**
 * 卡列表拓展表
 */
public interface YzCardInfoExpandMapper {

    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    public List<String> findInfoExpandArr(Map map);



    /**
     * 批量新增
     * @param map
     * @return
     * @throws Exception
     */
    public int saveInfoExpand(Map map);


    /**
     * 修改
     * @param map
     * @return
     * @throws Exception
     */
    public int updInfoExpand(Map map);



    /**
     * 查询批量更新 前 字段
     * @param map
     * @return
     */
    public List<Map<String,Object>> findReplaceBk(Map<String, Object> map);

}
