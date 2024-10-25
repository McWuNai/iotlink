package com.yunze.system.service.yunze;

import java.util.List;
import java.util.Map;

public interface IYzCcService {

    public Map<String,Object> ListCc(Map map);

    public boolean YzCcAdd(Map<String, Object> map);

    /**
     * 查询 通道简要信息  状态为 正常 划分通道用
     */
    public List<Map<String, Object>> NameAdd();

    public boolean UpdateCc(Map<String, Object> map);
}
