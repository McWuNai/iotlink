package com.yunze.common.mapper.yunze;


import java.util.List;
import java.util.Map;

/***
 * 资费计划 数据层
 */
public interface YzCardPacketMapper {


    /**
     *查询
     * @param map
     * @return
     */
    public List<Map<String,Object>> selMap(Map<String, Object> map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public List<String> selMapCount(Map<String, Object> map);


    /**
     * 条件查询获取资费组id
     * @param map
     * @return
     */
    public List<String> getPackage_id(Map<String, Object> map);


    /**
     *  查询 packet_id 是否存在
     * @param map
     * @return
     */
    public int isExist(Map<String, Object> map);


    /**
     * 新增 资费计划
     * @param map
     * @return
     */
    public int add(Map<String, Object> map);


    /**
     * 查询单条 资费计划
     * @param map
     * @return
     */
    public List<Map<String,Object>> find(Map<String, Object> map);


    /**
     * 修改 资费计划
     * @param map
     * @return
     */
    public int update(Map<String, Object> map);


    /**
     *查询 平台资费计划 划分信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> findPacket(Map<String, Object> map);



    /**
     *查询资费计划信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> FindList(Map<String, Object> map);


    /**
     * 查询 续费充值的资费计划信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> findToBR(Map<String, Object> map);


    /**
     * 获取 指定资费计划ID 与 指定ICCID 下iccid 卡号
     * @param map
     * @return
     */
    public List<Map<String,Object>> getPacketIdCard(Map<String, Object> map);

    /**
     * 查询资费计划单表
     * @param map
     * @return
     */
    public List<Map<String,Object>> findPacketSingleTable(Map<String, Object> map);


    /**
     * 查询 单条资费计划所有字段
     * @param map
     * @return
     */
    public Map<String,Object> findOnePacket(Map<String, Object> map);


    /**
     * 查询卡总数
     * @param map
     * */
    public Integer selCount(Map map);

    /**
     * 修改 用量总和 、已用流量 、剩余流量 、已用百分比 卡总数  总平台
     * @param map
     * */
    //public int updAmount(Map map);


    /**
     *
     * @param map
     * @return
     */
    public  List<Map<String,Object>> findPacketCount(Map<String, Object> map);


    /**
     * 修改  卡总数
     * @param map
     * @return
     */
    public int updCardCount(Map map);

    /**
     * 导出资费计划
     * @param map
     * @return
     */
    public  List<Map<String,Object>> exportPacket(Map<String, Object> map);

    Map<String, Object> findPackageInfo(Map<String, Object> map);

    Map<String, Object> selPackageInfo(Map<String, Object> resMap);
}
