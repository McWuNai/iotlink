package com.yunze.apiCommon.upstreamAPI.YiKong.Inquire;

import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.YiKong.YiKong_Api;
import com.yunze.apiCommon.upstreamAPI.YiKong.change.ServiceAccept_YK;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_YK extends YiKong_Api {

    public Query_YK(Map<String, Object> init_map) {
        super(init_map);
    }


    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "YZ345@2021");
        map.put("cd_pwd", "babd8389eb4ee5c8");
        map.put("cd_key", "c676c540534aa377");
        ServiceAccept_YK Ser = new ServiceAccept_YK(map);
        Query_YK qy = new Query_YK(map);



        //8986111924708526259 8986111924708526261
        //System.out.println(qy.getSIMInfo("8986031941208660333"));
        //System.out.println(qy.queryTraffic("8986111924708527186"));
        //System.out.println(qy.queryCardStatus("8986031941208660333"));//已激活、去激活、停机、拆机s
        System.out.println(Ser.changeCardStatus("8986111924708526259","0"));
        //System.out.println(Ser.IMEIRe("8986031941208660333"));//  {"resultCode":"0","description":{"iccid":"8986031941208660333","msg":"已成功申请机卡重绑，请耐心等待"},"resultMsg":"配置成功"}
    }

    /**
     * 单卡接口查询卡状态
     * @param iccid
     * @return
     */
    public String queryCardStatus(String iccid)  {
        String method =   "queryCardMainStatus";
        JSONObject json = new JSONObject();
        json.put("method", method);
        json.put("iccid", iccid);
        String Str = null;
        try {
            Str = send(null,json);
        }catch (Exception e){
            System.out.println("Query_Rht queryCardStatus 异常 "+e.getMessage());
        }
        return Str;
    }


    /**
     * 单卡当月流量查询接口
     * @param iccid
     * @return
     */
    public String queryTraffic(String iccid)  {
        String method =   "queryTraffic";
        JSONObject json = new JSONObject();
        json.put("method", method);
        json.put("iccid", iccid);
        String Str = null;
        try {
            Str = send(null,json);
        }catch (Exception e){
            System.out.println("Query_Rht queryTraffic 异常 "+e.getMessage());
        }
        return Str;
    }


    /**
     * 长周期卡单卡查询接口
     * @param iccid
     * @return
     */
    public String getSIMInfo(String iccid)  {
        String method =   "getSIMInfo";
        JSONObject json = new JSONObject();
        json.put("method", method);
        json.put("iccid", iccid);
        String Str = null;
        try {
            Str = send(null,json);
        }catch (Exception e){
            System.out.println("Query_Rht getSIMInfo 异常 "+e.getMessage());
        }
        return Str;
    }




}
