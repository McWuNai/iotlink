package com.yunze.apiCommon.upstreamAPI.DianXinCMP.change;



import com.yunze.apiCommon.upstreamAPI.DianXinCMP.DX_CMP_Api;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP.DesUtils;
import com.yunze.apiCommon.utils.CommonlyUsed;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;
import com.yunze.apiCommon.utils.XmlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class serviceAccept_DX extends DX_CMP_Api {


    public serviceAccept_DX(Map<String, Object> init_map) {
        super(init_map);
    }




    /**
     * 判断 在限速 标准 中
     * @param speedValue
     * @return bool = true 在标准中
     */
    public Map<String,Object> speedValue_set(String speedValue){
        boolean bool = false;
        Map<String,Object> map = new HashMap<String,Object>();
        List<String> rule = new ArrayList<String>();
        rule.add("10");rule.add("11");rule.add("12");rule.add("13");rule.add("14");
        rule.add("15");rule.add("16");rule.add("17");rule.add("18");rule.add("19");
        rule.add("20");rule.add("21");rule.add("23");rule.add("24");rule.add("25");
        rule.add("26");rule.add("27");rule.add("28");rule.add("29");rule.add("30");
        rule.add("31");rule.add("32");rule.add("33");
        if(speedValue.equals("0")){
            speedValue = "31";//不限制
        }else if(speedValue.equals("1")){
            speedValue = "12";//1Mbps
        }
        bool = CommonlyUsed.Val_Is_Arr(rule,speedValue);//判断 传入 类型是否在 已有 标准中
        map.put("speedValue",speedValue);
        map.put("bool",bool);
        return map;
    }




    /**
     * 自主限速接口
     * @param access_number
     * @param speedValue
     * 限速带宽速率：
     * 10	1Kbps
     * 11	512Kbps
     * 12	1Mbps
     * 13	3Mbps
     * 14	5Mbps
     * 15	7Mbps
     * 16	10Mbps
     * 17	20Mbps
     * 18	30Mbps
     * 19	40Mbps
     * 20	50Mbps
     * 21	60Mbps
     * 22	70Mbps
     * 23	80Mbps
     * 24	90Mbps
     * 25	100Mbps
     * 26	110Mbps
     * 27	120Mbps
     * 28	130Mbps
     * 29	140Mbps
     * 30	150Mbps
     * 31	不限制
     * 32	64Kbps
     * 33	256Kbps
     * @param action action=ADD，表示设置上网速率；action=DEL，表示退订自主限速（此场景speedValue传固定值10）
     * @return
     */
    public  String speedLimitAction(String access_number,String speedValue,String action) {
        ////System.out.println("action "+action);

        // 拼接接口地址 			  /m2m_ec/app/serviceAccept.do
        String api_url = server_Ip + "/m2m_ec/app/serviceAccept.do";
        //				 speedLimitAction
        String method = "speedLimitAction";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("access_number", access_number);
        params.put("speedValue", speedValue);//	速率
        params.put("action", action);
        params.put("user_id", user_id);
        //
        String[]    arr = {access_number,user_id,password,method,speedValue,action }; //加密数组，数组所需

        //key1,key2,key2为电信提供的9位长接口密钥平均分为三段所形成

        DesUtils des = new DesUtils(); //DES加密工具类实例化
        String    passWord = des.strEnc(password,key1,key2,key3); //密码加密
        String    sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("passWord", passWord);
        params.put("sign", sign);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        return res;
    }



    /**
     * 达量断网 接口
     * @param access_number  物联网接入号(149或10649号段
     * @param quota 断网阈值设置为1024M
     * @param action 新增达量断网功能时action为1，如果已经添加了达量断网功能，需要修改断网阈值，则action设置为2，取消达量断网功能，action设置为3
     * @return
     */
    public  Map<String, Object> offNetAction(String access_number,String quota,String action) {
        Map<String, Object> rMap = null;

        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "offNetAction";
        //请求参数 Json
        Map<String, Object> params = null;

        String type = "1"; //设置为1表示用户总使用量，设置为2表示超出套餐外

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        params.put("action", action);
        params.put("type", type);
        params.put("quota", quota);

        String[] arr = {access_number,user_id,password,action,quota,type,method}; //加密数组，数组所需参数根据对应的接口文档

        DesUtils    des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        //System.out.println(res);
        if(res.indexOf("已经订购该产品，请不要重复订购;")!=-1){//适配 新增 达量断网已经设置过时自动改为 修改再次提交
            action = "2";
            params.put("action", action);
            arr = new String[]{access_number,user_id,password,action,quota,type,method}; //加密数组，数组所需参数根据对应的接口文档
            des = new DesUtils(); //DES加密工具类实例化
            passWord = des.strEnc(password,key1,key2,key3);  //密码加密
            sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
            params.put("sign", sign);
            params.put("passWord", passWord);
            param_url = UrlUtil.getUrl(api_url, params);
            res = HttpUtil.get(param_url);
        }
        try {
            rMap =  XmlUtil.xmlToMap(res);
        } catch (Exception e) {
            System.err.println(Config_name+" 电信 [达量断网 接口] 返回结果异常");
        }
        return rMap;
    }

    /**
     * 单独添加/恢复断网接口
     * @param access_number
     * @param action
     * @return
     */
    public  Map<String, Object> singleCutNet(String access_number,String action) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "singleCutNet";
        //请求参数 Json
        Map<String, Object> params = null;

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        params.put("action", action); //单独添加断网时action为ADD，单独恢复上网时为DEL


        String[] arr = {access_number,user_id,password,action,method}; //加密数组，数组所需

        DesUtils    des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);

        ////System.out.println(params.toString());
        ////System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        ////System.out.println(res);
        if(res.indexOf("流水号")==-1 && res.length()>50) {
            try {
                rMap =  XmlUtil.xmlToMap(res);
            } catch (Exception e) {
                System.err.println(Config_name+" 电信 [单独添加/恢复断网接口] 返回结果异常");
            }
        }else {
            rMap = new HashMap<>();
            rMap.put("rspcode", res.substring(0,2));
            rMap.put("rspdesc", res.substring(2,res.length()));
        }

        return rMap;
    }


    /**
     * 	停机保号/复机/测试期去激活
     *
     * @param access_number SIM ID
     */
    public  Map<String, Object> disabledNumber(String access_number,String orderTypeId) {
        Map<String, Object> rMap = null;
        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "disabledNumber";
        //请求参数 Json
        Map<String, Object> params = null;
        String acctCd = "";//固定字段，保持为空

        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	speedLimitAction (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        params.put("acctCd", acctCd);
        params.put("orderTypeId", orderTypeId); //19表示停机保号，20表示停机保号后复机,21表示测试期去激活，22表示测试期去激活后回到测试激活

        String[]     arr = {access_number,user_id,password,method, acctCd, orderTypeId }; //加密数组，数组所需参数根据对应的接口文档

        DesUtils    des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值

        params.put("sign", sign);
        params.put("passWord", passWord);
        ////System.out.println(params.toString());
        ////System.out.println(UrlUtil.getUrl(api_url, params));
        String param_url = UrlUtil.getUrl(api_url, params);
        //System.out.println(param_url);
        String  res = HttpUtil.get(param_url);
        System.out.println(res);

        if(res.indexOf("流水号")==-1 && res.length()>50) {
            try {
                rMap =  XmlUtil.xmlToMap(res);
            } catch (Exception e) {
                System.err.println(Config_name+" 电信 [单独添加/恢复断网接口] 返回结果异常");
            }
        }else {
            rMap = new HashMap<>();
            rMap.put("rspcode", res.substring(0,2));
            rMap.put("rspdesc", res.substring(2,res.length()));
        }

        return rMap;
    }







    /**
     * 机卡重绑
     * @param access_number
     * @param bind_type
     * @param imei
     * @return
     * {"Response":{"RspType":"1","RspCode":"-5","RspDesc":"业务校验失败（单卡本月调用次数已超过限定次数！）"},"GROUP_TRANSACTIONID":"1000000190202103293103373268"}
     * {"Response":{"RspType":"0","RspCode":"0000","RspDesc":"成功接收消息"},"GROUP_TRANSACTIONID":"1000000190202103293055805587"}
     */
    public  String IMEIReRecord(String access_number,String bind_type,String imei) {
        Map<String, Object> rMap = null;

        // 拼接接口地址 			  /m2m_ec/query.do
        String api_url = server_Ip + "/m2m_ec/query.do";
        //
        String method = "IMEIReRecord";
        //请求参数 Json
        Map<String, Object> params = null;

        String action = "ADD"; //固定值
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        params.put("action", action);
        params.put("bind_type", bind_type);//不填，表示普通机卡重绑 填写2，表示固定机卡重绑
        if(bind_type.equals("2")){
            params.put("imei", imei);//只有在bind_typ=2时才需传入，表示固定绑定到该设备号
        }

        String[]     arr = {access_number ,user_id,password,method,action}; //加密数组，数组所需参数根据对应的接口文档

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
        return res;
    }

    /**
     * 实名制信息清除接口
     * @param access_number 物联网接入号(149或10649号段)
     * @return
     */
    public  String ordinaryRealNameClear(String access_number) {
        Map<String, Object> rMap = null;
        String api_url = server_Ip + "/m2m_ec/query.do";
        String method = "ordinaryRealNameClear";
        Map<String, Object> params = null;
        // 拼接请求参数
        params = new HashMap<String, Object>();
        params.put("method", method);//	 (固定值) 方法名
        params.put("user_id", user_id);
        params.put("access_number", access_number);
        String[]   arr = {access_number,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档
        DesUtils    des = new DesUtils(); //DES加密工具类实例化
        String     passWord = des.strEnc(password,key1,key2,key3);  //密码加密
        String     sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        params.put("sign", sign);
        params.put("passWord", passWord);
        String param_url = UrlUtil.getUrl(api_url, params);
        String  res = HttpUtil.get(param_url);
        return res;
    }



}
