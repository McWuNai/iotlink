package com.yunze.common.mapper.yunze.polling;

import java.util.List;
import java.util.Map;

public interface YzCardPollingErrorGroupMapper {

    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map map);

    /**
     * 单个修改
     * @param map
     * @return
     */
    public int updObj(Map map);


    public int upd(Map map);





    /**
     * 单个删除
     * @param map
     * @return
     */
    public int del(Map map);

    /**
     *查询是否存在
     * @param map
     * @return
     */
    public String findEx(Map map);





    public List<Map<String,Object>> selMap (Map map);


    public Integer selMapCount(Map map);


    /**
     * 批量删除
     * */
    public int delArr(Map<String,Object> map);

    /**
     * 批量修改
     * */
    public int updArr(Map<String,Object> map);


    /**
     * 删除单表所有数据 带下次自动生成
     * @param map
     * @return
     */
    public int delTableData(Map<String,Object> map);


}
