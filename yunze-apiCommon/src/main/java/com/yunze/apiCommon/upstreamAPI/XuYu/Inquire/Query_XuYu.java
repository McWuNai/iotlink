package com.yunze.apiCommon.upstreamAPI.XuYu.Inquire;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.XuYu.XunYu_Api;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_XuYu extends XunYu_Api {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "lMRucLU18aK3pIK6");//
        map.put("cd_pwd", "https://icmp.shingsou.com/open");//
        map.put("cd_key", "56Js4cNZ5");//

        Query_XuYu Qy = new Query_XuYu(map);

        for (int i = 0; i < 1; i++) {
            System.out.println(Qy.queryInfo("1440460073870"));
        }

    }


    public Query_XuYu(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 获取单个卡的流量
     * @param accessNum 接入号
     * @param
     * @return
     */
    public static String queryInfo(String accessNum) {
        String Rstr = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("msisdn",accessNum);
            params.put("agencyCode",agencyCode);
            String sign = getSign("msisdn="+accessNum+"&agencyCode="+agencyCode);
            params.put("key",key);
            params.put("sign",sign);
            String url = server_Ip + "/queryOpenCardInfoSingle";
            Rstr = HttpUtil.sendPostJson(url, JSON.toJSONString(params));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Rstr;
    }










}
