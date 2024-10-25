package com.yunze.iotapi.mapper.mysql;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 系统日志 Mapper
 */
@Mapper
public interface SysLogsMapper  {

    public int save(Map map);
}
