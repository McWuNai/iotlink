package com.yunze.common.mapper.yunze.warehouse.Administration;

import java.util.List;
import java.util.Map;

public interface YzCkglInventorydetailsMapper {

    /**
     * 导入
     */
    public int importStock(Map<String, Object> map);

    /**
     * 查询卡号是否存在
     */
    public List<String> iMEIStock (Map<String, Object> map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public int MapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> liststorage(Map map);


    /**
     * 导出
     */
    public List<Map<String, Object>> ExportInvent(Map map);

    /**
     * 自定义 vid
     */
    public String selMaxVid();



    /**
     * 没有iccid总数
     * @param map
     * @return
     */
    public int wCOUNT(Map map);


    /**
     * 导入至卡信息
     */
    public Integer ImportAddCard(Map map);


    /*
     * 查询两张表重复的 iccid
     */
    public List<Map<String, Object>> repeatIdArr(Map map);

    /**
     * 卡所属划分
     * @param map
     * @return
     */
    public int updateDivid(Map<String, Object> map);

    /**
     * 备份 卡所属关联
     * @return
     */
    public List<Map<String,Object>> BackupAssociate(Map map);


    /**
     * 获取匹配筛选条件下的卡板id
     * @param map
     * @return
     */
    public List<String> selId(Map<String, Object> map);

    /**
     * 划卡 只查询ID
     * */
    public List<Map<String,Object>> BackupId(Map map);


    /**
     * 按批次号分组
     * @param map
     * @return
     */
    public List<Map<String,Object>> groupByBatchNumber(Map map);

    /**
     * 按商品id分组
     * @param map
     * @return
     */
    public List<Map<String,Object>> groupByCyID(Map map);


    /**
     * 获取入库折线图
     * @param map
     * @return
     */
    public List<Map<String,Object>> getPolyline(Map map);

}
