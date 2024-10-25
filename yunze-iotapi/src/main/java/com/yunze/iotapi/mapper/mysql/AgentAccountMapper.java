package com.yunze.iotapi.mapper.mysql;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface AgentAccountMapper  {

    public Map<String,Object> getOne(Map<String,Object> map);


}
