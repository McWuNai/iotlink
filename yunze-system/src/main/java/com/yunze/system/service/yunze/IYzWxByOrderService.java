package com.yunze.system.service.yunze;

import com.yunze.common.core.domain.entity.SysUser;

import java.util.List;
import java.util.Map;

public interface IYzWxByOrderService {

    /**
     * 查询发货订单
     * @return
     */
    public Map<String, Object> findWxOrder(Map<String, Object> map);



    /**导出*/
    public String shopping_export(Map<String, Object> map, SysUser User);
}
