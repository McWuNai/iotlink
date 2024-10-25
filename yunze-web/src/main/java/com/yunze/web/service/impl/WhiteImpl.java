package com.yunze.web.service.impl;

import com.yunze.apiCommon.upstreamAPI.YiDongEC.Inquire.Query_YD;

import com.yunze.cn.mapper.YzVWhiteMapper;
import com.yunze.web.service.White;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 程志超
 * @Date: 2023年10月16日17:22:20
 * @Description:
 */
@Component
public class WhiteImpl implements White {
    @Resource
    private YzVWhiteMapper yzVWhiteMapper;


    @Override
    public Map<String, Object> selVoice(Map<String, Object> Pmap) {
        return null;
    }

    @Override
    public Map<String, Object> selRouteCreate(Map<String, Object> Pmap) {
       Map<String, Object> route = yzVWhiteMapper.selRoute(Pmap);
        String channel_id = route.get("channel_id").toString();//通道编码
        Map<String, Object> routeCreateMap = yzVWhiteMapper.selRouteCreate(route);//通道信息 用户名等地址
        return routeCreateMap;
    }

    @Override
    public Map<String, Object> setWhite(Map<String, Object> Pmap) {//设置白名单
        Map<String, Object> rMap = new HashMap<String,Object>();//返回map
        Map<String, Object> routeCreateMap = selRouteCreate(Pmap);//获取通道信息
        if (routeCreateMap.get("cd_code").toString().equals("YiDong_EC")){
            Query_YD query = new Query_YD(routeCreateMap, routeCreateMap.get("cd_code").toString());//移动查询类
            String groupId = query.getGroup(Pmap.get("iccid").toString());// 查询 语音分组ID
            Pmap.put("groupId",groupId);
            Map<String, Object> map = query.setWhite(Pmap);//配置白名单 测试
            return map;
        }
        return null;
    }

    @Override
    public Map<String, Object> updWhite(Map<String, Object> Pmap) {
        return null;
    }

    @Override
    public Map<String, Object> delWhite(Map<String, Object> Pmap) {
        Map<String, Object> rMap = new HashMap<String,Object>();//返回map
        Map<String, Object> routeCreateMap = selRouteCreate(Pmap);//获取通道信息
        if (routeCreateMap.get("cd_code").toString().equals("YiDong_EC")){
            Query_YD query = new Query_YD(routeCreateMap, routeCreateMap.get("cd_code").toString());//移动查询类
            String groupId = query.getGroup(Pmap.get("iccid").toString());// 查询 语音分组ID
            Pmap.put("groupId",groupId);
            Map<String, Object> map = query.delWhite(Pmap);//删除白名单
            return map;
        }
        return null;
    }

    @Override
    public Map<String, Object> List(Map<String, Object> Pmap) {//获取列表

        Map<String, Object> rMap = new HashMap<String,Object>();//返回map

        Map<String, Object> routeCreateMap = selRouteCreate(Pmap);//获取通道信息
        //判断cd_code 是否为移动EC YiDong_EC
        if (routeCreateMap.get("cd_code").toString().equals("YiDong_EC")){
            //是 查询白名单列表 与 语音剩余
            //
            Query_YD query = new Query_YD(routeCreateMap, routeCreateMap.get("cd_code").toString());//移动查询类
            String groupId = query.getGroup(Pmap.get("iccid").toString());// 查询 语音分组ID
            //获取 白名单列表
            Pmap.put("groupId",groupId);
            List<Map<String, Object>> whiteMap = query.getWhite(Pmap);
            int size = whiteMap.size();

            String[] arr= {"a","b","c","d","e"};
            if (5-size>=0){
                for (int i = 0; i < 5 - size; i++) {
                    Map<String, Object> addMpa = new HashMap<>();
                    addMpa.put("whiteNumber","");
                    addMpa.put("status","1");
                    addMpa.put("status",arr[i]);
                    whiteMap.add(addMpa);
                }
            }

            rMap.put("whiteMap",whiteMap);
            //query.setWhite(Pmap); 配置白名单 测试
            //获取 语音通话
            String use = query.queryWhiteVoice(Pmap);
            rMap.put("use",use);
            return rMap;
        }else{
            //否 结束
        }

        return null;
    }

    @Override
    public Map<String, Object> getPre(Map<String, Object> Pmap) {// iccid -> xxx
        HashMap<String, Object> rMap = new HashMap<>();

        Map<String, Object> packetMap = yzVWhiteMapper.getPacketForIccid(Pmap);
        Map<String, Object> preForPacket = yzVWhiteMapper.getPreForPacket(packetMap);
        String pre = preForPacket.get("pre").toString();
        if (pre .equals("1")){
            rMap.put("use","0");
            return rMap;
        }
        rMap.put("use","1");


        return rMap;
    }
}
