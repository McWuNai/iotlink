package com.yunze.common.mapper.yunze.automationCC;


import java.util.List;
import java.util.Map;

/**
 * 自动化 触发任务推送地址邮箱配置
 */
public interface YzAutomationCcUrlMapper {



    /**
     * 查询 触发任务推送地址邮箱配置
     * @return
     */
    public List<Map<String, Object>> findConcise(Map<String, Object> map);



}
