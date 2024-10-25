package com.yunze.apiCommon.mapper;

import java.util.List;
import java.util.Map;

public interface YzCardPollingUtilCzcMapper {
    //查询所有通道ID
    List<String> selAllRoute();
    //查询通道ID下有多少卡
    String selRouteCardCount(String route_id);
    //修改通道卡总数
    void updRouteCount(Map<String,Object> map);
    //查询通道下的卡iccid 状态为 1
    List<String> selCardIccid(Map<String,Object> map);
    //查询通道信息
    Map<String,Object> selRouteParma(Map<String,Object> map);
    //查询真实号码
    String selMsidn(String iccid);

    List<String> selRoute();
    String selRouteCode(String route);
    Integer selCountByRoute(String route);

    List<String> selIccids(String route);
    List<String> selIccidsLimit(Map<String,Object> map);

    Map<String,Object> selRouteMap(String route);

    Map<String,Object> selCardPacket(String iccid);

    Map<String,Object> selCardUseInfo(String iccid);

    void updCardUsedByDayIsFastDayInMonth(Map<String,Object> map);

    Map<String,Object> selCardOne(String iccid);

    void insertMonth(Map<String,Object> map);

    void updAutoPollingInfo(String iccid);
    void updAutoPollingError(String iccid);
    void updAutoPollingAll();
}
