package com.yunze.common.mapper.yunze;

import com.yunze.common.core.domain.entity.SysDictData;

import java.util.List;
import java.util.Map;

public interface YzCcUrlMapper {

    /**
     * 查询所有
     * */
    public List<Map<String,Object>> ListCcUrl (Map map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public int selMapCount(Map map);


    public int insertCcUrl(Map<String, Object> map);

    public int updateCcUrl(Map<String, Object> map);

    public int DeleteCcUrl(Map<String, Object> map);

    public Integer CountId(Map map);


}
