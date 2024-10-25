package com.yunze.common.mapper.yunze.warehouse.Administration;


import java.util.List;
import java.util.Map;

public interface YzCkglCommodityStorageMapper {


    /**新增入库信息表*/
    public int warehousingadd(Map<String, Object> map);


    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


    /**查询出库总数*/
    public Integer sle_warehouse (Map map);

    /**查询商品信息列表*/
    public List<Map<String,Object>> slestorage (Map map);
    /**查询商品信息列表总数*/
    public int selMapCount(Map map);

    /**上传文件地址*/
    public String sleFile(Map map);
    /**修改上传文件*/
    public int uploadFile(Map map);


}


























