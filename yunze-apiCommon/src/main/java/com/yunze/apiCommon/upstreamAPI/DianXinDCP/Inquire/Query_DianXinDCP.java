package com.yunze.apiCommon.upstreamAPI.DianXinDCP.Inquire;



import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.DianXinDCP.DcpDxService;
import com.yunze.apiCommon.upstreamAPI.DianXinDCP.DianXin_DCP_Api;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_DianXinDCP extends DianXin_DCP_Api {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "");
        map.put("cd_pwd","");
        map.put("cd_key", "");


        Query_DianXinDCP Qy = new Query_DianXinDCP(map);
        List<String> groups = new ArrayList<>();
        groups.add("epsLocationState");
        groups.add("epsIndividualUserType");
          DcpDxService dcpDxService = new DcpDxService();
          System.out.println(dcpDxService.queryFlow("8986",map));




    }


    public Query_DianXinDCP(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 查询 SIM 卡信息接口 【没调试好！2021-10-29】
     * @param iccids : ["123123789123123"],
     * @param groups : ["enterpriseInfo"]
     *               选择要查询的信息，枚举可选如下一项或多项：
     * -enterpriseInfo（返回 companyName）
     * -packageInfo（返回订购包名称和描述）
     * -dates（返回 firstActivationDate 和 pbrExitDate）
     * -label（返回 customerLabel）
     * -simSpecification（返回 SIM 卡规格 ID）
     * -lockStateInfo（返回锁状态和被锁原因）
     * -individualApn（返回在用 Apn。如果列表为空，则
     * 订购包中所有 APN 均在用）
     * @return
     */
    public static String QuerySimResource(List<String> iccids,List<String> groups) {
        // 拼接接口地址  https://global.ct10649.com/dcpapi/rest/subscriptionManagement/v1/subscriptions/details
        String api_url = "https://global.ct10649.com/dcpapi/rest/subscriptionManagement/v1/subscriptions/details";
        //请求参数头部
        Map<String, String> headers = null;
        //请求参数 Json
        JSONObject params = null;
        // 拼接请求参数
        params = new JSONObject();
        params.put("type", "iccid");
        params.put("values", iccids);
        params.put("groups", groups);
        //请求头部参数
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Access-Token", loginToken());
        System.out.println(params.toJSONString());
        String res = HttpUtil.post(api_url, params.toJSONString(), headers);
        return res;
    }


    public static String EPSMultiSC(String msisdn,List<String> moAttributes) {
        // 拼接接口地址  https://si.global.ct10649.com/api/dcpsiapi/eps/multisc
        String api_url = "https://si.global.ct10649.com/api/dcpsiapi/eps/multisc";
        //请求参数头部
        Map<String, String> headers = null;
        //请求参数 Json
        JSONObject params = null;
        // 拼接请求参数
        params = new JSONObject();
        params.put("msisdn", msisdn);
        params.put("moAttributes", moAttributes);
        //请求头部参数
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Access-Token", loginToken());
        System.out.println(params.toJSONString());
        String res = HttpUtil.post(api_url, params.toJSONString(), headers);
        return res;
    }


    /**
     * SIM 卡信息接口（按号码）
     * @param iccid
     * @return
     */
    public static String subscriptions(String iccid) {
        // 拼接接口地址  https://global.ct10649.com/iot/api/sica/subscriptions/sims/{id}?iden tifier_type={identifier_type}&range={range}
        String api_url = "https://global.ct10649.com/iot/api/sica/subscriptions/sims/"+iccid;
        //请求参数头部
        Map<String, String> headers = null;
        //请求参数 Json
        JSONObject params = null;
        // 拼接请求参数
        params = new JSONObject();
        params.put("identifier_type", "icc");
        params.put("range", "1");
        //请求头部参数
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Access-Token", loginToken());
        String param_url = UrlUtil.getUrl(api_url, params);
        System.out.println(param_url);
        String res = HttpUtil.get(param_url, headers);
        return res;
    }






}
