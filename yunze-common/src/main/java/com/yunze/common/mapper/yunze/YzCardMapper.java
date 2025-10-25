package com.yunze.common.mapper.yunze;


import com.yunze.common.core.domain.entity.YzCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 卡板信息 数据层
 *
 * @author root
 */
@Mapper
public interface YzCardMapper
{

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
    public Integer selMapCount(Map<String, Object> map);

    /**
     *查询 [切割iccid 19 位]
     * @param map
     * @return
     */
    public List<Map<String,Object>> selMapLianTong(Map<String, Object> map);

    /**
     * 查询总数 [切割iccid 19 位]
     * @param map
     * @return
     */
    public Integer selMapLianTongCount(Map<String, Object> map);


    /**
     * 查询单卡信息
     * @param map
     * @return
     */
    public Map<String,Object> find(Map<String, Object> map);

    /**
     * 查询单条 卡板数据 数据 不关联 Route
     * @param map
     * @return
     */
    public Map<String,Object> findNotRoute(Map<String, Object> map);


    /**
     * 导入卡板数据
     */
    public int importCard(Map<String, Object> map);


    /**
     * 查询单卡信息
     * @param map
     * @return
     */
    public List<String> isExistence(Map<String, Object> map);


    /**
     * 查询目前最大自定义号码
     * @return
     */
    public String findMaxVid();


    /**
     * 查询 卡号 分配 通道 状态
     * @param map
     * @return
     */
    public Map<String, Object> findRoute(Map<String, Object> map);

    /**
     * 查询 通道 状态
     * @param map
     * @return
     */
    public Map<String, Object> findRouteInfo(Map<String, Object> map);

    /**
     *按当前赛选条件获取导出数据
     * @param map
     * @return
     */
    public List<Map<String,Object>> outCard(Map<String, Object> map);


    /**
     *按当前赛选条件获取导出数据 [切割iccid 19 位]
     * @param map
     * @return
     */
    public List<Map<String,Object>> outCardLianTong(Map<String, Object> map);


    /**
     *按当前赛选条件获取导出数据 iccid
     * @param map
     * @return
     */
    public List<String> outCardIccid(Map<String, Object> map);



    /**
     *按当前赛选条件获取导出数据 iccid  [切割iccid 19 位]
     * @param map
     * @return
     */
    public List<String> outCardIccidLianTong(Map<String, Object> map);


    /**
     * 获取匹配筛选条件下的卡板id
     * @param map
     * @return
     */
    public List<String> selId(Map<String, Object> map);



    /**
     * 获取匹配筛选条件下的卡板id iccid  [切割iccid 19 位]
     * @param map
     * @return
     */
    public List<String> selIdLianTong(Map<String, Object> map);

    /**
     * 卡所属划分
     * @param map
     * @return
     */
    public int dividCard(Map<String, Object> map);


    /**
     * 通道下 已有使用量的卡 且 剩余 用量>0
     * @param map
     * @return
     */
    public List<Map<String,Object>> channelCardCommonly(Map<String, Object> map);

    /**
     * 通道下 超出用量 和 暂未使用用量查询 Serious  超出用量的 ， Slight 没用量的
     * @param map
     * @return
     */
    public List<Map<String,Object>> channelCardSeriousAndSlight(Map<String, Object> map);


    /**
     * 获取某通道下大于百分之多少的 卡信息  percentage 0.8 = 80%
     * @param map
     * @return
     */
    public List<Map<String,Object>> findPercentage(Map<String, Object> map);


    /**
     * 修改 激活时间
     * @param map
     * @return
     */
    public int updActivate(Map<String, Object> map);


    /**
     * 激活时间为空得的 iccid
     * @param map
     * @return
     */
    public List<Map<String,Object>> activate_dateNull(Map<String, Object> map);

    /**
     * 用量补偿查询
     * @param map
     * @return
     */
    public List<Map<String,Object>> findFlowCompensate(Map map);
    /**
     * 状态补偿查询
     * @param map
     * @return
     */
    public List<Map<String,Object>> findStatusCompensate(Map map);
    /**
     * 修改卡状态
     * @param map
     * @return
     */
    public int updStatusId(Map<String, Object> map);

    /**
     * 修改 批量 卡状态
     * @param map
     * @return
     */
    public int updStatusIdArr(Map<String, Object> map);


