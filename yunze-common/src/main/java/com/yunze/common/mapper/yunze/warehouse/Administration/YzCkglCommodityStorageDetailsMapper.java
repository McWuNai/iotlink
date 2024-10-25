package com.yunze.common.mapper.yunze.warehouse.Administration;

import java.util.Map;

public interface YzCkglCommodityStorageDetailsMapper {


    /**新增入库数量表*/
    public int quantity(Map<String, Object> map);

    /**查询入库总数*/
    public Integer sle_quantity (Map map);

    /**上传文件地址*/
    public String sleFile(Map map);
    /**修改上传文件*/
    public int uploadFile(Map map);
}
