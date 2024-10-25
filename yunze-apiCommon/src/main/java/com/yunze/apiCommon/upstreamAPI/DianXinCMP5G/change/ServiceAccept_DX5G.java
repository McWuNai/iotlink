package com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.change;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP.DesUtils;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.DianXin_DCP5G_Api;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;
import com.yunze.apiCommon.utils.XmlUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
public class ServiceAccept_DX5G extends DianXin_DCP5G_Api {


    public ServiceAccept_DX5G(Map<String, Object> init_map) {
        super(init_map);
    }

    /**
     * 2.3.2. CTIOT_5GCMP_LC002- 停机保号相关
     * @param access_number
     * @param orderTypeId 停机保号类型：
     * 19表示停机保号
     * 20表示停机保号复机
     * 21表示去激活停机
     * 22 表示去激活复机
     * @return
     */
    public  Map<String, Object> disabledNumberService(String access_number, String orderTypeId) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/5gcmp/openapi/v1/common/disabledNumberService";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        params.put("acctCd",  "");//文档中没有 ，请求示例中有 不传入会报错！
        params.put("orderTypeId", orderTypeId);
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  XmlUtil.xmlToMap(res);
            System.out.println(res);
            if(orderTypeId.equals("20") && rMap.get("result")!=null && rMap.get("result").equals("100008")){//适配 复机时 卡状态为待激活时改为 强制激活
                rMap =  requestServActive(access_number);
            }
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }









    /**
     * 2.7.2. CTIOT_5GCMP_MAPN002- APN 上网功能启用、停止操作
     * @param access_number
     * @param action ：
     * 执行动作
     * action=ADD，表示此 APN 可以上网；
     * action=DEL，表示此APN 不可以上网；
     * @return
     */
    public  Map<String, Object> apnNetStatusAction(String access_number, String action) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/5gcmp/openapi/v1/common/singleCutNet";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        params.put("action", action);

        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            rMap =  XmlUtil.xmlToMap(res);
            //{rspcode=-9, response=, svccont=, rspdesc=物联网CRM系统返回异常==>[O0300304-1148]已订购功能产品：产品ID：【381000646】，名称：【物联网基础业务-流量-取消/恢复上网】,不能重复订购}

        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }


    /**
     * 达量断网
     * @param access_number
     * @param quota
     * @param action 默认传入 2 修改 》否则会出现 直接新增订购提示成功 不提示 重复订购！
     * @return
     */
    public  Map<String, Object> offNetAction(String access_number,String quota,String action) {
        Map<String, Object> rMap = null;
        String  res = null;
        String type = "1"; //设置为1表示用户总使用量，设置为2表示超出套餐外
        // 拼接接口地址
        String Config_name =  "/5gcmp/openapi/v1/common/offNetAction";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number", access_number);
        params.put("action", action);
        params.put("type", type);
        params.put("quota", quota);
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            System.out.println(res);
            if(res.indexOf("未订购达量断网")!=-1){//适配 新增 达量断网已经设置过时自动改为 修改再次提交
                action = "1";
                params.put("action", action);
                getSign(params); //生成sign加密值
                param_url = UrlUtil.getUrl(api_url, params,true);
                res = HttpUtil.get(param_url,headers);
                System.out.println(res);
            }
            rMap =  XmlUtil.xmlToMap(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }


    /**
     * 强制激活
     * @param access_number
     * @return
     */
    public  Map<String, Object> requestServActive(String access_number) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/5gcmp/openapi/v1/common/requestServActive";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            res = HttpUtil.get(param_url,headers);
            System.out.println(res);
            rMap = JSON.parseObject(res);
            if(rMap.get("RESULT")!=null && rMap.get("SMSG")!=null && rMap.get("RESULT").equals("0") && rMap.get("SMSG").equals("成功")){//如果强制激活成功 转换为 复机解析数据
                rMap = new HashMap<>();
                rMap.put("result","0");
                rMap.put("resultmsg","强制激活成功！");
            }
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }


    /**
     * CTIOT_5GCMP_WUSMS001-发送短信
     * @param access_number 接入号码
     * @param statusCallback
     * @param returnSMSCallback
     * @param returnType
     * @param msgCharset
     * @return
     */
    public  Map<String, Object> sendSms(String access_number,String messageContent, String statusCallback,String returnSMSCallback,String returnType,String msgCharset) {
        Map<String, Object> rMap = null;
        String  res = null;
        // 拼接接口地址
        String Config_name =  "/api/v1/na/notice/message/sendSms";
        String api_url = server_Ip +Config_name;
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("access_number",  access_number);//
        DesUtils    des = new DesUtils(); //DES加密工具类实例化

        String key1 = secretKey.substring(0, 3);
        String key2 = secretKey.substring(3, 6);
        String key3 = secretKey.substring(6, 9);
        String[]   arr = {messageContent}; //加密数组，数组所需参数根据对应的接口文档
        String     content = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("content",  content);//

        //可选参数
        if(statusCallback!=null){
            params.put("statusCallback", statusCallback);//发送状态回调地址
        }
        if(returnSMSCallback!=null){
            params.put("returnSMSCallback", returnSMSCallback);//回传短信推送地址
        }
        if(returnType!=null){
            params.put("returnType", returnType);//返回类型 0：透传回传内容；1：json格式回传 内容 不填写默认为0
        }
        if(msgCharset!=null){
            params.put("msgCharset", msgCharset);//短消息编码 短消息编码，可不填，默认为15，需要对输入值进行有效性校验，若不在范围内，传默认值 【15：GB18030编码】【0：ASCII编码】，【3：短消息写卡操作】【 4：二进制短消息】，【8：UCS2编码】，【246：(U)SIM相相关消息】
        }
        getSign(params); //生成sign加密值
        String param_url = UrlUtil.getUrl(api_url, params,true);
        try {
            System.out.println(param_url);
            System.out.println(headers);
            res = HttpUtil.get(param_url,headers);
            System.out.println(res);
            rMap =  JSON.parseObject(res);
        } catch (Exception e) {
            rMap = new HashMap<>();
            rMap.put("error",res);
            System.out.println(Config_name+"  返回数据错误！"+res);
        }
        return rMap;
    }



}
