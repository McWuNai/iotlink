package com.yunze.system.service.yunze;

import com.yunze.common.core.domain.entity.SysUser;

import java.util.HashMap;
import java.util.Map;

public interface IYzCardFlowHisService {

    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String,Object> ListHis(Map map);

    public Map<String,Object> getById(HashMap<String, Object> map);

    /**用量详情*/
    public String FlowHis(Map<String, Object> map, SysUser User);

}
