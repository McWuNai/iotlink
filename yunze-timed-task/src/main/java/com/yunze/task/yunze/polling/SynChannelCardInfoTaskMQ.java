
package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.CardStatusReplacementUtil;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.mapper.yunze.polling.YzSynCardInfoMapper;
import com.yunze.common.mapper.yunze.polling.YzUpstreamGroupMapper;
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
 * 上游通道配置 了 需要 同步上游卡号数据 的进行同步成员信息（通道正常的）
 */
@Slf4j
@Component
public class SynChannelCardInfoTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzUpstreamGroupMapper yzUpstreamGroupMapper;
    @Resource
    private YzSynCardInfoMapper yzSynCardInfoMapper;
    @Resource
    private CardStatusReplacementUtil cardStatusReplacementUtil;


    @RabbitHandler
    @RabbitListener(queues = "admin_SynCardInfo_queue")
    public void SynCardInfo() {
        //1.状态 正常 且开启了需要同步上游成员 时
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);

        //调试用
//        List<String> cd_codeArr = new ArrayList<>();
//        cd_codeArr.add("DianXin_CMP_5G");
//        findRouteID_Map.put("cd_codeArr",cd_codeArr);


        /*findRouteID_Map.put("cd_lunxun","notChoose");//是否轮询 不选择（只要是状态正常就去获取）*/
        findRouteID_Map.put("sync_upstream","1");// 同步上游卡号数据

        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> ChannelObj = channelArr.get(i);
                String cd_code = ChannelObj.get("cd_code").toString();
                ChannelObj.put("channel_id",ChannelObj.get("cd_id"));
                int startNum = 1;
                if(cd_code.equals("DianXin_CMP") || cd_code.equals("DianXin_CMP_5G")){//电信CMP 需要要获取 成员组直接 查询
                    synTurnPages(ChannelObj,startNum,"");
                }else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")){
                    List<String> groupIdArr = yzUpstreamGroupMapper.findGroupId(ChannelObj);
                    if (groupIdArr != null && groupIdArr.size() > 0) {
                        for (int j = 0; j <  groupIdArr.size(); j++) {
                            String groupId = groupIdArr.get(j);
                            synTurnPages(ChannelObj,startNum,groupId);
                        }
                    }
                }else{
                    System.out.println("admin_SynCardInfo_queue 未匹对到通道类型");
                }

            }
        }
    }

    /**
     * 翻页同步
     * @param ChannelObj
     * @param startNum
     * @param groupId
     */
    public void synTurnPages(Map<String, Object> ChannelObj,int startNum,String groupId){
        boolean is_break = false;
        Map<String,Object> map = new HashMap<>();
        int pageSize = 50;
        Integer totalCount = 0;
        map.put("pageSize",pageSize);
        map.put("startNum",startNum);
        map.put("groupId",groupId);
        List<Map<String, Object>> updArr = new ArrayList<>();
        List<Map<String, Object>> addArr = new ArrayList<>();
        Map<String,Object> Rdata = internalApiRequest.queryGroupMember(map,ChannelObj);
        String code = Rdata.get("code").toString();
        String cd_code = Rdata.get("cd_code").toString();
        if(code.equals("200")){
            List<Map<String, Object>> memberinfoList = ((List<Map<String, Object>>) Rdata.get("memberinfoList"));
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    totalCount = Integer.parseInt(Rdata.get("totalCount").toString());
                    for (int j = 0; j < memberinfoList.size(); j++) {
                        Map<String, Object> group = memberinfoList.get(j);
                        group.put("msisdn", group.get("memberNum"));
                        group.put("channel_id", ChannelObj.get("cd_id"));
                        group.put("groupId", groupId);
                        Integer is_exist = yzSynCardInfoMapper.is_exist(group);
                        is_exist = is_exist != null ? is_exist : 0;
                        if (is_exist > 0) {
                            updArr.add(group);
                        } else {
                            addArr.add(group);
                        }
                    }
                } else if (cd_code.equals("DianXin_CMP")  || cd_code.equals("DianXin_CMP_5G")) {
                    totalCount = 99999999;//没有这个返回值直接给个默认最大值
                    for (int j = 0; j < memberinfoList.size(); j++) {
                        Map<String, Object> memberinfo = memberinfoList.get(j);
                        Map<String, Object> group = new HashMap<>();
                        String activationTime = memberinfo.get("activationTime")!=null?memberinfo.get("activationTime").toString():null;
                        String createTime = memberinfo.get("createTime")!=null?memberinfo.get("createTime").toString():null;
                        String custId = memberinfo.get("custId")!=null?memberinfo.get("custId").toString():null;

                        List<String> simStatus = memberinfo.get("simStatus")!=null? (List<String>) memberinfo.get("simStatus") :null;

                        group.put("msisdn", memberinfo.get("accNumber"));
                        group.put("iccid", memberinfo.get("iccid"));
                        if(activationTime!=null){
                           activationTime = activationTime.length()==8?activationTime.substring(0,4)+"-"+activationTime.substring(4,6)+"-"+activationTime.substring(6,8)+" 00:00:00":null;
                            if(activationTime!=null) {
                                group.put("activate_date", activationTime);
                            }else{
                                is_break = true;
                            }
                        }
                        if(createTime!=null){
                            createTime = createTime.length()==8?createTime.substring(0,4)+"-"+createTime.substring(4,6)+"-"+createTime.substring(6,8)+" 00:00:00":null;
                            if(createTime!=null) {
                                group.put("open_date", createTime);
                            }
                        }
                        if(custId!=null) {
                            group.put("custId", custId);
                        }
                        if(simStatus.get(0)!=null){
                            Integer status_id = Integer.parseInt(simStatus.get(0));
                            Map<String,Object> CardStatusMap = null;
                            if(cd_code.equals("DianXin_CMP")){
                                CardStatusMap = cardStatusReplacementUtil.getDianXin_CMP_CardStatus(status_id);
                                Integer statusCode = Integer.parseInt(CardStatusMap.get("statusCode").toString());
                                group.put("status_id", statusCode);
                            } else if (cd_code.equals("DianXin_CMP_5G")) {
                                CardStatusMap = cardStatusReplacementUtil.getDianXin_CMP_5G_CardStatus(""+status_id);
                                Integer statusCode = Integer.parseInt(CardStatusMap.get("statusCode").toString());
                                group.put("status_id", statusCode);
                            }
                        }
                        group.put("channel_id", ChannelObj.get("cd_id"));
                        group.put("groupId", groupId);
                        Integer is_exist = yzSynCardInfoMapper.is_exist(group);
                        is_exist = is_exist != null ? is_exist : 0;
                        if (is_exist > 0) {
                            updArr.add(group);
                        } else {
                            addArr.add(group);
                        }
                    }
                }
            }catch (Exception e){
                System.out.println("internalApiRequest.queryGroupMember 解析异常 "+e.getMessage());
            }
            int saveCount = 0;
            int updCount = 0;
            if(addArr.size()>0){
                Map<String,Object> saveMap = new HashMap<>();
                saveMap.put("arrs",addArr);
                saveCount += yzSynCardInfoMapper.save(saveMap);
            }
            if(updArr.size()>0){
                for (int j = 0; j < updArr.size(); j++) {
                    Map<String,Object> updMap = updArr.get(j);
                    updCount += yzSynCardInfoMapper.update(updMap);
                }
            }
            log.info("当前页号【"+startNum+"】 本次新增 "+saveCount+" 本次修改 " +updCount+" "+JSON.toJSONString(ChannelObj));
            //如果获取的集团数据大于 最大分页数*当前页号 需再次执行一次
            is_break = totalCount>pageSize*startNum?false:true;
            if(startNum>100000){is_break= true;}//默认最大页数10W页 防止死循环
            if(!is_break){
                startNum +=1;//翻页
                synTurnPages(ChannelObj,startNum,groupId);
            }
        }
    }

    public static void main(String[] args) {
        String time = "2018-05-27";
        System.out.println(time.substring(0,4)+" "+time.substring(5,7)+" "+time.substring(8,10));

    }

}
