package com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.Inquire;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.DianXin_DCP5G_Api;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.change.ServiceAccept_DX5G;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;
import com.yunze.apiCommon.utils.XmlUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
@Slf4j
/**
 * 查询类接口
 */
public class Query_DX5G extends DianXin_DCP5G_Api {
    protected Logger logger = Logger.getLogger(String.valueOf(this.getClass()));

    public Query_DX5G(Map<String, Object> init_map) {
        super(init_map);
    }




    /**
     * 流量查询（当月）接口
     */
    public  Map<String, Object> queryTraffic(String access_number,String needDtl) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/api/v1/openbill/queryTraffic";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        params.put("needDtl", needDtl);
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  XmlUtil.xmlToMap(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }



    /**
     * 在线状态和会话信息查询
     */
    public  Map<String, Object> getOnlineStatus(String access_number) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/openapi/v1/prodinst/getOnlineStatus";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }









    /**
     * 产品资料查询
     */
    public  Map<String, Object> prodInstQuery(String access_number) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/openapi/v1/prodinst/prodInstQuery";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  XmlUtil.xmlToMap(res);;
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }




    /**
     * 已订购产品查询
     */
    public  Map<String, Object> qryProdInstInfo(String access_number) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/api/v1/prod/qryProdInstInfo/"+access_number;
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+" 返回数据错误！"+res);
        }
        return rMap;
    }






    /**
     * 查询 SIM 卡主状态及从状态
     */
    public  Map<String, Object> queryCardMainStatus(String access_number,String queryDropCard) {
        Map<String, Object> rMap = null;
        String  res = null;
        queryDropCard = queryDropCard!=null?queryDropCard:"false";
        // 拼接接口地址
        String Config_name =  "/openapi/v1/prodinst/queryCardMainStatus";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        params.put("queryDropCard",  queryDropCard);//是否需要查询拆机状态,若该字段存在且传值为 true，则查询拆机状态，否则不查询 拆机

        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+" 返回数据错误！"+res);
        }
        return rMap;
    }



    /**
     * 2.3.4. CTIOT_5GCMP_LC005-卡变更状态记录查询
     */
    public  Map<String, Object> cradStatus(String access_number,String page,String size) {
        Map<String, Object> rMap = null;
        String  res = null;
        page = page!=null?page:"1";
        size = size!=null?size:"6";
        // 拼接接口地址
        String Config_name =  "/openapi/v1/prodinst/cradStatus";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        params.put("page",  page);//
        params.put("size",  size);//

        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+" 返回数据错误！"+res);
        }
        return rMap;
    }




    /**
     * 2.1.8. CTIOT_5GCMP_BQ008-实名状态查询
     */
    public  Map<String, Object> realNameQueryIot(String access_number) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/openapi/v1/prodinst/realNameQueryIot";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  XmlUtil.xmlToMap(res);;
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }




    /**
     * 2.2.11. CTIOT_5GCMP_PM011-前向流量池成员查询
     */
    public  Map<String, Object> forwardFlowPoolMember(String access_number,String pageIndex) {
        Map<String, Object> rMap = null;
        String  res = null;
        pageIndex = pageIndex!=null?pageIndex:"1";
        // 拼接接口地址
        String Config_name =  "/openapi/v1/flowpool/forwardFlowPoolMember";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        params.put("pageIndex",  pageIndex);//

        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+" 返回数据错误！"+res);
        }
        return rMap;
    }


    /**
     * 2.1.4. CTIOT_5GCMP_BQ004-SIM 卡列表查询
     */
    public  Map<String, Object> getSIMList(String pageIndex,String custId,String access_number,String iccid,String activationTimeBegin,String activationTimeEnd	,String simStatus,String groupId) {
        Map<String, Object> rMap = null;
        String  res = null;
        pageIndex = pageIndex!=null?pageIndex:"1";
        // 拼接接口地址
        String Config_name =  "/openapi/v1/prodinst/getSIMList";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("pageIndex",  pageIndex);//

        if(custId!=null){
            params.put("custId", custId);
        }
        if(access_number!=null){
            params.put("access_number", access_number);
        }
        if(iccid!=null){
            params.put("iccid", iccid);
        }
        if(activationTimeBegin!=null){
            params.put("activationTimeBegin", activationTimeBegin);
        }
        if(activationTimeEnd!=null){
            params.put("activationTimeEnd", activationTimeEnd);
        }
        if(simStatus!=null){
            params.put("simStatus", simStatus);
        }
        if(groupId!=null){
            params.put("groupId", groupId);
        }
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+" 返回数据错误！"+res);
        }
        return rMap;
    }





    public  Map<String, Object> apnSel(String access_number) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/openapi/v1/subscribers/"+access_number+"/apn";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        getSign(params); //生成sign加密值
        try {
            System.out.println(api_url);
            System.out.println(headers);
            res = HttpUtil.get(api_url,headers);
            System.out.println(res);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }


    /***
     * CTIOT_5GCMP_UQ002-流量查询（时间段）
     */
    public Map<String,Object> queryTrafficByDate (String iccid,String startDate,String endDate){
        Map<String, Object> rMap = null;
        String res = null;
        // 拼接接口地址
        String Config_name = "/api/v1/openbill/queryTrafficByDate";
        String api_url = server_Ip + Config_name;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("iccid", iccid);//
        params.put("needDtl", "0");
        params.put("startDate",startDate);
        params.put("endDate", endDate);
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params, true);
        try {
            res = HttpUtil.get(param_url, headers);
            rMap = XmlUtil.xmlToMap(res);
            try {
                log.info(">>Query_DX5G - queryTraffic - :access_number {} | {}<<", iccid, JSON.toJSON(rMap));
            } catch (Exception e) {

            }
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error", res);
            System.out.println(Config_name + "  返回数据错误！" + res);
        }

        return rMap;
    }
}
