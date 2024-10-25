package com.yunze.common.mapper.yunze.card;

import java.util.List;
import java.util.Map;

public interface YzAgentAccountMapper {

    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> getList(Map map);


    public Integer save(Map map);

    public Integer upd(Map map);

    /**
     * 是否存在 appId
     * @param map
     * @return
     */
    public Integer is_exAppId(Map map);


    /**
     * agent_id 下是否存在 appId
     * @param map
     * @return
     */
    public List<String> is_exaAgentId(Map map);


    public Integer del(Map map);

}
