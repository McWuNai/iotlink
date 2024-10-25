package com.yunze.task.yunze.polling;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.mapper.yunze.polling.YzSynCardInfoMapper;
import com.yunze.common.utils.yunze.Different;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对比主表数据 与 上游成员 标记是否修改 和新增
 */
@Slf4j
@Component
public class SynCardDataComparisonTaskMQ {


    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzSynCardInfoMapper yzSynCardInfoMapper;
    @Resource
    private YzCardMapper yzCardMapper;


    @RabbitHandler
    @RabbitListener(queues = "admin_CardDataComparison_queue")
    public void DataComparison() {
        //1.状态 正常 且开启了需要同步上游成员 时
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        /*findRouteID_Map.put("cd_lunxun","notChoose");//是否轮询 不选择（只要是状态正常就去获取）*/
        findRouteID_Map.put("sync_upstream","1");// 同步上游卡号数据

        // findRouteID_Map.put("FindCd_id","39");// 调试用
        String endTime = VeDate.getStringDateShort();
        Map<String,Object> fConfigMap = new HashMap<>();
        fConfigMap.put("config_key","iotlink.synChannel.updTime");
        String longDay = yzWxByProductAgentMapper.findConfig(fConfigMap);
        longDay = longDay!=null?longDay:"2";//默认为近三天的
        int day = Integer.parseInt("-"+longDay);
        String staTime =  VeDate.getBeforeAfterDate(endTime,day);
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> ChannelObj = channelArr.get(i);
                String channel_id = ChannelObj.get("cd_id").toString();
                ChannelObj.put("channel_id",channel_id);
                ChannelObj.put("staTime",staTime);
                ChannelObj.put("endTime",endTime);
                ChannelObj.put("inconsistent_iccid","0"); //是否 与 card_info 表 iccid不一致 [为 否]
                ChannelObj.put("is_new","0");//是否 需要新增到 card_info 表 【为 否】
                ChannelObj.put("iccidNotNull","1");// ICCID 是否不能为空 【是】




                String sync_data_type = ChannelObj.get("sync_data_type")!=null ?ChannelObj.get("sync_data_type").toString():null;//'上游返回的数据-同步类型'
                List<Map<String, Object>>  toExistArr = yzCardMapper.findSynIsExist(ChannelObj);//以存在在 主表 card_info 且 没有与主表一致 且 不需要 更新到 主表的（近期通不过但没有检测是否修改 或新增的卡数据）
                List<Map<String, Object>>  recentSyncArr = yzSynCardInfoMapper.getSyncCardInfo(ChannelObj);//近期同步获取导数据的卡号
                List<Map<String, Object>> addArr = new ArrayList<>();
                List<Map<String, Object>> updArr = new ArrayList<>();
                if (toExistArr != null && toExistArr.size() > 0) {
                    if(toExistArr.size()!=recentSyncArr.size()){//以存在 表 == 近期同步获取导数据的卡号 （已经同步过了只需要对比进行修改不需要新增）
                        List<String> msisdnArr = new ArrayList<>();
                        for (int j = 0; j < toExistArr.size(); j++) {
                            String msisdn = toExistArr.get(j).get("msisdn")!=null?toExistArr.get(j).get("msisdn").toString():null;
                            if(msisdn!=null){
                                msisdnArr.add(msisdn);
                            }
                        }
                        // 获取 数组去重数据 和 重复值
                        Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(recentSyncArr, msisdnArr, "msisdn");//获取 筛选不重复的某列值 和 重复的
                        recentSyncArr = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 设置数据
                        addArr.addAll(recentSyncArr);//加入 【修改 is_new = 1 】 数组
                        List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 设置数据
                        //找出与数据库已存在 相同 msisdn 进行对比 iccid 是否一致 不一致的放入修改 队列
                        if (Out_list_Different.size() > 0) {
                            //与主表 对比 同 msisdn 》 ICCID 是否相同不相同 》加入修改 队列
                            for (int j = 0; j < Out_list_Different.size(); j++) {
                                Map<String, Object>  differentMap = Out_list_Different.get(j);
                                String msisdn = differentMap.get("msisdn")!=null?differentMap.get("msisdn").toString():null;
                                String iccid = differentMap.get("iccid")!=null?differentMap.get("iccid").toString():null;
                                if(msisdn!=null && iccid!=null){
                                    for (int k = 0; k < toExistArr.size(); k++) {
                                        Map<String, Object>  exMap = toExistArr.get(k);
                                        String ex_msisdn = exMap.get("msisdn")!=null?exMap.get("msisdn").toString():null;
                                        String ex_iccid = exMap.get("iccid")!=null?exMap.get("iccid").toString():null;
                                        //开始对比
                                        if(ex_msisdn!=null && ex_iccid!=null) {
                                            boolean bool_msisdn = msisdn.equals(ex_msisdn);
                                            boolean bool_iccid = iccid.equals(ex_iccid);
                                            if(bool_msisdn){
                                                if(!bool_iccid){
                                                    //准备放入修改数组 ；1. 判断 同步类型 [不存在时更新 0 ] [覆盖式更新 1 ]
                                                    Map<String,Object> upd_Map = new HashMap<>();
                                                    if(sync_data_type.equals("1")){
                                                        upd_Map.put("msisdn",msisdn);
                                                        updArr.add(upd_Map);//需要修改 inconsistent_iccid 的数组
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else{
                    if(recentSyncArr!=null && recentSyncArr.size()>0){//如果近期有 获取到数据 ，且 主表中 没有 相对应的卡号 进行 修改 需要新增字段
                        addArr.addAll(recentSyncArr);//加入 【修改 is_new = 1 】 数组
                    }else{
                        log.info(" ==== {} [近期没有需要同步的数据] ", JSON.toJSONString(ChannelObj));
                    }
                }

                if(addArr!=null && addArr.size()>0){
                    Map<String,Object> upd_Map = new HashMap<>();
                    upd_Map.put("is_new","1");
                    upd_Map.put("updArr",addArr);
                    int upd_count = yzSynCardInfoMapper.updateArr(upd_Map);
                    log.info(" ==== {} [is_new = 1 ] 卡数量 {} 成功数量 {}", JSON.toJSONString(ChannelObj),addArr.size(),upd_count);
                }
                if(updArr!=null && updArr.size()>0){
                    Map<String,Object> upd_Map = new HashMap<>();
                    upd_Map.put("inconsistent_iccid","1");
                    upd_Map.put("updArr",updArr);
                    int upd_count = yzSynCardInfoMapper.updateArr(upd_Map);
                    log.info(" ==== {} [inconsistent_iccid = 1 ] 卡数量 {} 成功数量 {}", JSON.toJSONString(ChannelObj),updArr.size(),upd_count);
                }
            }
        }
    }


}