    public int updction(Map<String, Object> map);

    /**
     * 查询通道下 所属卡
     * @param map
     * @return
     */
    public List<Map<String,Object>> findChannelIdCar(Map<String, Object> map);


    /**
     * 修改 卡用量
     * @param map
     * @return
     */
    public int updUsed(Map<String, Object> map);

    /**
     * 修改 卡实名状态
     * @param map
     * @return
     */
    public int updRealNameStatus(Map<String, Object> map);


    /**
     * 通道下 所属卡 设置达量停机的卡
     * @param map
     * @return
     */
    public  List<Map<String,Object>>  findChannelIdCarStop(Map<String, Object> map);


    /**
     * 备份 卡所属关联
     * @return
     */
    public List<Map<String,Object>> BackupAssociate();


    /**
     * 查询单卡激活时间
     * @param map
     * @return
     */
    public String findActivateDate(Map<String, Object> map);


    /**
     * yz_card_info 新增 剩余
     * @param map
     * @return
     */
    public int addInfoRemaining(Map<String, Object> map);

    /**
     * 物联卡管理 设置时 备份 卡所属关联
     * @param map
     * @return
     */
    public List<Map<String,Object>> SetBackupAssociate(Map<String, Object> map);


    /**
     * 连接管理设置 修改
     * @param map
     * @return
     */
    public int SetCard(Map<String, Object> map);


    /**
     * 修改 imei
     * @param map
     * @return
     */
    public int updimei(Map<String, Object> map);




    /**
     * 查询 卡号 分配 通道
     * @param map
     * @return
     */
    public List<Map<String,Object>> findRouteArr(Map<String, Object> map);

    /**
     *月初有订购期内流量卡 进行 复机
     * */
    public List<Map<String,Object>> StatusChange(Map map);



    /**
     * 通道下 所属卡 设置 未订购资费停机 的卡
     * @param map
     * @return
     */
    public  List<Map<String,Object>>  findChannelIdDisconnected(Map<String, Object> map);

    /**
     * 通道下 所属卡 设置 未订购资费断网 的卡
     * @param map
     * @return
     */
    public  List<Map<String,Object>>  findChannelIdBreakNetwork(Map<String, Object> map);

    /**
     * 查询字典信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> findDict(Map<String, Object> map);

    /**
     * 连接管理设置 修改
     * @param map
     * @return
     */
    public int updConnectionStatus(Map<String, Object> map);

    /**
     * 查询使用降序查询时间
     * */
    public Map<String,Object> selTime(Map<String, Object> map);

    /**
     * 新增
     * @param map
     * @return
     * @throws Exception
     */
    public List<Map<String,Object>> addsel(Map map);



    /**
     * 查询 代理 所属卡
     * @param map
     * @return
     */
    public List<String> findBelongingCard(Map<String, Object> map);


    /**
     * 修改 分组 备注
     * @param map
     * @return
     */
    public List<Map<String,Object>> findGroupingAndRemarks(Map<String, Object> map);


    /**
     * 修改分组 备注
     * @param map
     * @return
     */
    public int updGroupingAndRemarks(Map<String, Object> map);


    /**
     * 查询 代理下 卡号
     * @param map
     * @return
     */
    public List<String> findAgentIccid(Map<String, Object> map);




    /**
     * 查询下级代理商 返回 格式 1,2
     * @param map
     * @return
     */
    public String queryChildrenAreaInfo(Map<String, Object> map);


    /**
     * 获取 所属代理下 卡分组
     * @param map
     * @return
     */
    public List<String> getCardGrouping(Map<String, Object> map);


    /**
     * 查询 销售 合伙人
     * @param map
     * @return
     */
    public List<Map<String,Object>> findSalesPartner(Map<String, Object> map);

    /**
     * C端充值 轮询 卡状态变更失败的 进行再次 复机；（前提是没用完用量）
     * */
    public List<Map<String,Object>> SleMulti (Map map);

    /**
     * 划卡回滚
     * @param map
     * @return
     */
    public int dividCardBk(Map<String, Object> map);



    /**
     * 修改 备注 分组
     */
    public int UpdateFill(Map<String, Object> map);

    /**
     * 同步时间
     */
    public int updDueExpireTime(Map<String, Object> map);


