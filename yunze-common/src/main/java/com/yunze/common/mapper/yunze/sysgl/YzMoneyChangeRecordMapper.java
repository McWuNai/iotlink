package com.yunze.common.mapper.yunze.sysgl;


import java.util.Map;

public interface YzMoneyChangeRecordMapper {


    /**
     * 新增 金额变更记录表
     * @param map
     * @return
     */
    public int saveMoneyChange(Map<String, Object> map);



}
