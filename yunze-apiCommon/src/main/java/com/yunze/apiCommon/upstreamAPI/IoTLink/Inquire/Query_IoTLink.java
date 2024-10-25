package com.yunze.apiCommon.upstreamAPI.IoTLink.Inquire;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.IoTLink.IoTLink_Api;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.*;

/**
 * 查询类接口
 */
public class Query_IoTLink extends IoTLink_Api {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "");
        map.put("cd_pwd", "");
        map.put("cd_key", "http://");


        Query_IoTLink Qy = new Query_IoTLink(map);
        System.out.println(Qy.cardInfo("89860"));

    }


    public Query_IoTLink(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 卡账期用量查询
     * @param iccid
     */
    public  String simDataUsage(String iccid) {
        String api_url = server_Ip + "/simDataUsage";
        Map<String, Object> Parmap = new HashMap<String, Object>();
        Parmap.put("iccid", iccid);
        String data = JSON.toJSONString(getPmap(Parmap));
        return HttpUtil.post(api_url, data);
    }

    /**
     * 卡生命周期查询
     * @param iccid
     */
    public  String simStatus(String iccid) {
        String api_url = server_Ip + "/simStatus";
        Map<String, Object> Parmap = new HashMap<String, Object>();
        Parmap.put("iccid", iccid);
        String data = JSON.toJSONString(getPmap(Parmap));
        return HttpUtil.post(api_url, data);
    }

    /**
     * 在网状态查询
     * @param iccid
     */
    public  String simSession(String iccid) {
        String api_url = server_Ip + "/simSession";
        Map<String, Object> Parmap = new HashMap<String, Object>();
        Parmap.put("iccid", iccid);
        String data = JSON.toJSONString(getPmap(Parmap));
        return HttpUtil.post(api_url, data);
    }



    /**
     * 卡信息详情
     * @param iccid
     */
    public  String cardInfo(String iccid) {
        String api_url = server_Ip + "/cardInfo";
        Map<String, Object> Parmap = new HashMap<String, Object>();
        Parmap.put("cardno", iccid);
        Parmap.put("type", "iccid");
        String data = JSON.toJSONString(getPmap(Parmap));
        return HttpUtil.post(api_url, data);
    }


}
