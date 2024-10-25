package com.yunze.system.service.impl.yunze.sysgl.Dictionaries;

import com.yunze.common.mapper.yunze.sysgl.Dictionaries.SysDictionaryDetailsMapper;
import com.yunze.system.service.yunze.sysgl.Dictionaries.ISysDictionaryDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SysDictionaryDetailsServiceImpl implements ISysDictionaryDetailsService {

    @Resource
    private SysDictionaryDetailsMapper sysDictionaryDetailsMapper;

    @Override
    public List<Map<String, Object>> classification(Map map) {
        return sysDictionaryDetailsMapper.classification(map);
    }
}
