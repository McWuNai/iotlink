package com.yunze.iotapi.service.impl;

import com.yunze.iotapi.mapper.mysql.SysLogsMapper;
import com.yunze.iotapi.service.SysLogsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 系统日志 业务实现类
 */
@Service
public class SysLogsServiceImpl  implements SysLogsService {

    @Resource
    private SysLogsMapper sysLogsMapper;


    @Override
    public int save(Map map) {
        return sysLogsMapper.save(map);
    }
}
