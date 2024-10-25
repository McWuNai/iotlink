package com.yunze.common.mapper.yunze.upStream;

import java.util.List;
import java.util.Map;

public interface upStreamMapper {

    /**
     * 查询总数
     * @param map
     * @return
     */
    public int MapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> getList(Map map);

    /**
     * 导出成员组信息
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> outChannel(Map<String, Object> map);


}
