package com.yunze.apiCommon.upstreamAPI.ShuoLang.Inquire;


import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.ShuoLang.ShuoLang_Api;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_Shuolang extends ShuoLang_Api {


    public Query_Shuolang(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 获取单卡信息 用量 是 KB
     * @param cardno 接入号
     * @param
     * @return
     */
    public static String queryInfo(String cardno) {
        String Rstr = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("cardno", cardno);
            String times = System.currentTimeMillis()+"";
            params.put("times", times);
            String url = server_Ip + "/api/v1/getChaxun";
            params.put("sign", getSign(times));

            Rstr = HttpUtil.get(UrlUtil.getUrl(url, params));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Rstr;
    }










}
