package com.yunze.system.service.yunze.warehouse.Administration;

import java.util.Map;

public interface YzCkglCommodityOutputService {

    public Map<String,Object> SleOutKu(Map map);

    /**
     * 新增出库信息
     *
     * @return*/
    public Map<String, Object> add_CODS(Map<String,Object> map);
}
