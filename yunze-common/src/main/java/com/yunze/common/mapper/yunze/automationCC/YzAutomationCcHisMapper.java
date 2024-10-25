package com.yunze.common.mapper.yunze.automationCC;


import java.util.List;
import java.util.Map;

/**
 * 自动化 推送记录
 */
public interface YzAutomationCcHisMapper {



    /**
     * 新增
     * @return
     */
    public int save(Map<String, Object> map);


    /**
     * 修改抄送记录
     * @param map
     * @return
     */
    public int updCcCount(Map<String, Object> map);


    /**
     * 获取未抄送成功的数据进行抄送
     * @param map
     * @return
     */
    public List<Map<String, Object>> findNotPerformed(Map<String, Object> map);



}
