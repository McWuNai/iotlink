package com.yunze.common.mapper.yunze;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单 数据层
 *
 * @author yunze
 */
public interface YzOrderMapper {

    /**
     * 获取执行的加包订单 去生产任务
     *
     * @return
     */
    public List<Map<String, Object>> findAddPackage(Map<String, Object> map);

    /**
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> findCallbackAddress(Map<String, Object> map);
    /**
     * 查询
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> selMap(Map<String, Object> map);

    /**
     * 查询总数
     *
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String, Object> map);


    /**
     * 修改已经加包的订单 加包状态
     *
     * @param map
     * @return
     */
    public int updAddPackage(Map<String, Object> map);


    /**
     * 自动 修改已经加包的订单加包状态
     *
     * @return
     */
    public int updAutomatic();

    public int updActivate(Map<String, Object> map);


    /**
     * 修改 加包执行状态
     *
     * @param map
     * @return
     */
    public int updAddPackageInfo(Map<String, Object> map);


    /**
     * 查询订单加包参数
     *
     * @param map
     * @return
     */
    public Map<String, Object> findOrder(Map<String, Object> map);


    /**
     * 批量插入订单
     *
     * @param map
     * @return
     */
    public int importOrder(Map<String, Object> map);


    /**
     * 新增单条订单
     *
     * @param map
     * @return
     */
    public int save(Map<String, Object> map);


    /**
     * 购物插入订单
     *
     * @param map
     * @return
     */
    public int ShoppingSave(Map<String, Object> map);


    /**
     * 导出
     */
    public List<Map<String, Object>> exportallorders(Map map);

    public List<Map<String, Object>> arrorders(Map map);


    /**
     * getOrderSum
     * @param map
     * @return
     */
    public String getOrderSum(Map<String, Object> map);

    /**
     * 获取交易总金额
     * @param map
     * @return
     */
    public String getSumPrice(Map<String, Object> map);


    /**
     * 修改 微信订单号订单支付状态
     * @param map
     * @return
     */
    public int updStatus(Map<String, Object> map);


    /**
     * 获取 订单简要 信息
     * @param map
     * @return
     */
    public Map<String,Object> getOrderBriefly(Map<String, Object> map);


    /**
     * 卡详情界面 获取 订单信息
     * @param map
     * @return
     */
    public List<Map<String, Object>> getOrderCard(Map map);


    /**
     *  备份 order 表
     * */
    public List<Map<String,Object>> backupsOr(Map map);

    /**
     * 批量修改 order 表状态为 退款
     * */
    public boolean updState (Map map);

    /**
     * 删除 订单号
     * */
    public boolean delFloArr(Map map);

    /*
    * 批量执行加包
    * */
    public boolean updBatch(Map map);

    /**
     * 批量 获取执行的加包订单 去生产任务
     * */
    public List<Map<String, Object>> findAddPackageBatch(Map<String, Object> map);


    /**
    * 批-量 获取执行的加包订单 去生产任 不判断是否已加包
     * * */
    public List<Map<String, Object>> findAddPackageNoAddPackage(Map<String, Object> map);


    /**
     * 重置 加包执行状态 【未找到加包】的 已经执行过的
     * @param map
     * @return
     */
    public int resetAddPackage(Map<String, Object> map);


    /**
     * 获取已经充值过的卡号
     * @param map
     * @return
     */
    public List<String> findRecharged(Map<String, Object> map);
    /**
     * 查询 已支付 需要 执行 API请求但未执行成功 卡号且 在执行次数最大值以下卡号
     * @param map
     * @return
     */
    public List<Map<String, Object>> findRetryOrderApi(Map<String, Object> map);
    /**
     * 获取需要请求执行 api的 iccid卡号订单情况
     * @param map
     * @return
     */
    public List<Map<String, Object>> findIccidOrderApi(Map<String, Object> map);

    /**
     * 修改审核状态
     * @param map
     * @return
     */
    public int updaudit(Map<String, Object> map);
    /**
     * 查询卡号是否首次充值
     * @param map
     * @return
     */
    public Integer findFirstOrder(Map<String, Object> map);
    List<Map<String,Object>> filterNotAddCard(Map<String, Object> parammap);

    List<Map<String, Object>> periodicExtension(Map<String, Object> parammap);

    List<Map<String, Object>> nextMonth(Map<String, Object> parammap);

    List<Map<String, Object>> activateEffect(Map<String, Object> parammap);

    void updateAddPackage(@Param("addPackage") int i, @Param("orderId") String orderId);

    void updOrderCallback(Map<String, Object> returnExecuteParameter);
}

