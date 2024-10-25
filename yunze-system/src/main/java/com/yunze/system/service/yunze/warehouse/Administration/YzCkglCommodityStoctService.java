package com.yunze.system.service.yunze.warehouse.Administration;

import java.util.Map;

public interface YzCkglCommodityStoctService {



    /**
     * 查询
     * @param map
     * @return
     */
    public Map<String,Object> selMap(Map map);


    /**
     * 修改
     * @param map
     * @return
     */
    public boolean upd(Map map);

    /**
     * 编辑库存
     * @param map
     * @return
     * @throws Exception
     */
    public boolean add_CSK(Map<String, Object> map);

}
