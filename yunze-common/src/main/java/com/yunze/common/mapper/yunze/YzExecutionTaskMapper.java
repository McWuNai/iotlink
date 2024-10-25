package com.yunze.common.mapper.yunze;


import java.util.List;
import java.util.Map;

/**
 * 执行 任务 表格
 */
public interface YzExecutionTaskMapper  {


    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    List<Map<String, String>> sel_Map(Map map);


    /**
     * 查询总数
     *
     * @param map
     * @return
     * @throws Exception
     */
    public int sel_Map_Count(Map map);


    /**
     * 新增任务
     *
     * @param map
     * @return
     */
    public int add(Map map);

    /**
     * 修改任务结束时间
     *
     * @param map
     * @return
     */
    public int set_end_time(Map map);


    /**
     * 修改 地址
     *
     * @param map
     * @return
     */
    public int upd(Map map);


    /**
     * 查询 需要删除的 执行任务数据-
     * @param map
     * @return
     */
    List<Map<String, Object>> findUrlArr(Map map);

    /**
     * 按id删除
     * @param map
     * @return
     */
    public int delIdArr(Map map);


}
