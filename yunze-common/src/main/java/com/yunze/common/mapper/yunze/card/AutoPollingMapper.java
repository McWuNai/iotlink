package com.yunze.common.mapper.yunze.card;

import com.yunze.common.core.domain.entity.SysAutoPolling;

import java.util.List;
import java.util.Map;

public interface AutoPollingMapper {
    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);

    List<Map<String,Object>> list(Map<String,Object> map);

    List<Map<String,Object>> test(Map<String,Object> map);
    void ins(Map<String,Object> map);


    void del(Map<String, Object> map);

    Map<String, Object> selectPollingByIccid(SysAutoPolling autoPolling);


    Map<String, Object> selectCardByIccid(SysAutoPolling autoPolling);
}
