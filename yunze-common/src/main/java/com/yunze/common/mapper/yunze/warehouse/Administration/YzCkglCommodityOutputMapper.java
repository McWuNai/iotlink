package com.yunze.common.mapper.yunze.warehouse.Administration;

import java.util.List;
import java.util.Map;

public interface YzCkglCommodityOutputMapper {

    /**查询出库列表*/

    public List<Map<String,Object>> SleOutKu (Map map);

    /**查询总数*/

    public int  selMapCount (Map map);

    /**
     * 新增商品出库
     * */
    public int add_COT(Map map);

    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();
}
