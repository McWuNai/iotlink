package com.yunze.apiCommon.upstreamAPI.YiYuan.Inquire;


import com.yunze.apiCommon.upstreamAPI.YiYuan.YiYuan_Api;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
public  class Query_YiYuan extends YiYuan_Api {



    public Query_YiYuan(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 获取单个卡 信息
     * @param iccid
     * @param
     * @return
     */
    public static String queryInfo(String iccid) {

        String Rstr = null;
        try {
            String method = "fc.function.card.info";//资产详细信息
            String time = System.currentTimeMillis()+"";
            time = time.substring(0,time.length()-3);
            Map<String, Object> params = new HashMap<>();
            params.put("appKey",appKey);
            params.put("method",method);
            params.put("iccid",iccid);
            params.put("t",time);
            String sign = getSign(params);
            params.put("sign",sign);
            String url = server_Ip ;
            //System.out.println(reqUrl);
             Rstr = HttpUtil.doPost(url, params);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Rstr;
    }










}
