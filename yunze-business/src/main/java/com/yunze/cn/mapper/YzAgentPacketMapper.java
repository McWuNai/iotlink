package com.yunze.cn.mapper;


import java.util.List;
import java.util.Map;

/***
 * 资费计划 数据层
 */
public interface YzAgentPacketMapper {


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
     * 查询 资费组简要信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> find_simple(Map<String, Object> map);



    /**
     *查询 资费计划 划分信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> findPacket(Map<String, Object> map);


    /**
     *划卡 资费计划信息 修改
     * @param map
     * @return
     */
    public int updatePacket(Map<String, Object> map);


    /**
     * 查询 资费计划 设置时 已经设置好的 成本 售价 系数
     * @param map
     * @return
     */
    public List<Map<String,Object>> findExistence(Map<String, Object> map);

    /**
     * 从 企业客户 复制资费计划到 用户
     * @param map
     * @return
     */
    public int addAgent(Map<String, Object> map);

    /**
     *查询资费计划信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> FindList(Map<String, Object> map);


    /**
     * 微信充值 获取资费计划列表
     * @param map
     * @return
     */
    public List<Map<String,Object>> findWebRecharge(Map<String, Object> map);


    /**
     * 微信充值 获取资费计划 支付类型
     * @param map
     * @return
     */
    public Map<String,Object> findPayType(Map<String, Object> map);


    /**
     * 查询 是否已充值过 仅一次充值自资费计划类型
     * @return
     */
    public List<String> findWebOnlyOnce(Map<String, Object> map);


}
