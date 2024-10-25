package com.yunze.apiCommon.upstreamAPI.YiDongEcV2.Inquire;

import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.YiDongEcV2.YD_EcV2_Api;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
@Slf4j
public class Query_EcV2 extends YD_EcV2_Api {


    public Query_EcV2(Map<String, Object> init_map) {
        super(init_map);
    }

    public static void main(String[] args) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "");
        map.put("cd_pwd", ",");
        map.put("cd_key", "");
        Query_EcV2 Qy = new Query_EcV2(map);

        for (int i = 0; i < 1; i++) {
            System.out.println(Qy.gprsrealtimeinfo("8986041"));
        }

    }



    /**
     * 套餐内GPRS流量使用情况实时查询
     * @param iccid
     * @return
     */
    public String gprsrealtimeinfo(String iccid)  {
        String functionNm =   "/gprsrealtimeinfo";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        json.put("transid", transid);
        json.put("token", token);
        json.put("appid", appId);
        json.put("ebid", "0001000000083");
        json.put("iccid", iccid);
        String Str = send(url,json);
        log.info(">>  {} gprsrealtimeinfo  :{} <<", iccid, Str);
        return Str;
    }


    /**
     * 单卡接口查询卡状态
     * @param iccid
     * @return
     */
    public String queryCardStatus(String iccid)  {
        String functionNm =   "/userstatusrealsingle";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        json.put("transid", transid);
        json.put("token", token);
        json.put("appid", appId);
        json.put("ebid", "0001000000009");
        json.put("iccid", iccid);
        String Str = send(url,json);
        log.info(">>  {} queryCardStatus  :{} <<", iccid, Str);
        return Str;
    }

    /**
     * 月流量查询接口
     * @param iccid
     * @return
     */
    public String queryCardFlow(String iccid) {
        String functionNm =   "/gprsusedinfosingle";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        json.put("transid", transid);
        json.put("token", token);
        json.put("appid", appId);
        json.put("ebid", "0001000000012");
        json.put("iccid", iccid);
        String Str = send(url,json);
        log.info(">>  {} queryCardFlow  :{} <<", iccid, Str);
        return Str;
    }





    /**
     * 单卡在线信息实时查询
     * @param iccid
     * @return
     */
    public String sim_session(String iccid) {
        String functionNm =   "/gprsrealsingle";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        json.put("transid", transid);
        json.put("token", token);
        json.put("appid", appId);
        json.put("ebid", "0001000000008");
        json.put("iccid", iccid);
        String Str = send(url,json);
        return Str;
    }

    /**
     * 物联卡资费套餐查询
     * @param iccid
     * @return
     */
    public String querycardprodinfod(String iccid) {
        String functionNm =   "/querycardprodinfo";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        json.put("transid", transid);
        json.put("token", token);
        json.put("appid", appId);
        json.put("ebid", "0001000000264");
        json.put("iccid", iccid);
        String Str = send(url,json);
        return Str;
    }


}
