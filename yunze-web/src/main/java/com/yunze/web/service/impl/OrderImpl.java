package com.yunze.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.yunze.web.service.IOrder;
import com.yunze.cn.mapper.YzCardFlowMapper;
import com.yunze.cn.mapper.YzOrderMapper;
import com.yunze.cn.service.impl.YzCardServiceImpl;
import com.yunze.cn.util.RedisUtil_Yz;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/09/10/17:36
 * @Description:
 */
@Component
public class OrderImpl implements IOrder {

    @Resource
    private YzCardServiceImpl yzCardServiceImpl;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    @Resource
    private RedisUtil_Yz redisUtilYz;



    @Override
    public Map<String, Object> queryOrders(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        List<Map<String,Object>> Arr = new ArrayList<>();
        Rmap.put("code", "500");
       try {
           List<Map<String,Object>> OrderArr = yzOrderMapper.WxfindOrder(Pmap);
           if(OrderArr!=null && OrderArr.size()>0){
               for (int i = 0; i < OrderArr.size(); i++) {
                   Map<String, Object> Obj = OrderArr.get(i);
                   if(Obj.get("add_parameter")!=null){
                       Map<String, Object> add_parameter = JSON.parseObject(Obj.get("add_parameter").toString());
                       Obj.put("packet_flow",add_parameter.get("packet_flow"));
                       Obj.put("packet_valid_name",add_parameter.get("packet_valid_name"));
                       Obj.put("packet_valid_time",add_parameter.get("packet_valid_time"));

                       Obj.remove("add_parameter");
                   }
                   Arr.add(Obj);
               }
           }
           Rmap.put("Data", Arr);
           Rmap.put("code", "200");
       }catch (Exception e){
           Rmap.put("Message", "未找到号码！信息同步取消！");
       }
        return Rmap;
    }

    @Override
    public Map<String, Object> queryPackage(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        List<Map<String,Object>> cardPackets = new ArrayList<>();
        Rmap.put("code", "500");
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        try {
            if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
                String agent_id = IccidMap.get("agent_id").toString();

                Object obj = redisUtilYz.redisTemplate.opsForValue().get(agent_id+"_web_cardPackets");
                if (obj != null) {
                    cardPackets = (List<Map<String, Object>>) obj;
                }else {
                    if(agent_id.equals("100")){
                        cardPackets = yzCardFlowMapper.FindPacketName_concise(Pmap);
                    }else{
                        Pmap.put("agent_id",agent_id);
                        cardPackets = yzCardFlowMapper.FindPacketNameAgent_concise(Pmap);
                    }
                    redisUtilYz.setExpire(agent_id+"_web_cardPackets", cardPackets,60 * 5);//5分钟 资费计划名称缓存
                }





                Rmap.put("Data", cardPackets);
                Rmap.put("code", "200");
            } else {
                Rmap.put("code", "500");
                Rmap.put("Message", "未找到号码！信息同步取消！");
            }
        }catch (Exception e){
            System.out.println( "获取资费计划异常 " + e.getMessage());
            Rmap.put("Message", "获取资费计划异常!");
        }
        return Rmap;
    }








    @Override
    public Map<String, Object> WxfindOrderDs(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        List<Map<String,Object>> Arr = new ArrayList<>();
        Rmap.put("code", "500");
        try {
            Rmap.put("Data", yzOrderMapper.WxfindOrderDs(Pmap));
            Rmap.put("code", "200");
        }catch (Exception e){
            System.out.println("WxfindOrderDs 异常 "+e.getMessage());
            Rmap.put("Message", "订单详情查询异常！");
        }
        return Rmap;
    }



}