    /**
     * 用量详情导出 （同步导出 分组 、备注、发货时间）
     * */
    public List<Map<String,Object>> hisList (Map map);


    /**
     * 查询已订购用量状态异常卡号
     * @param map
     * @return
     */
    public List<Map<String,Object>> findStatusAbnormal (Map map);


    /**
     * 卡状态 分组 统计
     * @param map
     * @return
     */
    public List<Map<String,Object>> status_ShowIdGroup (Map map);


    /**
     * 获取 vid
     * @param map
     * @return
     */
    public List<Map<String,Object>> selVid (Map map);


    /**
     *智能匹对卡号数量
     * @param map
     * @return
     */
    public Integer cardMatchCount (Map map);

    /**
     * 智能匹对卡号获取其中一条
     * @param map
     * @return
     */
    public Map<String,Object> cardMatchOne (Map map);



    /**
     * 通道下 所属卡 已到沉默期 无激活时间 无发货日期
     * @param map
     * @return
     */
    public List<Map<String,Object>> findChannelIdCarSilencePeriod(Map<String, Object> map);


    /**
     * 状态未知 或 无状态 卡号
     * @param map
     * @return
     */
    public List<Map<String,Object>> findChannelIdCarStatusShowId(Map<String, Object> map);


    /**
     * 通道下 所属卡 当天没有用量记录的
     * @param map
     * @return
     */
    public List<Map<String,Object>> findChannelIdCarNotRecord(Map<String, Object> map);


    /**
     * 查询批量更新 前 字段
     * @param map
     * @return
     */
    public List<Map<String,Object>> findReplaceBk(Map<String, Object> map);


    /**
     * 批量更新卡信息
     * @param map
     * @return
     */
    public int updCardReplace(Map<String, Object> map);


    /**
     * 查询icci 通过 不同卡类型 获取 iccid
     * @param map
     * @return
     */
    public List<Map<String, Object>> findCardIccid(Map<String, Object> map);


    /**
     * 对外开放查询卡信息
     * @param map
     * @return
     */
    public Map<String, Object> selCardOpen(Map<String, Object> map);



    /**
     * 查询通道下 状态正常没有激活时间 且用量 =0 卡号
     * @param map
     * @return
     */
    public List<Map<String, Object>> findNormalNotActivateDate(Map<String, Object> map);


    /**
     * 查询通道下   没有激活时间 且用量>0 卡号 (不包含测试期卡号)
     * @param map
     * @return
     */
    public List<Map<String, Object>> findUsedNotActivateDate(Map<String, Object> map);


    /**
     * 修改 测试期 已有激活时间状态 为 已激活
     * @param map
     * @return
     */
    public int updStatusShowId(Map<String, Object> map);


    /**
     *  单卡 修改 备注 分组
     */
    public int UpdateSingle(Map<String, Object> map);


    /**
     * 获取字典
     * @param map
     * @return
     */
    public List<Map<String, Object>> selectDictDataByType(Map<String, Object> map);

    /**
     * 查询iccid 是否有激活时间
     * @param map
     * @return
     * */
    public List<Map<String,Object>> itemIccid(Map map);


    /**
     * 查询 卡号是否开启了短信服务且配置了短信发送号码
     * @param map
     * @return
     */
    public List<String> findSmsArr(Map<String, Object> map);
    /**
     * 查询指定通道下 卡号 是否已存在 yz_card_info 表
     * @param map
     * @return
     */
    public List<Map<String,Object>> findSynIsExist(Map map);
    void addAutoPolling(Map<String,Object> map);
    String selCardIsInAuto(Map<String,Object> map);

    void dividCardOne(Map<String, Object> map);

    void updateOther(Map<String, String> stringMap);

    Map<String, Object> selOneCardInfo(Map<String, String> stringMap);

    void updateEndTime(Map<String, Object> cardMap);

    void importCardInfoList(@Param("yzCardList") List<YzCard> yzCardList);

    List<YzCard> getCardInfoList(Map<String, Object> map);

    public int updBalance(Map<String, Object> map);

    /**
     *  单卡 补全部分信息
     */
    public int updSingleCardData(Map<String, Object> map);

    int updGroupingAndRemarkArrs(List<Map<String, Object>> card_arrs);
}













