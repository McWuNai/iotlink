package com.yunze.system.service.yunze.memberGroup;

import com.yunze.common.core.domain.entity.SysUser;

import java.util.Map;

/**
 * 2022年9月26日13:48:11
 */
public interface YzMGService {

    public Map<String,Object> getList(Map map);

    /**
     * 导出成员组
     * @param map
     * @return
     */
    public String exportData(Map<String, Object> map, SysUser User);

}
