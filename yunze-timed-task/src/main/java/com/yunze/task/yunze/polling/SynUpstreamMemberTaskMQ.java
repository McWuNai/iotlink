
package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.InternalApiRequest;
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
public class SynUpstreamMemberTaskMQ {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzUpstreamGroupMapper yzUpstreamGroupMapper;




    @RabbitHandler
    @RabbitListener(queues = "admin_SynUpstreamMember_queue")
    public void SynOfferinginfo() {


        //1.状态 正常 且开启了需要同步上游成员 时
        Map<String,Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id",null);
        /*findRouteID_Map.put("cd_lunxun","notChoose");//是否轮询 不选择（只要是状态正常就去获取）*/
        findRouteID_Map.put("sync_upstream","1");// 同步上游卡号数据

        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> ChannelObj = channelArr.get(i);
                int startNum = 1;
                synTurnPages(ChannelObj,startNum);
            }
        }
    }

    /**
     * 翻页同步
     * @param ChannelObj
     * @param startNum
     */
    public void synTurnPages(Map<String, Object> ChannelObj,int startNum){
        Map<String,Object> map = new HashMap<>();
        int pageSize = 50;
        Integer totalCount = 0;
        map.put("pageSize",pageSize);
        map.put("startNum",startNum);
        List<Map<String, Object>> updArr = new ArrayList<>();
        List<Map<String, Object>> addArr = new ArrayList<>();
        Map<String,Object> Rdata = internalApiRequest.queryGroupInfo(map,ChannelObj);
        String code = Rdata.get("code").toString();
        if(code.equals("200")){
            totalCount = Integer.parseInt(Rdata.get("totalCount").toString());
            List<Map<String, Object>> groupList = ((List<Map<String, Object>>)Rdata.get("groupList"));
            for (int j = 0; j < groupList.size(); j++) {
                Map<String, Object>  group = groupList.get(j);
                group.put("channel_id",ChannelObj.get("cd_id"));
                Integer is_exist = yzUpstreamGroupMapper.is_exist(group);
                is_exist = is_exist!=null?is_exist:0;
                if(is_exist>0){
                    updArr.add(group);
                }else{
                    addArr.add(group);
                }
            }
            int saveCount = 0;
            int updCount = 0;
            if(addArr.size()>0){
                Map<String,Object> saveMap = new HashMap<>();
                saveMap.put("arrs",addArr);
                saveCount += yzUpstreamGroupMapper.save(saveMap);
            }

            if(updArr.size()>0){
                for (int j = 0; j < updArr.size(); j++) {
                    Map<String,Object> updMap = updArr.get(j);
                    updCount += yzUpstreamGroupMapper.update(updMap);
                }
            }
            log.info("当前页号【"+startNum+"】 本次新增 "+saveCount+" 本次修改 " +updCount+" "+JSON.toJSONString(ChannelObj));
            //如果获取的集团数据大于 最大分页数*当前页号 需再次执行一次
            if(totalCount>pageSize*startNum){
                startNum +=1;//翻页
                synTurnPages(ChannelObj,startNum);
            }
        }
    }


}
