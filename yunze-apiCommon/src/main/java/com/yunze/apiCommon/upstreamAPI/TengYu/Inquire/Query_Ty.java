package com.yunze.apiCommon.upstreamAPI.TengYu.Inquire;

import com.yunze.apiCommon.upstreamAPI.TengYu.TengYu_Api;
import com.yunze.apiCommon.upstreamAPI.TengYu.change.ServiceAccept_Ty;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_Ty extends TengYu_Api {

    public Query_Ty(Map<String, Object> init_map) {
        super(init_map);
    }


    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "a2212d8fa23d22be92a7af4a0a89f7ee");
        map.put("cd_pwd", "1");
        map.put("cd_key", "1");
        ServiceAccept_Ty Ser = new ServiceAccept_Ty(map);
        Query_Ty qy = new Query_Ty(map);

        //System.out.println(qy.queryCardInfo("89860620220031652733"));
        System.out.println(Ser.changeCardStatus("89860620220031652733","0"));
    }

    /**
     * 获取卡信息
     * @param iccid
     * @return
     */
    public String queryCardInfo(String iccid)  {
        String method =   "/card/getcardInfo";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("appid", API_ID);
        params.put("iccid", iccid);
        String param_url = UrlUtil.getUrl(server_Ip+method, params);
        String res = HttpUtil.get(param_url);
        return res;
    }








}
