package com.yunze.common.mapper.yunze;

import java.util.List;
import java.util.Map;

public interface YzCcHisMapper {
    /**
     * 查询所有
     * */
    public List<Map<String,Object>> ListCcHis(Map map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public int selMapCount(Map map);
}
