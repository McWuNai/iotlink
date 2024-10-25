package com.yunze.apiCommon.upstreamAPI.DianXinDCP.change;

import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.DianXinDCP.DianXin_DCP_Api;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 变更 接口
 */
public class serviceAccept_DianXinDCP extends DianXin_DCP_Api {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "");
        map.put("cd_pwd","");
        map.put("cd_key", "");



        List<String> arr = new ArrayList<>();
        arr.add("898611");


        serviceAccept_DianXinDCP Qy = new serviceAccept_DianXinDCP(map);
        for (int i = 0; i <arr.size() ; i++) {
            String iccid = arr.get(i);
            System.out.println(Qy.stateChanges(iccid,"iccid","3"));

        }
    }


    public serviceAccept_DianXinDCP(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 机卡分离解绑
     * @param iccid
     * @param contactName
     * @param contactPhone
     * @return
     */
    public static String unlockSimbind(String iccid,String contactName,String contactPhone) {
        // 拼接接口地址  https://si.global.ct10649.com/api/dcpsiapi /unlock/simbind
        String api_url = server_Ip + "/api/dcpsiapi/unlock/simbind";
        //请求参数头部
        Map<String, String> headers = null;
        //请求参数 Json
        JSONObject params = null;
        // 拼接请求参数
        params = new JSONObject();
        customerNo = customerNo!=null?customerNo:"31000069";//默认
        params.put("customerNo", customerNo);
        params.put("iccid", iccid);
        params.put("contactName", contactName);
        params.put("contactPhone", contactPhone);
        //请求头部参数
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Access-Token", loginToken());
        String res = HttpUtil.post(api_url, params.toJSONString(), headers);
        return res;
    }



    /**
     * UE  号码级速率限制更新接口
     * @param cardno SIM ID
     * @param UE_AMBR_DL 下行速率
     */
    public static String ueAmbrUpdate(String cardno,String UE_AMBR_DL,String type) {

        // 拼接接口地址
        String api_url = server_Ip + "/api/dcpsiapi/ue/ambr/update";
        //请求参数头部
        Map<String, String> headers = null;
        //请求参数 Json
        JSONObject params = null;
        // 拼接请求参数
        params = new JSONObject();
        if(type!=null) {
            params.put("msisdn", cardno);
        }else {
            params.put("imsi", cardno);
        }
        //params.put("UE_AMBR_UL", cardType);//上行速率
        params.put("UE_AMBR_DL", UE_AMBR_DL);//下行速率
        //请求头部参数
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Access-Token", loginToken());
        String res = HttpUtil.post(api_url, params.toJSONString(), headers);
        return res;
    }


    /**
     * UE  取消实名
     * @param cardno SIM ID
     */
    public static String realNameRemove(String cardno,String type) {
        // 拼接接口地址
        String api_url = server_Ip + "/api/dcpsiapi/realName/remove";
        //请求参数头部
        Map<String, String> headers = null;
        //请求参数 Json
        JSONObject params = null;
        // 拼接请求参数
        params = new JSONObject();
        if(type!=null) {
            if(type.equals("iccid")){
                params.put("iccid", cardno);
            }else if(type.equals("msisdn")){
                params.put("msisdn", cardno);
            }
        }else {
            params.put("imsi", cardno);
        }
        //请求头部参数
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Access-Token", loginToken());
        String param_url = UrlUtil.getUrl(api_url, params);
        //System.out.println(param_url);
        String res = HttpUtil.get(param_url,headers);
        return res;
    }

    /**
     *
     * @param cardno
     * @param type
     * @param opState  -ACTIVE(已激活) -PAUSE（停机保号） -DEACTIVATED（去激活） -ACTIVE_NO_BILLING（激活无计费） -DEACTIVATED_NO_BILLING（去激活无计费）
     * @return
     */
    public static String stateChanges(String cardno,String type,String opState) {

        //请求参数 Json
        JSONObject params = null;
        // 拼接请求参数
        params = new JSONObject();
        JSONObject change = new JSONObject();
        JSONObject subscriptions = new JSONObject();
        JSONObject on_behalf_of = new JSONObject();
        String state = null;
        switch (opState) {
            case "2":
                state = "ACTIVE";
                break;
            case "3":
                state = "PAUSE";
                break;
            default:
                break;
        }
        change.put("state",state);
        // 拼接接口地址
        String api_url = "https://global.ct10649.com/iot/api/sica/subscriptions/requests/state-changes";
        //请求参数头部
        Map<String, String> headers = null;
        on_behalf_of.put("company_id",customerNo);






        List<String> identifiers = new ArrayList<>();
        identifiers.add(cardno);
        subscriptions.put("identifier_type",type);
        subscriptions.put("identifiers",identifiers);



        params.put("subscriptions", subscriptions);
        params.put("change", change);
        params.put("on_behalf_of", on_behalf_of);


        //请求头部参数
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Access-Token", loginToken());
        String param_url = UrlUtil.getUrl(api_url, params);
        System.out.println(param_url);
        String res = HttpUtil.post(api_url, params.toJSONString(), headers);

        return res;
    }


}
