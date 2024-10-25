package com.yunze.common.mapper.yunze.wechat;

import java.util.List;
import java.util.Map;

public interface YzWxConfigMapper {


    /**
     *查询 单条微信配置文件
     * @param map
     * @return
     */
    public Map <String,Object> findOne(Map<String,Object> map);


    /**
     * 获取所有配置文件

     * @return
     */
    public List<Map <String,Object>> getWxConfigs();


}
