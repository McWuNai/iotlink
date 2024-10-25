package com.yunze.apiCommon.upstreamAPI.DianXinCMP.Inquire;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP.DX_CMP_Api;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP.DesUtils;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP.change.serviceAccept_DX;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;
import com.yunze.apiCommon.utils.XmlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 查询类接口
 */
public class Query_DX extends DX_CMP_Api {
    protected Logger logger = Logger.getLogger(String.valueOf(this.getClass()));

    public Query_DX(Map<String, Object> init_map) {
        super(init_map);
    }




    /**
     * 流量查询（当月）接口
     * @param access_number SIM ID
     */
    public  Map<String, Object> query(String access_number) {
        Map<String, Object> rMap = null;

        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "queryTraffic";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        params.put("needDtl", "0");

        String[]    arr = {access_number,user_id,password,method }; //加密数组，数组所需

        DesUtils des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        // System.out.println(param_url);
        String  res = HttpUtil.get(param_url);
        //System.out.println(res);
        try {
            rMap =  XmlUtil.xmlToMap(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+" 电信[流量查询（当月）接口] 返回数据错误！"+res);
        }
        return rMap;
    }


    /**
     * 	套餐使用量批量查询接口
     * @param access_numbers SIM ID
     */
    public  Map<String, Object> querys(String access_numbers) {
        Map<String, Object> rMap = null;

        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "queryPakagePlus";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_numbers);
        params.put("monthDate", "20201001");

        String[]    arr = {user_id,password,method }; //加密数组，数组所需

