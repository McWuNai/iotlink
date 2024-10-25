package com.yunze.common.mapper.yunze;


import java.util.List;
import java.util.Map;

/***
 * 查询短信下发内容
 * 2022年9月5日10:10:45
 */
public interface YzSmsLogMapper {

    public List<Map<String,Object>> selMap(Map<String,Object> map);

    public int MapCount(Map map);
}
