package com.yunze.common.mapper.yunze.automationCC;


import java.util.List;
import java.util.Map;

/**
 * 自动化 推送记录
 */
public interface YzAutomationCcMapper {



    /**
     * 查询 自动化抄送
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);



}
