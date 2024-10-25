package com.yunze.apiCommon.upstreamAPI.YiDongEC.Inquire;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.YD_EC_Api;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;
import com.yunze.apiCommon.utils.VeDate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_YD extends YD_EC_Api {



    public Query_YD() {
        super();
    }

    public Query_YD(Map<String, Object> init_map, String cd_code) {
        super(init_map,cd_code);
    }






    /**
     * 单卡本月套餐内流量使用量实时查询
     * @param card_no
     * @return
     */
    public String queryGprsMarginBySim(String card_no) {
        String functionNm =   "/query/sim-data-margin";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String url = server_Ip + functionNm;
        String Str = send(url,json);
        return repeat(Str,json,url);
    }
    public String autoPolling(List<String> cardForiList){
        String functionNm = "/query/sim-data-usage-monthly/batch";
        String url = server_Ip+functionNm;
        //https://api.iot.10086.cn/v5/ec/query/sim-data-usage-daily/batch?transid=xxxxxx&token=xxxxx&iccids=xxx_xxx_xxx&queryDate= xxxxxx
        String transid = appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date());
        String queryDate = VeDate.getYesterday();
        String pinUrl=url+"?"+"transid="+transid+"&token="+token+"&iccids=";
        for (int i = 0; i < cardForiList.size(); i++) {

            if (i==0){
                pinUrl =pinUrl+cardForiList.get(i);
            }else{
                pinUrl =pinUrl+"_"+cardForiList.get(i);
            }
        }
        pinUrl = pinUrl + "&queryDate=" + queryDate;
        String res = HttpUtil.get(pinUrl);
        return res;
    }
    public String getGroup(String card_no) {//查询 卡号所在 的 语音通信分组
        //String functionNm =   "/query/sim-voice-usage";//成功 但是 无信息https://api.iot.10086.cn/v5/ec/query/group-by-member
        String functionNm = "/query/group-by-member";//查询不到信息
        String pinUrl = server_Ip + functionNm + "?transid=" + appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()) + "&token=" + token + "&iccid=" + card_no;
        String res = HttpUtil.get(pinUrl);
        Map<String, Object> resMap = (Map<String, Object>) JSONObject.parseObject(res);
        String status = resMap.get("status").toString();
        if (status.equals("0")) {//成功
            List result = (List) resMap.get("result");
            Map<String, Object> groupList = (Map<String, Object>) result.get(0);
            List<Map<String, Object>> all = (List<Map<String, Object>>) groupList.get("groupList");
            for (int i = 0; i < all.size(); i++) {
                Map<String, Object> map = all.get(i);
                String offeringName = map.get("offeringName").toString();
                if (offeringName.indexOf("语音通信") != -1) {//存在
                    return map.get("groupId").toString();
                }
            }

        }

        return "错误!!";
    }
    public String queryWhiteVoice(Map<String, Object> pMap) {
        String iccid = pMap.get("iccid").toString();
        String functionNm = "/query/sim-voice-usage";//成功 但是 无信息
        // String functionNm =   "/query/sim-voice-margin";//查询不到信息
        String pinUrl = server_Ip + functionNm + "?transid=" + appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()) + "&token=" + token + "&iccid=" + iccid;
        String res = HttpUtil.get(pinUrl);
        Map<String, Object> resMap = JSON.parseObject(res);
        String status = resMap.get("status").toString();
        if (status.equals("0")) {
            List<Map<String, Object>> useList = (List<Map<String, Object>>) resMap.get("result");
            String voiceAmount = useList.get(0).get("voiceAmount").toString();
            return voiceAmount;
        }
        return "错误";
    }
    /**
     * ri流量查询接口
     *
     * @param card_no
     * @return
     */
    public String selDayUsage(String card_no,String queryDate) {
        String functionNm = "/query/sim-data-usage-daily/batch";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        if (!cd_code.equals("YiDong_EC_TengYu")) {
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("iccids", card_no);
            json.put("queryDate",queryDate);
        } else {
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("msisdn", card_no);
            json.put("requestParams", requestParams);
        }
        String url = server_Ip + functionNm;
        String Str = send(url, json);
        //System.out.println(Str);
        return repeat(Str, json, url);
    }
    public String newDesignatedMonth(String card_no, String queryDate) {
        String functionNm = "/query/sim-data-usage-monthly/batch";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        if (!cd_code.equals("YiDong_EC_TengYu")) {
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("iccids", card_no);
        } else {
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("msisdn", card_no);
            json.put("requestParams", requestParams);
        }
        json.put("queryDate", queryDate);
        String url = server_Ip + functionNm;
        String Str = send(url, json);
        return repeat(Str, json, url);
    }
    public List<Map<String, Object>> getWhite(Map<String, Object> map) {// 查询成员组
        String iccid = map.get("iccid").toString();
        String groupId = map.get("groupId").toString();
        String functionNm = "/query/member-voice-whitelist"; //https://api.iot.10086.cn/v5/ec/query/member-voice-whitelist
        String pinUrl = server_Ip + functionNm + "?transid=" + appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()) + "&token=" + token + "&iccid=" + iccid + "&groupId=" + groupId;
        String res = HttpUtil.get(pinUrl);
        Map<String, Object> resMap = JSON.parseObject(res);
        String status = resMap.get("status").toString();
        if (status.equals("0")) {//成功
            List<Map<String, Object>> bodyList = (List<Map<String, Object>>) resMap.get("result");
            Map<String, Object> whiteMap = bodyList.get(0);
            List<Map<String, Object>> whiteList = (List<Map<String, Object>>) whiteMap.get("memVoiceWhiteList");
            return whiteList;//白名单列表
        }
        return null;
    }
    public Map<String, Object> delWhite(Map<String, Object> PMap) {
        HashMap<String, Object> rMap = new HashMap<>();
        String iccid = PMap.get("iccid").toString();
        String groupId = PMap.get("groupId").toString();
        String whiteNumber = PMap.get("delNumber").toString();
        //String whiteNumber ="18615133198";
        String operType = "1";
        try {
            String functionNm = "/config/member-voice-whitelist";//配置 白名单
            String pinUrl = server_Ip + functionNm + "?transid=" + appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()) + "&token=" + token + "&iccid=" + iccid + "&groupId=" + groupId + "&operType=4" + "&whiteNumber=" + whiteNumber;
            String res = HttpUtil.get(pinUrl);
            Map<String, Object> resBody = (Map<String, Object>) JSON.parseObject(res);
            return resBody;
        } catch (Exception e) {
            return null;
        }

    }
    public Map<String, Object> setWhite(Map<String, Object> PMap) {
        HashMap<String, Object> rMap = new HashMap<>();
        String iccid = PMap.get("iccid").toString();
        String groupId = PMap.get("groupId").toString();
        String whiteNumber = PMap.get("addNumber").toString();
        //String whiteNumber ="18615133198";
        String operType = "1";
        try {
            String functionNm = "/config/member-voice-whitelist";//配置 白名单
            String pinUrl = server_Ip + functionNm + "?transid=" + appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()) + "&token=" + token + "&iccid=" + iccid + "&groupId=" + groupId + "&operType=1" + "&whiteNumber=" + whiteNumber;
            String res = HttpUtil.get(pinUrl);
            Map<String, Object> resBody = (Map<String, Object>) JSON.parseObject(res);
            return resBody;
        } catch (Exception e) {
            return null;
        }

    }
    /**
     * 单卡接口查询卡状态
     * @param card_no
     * @return
     */
    public String queryCardStatus(String card_no)  {
        String functionNm =   "/query/sim-status";

        JSONObject json = new JSONObject();
        String cardStatus = null;
        String lastChangeDate = null;
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        json.put("msisdn", card_no);
        String Str = send(url,json);
        return repeat(Str,json,url);
    }

    /**
     * 月流量查询接口
     * @param card_no
     * @return
     */
    public String selectFlow(String card_no) {
        String functionNm =   "/query/sim-data-usage";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String url = server_Ip + functionNm;
        String Str = send(url,json);
        //System.out.println(Str);
        return repeat(Str,json,url);
    }

    /**
     * 查询指定月份的已用流量。6个月内的
     *
     * @param card_no
     * @param queryDate
     * @return
     */
    public String designatedMonth(String card_no, String queryDate) {
        String functionNm =  "/query/sim-data-usage-monthly/batch";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdns", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }

        json.put("queryDate", queryDate);
        String url = server_Ip + functionNm;
        String Str = send(url,json);
        return repeat(Str,json,url);
    }


    /**
     * 单卡卡状态操作
     * @param card_no
     * @param flag
     * @return
     */
    /*public String changeCardStatus(String card_no, String flag) {
        String functionNm =  "/change/sim-status";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        json.put("operType", flag);
        String Str = send(url,json);
        return repeat(Str,json,url);
    }*/

    /**
     * 查询单卡的机卡分离状态
     * @param card_no
     * @return
     */
    public String queryCardBindStatus(String card_no) {
        String functionNm =  "/query/card-bind-status";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        String cardStatus = null;
        String sepTime = null;
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }

        json.put("testType", 0);
        String Str = send(url,json);
        return repeat(Str,json,url);
    }

    /**
     * 解除单卡的机卡分离
     * @param card_no
     * @return
     */
   /* public String upCardBindStatus(String card_no) {
        String functionNm =  "/operate/card-bind-by-bill";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }

        json.put("operType", 2);
        String Str = send(url,json);
        return repeat(Str,json,url);
    }*/

    /**
     * 单卡查询激活以及开卡时间
     * @param card_no
     * @return
     */
    public String queryCardActiveTime(String card_no) {
        String functionNm =  "/query/sim-basic-info";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }

        String Str = send(url,json);
        return repeat(Str,json,url);
    }

    /**
     * @param @param  card_no
     * @param @return 参数
     * @return JSONObject    返回类型
     * @throws
     * @Description:单卡查询apn状态
     * @author
     * @date 2020年5月25日
     */
    public  Map<String, Object> queryApnStatus(String card_no) {
        String functionNm =  "/query/sim-communication-function-status";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }

        //String Str = HttpUtil.post(url, json.toJSONString());
        String Str = send(url,json);
        return JSONObject.parseObject(repeat(Str,json,url));
    }

    /**
     *  流量池信息查询
     * @return
     */
    public static String queryFlowPool(){
        String functionNm =  "/query/group-data-margin";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
        }
        json.put("groupId", appId);
        String Str = HttpUtil.post(url, json.toJSONString());

        return Str;

    }


    /**
     * 单卡在线信息实时查询
     * @param card_no
     * @return
     */
    public String sim_session(String card_no) {
        String functionNm =  "/query/sim-session";
        JSONObject json = new JSONObject();
        String cardStatus = null;
        String lastChangeDate = null;
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }

        String Str = send(url,json);
        return repeat(Str,json,url);
    }


    /**
     * 单卡实时使用终端 IMEI 查询
     * @param card_no
     * @return
     */
    public String queryCardImei(String card_no) {
        String functionNm =  "/query/sim-imei";
        JSONObject json = new JSONObject();
        String cardStatus = null;
        String lastChangeDate = null;
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }



    /**
     * 单卡停机原因查询
     * @param card_no
     * @return
     */
    public String simStopReason(String card_no) {
        String functionNm =  "/query/sim-stop-reason";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }




    /**
     * 单卡开关机状态实时查询
     * @param card_no
     * @return
     */
    public String onOffStatus(String card_no) {
        String functionNm =  "/query/on-off-status";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }

    /**
     * 单卡已开通APN信息查询
     * @param card_no
     * @return
     */
    public String apnInfo(String card_no) {
        String functionNm =  "/query/apn-info";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }


    /**
     * 物联卡机卡分离状态查询
     * @param card_no
     * @param testType 分离检测方式：
     * 0：话单侧检测
     * 1：网络侧检测
     * @return
     */
    public String cardBindStatus(String card_no,String testType) {
        String functionNm =  "/query/card-bind-status";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
            json.put("testType", testType);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            json.put("testType", testType);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }



    /**
     * 单卡状态变更历史查询
     * @param card_no
     * @return
     */
    public String simChangeHistory(String card_no) {
        String functionNm =  "/query/sim-change-history";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }








    /**
     * 单卡本月套餐内流量使用量实时查
     * @param card_no
     * @return
     */
    public String simDataMargin(String card_no) {
        String functionNm =  "/query/sim-data-margin";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }



    /**
     * 资费订购实时查询
     * @param card_no
     * @return
     */
    public String queryOffering(String card_no) {
        String functionNm =  "/query/ordered-offerings";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
            json.put("queryType", "3");
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            json.put("queryType", "3");
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }


    /**
     * CMIOT_API23E06-集团群组信息查询
     * @param pageSize 每页查询的数目，不超过50
     * @param startNum 开始页，从1开始
     * @return
     */
    public String queryGroupInfo(String pageSize,String startNum) {
        String functionNm =  "/query/group-info";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("pageSize", pageSize);
            json.put("startNum", startNum);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            json.put("pageSize", pageSize);
            json.put("startNum", startNum);
            Map<String,Object> requestParams = new HashMap<>();
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }

    /**
     * CMIOT_API23E01-群组所属成员查询
     * @param pageSize 每页查询的数目，不超过50
     * @param startNum 开始页，从1开始
     * @param groupId 群组ID
     * @return
     */
    public String queryGroupMember(String pageSize,String startNum,String groupId) {
        String functionNm =  "/query/group-member";
        JSONObject json = new JSONObject();
        String url = server_Ip + functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("groupId", groupId);
            json.put("pageSize", pageSize);
            json.put("startNum", startNum);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            json.put("groupId", groupId);
            json.put("pageSize", pageSize);
            json.put("startNum", startNum);
            Map<String,Object> requestParams = new HashMap<>();
            json.put("requestParams", requestParams);
        }
        String Str = send(url,json);
        return repeat(Str,json,url);
    }


}
