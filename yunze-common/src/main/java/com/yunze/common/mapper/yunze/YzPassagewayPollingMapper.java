package com.yunze.common.mapper.yunze;


import java.util.List;
import java.util.Map;

/**
 * 通道轮询详情表
 */
public interface YzPassagewayPollingMapper {

    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> selMap(Map<String, Object> map);


    /**
     * 查询总数
     * @param map
     * @return
     * @throws Exception
     */
    public int selMapCount(Map<String, Object> map);


    /**
     * 新增 轮询详情
     * @param map
     * @return
     */
    public int add(Map<String, Object> map);

    /**
     * 修改当前轮询 数量
     * @param map
     * @return
     */
    public int update_cd_current(Map<String, Object> map);


    /**
     *查询  一天内 正在执行的轮询任务
     * @return
     */
    public List<Map<String, Object>> find_execute();


    /**
     * 查询  一天内已完成轮询任务
      * @return
     */
    public List<Map<String, Object>> find_completed();

}
