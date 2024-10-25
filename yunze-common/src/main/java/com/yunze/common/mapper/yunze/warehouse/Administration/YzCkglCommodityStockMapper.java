package com.yunze.common.mapper.yunze.warehouse.Administration;

import java.util.List;
import java.util.Map;

public interface YzCkglCommodityStockMapper {

    /**查询商品库存*/
    public Integer sle_stock (Map map);
    /**修改库存数量*/
    public int update_quantity (Map map);
    /**添加库存数量*/
    public int add_quantity (Map map);


    /**
     * 查询 总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> selMap(Map map);


    /**
     * 修改库存数量
     * @param map
     * @return
     */
    public int upd_Stock_Quantity (Map map);


    public int sel_Ex(Map map);

    public int add_CSK(Map map);

    public int sel_data_count();

}
