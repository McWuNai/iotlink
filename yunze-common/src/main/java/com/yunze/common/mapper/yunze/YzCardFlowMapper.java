package com.yunze.common.mapper.yunze;





import com.yunze.common.core.domain.entity.YzCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 资费用量记录表 数据层
 *
 * @author root
 */
public interface YzCardFlowMapper
{
    /**
     *查询套餐数据
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryflow(Map<String, Object> map);


    /**
     *查询套餐数据 代理表
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryflow_agent(Map<String, Object> map);


    /**
     * 查询当前套餐简要信息
     * @param map
     * @return
     */
    public HashMap<String,Object> queryflow_simple(Map<String, Object> map);


    /**
     * 获取资费组简要信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryPackage_simple(Map<String, Object> map);



    /**
     * 获取 代理 资费组简要信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryAgentPackage_simple(Map<String, Object> map);


    /**
     * 查询当前套餐简要信息 代理
     * @param map
     * @return
     */
    public Map<String,Object> queryflowAgent_simple(Map<String, Object> map);


    /**
     * 卡板信息导出需要用到的套餐数据
     * @param map
     * @return
     */
    public List<Map<String,Object>> outCardFlow(Map<String, Object> map);

    /**
     *查询 生效中 未生效 资费计划
     * @param map
     * @return
     */
    public List<Map<String,Object>> findInEffect(Map<String, Object> map);

    /**
     * 查询 已失效 资费计划
     * @param map
     * @return
     */
    public String findInvalidationSumErrorFlow(Map<String, Object> map);

    /**
     * 修改 资费计划用量
     * @param map
     * @return
     */
    public int updFlow(Map<String, Object> map);


    /**
     * 修改 时间到期资费计划
     * @param map
     * @return
     */
    public int updStatus(Map<String, Object> map);

    /**
     * 查询卡号对应订单号是否已经存在
     * @param map
     * @return
     */
    public Integer findExistence(Map<String, Object> map);


    /**
     * 查询到期时间
     * @param map
     * @return
     */
    public String findEndTime(Map<String, Object> map);

    /**
     * 插入flow
     * @param map
     * @return
     */
    public int saveFlow(Map<String, Object> map);


    /**
     * 获取资费计划简要信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryPacket_simple(Map<String, Object> map);

    /**
     *获取 代理 资费计划简要信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryAgentPacket_simple(Map<String, Object> map);


    /**
     * 获取 资费计划  信息
     * @param map
     * @return
     */
    public Map<String,Object> FindPacket(Map<String, Object> map);

    /**
     * 获取 代理 资费计划  信息
     * @param map
     * @return
     */
    public Map<String,Object> FindPacketAgent(Map<String, Object> map);

    /**
     * 获取 资费计划 简要 信息
     * @param map
     * @return
     */
    public Map<String,Object> FindPacket_concise(Map<String, Object> map);

    /**
     * 获取 代理 资费计划 简要 信息
     * @param map
     * @return
     */
    public Map<String,Object> FindPacketAgent_concise(Map<String, Object> map);


    /**
     * 查询总数 订购资费查询
     * @param map
     * @return
     */
    public int MapCount(Map map);

    /**
     *查询 订购资费查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> getList(Map map);


    /**
     * 查询资费计划类型
     * @param map
     * @return
     */
    public Map<String,Object> findPacketType(Map<String, Object> map);

    /**
     * 同步时间查询
     */
    public List<Map<String,Object>> sletume(Map map);

    /**
    * 到期时间
    * */
    public List<Map<String,Object>> sleTime(Map map);

    /**
     * 订购资费导出查询
     * */
    public  List<Map<String,Object>> exportflow (Map map);

    /**
     * 查询导出
     */
    public List<Map<String, Object>> exportArr(Map map);

    /**
     *查询失效的防超套系数
     * @param map
     * @return
     */
    public String FindError_time(Map map);

    /**
     * 查询 已失效 资费计划
     * @param map
     * @return
     */
    public String findInvalidationSumErrorFlow_Year(Map<String, Object> map);

    /**
     * 查询 已失效 资费计划 总用量和总已用
     * @param map
     * @return
     */
    public Map<String,Object> findInvalidationSum(Map<String, Object> map);

    /**备份 Flos 表*/
    public List<Map<String,Object>> backupsFl(Map map);

    /**
     *  批量删除对应order 表的 订单号
     * */
    public boolean delOrdArr(Map map);


    public List<Map<String,Object>> consumption();

    /**
     * 询  资费计划 真实用量 当月可用
     * @param map
     * @return
     */
    public String findSumErrorFlow(Map<String, Object> map);

    int selCardFlow(Map<String, String> stringMap);

    void insertFlow(Map<String, Object> insertMap);

    Integer filterIsAddPackage(String iccid);

    int selOneCardFlow(String iccid);

    List<YzCard> outCardFlowByList(Map<String, Object> map);
}
