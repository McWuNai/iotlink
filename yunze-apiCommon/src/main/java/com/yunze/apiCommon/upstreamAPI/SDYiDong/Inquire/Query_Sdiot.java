package com.yunze.apiCommon.upstreamAPI.SDYiDong.Inquire;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.SDYiDong.Sdiot_Api;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_Sdiot extends Sdiot_Api {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
       /* map.put("cd_username", "sdzywlw20180510");
        map.put("cd_pwd", "sdwlwzy@2018!");*/

        map.put("cd_username", "Ib!Vs003gssBVRrPLvn&Kx7");
        map.put("cd_pwd", "h!L&9mewhitS7f%Opc3@48S2Sk");
        map.put("cd_key", "531");
        Query_Sdiot Qy = new Query_Sdiot(map);

        for (int i = 0; i < 1; i++) {
            System.out.println(Qy.queryCardStatus("1064770568005"));
        }


    }


    public Query_Sdiot(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 获取单卡的流量
     * @param queryNum 接入号
     * @param
     * @return
     */
    public static String queryFlow(String queryNum) {
        String Rstr = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("appkey",appkey);
            params.put("token",token);
            params.put("numType","1");
            params.put("queryNum",queryNum);
            params.put("provinceid",provinceid);
            String url = server_Ip + "/cmiotopen/qryGPRSRealTimeInfo";
            Rstr = HttpUtil.sendPostJson(url, JSON.toJSONString(params));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Rstr;
    }

    /**
     * 获取单卡的状态
     * 	00-正常；
     * 	01-单向停机；
     * 	02-停机；
     * 	03-预销号；
     * 	04-销号；
     * 	05-过户；
     * 	06-休眠；
     * 	07-待激；
     * 	99-号码不存在
     * @param msisdn 接入号
     * @param
     * @return
     */
    public static String queryCardStatus(String msisdn) {
        String Rstr = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("appkey",appkey);
            params.put("token",token);
            params.put("msisdn",msisdn);
            params.put("provinceid",provinceid);
            String url = server_Ip + "/cmiotopen/qryUserStatusRealSingle";
            Rstr = HttpUtil.sendPostJson(url, JSON.toJSONString(params));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Rstr;
    }


    /**
     * 在线信息查询
     * @param msisdn
     * @return
     */
    public static String qryGPRSInfo(String msisdn) {
        String Rstr = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("appkey",appkey);
            params.put("token",token);
            params.put("msisdn",msisdn);
            params.put("provinceid",provinceid);
            String url = server_Ip + "/cmiotopen/qryGPRSInfo";
            Rstr = HttpUtil.sendPostJson(url, JSON.toJSONString(params));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Rstr;
    }







}