        DesUtils    des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);

        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //System.out.println(res);
        try {
            rMap =  XmlUtil.xmlToMap(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.err.print(Config_name+" 电信  [套餐使用量批量查询接口] 返回数据错误！");
        }
        return rMap;
    }


    /**
     * 	流量查询（时间段）接口
     * @param access_numbers 149 短号
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param needDtl 是否需要明细 0：只返回总使用量，1：返回明细。建议使用0
     * @return
     */
    public  Map<String, Object> queryTrafficByDate(String access_numbers,String startDate,String endDate,String needDtl) {
        Map<String, Object> rMap = null;

        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "queryTrafficByDate";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_numbers);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("needDtl", needDtl);

        String[]    arr = {access_numbers,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据iccid查询时加密数组里面传入iccid
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);

        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        // System.out.println(param_url);
        String  res = HttpUtil.get(param_url);
        // System.out.println(res);
        try {
            rMap =  XmlUtil.xmlToMap(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.err.print(Config_name+" 电信  [套餐使用量批量查询接口] 返回数据错误！");
        }
        return rMap;
    }



    /**
     * 	卡主状态查询接口
     * @param access_number 149 短号
     * @return
     */
    public  String queryCardMainStatus(String access_number) {
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "queryCardMainStatus";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
       // params.put("queryDropCard", true);//若该字段存在且传值为true，则查询拆机状态，否则不查询拆机
        String[]   arr = {access_number,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);

        return res;
    }

    /**
     * 	机卡断开网状态查询
     * 	author:
     * 	date:2021.05.21
     * @param access_number 149 短号
     * @return
     */
    public  String queryMainOnlineStatus(String access_number) {
        // 拼接接口地址
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "getOnlineStatus";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        // params.put("queryDropCard", true);//若该字段存在且传值为true，则查询拆机状态，否则不查询拆机
        String[]   arr = {access_number,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //System.out.println(res);
        return res;
    }

    /**
     * 	机卡绑定状态查询
     * 	author:
     * 	date:2021.05.24
     * @param imei 号
     * @return
     */
    public  String queryMainCardBIndStatus(String imei) {
        // 拼接接口地址
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "queryIMSI";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("imei", imei);
        // params.put("queryDropCard", true);//若该字段存在且传值为true，则查询拆机状态，否则不查询拆机
        String[]   arr = {imei,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);

        return res;
    }







    /**
     * 	实名状态查询接口
     * @param access_number 149 短号
     * @return
     */
    public  Map<String, Object> realNameQueryIot(String access_number) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "realNameQueryIot";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        // params.put("queryDropCard", true);//若该字段存在且传值为true，则查询拆机状态，否则不查询拆机
        String[] arr = {access_number,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据iccid查询时加密数组里面传入iccid
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        try {
            rMap =  XmlUtil.xmlToMap(res);
           /* if(rMap.get("certnumber")!=null){
                rMap.remove("certnumber");
            }*/
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.err.print(Config_name+" 电信  [实名状态查询接口] 返回数据错误！");
        }
        return rMap;
    }


    /**
     * 在线状态和会话信息查询接口
     * @param access_number
     * @return
     */
    public  Map<String, Object> getOnlineStatus(String access_number) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "getOnlineStatus";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        // params.put("queryDropCard", true);//若该字段存在且传值为true，则查询拆机状态，否则不查询拆机
        String[] arr = {access_number,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据iccid查询时加密数组里面传入iccid
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        try {
            rMap = res!=null?JSONObject.parseObject(res):null;
        }catch (Exception e){
            System.out.println("在线状态和会话信息查询接口 返回异常 "+res+"  "+e.getMessage());
        }
        return rMap;
    }



    /**
     * 基站粗定位接口
     * @param access_number
     * @return
     */
    public  String getLocationByPhone(String access_number) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "getLocationByPhone";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        String[] arr = {access_number,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据iccid查询时加密数组里面传入iccid
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //解析响应密文
        String position= des.strDec(res,key1,key2,key3);    //定位数据解密，调用DES解密函数

        return position;
    }



    /**
     * SIM卡列表查询接口
     * @param access_number
     * @return
     */
    public  String getSIMList(String access_number) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "getSIMList";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        params.put("pageIndex", "1");

        String[]   arr = {user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        System.out.println(res);
        //解析响应密文
        String position= des.strDec(res,key1,key2,key3);    //定位数据解密，调用DES解密函数

        return position;
    }



    /**
     * 前向流量池内各成员查询接口
     * @param access_number
     * @return
     */
    public  String forwardFlowPoolMember(String access_number) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "forwardFlowPoolMember";
        //请求参数 Json
        Map<String, Object> params = null;
        String pageIndex = "1";
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        params.put("pageIndex", pageIndex);

        String[]   arr = {user_id,password,method, pageIndex,access_number }; //加密数组，数组所需参数根据对应的接口文档

        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        System.out.println(res);
        //解析响应密文
        String position= des.strDec(res,key1,key2,key3);    //定位数据解密，调用DES解密函数

        return position;
    }



    /**
     * 接入号码查询接口
     * @param iccid
     * @return
     */
    public  String getTelephone(String iccid) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "getTelephone";
        //请求参数 Json
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("iccid", iccid);

        String[]     arr = {iccid,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据imsi查询时加密数组传入imsi
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        //System.out.println(params.toString());
        //System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        // System.out.println(res);
        //解析响应密文
        String position= des.strDec(res,key1,key2,key3);    //定位数据解密，调用DES解密函数

        return position;
    }










    /**
     * 套餐使用量查询接口
     * @param iccid
     * @return
     */
    public  String queryPakage(String iccid) {
        String api_url = server_Ip + "/m2m_ec/query.do";
        String method = "queryPakage";
        //请求参数 Json
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("iccid", iccid);

        String[]     arr = {iccid,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据imsi查询时加密数组传入imsi
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //解析响应密文
        String position = res;    //定位数据解密，调用DES解密函数
        try {
            position = JSON.toJSONString(XmlUtil.xmlToMap(res));
        } catch (Exception e) {
            System.err.println(Config_name+" 电信CMP [套餐使用量查询接口 接口] 返回结果异常 "+res+" e "+e.getMessage());
        }
        return position;
    }





    /**
     * 余额查询接口
     * @param iccid
     * @return
     */
    public  String queryBalance(String iccid) {
        String api_url = server_Ip + "/m2m_ec/query.do";
        String method = "queryBalance";
        //请求参数 Json
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("iccid", iccid);

        String[]     arr = {iccid,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据imsi查询时加密数组传入imsi
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //解析响应密文
        String position = res;    //定位数据解密，调用DES解密函数
        try {
            position = JSON.toJSONString(XmlUtil.xmlToMap(res));
        } catch (Exception e) {
            System.err.println(Config_name+" 电信CMP [余额查询接口 接口] 返回结果异常 "+res+" e "+e.getMessage());
        }
        return position;
    }








    /**
     * 流量池列表查询接口
     * @return
     */
    public  String getPoolList() {
        String api_url = server_Ip + "/m2m_ec/query.do";
        String method = "getPoolList";
        //请求参数 Json
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);

        String[]     arr = {user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据imsi查询时加密数组传入imsi
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //解析响应密文
        String position = res;    //定位数据解密，调用DES解密函数
        try {
            position = JSON.toJSONString(XmlUtil.xmlToMap(res));
        } catch (Exception e) {
            System.err.println(Config_name+" 电信CMP [流量池列表查询接口 接口] 返回结果异常 "+res+" e "+e.getMessage());
        }
        return position;
    }





    /**
     * 产品资料查询接口
     * @return
     */
    public  String prodInstQuery(String iccid) {
        String api_url = server_Ip + "/m2m_ec/query.do";
        String method = "prodInstQuery";
        //请求参数 Json
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", iccid);

        String[]     arr = {iccid,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档，根据imsi查询时加密数组传入imsi
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //解析响应密文
        String position = res;    //定位数据解密，调用DES解密函数
        try {
            position = JSON.toJSONString(XmlUtil.xmlToMap(res));
        } catch (Exception e) {
            System.err.println(Config_name+" 电信CMP [产品资料查询接口 接口] 返回结果异常 "+res+" e "+e.getMessage());
        }
        return position;
    }







    /**
     * 欠费查询接口
     * @return
     */
    public  String queryOwe(String iccid,String Query_Flag) {
        String api_url = server_Ip + "/m2m_ec/query.do";
        String method = "queryOwe";
        //请求参数 Json
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", iccid);
        params.put("Query_Flag", Query_Flag);

        String[]   arr = {iccid,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //解析响应密文
        String position = res;    //定位数据解密，调用DES解密函数
        try {
            position = JSON.toJSONString(XmlUtil.xmlToMap(res));
        } catch (Exception e) {
            System.err.println(Config_name+" 电信CMP [欠费查询接口 接口] 返回结果异常 "+res+" e "+e.getMessage());
        }
        return position;
    }


    /**
     * SIM卡列表查询接口  是否必须 [Y/N]
     * @param pageIndex Y [起始值为1，每页固定50条记录。 当分页索引值超过了符合条件的总页数时，返回空记录]
     * @param custId 客户ID
     * @param access_number 接入号
     * @param iccid
     * @param activationTimeBegin 激活时间段：起始
     * @param activationTimeEnd 激活时间段：截止
     * @param simStatus SIM卡状态
     * @param groupId 群组ID
     * @return
     */
    public  String getSIMList(String pageIndex,String custId,String access_number,String iccid,String activationTimeBegin,String activationTimeEnd	,String simStatus,String groupId) {
        String api_url = server_Ip + "/m2m_ec/query.do";
        String method = "getSIMList";
        //请求参数 Json
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//
        params.put("user_id", user_id);
        params.put("pageIndex", pageIndex);
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
        String[]   arr = {user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档
        DesUtils     des = new DesUtils(); //DES加密工具类实例化
        String   passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String   sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        return res;
    }


















}
