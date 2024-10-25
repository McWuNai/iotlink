package com.yunze.iotapi.service.impl;

import com.yunze.iotapi.mapper.mysql.AgentAccountMapper;
import com.yunze.iotapi.service.AgentAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class AgentAccountServiceImpl  implements AgentAccountService {

    @Resource
    private AgentAccountMapper agentAccountMapper;


    @Override
    public Map<String, Object> getOne(Map<String,Object> map) {
        return agentAccountMapper.getOne(map);
    }
}
