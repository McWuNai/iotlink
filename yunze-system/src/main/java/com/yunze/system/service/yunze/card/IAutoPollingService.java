package com.yunze.system.service.yunze.card;

import com.yunze.common.core.domain.entity.SysAutoPolling;

import java.util.List;
import java.util.Map;

public interface IAutoPollingService {

    Map<String,Object> list(Map<String,Object> map);


    void del(Map<String, Object> parammap);

    String importAuto(List<SysAutoPolling> list);
}
