package com.yunze.common.mapper.yunze.bulk;

import java.util.List;
import java.util.Map;

public interface YzSmSBulkBusinessMapper {


    public int add(Map map);

    public int update(Map map);

    /**
     * 查询
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> selMap(Map map);

    /**
     * 查询总数
     *
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String, Object> map);

    /**
     *
     * @param map
     * @return
     */
    public List<String> findIdArr(Map map);


    /**
     * 查询 单条 状态为 执行中 的 短信任务进行下发执行
     * @param map
     * @return
     */
    public List<Map<String, Object>> findOneExecution(Map map);


}
