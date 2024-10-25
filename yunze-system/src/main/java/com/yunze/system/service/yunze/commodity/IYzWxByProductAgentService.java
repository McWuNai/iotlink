package com.yunze.system.service.yunze.commodity;

import java.util.List;
import java.util.Map;

public interface IYzWxByProductAgentService {

    /**
     * 查询 商品代理
     * @param map
     * @return
     */
    public List<Map<String, Object>> find(Map<String, Object> map);


    /**
     * 编辑
     * @param map
     * @return
     */
    public boolean edit(Map<String, Object> map);




}
