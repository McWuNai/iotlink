package com.yunze.apiCommon.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.Vo.HistoryUsed;
import com.yunze.apiCommon.upstreamAPI.PublicApiService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiUtil_NoStatic {


    @Resource
    private PublicApiService publicApiService;
    @Resource
    private CardStatusReplacementUtil cardStatusReplacementUtil;
    @Resource
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {

        ApiUtil_NoStatic api = new ApiUtil_NoStatic();

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> Yzmap = new HashMap<String, Object>();
        Map<String, Object> Parmap = new HashMap<String, Object>();


        map.put("verify", Yzmap);

        String result = null;


        // result =queryRealNameStatus(map);//查询是否实名
        Parmap.put("iccid", "8986031941208662579");
        map.put("Param", Parmap);
        Map<String, Object> Rmap = api.queryFlow(map, null);
        System.out.println("result    = " + Rmap);

    }

    public static String getSign(Map<String, Object> map, String key) {//入参为：appid,password,version,iccid,timestamp,sign
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {// 循环取键值进行判断
            String m = iterator.next();// 键
            if (m.startsWith("sign")) {
                iterator.remove();// 移除map中以sign字符开头的键对应的键值对
            }
            if (m.startsWith("iccids")) {
                iterator.remove();
            }
        }
        List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            String k = list.get(i);
            String v = (String) map.get(k);
            sb.append(k).append("=").append(v).append("&");
        }
        String signstr = sb.append("key=").append(key).toString();
        //System.out.println(signstr);
        String sign = MD5Util.MD5Encode(signstr).toUpperCase();
        //System.out.println(sign);
        return sign;
    }

    public Map<String, Object> queryFlow(Map<String, Object> map) {
        return queryFlow(map, null);
    }

    public Map<String, Object> queryFlowHis(Map<String, Object> map) {
        return queryFlowHis(map, null);
    }

    public Map<String, Object> queryCardStatus(Map<String, Object> map) {
        return queryCardStatus(map, null);
    }

    public Map<String, Object> changeCardStatus(Map<String, Object> map) {
        return changeCardStatus(map, null);
    }

    public Map<String, Object> queryRealNameStatus(Map<String, Object> map) {
        return queryRealNameStatus(map, null);
    }

    public Map<String, Object> queryOnlineStatus(Map<String, Object> map) {
        return queryOnlineStatus(map, null);
    }

    public Map<String, Object> queryCardActiveTime(Map<String, Object> map) {
        return queryCardActiveTime(map, null);
    }

    public Map<String, Object> unbundling(Map<String, Object> map) {
        return unbundling(map, null);
    }

    public Map<String, Object> FunctionApnStatus(Map<String, Object> map) {
        return FunctionApnStatus(map, null);
    }

    public Map<String, Object> queryAPNInfo(Map<String, Object> map) {
        return queryAPNInfo(map, null);
    }

    public Map<String, Object> queryCardImei(Map<String, Object> map) {
        return queryCardImei(map, null);
    }

    public Map<String, Object> SpeedLimit(Map<String, Object> map) {
        return SpeedLimit(map, null);
    }

    public Map<String, Object> simStopReason(Map<String, Object> map) {
        return simStopReason(map, null);
    }

    public Map<String, Object> onOffStatus(Map<String, Object> map) {
        return onOffStatus(map, null);
    }

    public Map<String, Object> apnInfo(Map<String, Object> map) {
        return apnInfo(map, null);
    }

    public Map<String, Object> cardBindStatus(Map<String, Object> map) {
        return cardBindStatus(map, null);
    }

    public Map<String, Object> simChangeHistory(Map<String, Object> map) {
        return simChangeHistory(map, null);
    }

    public Map<String, Object> changeCardStatusFlexible(Map<String, Object> map) {
        return changeCardStatusFlexible(map, null);
    }

    public Map<String, Object> queryOffering(Map<String, Object> map) {
        return queryOffering(map, null);
    }

    public Map<String, Object> InternetDisconnection(Map<String, Object> map) {
        return InternetDisconnection(map, null);
    }

    /**
     * 获取当前卡的流量使用 月查询接口
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryFlow(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        Map<String, Object> Param = (Map<String, Object>) map.get("Param");
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        List<Map<String, Object>> setResult = new ArrayList<>();

        boolean synErrorBool = false;//是否需要同步错误记录到数据库
        Map<String, Object> errorMap = new HashMap<>();
        String iccid = map.get("iccid") != null ? map.get("iccid").toString() : "";
        String cd_code = "";
        String codeOn = "500";
        String rtMessage = "";

        Map<String, Object> Rdata = null;
        try {
            //返回数据解析
            Rdata = publicApiService.insideApi(map, "queryFlow", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            Double Use = -1d;//使用量  MB

            cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("IoTLink")) {
                    //IoTLink 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    try {
                        codeOn = JsonData.get("status").toString();
                        rtMessage = JsonData.get("message").toString();
                    } catch (Exception e) {
                    }
                    Map<String, Object> Data = (Map<String, Object>) JsonData.get("result");
                    double use = Double.parseDouble(Data.get("useAmount").toString());
                    String useUnit = Data.get("useUnit").toString();
                    if (useUnit.equalsIgnoreCase("MB")) {
                        Use = use;//MB
                    } else if (useUnit.equalsIgnoreCase("KB")) {
                        Use = Arith.formatToTwo(Arith.div(use, 1024));//KB 转 MB
                    }
                } else if (cd_code.equals("DianXin_DCP")) {
                    //电信DCP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("data");
                    try {
                        codeOn = Data.get("status").toString();
                        rtMessage = Data.get("msg").toString();
                    } catch (Exception e) {
                    }
                    Double usestr = Double.parseDouble("" + Data.get("totalVolumnGPRS"));//MB
                    Double toGPRS = Arith.div(Arith.div(usestr, 1024), 1024);
                    //DecimalFormat df=new DecimalFormat("#0.00");
                    //Use=Double.parseDouble(df.format(toGPRS));
                    Use = toGPRS;
                } else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    try {
                        rtMessage = Data.get("error") != null ? Data.get("error").toString() : Data.get("message").toString();
                        //返回的是个 map
                        if (rtMessage.indexOf("{") != -1) {
                            Map<String, Object> rMapData = JSON.parseObject(rtMessage);
                            rtMessage = rMapData.get("message").toString();
                            codeOn = rMapData.get("code").toString();
                        } else if (rtMessage.indexOf("：") != -1) {//-1：无权限（请检查做业务的卡/流量池是否属于本账号，或是否有权限调用此接口）
                            String messageArr[] = rtMessage.split("：");
                            rtMessage = messageArr[1];
                            codeOn = messageArr[0];
                        } else {
                            codeOn = Data.get("code").toString();
                        }
                    } catch (Exception e) {
                    }
                    String total_bytes_cnt = Data.get("total_bytes_cnt").toString();
                    int len = total_bytes_cnt.length();
                    //System.out.println(total_bytes_cnt);
                    total_bytes_cnt = total_bytes_cnt.substring(0, len - 2);
                    //System.out.println(total_bytes_cnt);
                    Use = Double.parseDouble(total_bytes_cnt);
                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> JsonData = (Map<String, Object>) data.get("Data");
                    try {
                        codeOn = JsonData.get("resultCode").toString();
                        rtMessage = JsonData.get("resultDesc").toString();
                    } catch (Exception e) {
                    }
                    Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("terminals")).get(0);
                    Outdata.put("realNameStatus", Data.get("realNameStatus").toString());
                    if (codeOn.equals("0000")) {
                        Use = Double.parseDouble(Data.get("monthToDateUsage").toString());
                    } else {
                        Use = 0.00;
                    }
                    //根据联通接口26账单日数据封装自然月
                    //Use = Use - queryHisFlowSys(iccid);
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    try {
                        rtMessage = JsonData.get("message").toString();
                        if (cd_code.equals("YiDong_EC_TengYu")) {
                            codeOn = JsonData.get("errorCode") != null ? JsonData.get("errorCode").toString() : JsonData.get("status").toString();
                        } else {
                            codeOn = JsonData.get("status").toString();
                        }
                    } catch (Exception e) {
                    }
                    Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                    double kb = 0.0;
                    if (cd_code.equals("YiDong_EC_Combo")) {//套餐结算形式
                        setResult = ((List<Map<String, Object>>) Data.get("accmMarginList"));
                        Map<String, Object> accmMargin = setResult.get(0);
                        System.out.println(setResult);
                        kb = Double.parseDouble(accmMargin.get("useAmount").toString());
                        Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                    } else {
                        kb = Double.parseDouble(Data.get("dataAmount").toString());
                        Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                    }

                } else if (cd_code.equals("XuYuWuLian")) {
                    //旭宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    //System.out.println(JsonData);
                    try {
                        codeOn = JsonData.get("resultCode").toString();
                        rtMessage = JsonData.get("resultMsg").toString();
                    } catch (Exception e) {
                    }
                    Map<String, Object> Data = JSON.parseObject(JsonData.get("info").toString());
                    Use = Double.parseDouble(Data.get("useGprs").toString());
                } else if (cd_code.equals("SDIOT")) {
                    //山东移动老系统 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    //System.out.println(JsonData);
                    try {
                        codeOn = JsonData.get("status").toString();
                        rtMessage = JsonData.get("message").toString();
                    } catch (Exception e) {
                    }
                    Map<String, Object> Data = ((List<Map<String, Object>>) (((Map<String, Object>) ((List<Map<String, Object>>) JsonData.get("result")).get(0)).get("gprs"))).get(0);

                    double kb = Double.parseDouble(Data.get("used").toString());
                    Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                } else if (cd_code.equals("YYWL")) {
                    //移远物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    try {
                        codeOn = JsonData.get("resultCode").toString();
                        rtMessage = JsonData.get("errorMessage").toString();
                    } catch (Exception e) {
                    }
                    Use = Double.parseDouble(JsonData.get("flow").toString());
                } else if (cd_code.equals("ShuoLang")) {
                    //硕朗 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                    String code = JsonData.get("code").toString();
                    try {
                        codeOn = code;
                        rtMessage = JsonData.get("msg").toString();
                    } catch (Exception e) {
                    }
                    if (code.equals("0")) {
                        double kb = Double.parseDouble(Data.get("used").toString());
                        if (kb > -1) {
                            Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                        }
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("ZCWL")) {
                    //中创物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    try {
                        codeOn = code;
                        rtMessage = JsonData.get("message").toString();
                    } catch (Exception e) {
                    }
                    if (code.equals("0")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        Use = Double.parseDouble(Data.get("usedFlow").toString());
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("YKWL")) {
                    //翼控物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("resultCode").toString();
                    try {
                        codeOn = code;
                        rtMessage = JsonData.get("resultMsg").toString();
                    } catch (Exception e) {
                    }
                    if (code.equals("0")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("description").toString());
                        double b = Double.parseDouble(Data.get("totalVolumnGPRS").toString());
                        if (b > -1) {
                            Use = Arith.formatToTwo(Arith.div(Arith.div(b, 1024), 1024));//b 转 MB
                        }
                    } else {
                        data.put("Message", JsonData.get("msresultMsgg"));
                    }
                } else if (cd_code.equals("TenngYu")) {
                    //腾宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    try {
                        codeOn = code;
                        rtMessage = JsonData.get("msg").toString();
                    } catch (Exception e) {
                    }
                    if (code.equals("1")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        Use = Double.parseDouble(Data.get("used_accumulative").toString());
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo")) {
                    // 移动 EC V2 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    try {
                        codeOn = Data.get("status").toString();
                        rtMessage = Data.get("message").toString();
                    } catch (Exception e) {
                    }
                    Map<String, Object> Obj = ((List<Map<String, Object>>) Data.get("result")).get(0);
                    double kb = 0.00;
                    if (cd_code.equals("YiDong_ECv2_Combo")) {
                        setResult = ((List<Map<String, Object>>) Obj.get("gprs"));
                        Map<String, Object> gprs = setResult.get(0);
                        System.out.println(setResult);
                        kb = Double.parseDouble(gprs.get("used").toString());
                        if (kb > -1) {
                            Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                        }
                    } else {
                        kb = Double.parseDouble(Obj.get("total_gprs").toString());
                        if (kb > -1) {
                            Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                        }
                    }
                } else if (cd_code.equals("DianXin_CMP_5G")) {
                    // DianXin_CMP_5G 解析
                    //System.out.println(data);
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    try {
                        codeOn = Data.get("iresult").toString();
                        rtMessage = "";
                    } catch (Exception e) {
                    }

                    if (Data.get("total_bytes_cnt") != null && Data.get("total_bytes_cnt").toString().length() > 2) {//  total_bytes_cnt=0.07MB
                        String total_bytes_cnt = Data.get("total_bytes_cnt").toString();
                        String useSuffix = total_bytes_cnt.substring(total_bytes_cnt.length() - 2, total_bytes_cnt.length());
                        total_bytes_cnt = total_bytes_cnt.substring(0, total_bytes_cnt.length() - 2);
                        if (useSuffix.indexOf("MB") != -1) {
                            Use = Double.parseDouble(total_bytes_cnt);
                        } else if (useSuffix.indexOf("KB") != -1) {
                            Use = Arith.formatToTwo(Arith.div(Double.parseDouble(total_bytes_cnt), 1024));//KB 转 MB
                        } else if (useSuffix.indexOf("GB") != -1) {
                            Use = Arith.formatToTwo(Arith.mul(Double.parseDouble(total_bytes_cnt), 1024));//GB 转 MB
                        }
                    }
                } else if (cd_code.equals("MwFengZuShou")) {
                    // ChenZe 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    try {
                        codeOn = Data.get("retcode").toString();
                        rtMessage = Data.get("msg").toString();
                    } catch (Exception e) {
                    }
                    double kb = 0.00;
                    if (Data.get("cardtariffuserd") != null && (Data.get("cardtariffuserd") + "").length() > 0) {
                        kb = Double.parseDouble(Data.get("cardtariffuserd").toString());
                        if (kb > -1) {
                            Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                        }
                    }
                } else if (cd_code.equals("TenngYu")) {
                    //腾宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    try {
                        codeOn = code;
                        rtMessage = JsonData.get("msg").toString();
                    } catch (Exception e) {
                    }
                    if (code.equals("1")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        Use = Double.parseDouble(Data.get("used_accumulative").toString());
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                }


                try {
                    if (setResult != null && setResult.size() > 0 && cd_code.equals("YiDong_EC_Combo") || cd_code.equals("YiDong_ECv2_Combo")) {
                        Map<String, Object> sendMap = new HashMap<>();
                        sendMap.put("cd_code", cd_code);
                        sendMap.put("setResult", setResult);
                        sendMap.put("iccid", find_card_route_map.get("iccid"));
                        sendSynUsageReminder(sendMap);
                    }
                } catch (Exception e) {
                    System.out.println(" sendSynUsageReminder " + e);
                }

                Outdata.put("code", "200");
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                Outdata.put("code", "500");
                Outdata.put("Message", "内部接收消息，解析数据异常！");
                Outdata.put("Use", -1);
                System.out.println(e);
            }

            Outdata.put("Use", Use);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Use", -1);
            System.out.println(e);
        }
        String rtCode = Outdata.get("code").toString();
        synErrorBool = rtCode.equals("200") ? false : true;
        try {
            if (synErrorBool) {
                boolean isSave = true;
                //过滤 一些 不需要存储的一些错误 比方说 接口超频
                if (cd_code.equals("DianXin_CMP")) {
                    if (codeOn.equals("101004")) {//接口超过单企业调用限制，请稍后再试
                        isSave = false;
                    } else if (codeOn.equals("101006")) {//当前网络系统繁忙，请稍后再试
                        isSave = false;
                    }
                  /* else if(codeOn.equals("-1") && rtMessage.equals("物联网卡还未产生用量")){//物联网卡还未产生用量
                       isSave = false;
                   }*/
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW") || cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo")) {
                    if (codeOn.equals("12021")) {//TOKEN不存在或已过期，请重新获取
                        isSave = false;
                    } else if (codeOn.equals("13000")) {//系统忙，请稍后再试
                        isSave = false;
                    }
                }
                errorMap.put("iccid", iccid);
                errorMap.put("codeOn", codeOn);
                errorMap.put("message", rtMessage);
                errorMap.put("cd_code", cd_code);
                errorMap.put("rt_map", JSON.toJSONString(Rdata));
                if (isSave) {
                    sendQuerFlowError(errorMap);
                } else {
                    System.out.println("sendQuerFlowError 不存储错误 " + JSON.toJSONString(errorMap));
                }
            }
        } catch (Exception e) {
            System.out.println(" sendQuerFlowError " + e);
        }
        return Outdata;
    }

    public Map<String, Object> addRatePlan(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        String cd_code;
        String codeOn = "500";
        String rtMessage = "";
        Map<String, Object> Rdata = null;
        try {
            //返回数据解析
            Rdata = publicApiService.insideApi(map, "addRatePlan", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");

            cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> JsonData = (Map<String, Object>) data.get("Data");
                    try {
                        codeOn = JsonData.get("resultCode").toString();
                        rtMessage = JsonData.get("resultDesc").toString();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                Outdata.put("code", "200");
                Outdata.put("Message", data.get("Message"));
                Outdata.put("resultCode", codeOn);
            } catch (Exception e) {
                Outdata.put("code", "500");
                Outdata.put("Message", "内部接收消息，解析数据异常！");
                System.out.println(e.getMessage());
            }

        } catch (Exception e) {
            Outdata.put("code", "500");
        }
        return Outdata;
    }

    public Map<String, Object> wsQueryIdByName(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        String cd_code;
        String codeOn = "500";
        String rtMessage = "";
        Map<String, Object> Rdata = null;
        try {
            //返回数据解析
            Rdata = publicApiService.insideApi(map, "wsQueryIdByName", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");

            cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> JsonData = (Map<String, Object>) data.get("Data");
                    try {
                        codeOn = JsonData.get("resultCode").toString();
                        rtMessage = JsonData.get("resultDesc").toString();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    if (codeOn.equals("0000")) {
                        String accountId = JsonData.get("accountId").toString();
                        // 将结果放入OutData
                        Outdata.put("accountId", accountId);
                    }
                }
                Outdata.put("code", "200");
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                Outdata.put("code", "500");
                Outdata.put("Message", "内部接收消息，解析数据异常！");
                System.out.println(e.getMessage());
            }

        } catch (Exception e) {
            Outdata.put("code", "500");
        }
        return Outdata;
    }

    /**
     * @param map
     * @return
     */
    public Map<String, Object> queryHistoryFlow(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        String cd_code;
        String codeOn = "500";
        String rtMessage = "";

        Map<String, Object> Rdata = null;
        try {
            //返回数据解析
            Rdata = publicApiService.insideApi(map, "queryFlow", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");

            cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> JsonData = (Map<String, Object>) data.get("Data");
                    try {
                        codeOn = JsonData.get("resultCode").toString();
                        rtMessage = JsonData.get("resultDesc").toString();
                    } catch (Exception e) {
                    }
                    if (codeOn.equals("0000")) {
                        Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("terminals")).get(0);

                        List<HistoryUsed> historyFlow = new ArrayList<>();
                        String[][] fieldsAndLabels = {
                                {"monthToDateDataUsage", "数据用量(MB)"},
                                {"monthToDateSMSUsage", "短信用量(条)"},
                                {"monthToDateVoiceUsage", "语音用量(分钟)"}
                        };

                        // 循环处理每个字段
                        for (String[] fieldAndLabel : fieldsAndLabels) {
                            HistoryUsed usage = new HistoryUsed();
                            usage.setObj(fieldAndLabel[1]);
                            usage.setUsed(Data.get(fieldAndLabel[0]));
                            historyFlow.add(usage);
                        }

                        // 将结果放入OutData
                        Outdata.put("Data", historyFlow);
                    }
                }
                Outdata.put("code", "200");
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                Outdata.put("code", "500");
                Outdata.put("Message", "内部接收消息，解析数据异常！");
                System.out.println(e);
            }

        } catch (Exception e) {
            Outdata.put("code", "500");
        }
        return Outdata;
    }

    /**
     * 单卡历史流量查询
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryFlowHis(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryFlowHis", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            Double Use = -1d;//使用量  MB

            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String total_bytes_cnt = Data.get("total_bytes_cnt").toString();
                    int len = total_bytes_cnt.length();
                    total_bytes_cnt = total_bytes_cnt.substring(0, len - 2);
                    Use = Double.parseDouble(total_bytes_cnt);
                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    Use = Double.parseDouble(Data.get("totalDataVolume").toString());
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    System.out.println(JsonData);
                    Map<String, Object> dataAmountList = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                    System.out.println(dataAmountList);
                    //((List<Map<String, Object>>)((Map<String, Object>) JsonData.get("result")).get("dataAmountList")).get(0);
                    Map<String, Object> Obj = ((List<Map<String, Object>>) dataAmountList.get("dataAmountList")).get(0);
                    System.out.println(Obj);

                    double kb = Double.parseDouble(Obj.get("dataAmount").toString());
                    Use = double_format(kb / 1024, 2);//KB 转 MB
                }


                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                Outdata.put("Message", "内部接收消息，解析数据异常！");
                Outdata.put("Use", -1);
                System.out.println(e);
            }
            Outdata.put("code", Rdata.get("code"));
            Outdata.put("Use", Use);
        } catch (Exception e) {
            Outdata.put("Use", -1);
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 单卡生命周期查询
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryCardStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryCardStatus", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 0;
            String statusMessage = null;
            String activateDate = null;
            String cd_code = data.get("cd_code").toString();
            try {

                if (cd_code.equals("IoTLink")) {
                    //IoTLink 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = (Map<String, Object>) JsonData.get("result");
                    int StatusCd = Integer.parseInt(Obj.get("statusCode").toString());
                    Map<String, Object> IoTLinkCardStatus = cardStatusReplacementUtil.getIoTLinkCardStatus(StatusCd);
                    statusCode = Integer.parseInt(IoTLinkCardStatus.get("statusCode").toString());
                    statusMessage = IoTLinkCardStatus.get("statusMessage") != null ? IoTLinkCardStatus.get("statusMessage").toString() : "";
                } else if (cd_code.equals("DianXin_DCP")) {
                    //电信 DCP
                    Map<String, Object> JsonData = JSON.parseObject(data.get("data").toString());
                    JSONObject j0 = JSONObject.parseObject(JsonData.toString());
                    Object Envelope = j0.get("env:Envelope");
                    JSONObject j1 = JSONObject.parseObject(Envelope.toString());
                    Object Body = j1.get("env:Body");
                    JSONObject j2 = JSONObject.parseObject(Body.toString());
                    Object qsr = j2.get("ns2:QuerySimResourceResponse");
                    JSONObject j3 = JSONObject.parseObject(qsr.toString());
                    Object SimResource = j3.get("SimResource");
                    JSONObject j4 = JSONObject.parseObject(SimResource.toString());
                    Object simSubscriptionStatus = j4.get("simSubscriptionStatus");

                    Map<String, Object> DianXin_DCP_CardStatusMap = cardStatusReplacementUtil.getDianXin_DCP_CardStatus(simSubscriptionStatus.toString());
                    statusCode = Integer.parseInt(DianXin_DCP_CardStatusMap.get("statusCode").toString());
                    statusMessage = DianXin_DCP_CardStatusMap.get("statusMessage").toString();

                } else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> productInfo = ((List<Map<String, Object>>) JsonData.get("productInfo")).get(0);
                    int StatusCd = Integer.parseInt(productInfo.get("productMainStatusCd").toString());

                    Map<String, Object> DianXin_CMP_CardStatusMap = cardStatusReplacementUtil.getDianXin_CMP_CardStatus(StatusCd);
                    statusCode = Integer.parseInt(DianXin_CMP_CardStatusMap.get("statusCode").toString());
                    statusMessage = DianXin_CMP_CardStatusMap.get("statusMessage").toString();

                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    Map<String, Object> Obj = ((List<Map<String, Object>>) Data.get("terminals")).get(0);
                    int StatusCd = Integer.parseInt(Obj.get("simStatus").toString());

                    Map<String, Object> LianTong_CMP_CardStatusMap = cardStatusReplacementUtil.getLianTong_CMP_CardStatus(StatusCd);
                    statusCode = Integer.parseInt(LianTong_CMP_CardStatusMap.get("statusCode").toString());
                    statusMessage = LianTong_CMP_CardStatusMap.get("statusMessage").toString();

                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    // System.out.println(JsonData);
                    Map<String, Object> Obj = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                    // System.out.println(Obj);
                    int StatusCd = Integer.parseInt(Obj.get("cardStatus").toString());

                    Map<String, Object> EcV5CardStatusMap = cardStatusReplacementUtil.getEcV5CardStatus(StatusCd);
                    statusCode = Integer.parseInt(EcV5CardStatusMap.get("statusCode").toString());
                    statusMessage = EcV5CardStatusMap.get("statusMessage").toString();
                } else if (cd_code.equals("SDIOT") || cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo")) {
                    //山东移动老系统 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = ((List<Map<String, Object>>) Data.get("result")).get(0);
                    String StatusCd = Obj.get("STATUS").toString();

                    Map<String, Object> DongXin_ECV2_CardStatusMap = cardStatusReplacementUtil.getDongXin_ECV2_CardStatus(StatusCd);
                    statusCode = Integer.parseInt(DongXin_ECV2_CardStatusMap.get("statusCode").toString());
                    statusMessage = DongXin_ECV2_CardStatusMap.get("statusMessage").toString();

                } else if (cd_code.equals("YYWL")) {
                    //移远物联 解析 status
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    //SysstatusCodetem.out.println(Obj);
                    String StatusCd = Data.get("status").toString();
                    Map<String, Object> States = cardStatusReplacementUtil.getStates(StatusCd);
                    statusCode = Integer.parseInt(States.get("statusCode").toString());
                    statusMessage = States.get("statusMessage").toString();
                } else if (cd_code.equals("ShuoLang")) {
                    //硕朗 解析 message
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                    String message = Data.get("message").toString();
                    Map<String, Object> States = cardStatusReplacementUtil.getStates(message);
                    statusCode = Integer.parseInt(States.get("statusCode").toString());
                    statusMessage = States.get("statusMessage").toString();
                } else if (cd_code.equals("ZCWL")) {
                    //中创物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    if (code.equals("0")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        String status = Data.get("status").toString();
                        Map<String, Object> States = cardStatusReplacementUtil.getStates(status);
                        statusCode = Integer.parseInt(States.get("statusCode").toString());
                        statusMessage = States.get("statusMessage").toString();
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("YKWL")) {
                    //翼控物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("resultCode").toString();
                    if (code.equals("0")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("description").toString());
                        String status = Data.get("simStatus").toString();
                        Map<String, Object> States = cardStatusReplacementUtil.getStates(status);
                        statusCode = Integer.parseInt(States.get("statusCode").toString());
                        statusMessage = States.get("statusMessage").toString();
                    } else {
                        data.put("Message", JsonData.get("msresultMsgg"));
                    }
                } else if (cd_code.equals("TenngYu")) {
                    //腾宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    if (code.equals("1")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        String status = Data.get("sim_status").toString();

                        Map<String, Object> TenngYu_CardStatusMap = cardStatusReplacementUtil.getTenngYu_CardStatus(status);
                        statusCode = Integer.parseInt(TenngYu_CardStatusMap.get("statusCode").toString());
                        statusMessage = TenngYu_CardStatusMap.get("statusMessage").toString();

                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("DianXin_CMP_5G")) {
                    // DianXin_CMP_5G 解析
                    System.out.println(data);
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String result = Data.get("result").toString();
                    if (result.equals("0")) {
                        List<Map<String, Object>> productInfo = (List<Map<String, Object>>) Data.get("productInfo");
                        if (productInfo != null && productInfo.size() > 0) {
                            Map<String, Object> obj = productInfo.get(0);
                            String status = obj.get("productMainStatusCd").toString();
                            Map<String, Object> CardStatusMap = cardStatusReplacementUtil.getDianXin_CMP_5G_CardStatus(status);
                            statusCode = Integer.parseInt(CardStatusMap.get("statusCode").toString());
                            statusMessage = CardStatusMap.get("statusMessage").toString();
                        }
                    }
                } else if (cd_code.equals("XuYuWuLian")) {
                    //旭宇物联 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = JSON.parseObject(Data.get("info").toString());
                    String workingCondition = Obj.get("workingCondition").toString();
                    //String PROVIDER = Obj.get("PROVIDER").toString();
                    Map<String, Object> States = cardStatusReplacementUtil.getStates(workingCondition);
                    statusCode = Integer.parseInt(States.get("statusCode").toString());
                    statusMessage = States.get("statusMessage").toString();

                } else if (cd_code.equals("MwFengZuShou")) {
                    //山东承泽 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String retcode = JsonData.get("retcode").toString();
                    if (retcode.equals("0")) {
                        String cardstatus = JsonData.get("cardstatus").toString();
                        Map<String, Object> CardStatusMap = cardStatusReplacementUtil.getMwFengZuShou_CardStatus(cardstatus);
                        statusCode = Integer.parseInt(CardStatusMap.get("statusCode").toString());
                        statusMessage = CardStatusMap.get("statusMessage").toString();
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("TenngYu")) {
                    //腾宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    if (code.equals("1")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        String status = Data.get("sim_status").toString();

                        Map<String, Object> TenngYu_CardStatusMap = cardStatusReplacementUtil.getTenngYu_CardStatus(status);
                        statusCode = Integer.parseInt(TenngYu_CardStatusMap.get("statusCode").toString());
                        statusMessage = TenngYu_CardStatusMap.get("statusMessage").toString();

                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                }


                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", "200");
            Outdata.put("statusCode", statusCode);
            Outdata.put("statusMessage", statusMessage);
        } catch (Exception e) {
            Outdata.put("statusMessage", "内部接收消息，解析数据异常！");
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 单卡生命周期 变更
     *
     * @param map
     * @return
     */
    public Map<String, Object> changeCardStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "changeCardStatus", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;

            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("IoTLink")) {
                    //IoTLink 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("message").toString();
                    }
                } else if (cd_code.equals("DianXin_DCP")) {
                    //电信 DCP
                    Map<String, Object> Data = (Map<String, Object>) data.get("data");
                    JSONObject jss = JSONObject.parseObject(Data.toString());
                    Object envEnvelope = jss.get("env:Envelope");
                    JSONObject jss1 = JSONObject.parseObject(envEnvelope.toString());
                    Object envBodys = jss1.get("env:Body");
                    JSONObject jss2 = JSONObject.parseObject(envBodys.toString());
                    Object ns2QuerySubscriptionStatusChangeResponse = jss2.get("ns2:QuerySubscriptionStatusChangeResponse");
                    JSONObject jss3 = JSONObject.parseObject(ns2QuerySubscriptionStatusChangeResponse.toString());
                    Object statusRequestResponse = jss3.get("statusRequestResponse");
                    switch (statusRequestResponse.toString()) {
                        case "Completed":
                            statusCode = 200;
                            statusMessage = "操作成功！";
                            break;
                        case "In Progress":
                            statusCode = 200;
                            statusMessage = "执行中！";
                            break;
                        case "Pending":
                            statusCode = 200;
                            statusMessage = "待处理！";
                            break;
                        case "Canceled":
                            statusCode = 204;
                            statusMessage = "已取消！";
                            break;
                        case "Rejected":
                            statusCode = 205;
                            statusMessage = "已拒绝！";
                            break;
                        default:
                            statusMessage = "操作失败！";
                            break;
                    }
                } else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    if (Data.get("result") != null) {
                        statusCode = Integer.parseInt(Data.get("result").toString());
                    } else {
                        String rspcode = Data.get("rspcode").toString();
                        //System.out.println(rspcode);
                        if (rspcode.indexOf("：") != -1) {
                            rspcode = rspcode.substring(0, rspcode.indexOf("："));
                        }
                        statusCode = Integer.parseInt(rspcode);
                        //statusCode = Integer.parseInt(Data.get("rspcode").toString());
                    }
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else if (statusCode == 5) {
                        statusCode = 200;
                        statusMessage = "已是操作生命周期无需操作！";
                    } else {
                        String rspdesc = Data.get("rspdesc").toString();
                        if (rspdesc.indexOf(" 无停机信息，无法复机") != -1) {
                            statusCode = 200;
                            statusMessage = "无停机信息，无法复机！";
                        } else {
                            statusMessage = Data.get("rspdesc").toString();
                        }
                    }

                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String Rcode = Data.get("resultCode").toString();
                    if (Rcode.equals("0000") || Rcode.equals("1193")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("message").toString();
                    }
                } else if (cd_code.equals("DianXin_CMP_5G")) {
                    // DianXin_CMP_5G 解析
                    System.out.println(data);
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String result = Data.get("result").toString();
                    String resultmsg = Data.get("resultmsg") != null ? Data.get("resultmsg").toString() : null;
                    if (result.equals("0")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else if (result.equals("100008")) {//这里需要判断 如 已经是 激活状态 变更成 激活 或 已停机变更成停机 给 成功 状态

                    } else {
                        data.put("Message", Data.get("resultMsg"));
                    }
                } else if (cd_code.equals("YKWL")) {
                    //翼控物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("resultCode").toString();
                    if (code.equals("0")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = JsonData.get("resultMsg").toString();
                    }
                } else if (cd_code.equals("MwFengZuShou")) {
                    //梦网-蜂助手 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String cardretcode = JsonData.get("cardretcode").toString();
                    if (cardretcode.equals("0")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        data.put("Message", JsonData.get("cardmsg"));
                    }
                } else if (cd_code.equals("TenngYu")) {
                    //腾宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    if (code.equals("1")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                }

                Outdata.put("Message", statusMessage);
            } catch (Exception e) {
                Outdata.put("code", "500");
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
                return Outdata;
            }
            Outdata.put("code", statusCode);
            Outdata.put("status", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", e.getMessage().toString());
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 查询是否实名
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryRealNameStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryRealNameStatus", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;

            String Is_status = "0";// 实名状态 0未实名-1已实名
            String Is_statusName = "未实名";//实名状态 说明


            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    if (Data.get("result") != null) {
                        statusCode = Integer.parseInt(Data.get("resultcode").toString());
                    }
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Is_status = "1";// 实名状态 0未实名-1已实名
                        Is_statusName = "已实名";//实名状态 说明
                    } else {
                        statusMessage = Data.get("resultmsg").toString();
                    }
                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String Rcode = Data.get("rspcode").toString();
                    if (Rcode.equals("0001")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Is_status = "1";// 实名状态 0未实名-1已实名
                        Is_statusName = "已实名";//实名状态 说明
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                }

                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }

            Outdata.put("code", Rdata.get("code"));
            Outdata.put("status", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("Is_status", Is_status);
            Outdata.put("Is_statusName", Is_statusName);
        } catch (Exception e) {
            System.out.println(e);
        }
        //return MyRetunSuccess(Outdata,null);
        return Outdata;
    }

    /**
     * 查询APN设置信息
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryAPNInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryAPNInfo", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;

            String APN = "";//

            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String Rcode = Data.get("resultCode").toString();
                    if (Rcode.equals("0000")) {
                        String nacId = String.valueOf(((Map<String, Object>) ((List<?>) Data.get("nacs")).get(0)).get("nacId"));
                        switch (nacId) {
                            case "20996586":
                                APN = "LTD-3";
                                break;
                            case "20996585":
                                APN = "LTD-1";
                                break;
                            case "20996415":
                                APN = "SCUIOT";
                                break;
                            case "21997854":
                                APN = "SCUIOT1";
                                break;
                            case "21997291":
                                APN = "SCUIOT1";
                                break;
                            default:
                                break;
                        }
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());

                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Map<String, Object> result = ((List<Map<String, Object>>) Data.get("result")).get(0);
                        Map<String, Object> serviceTypeList = ((List<Map<String, Object>>) result.get("serviceTypeList")).get(0);
                        APN = serviceTypeList.get("apnName").toString();
                    } else {
                        statusMessage = Data.get("message").toString();
                    }

                }

                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", Rdata.get("code"));
            Outdata.put("status", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("APN", APN);
        } catch (Exception e) {
            System.out.println(e);
        }
        //return MyRetunSuccess(Outdata,null);
        return Outdata;
    }

    /**
     * 单端断网
     *
     * @param map
     * @return
     */
    public Map<String, Object> FunctionApnStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "FunctionApnStatus", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    String rspcode = "";
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String rspdesc = Data.get("rspdesc") != null ? Data.get("rspdesc").toString() : null;

                    if (Data.get("result") != null) {
                        statusCode = Integer.parseInt(Data.get("resultcode").toString());
                    } else {
                        rspcode = Data.get("rspcode").toString();
                    }
                    if (statusCode == 0 || Data.get("rspcode").toString().equals("0000")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else if (rspcode.equals("-5") && rspdesc != null) {
                        if (map.get("Is_Break").toString().equals("0") && rspdesc.indexOf("该号码未订购单独断网功能") != -1) {//开网 未订购 本来就是 开网状态 返回成功
                            statusCode = 200;
                            statusMessage = "操作成功！";
                        } else if (rspdesc.indexOf("已是该网络状态无需操作") != -1) {
                            statusCode = 200;
                            statusMessage = "已是该网络状态无需操作！";
                        }
                    } else {
                        statusMessage = Data.get("rspdesc").toString();
                    }
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("message").toString();
                    }

                } else if (cd_code.equals("DianXin_CMP_5G")) {
                    // DianXin_CMP_5G 解析
                    System.out.println(data);
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String rspcode = Data.get("rspcode").toString();
                    String rspdesc = Data.get("rspdesc") != null ? Data.get("rspdesc").toString() : null;
                    if (rspcode.equals("0000")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else if (rspcode.equals("-9")) {//重复订购或退订时
                        if (rspdesc != null) {
                            String Is_Break = map.get("Is_Break").toString();//  【断网复机】 是否   0 开机 1 停机
                            if (Is_Break.equals("1") && rspdesc.indexOf("不能重复订购") != -1) {//订购时 执行断网 返回 【不能重复订购】 成功
                                statusCode = 200;
                                statusMessage = "操作成功！";
                            } else if (Is_Break.equals("0") && rspdesc.indexOf("功能产品实例标识不能为空或者0") != -1) {//取消单独断网时 执行 返回 【功能产品实例标识不能为空或者0】 成功
                                statusCode = 200;
                                statusMessage = "操作成功！";
                            }
                        }
                    } else {
                        data.put("Message", Data.get("resultMsg"));
                    }
                }
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 限速
     *
     * @param map
     * @return
     */
    public Map<String, Object> SpeedLimit(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "SpeedLimit", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;

            String cd_code = data.get("cd_code").toString();
            try {//.equals()
                System.out.println(data.get("Data") instanceof String);

                // data.get("Data")
                JSONObject Jobj = null;
                if (data.get("Data") instanceof String) {
                    Jobj = JSONObject.parseObject(data.get("Data").toString());
                    //System.out.println(Jobj.get("resultCode"));
                }

                if (data.get("Data") instanceof String && Jobj != null && Jobj.get("resultCode") == null) {
                    Outdata.put("code", Rdata.get("code"));
                    Outdata.put("status", statusCode);
                    Outdata.put("Message", data.get("Data"));
                    //return MyRetunSuccess(Outdata,null);
                    return null;
                }
                JSONObject Data;
                if (data.get("Data") instanceof HashMap) {
                    Data = JSONObject.parseObject(JSONObject.toJSONString(data.get("Data")));
                } else {
                    Data = JSONObject.parseObject(data.get("Data").toString());
                }

                //Map<String,Object> Data = (Map<String, Object>) data.get("Data");
                //System.out.println(Data);
                //System.out.println(Data.containsKey("resultCode"));
                if (!Data.containsKey("resultCode")) {
                    Outdata.put("code", Rdata.get("code"));
                    Outdata.put("status", statusCode);
                    Outdata.put("Message", data.get("Data"));
                    //return MyRetunSuccess(Outdata,null);
                    return null;
                }
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    String resultcode = Data.get("resultCode").toString();
                    if (resultcode.equals("0000") || resultcode.equals("-5")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultmsg").toString();
                    }
                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    //Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String Rcode = Data.get("resultCode").toString();
                    if (Rcode.equals("0000") || Rcode.equals("6079")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    //Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("message").toString();
                    }

                }

                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", Rdata.get("code"));
            Outdata.put("status", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            System.out.println(e);
        }
        //return MyRetunSuccess(Outdata,null);
        return Outdata;
    }

    /**
     * 查询卡是否在网状态
     * date:2021.05.24
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryOnlineStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryOnlineStatus", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int network_status = 99;
            String statusMessage = null;
            String codd = null;
            String imei = null;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("IoTLink")) {
                    //IoTLink 解析
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        Map<String, Object> Data = (Map<String, Object>) JsonData.get("result");
                        String IoTLink_cd_code = Data.get("cd_code").toString();
                        cd_code = IoTLink_cd_code;
                        if (IoTLink_cd_code.equals("DianXin_CMP")) {
                            Map<String, Object> productInfo = (Map<String, Object>) JsonData.get("description");
                            Outdata.put("Data", productInfo);
                            codd = "200";
                        } else if (IoTLink_cd_code.equals("YiDong_EC") || IoTLink_cd_code.equals("YiDong_EC_TOKE_ShuoLang") || IoTLink_cd_code.equals("YiDong_EC_TengYu") || IoTLink_cd_code.equals("YiDong_EC_Combo")) {
                            Map<String, Object> IoTLink_JsonData = JSON.parseObject(data.get("Data").toString());
                            Map<String, Object> IoTLink_Data = null;
                            IoTLink_Data = (Map<String, Object>) IoTLink_JsonData.get("result");
                            Map<String, Object> simSessionList = (Map<String, Object>) IoTLink_Data.get("Data");
                            Outdata.put("Data", simSessionList);
                            codd = "200";
                        } else if (IoTLink_cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo") || IoTLink_cd_code.equals("SDIOT")) {
                            Map<String, Object> IoTLink_JsonData = JSON.parseObject(data.get("Data").toString());
                            Map<String, Object> IoTLink_Data = (Map<String, Object>) IoTLink_JsonData.get("result");
                            Outdata.put("Data", IoTLink_Data);
                            codd = "200";
                        } else if (IoTLink_cd_code.equals("ZCWL")) {
                            Map<String, Object> IoTLink_JsonData = JSON.parseObject(data.get("Data").toString());
                            String code = IoTLink_JsonData.get("code").toString();
                            if (code.equals("0")) {
                                Map<String, Object> IoTLink_Data = JSON.parseObject(IoTLink_JsonData.get("data").toString());
                                Outdata.put("Data", IoTLink_Data);
                                codd = "200";
                            } else {
                                data.put("Message", IoTLink_JsonData.get("msg"));
                            }
                        }

                    } catch (Exception e) {
                        System.out.println(e);
                        data.put("Message", data.get("Data"));
                        //return Myerr(data.get("Data").toString());
                    }
                } else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());

                    try {
                        //Map<String, Object> productInfo = ((List<Map<String, Object>>) JsonData.get("description")).get(0);
                        Map<String, Object> productInfo = (Map<String, Object>) JsonData.get("description");
                        int StatusCd = Integer.parseInt(productInfo.get("result").toString());
                        imei = productInfo.get("imei").toString();
                        String acctStatusType = productInfo.get("acctStatusType").toString();
                        String netModel = productInfo.get("netModel").toString();
                        // 卡停机状态：netModel":"2","acctStatusType":"stop",
                        if ("start".equals(acctStatusType)) {
                            network_status = 1;
                            statusMessage = "开网状态";
                        } else if ("stop".equals(acctStatusType)) {
                            network_status = 0;
                            statusMessage = "断网状态";
                        }
                        Outdata.put("Data", productInfo);
                        codd = "200";
                    } catch (Exception e) {
                        statusMessage = JsonData.get("resultMsg").toString();
                        e.printStackTrace();
                    }

                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        System.out.println(JsonData);
                        Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                        Map<String, Object> simSessionList = ((List<Map<String, Object>>) Data.get("simSessionList")).get(0);
                        Outdata.put("Data", simSessionList);
                        codd = "200";
                    } catch (Exception e) {
                        System.out.println(e);
                        data.put("Message", data.get("Data"));
                        //return Myerr(data.get("Data").toString());
                    }
                } else if (cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo") || cd_code.equals("SDIOT")) {
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        System.out.println(JsonData);
                        Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                        Outdata.put("Data", Data);
                        codd = "200";
                    } catch (Exception e) {
                        System.out.println(e);
                        data.put("Message", data.get("message"));
                        //return Myerr(data.get("Data").toString());
                    }
                } else if (cd_code.equals("ZCWL")) {
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        String code = JsonData.get("code").toString();
                        if (code.equals("0")) {
                            Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                            Outdata.put("Data", Data);
                            codd = "200";
                        } else {
                            data.put("Message", JsonData.get("msg"));
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        data.put("Message", data.get("Data"));
                        //return Myerr(data.get("Data").toString());
                    }
                }


                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                //System.out.println(e);
            }
            Outdata.put("cd_code", cd_code);
            Outdata.put("code", codd);
            Outdata.put("imei", imei);
            Outdata.put("statusCode", network_status);
            Outdata.put("statusMessage", statusMessage);
        } catch (Exception e) {
            Outdata.put("statusMessage", "内部接收消息，解析数据异常！");
            //System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 单卡查询 激活日期
     *
     * @param map
     * @return
     */
    public Map<String, Object> queryCardActiveTime(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryCardActiveTime", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 500;
            String openDate = null, statusMessage = null;
            String activateDate = null;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("IoTLink")) {
                    //IoTLink 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Data = (Map<String, Object>) JsonData.get("result");
                    if (Data.get("activateDate") != null) {
                        activateDate = Data.get("activateDate").toString();
                    }
                } else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> productInfo = ((List<Map<String, Object>>) JsonData.get("productInfo")).get(0);
                    int StatusCd = Integer.parseInt(productInfo.get("productMainStatusCd").toString());
                    // 1：可激活 2：测试激活 3：测试去激活 4：在用 5：停机 6：运营商管理状态 7：拆机
                    if (StatusCd == 4 || StatusCd == 5 || StatusCd == 6) {
                        Object servCreateDate = JsonData.get("servCreateDate");
                        if (servCreateDate != null && servCreateDate != "" && servCreateDate.toString().length() > 0) {
                            activateDate = servCreateDate.toString().substring(0, 4) + "-" + servCreateDate.toString().substring(4, 6)
                                    + "-" + servCreateDate.toString().substring(6, servCreateDate.toString().length());
                        }
                    }
                    statusCode = 200;
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Map<String, Object> result = ((List<Map<String, Object>>) Data.get("result")).get(0);
                        activateDate = result.get("activeDate").toString();
                        openDate = result.get("openDate").toString();
                    } else {
                        statusMessage = Data.get("message").toString();
                    }
                } else if (cd_code.equals("YYWL")) {
                    //移远物联 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    String active = Data.get("active").toString();
                    String activateTime = Data.get("activateTime").toString();
                    if (active != null && activateTime != null && active.equals("激活")) {
                        activateDate = activateTime;
                        statusCode = 200;
                    } else {
                        statusMessage = Data.get("requestMsg").toString();
                    }
                } else if (cd_code.equals("YKWL")) {
                    //翼控物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("resultCode").toString();
                    if (code.equals("0")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("description").toString());
                        if (Data.get("activate_date") != null) {
                            activateDate = Data.get("activate_date").toString();
                            //activateDate = activateDate.replace("T"," ");
                            activateDate = activateDate.split("T")[0];
                            statusCode = 200;
                        }
                    } else {
                        statusMessage = JsonData.get("msresultMsgg").toString();
                    }
                } else if (cd_code.equals("XuYuWuLian")) {
                    //旭宇物联 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("requestCode").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Map<String, Object> result = (Map<String, Object>) Data.get("info");
                        activateDate = result.get("activateDate") != null ? result.get("activateDate").toString() : null;
                        openDate = result.get("openDate") != null ? result.get("openDate").toString() : null;
                    } else {
                        statusMessage = Data.get("requestMsg").toString();
                    }
                } else if (cd_code.equals("MwFengZuShou")) {
                    //第三方梦网-蜂助手 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String retcode = JsonData.get("retcode").toString();
                    if (retcode.equals("0")) {

                        if (JsonData.get("cardactivedate") != null && (JsonData.get("cardactivedate") + "").length() > 0) {
                            activateDate = JsonData.get("cardactivedate").toString();
                            statusCode = 200;
                        }
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("TenngYu")) {
                    //腾宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    if (code.equals("1")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        if (Data.get("activate_date") != null) {
                            activateDate = Data.get("activate_date").toString();
                            statusCode = 200;
                        }
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> dataMap = (Map<String, Object>) data.get("Data");
                    if (dataMap.get("resultCode").equals("0000")) {
                        Map<String, Object> terminals = ((List<Map<String, Object>>) dataMap.get("terminals")).get(0);
                        activateDate = terminals.get("dateActivated") != null ? terminals.get("dateActivated").toString() : activateDate;
                        openDate = terminals.get("dateShipped") != null ? terminals.get("dateShipped").toString() : openDate;
                        statusCode = activateDate != null || openDate != null ? 200 : statusCode;
                    }
                }

                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            activateDate = activateDate != null && activateDate.length() > 9 ? activateDate.substring(0, 10) : activateDate;
            Outdata.put("code", statusCode);
            Outdata.put("activateDate", activateDate);
            Outdata.put("openDate", openDate);
            Outdata.put("statusMessage", statusMessage);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("statusMessage", "内部接收消息，解析数据异常！");
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 机卡解绑
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> unbundling(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            String statusCode = "500";
            String Q_cd_code = find_card_route_map.get("cd_code").toString();
            if (Q_cd_code.equals("DianXin_CMP") || Q_cd_code.equals("DianXin_DCP") || Q_cd_code.equals("YKWL")) {

                if (Q_cd_code.equals("DianXin_CMP")) {
                    map.put("bind_type", null);//解绑时 电信CMP 赋值为空
                }
                Map<String, Object> Rdata = publicApiService.insideApi(map, "MachineCardBinding", find_card_route_map);


                Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
                int network_status = 99;
                String statusMessage = null;
                String codd = null;
                String imei = null;
                String cd_code = data.get("cd_code").toString();
                try {
                    if (cd_code.equals("DianXin_CMP")) {
                        //电信 CMP 解析
                        JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                        Map<String, Object> Data = Data_json;
                        JSONObject responseJson = JSONObject.parseObject(Data.get("Response").toString());
                        Map<String, Object> response = responseJson;
                        String rspcode = response.get("RspCode").toString();
                        if (rspcode.equals("0000")) {
                            statusCode = "200";
                            statusMessage = "下发指令成功！";
                        } else {
                            statusMessage = response.get("RspDesc").toString();
                        }
                    } else if (cd_code.equals("DianXin_DCP")) {
                        //电信 DCP 解析
                        JSONObject Data_json = JSONObject.parseObject(data.get("data").toString());
                        Map<String, Object> Data = Data_json;
                        String status = Data.get("status").toString();
                        if (status.equals("200")) {
                            statusCode = "200";
                            statusMessage = "下发指令成功！";
                        } else {
                            statusMessage = Data.get("msg").toString();
                        }
                    } else if (cd_code.equals("YKWL")) {
                        //翼控物联 解析
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        JSONObject responseJson = JSONObject.parseObject(JsonData.get("description").toString());
                        statusMessage = responseJson.get("msg").toString();
                        String code = JsonData.get("resultCode").toString();
                        //JsonData.get("msresultMsgg").toString();
                        if (code.equals("0")) {
                            statusCode = "200";
                        }
                    }
                } catch (Exception e) {
                    statusMessage = "内部接收消息，解析数据异常！";
                    System.out.println(e);
                }

                Outdata.put("code", statusCode);
                Outdata.put("Message", statusMessage);
            } else if (Q_cd_code.equals("LianTong_CMP")) {
                map.put("unbind", "true");
                Map<String, Object> Rdata = publicApiService.insideApi(map, "changeCardStatusFlexible", find_card_route_map);
                Outdata.put("code", Rdata.get("code"));
                Outdata.put("Message", Rdata.get("msg"));
            } else {
                Outdata.put("code", "400");
                Outdata.put("Message", "暂不支持该 资源 API 机卡绑定操作！");
            }
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 查询imei码
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> queryCardImei(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        String imei = "";
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryCardImei", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            if (((List<Object>) Data_json.get("result")).size() > 0) {
                                Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                                if (Data_1 != null) {
                                    if (Data_1.get("imei") != null) {
                                        imei = Data_1.get("imei").toString();
                                    }
                                }
                            } else {
                                statusMessage = Data_json.get("message") != null ? Data_json.get("message").toString() : "返回数据为空！";
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    }
                }
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", "200");
            Outdata.put("Message", statusMessage);
            Outdata.put("imei", imei);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 移除实名
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> realNameRemove(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "realNameRemove", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            String cd_code = data.get("cd_code").toString();
            String statusCode = "500";
            try {
                if (cd_code.equals("DianXin_DCP")) {
                    //电信 DCP 解析
                    JSONObject Data_json = JSONObject.parseObject(data.get("data").toString());
                    Map<String, Object> Data = Data_json;
                    String status = Data.get("status").toString();
                    if (status.equals("200")) {
                        statusCode = "200";
                        statusMessage = "下发指令成功！";
                    } else {
                        statusMessage = Data.get("msg").toString();
                    }
                } else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = JSONObject.parseObject(data.get("Data").toString());
                    if (Data.get("resultCode") != null) {
                        statusCode = Data.get("resultCode").toString();
                    } else {
                        statusCode = Data.get("rspcode") != null ? Data.get("rspcode").toString() : "500";
                    }
                    statusMessage = Data.get("resultMsg") != null ? Data.get("resultMsg").toString() : "操作成功！";
                    if (statusCode.equals("0") || statusCode.equals("0000")) {
                        statusCode = "200";
                    } else {
                        System.out.println(JSON.toJSONString(Data));
                    }
                }


            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            System.out.println(e.getMessage());
        }
        return Outdata;
    }

    /**
     * 单卡停机原因查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> simStopReason(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        String stopReason = "";
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "simStopReason", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {

                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            if (Data_1 != null) {
                                if (Data_1.get("stopReason") != null) {
                                    statusCode = 200;
                                    String stopCode = Data_1.get("stopReason").toString();
                                    if (stopCode.equals("000000000000")) {
                                        stopReason = "该卡当前不处于“已停机”或系统暂无停机原因";
                                    } else {
                                        stopReason = getStopInfo(stopCode);
                                    }
                                    stopReason = " [ " + stopReason + " ] ";
                                }
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    }
                } else if (cd_code.equals("ZCWL")) {
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        if (JsonData.get("msg") != null) {
                            stopReason = JsonData.get("msg").toString();
                        }
                        statusCode = 200;
                    } catch (Exception e) {
                        System.out.println(e);
                        data.put("Message", data.get("Data"));
                        //return Myerr(data.get("Data").toString());
                    }
                }
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("stopReason", stopReason);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("stopReason", stopReason);
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 移动获取停机原因
     *
     * @param stopCode
     * @return
     */
    public String getStopInfo(String stopCode) {
        String stopReason = "";
        if (stopCode.substring(0, 1).equals("2")) {
            stopReason += " + 窄带网商品到期失效停机";
        }
        if (stopCode.substring(1, 2).equals("2")) {
            stopReason += " + 机卡分离停机";
        }
        if (stopCode.substring(3, 4).equals("2")) {
            stopReason += " + M2M 管理停机";
        }
        if (stopCode.substring(4, 5).equals("2")) {
            stopReason += " + 信控双向停机";
        }
        if (stopCode.substring(8, 9).equals("2")) {
            stopReason += " + 主动（申请）双向停机";
        }
        if (stopCode.substring(9, 10).equals("2")) {
            stopReason += " + 区域限制（位置固定）管理型停机";
        }
        if (stopCode.substring(10, 11).equals("2")) {
            stopReason += " + 强制双向停机";
        }
        stopReason = stopReason.substring(0, 3).equals(" + ") ? stopReason.substring(3, stopReason.length()) : stopReason;
        return stopReason;
    }

    /**
     * 单卡开关机状态实时查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> onOffStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        String Openstatus = "";
        String status = "";
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "onOffStatus", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            statusCode = 200;
                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            if (Data_1 != null) {
                                if (Data_1.get("status") != null) {
                                    status = Data_1.get("status").toString();
                                    if (status.equals("1")) {
                                        Openstatus = "开机";
                                    } else if (status.equals("0")) {
                                        Openstatus = "关机";
                                    } else {
                                        Openstatus = "未知";
                                    }
                                }
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    }
                } else if (cd_code.equals("ZCWL")) {
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        String code = JsonData.get("code").toString();
                        if (code.equals("0")) {
                            Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                            if (Data.get("status") != null) {
                                status = Data.get("status").toString();
                                if (status.equals("1")) {
                                    Openstatus = "开机";
                                } else if (status.equals("0")) {
                                    Openstatus = "关机";
                                } else {
                                    Openstatus = "未知";
                                }
                            }
                            statusCode = 200;
                        } else {
                            data.put("Message", JsonData.get("msg"));
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        data.put("Message", data.get("Data"));
                        //return Myerr(data.get("Data").toString());
                    }
                }
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("Openstatus", Openstatus);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("Openstatus", Openstatus);
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 单卡已开通APN信息查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> apnInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        List<Map<String, Object>> RapnList = new ArrayList<>();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "apnInfo", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            if (Data_1 != null) {
                                List<Map<String, Object>> apnList = (List<Map<String, Object>>) Data_1.get("apnList");
                                statusCode = 200;
                                if (apnList != null) {
                                    for (int i = 0; i < apnList.size(); i++) {
                                        Map<String, Object> obj = apnList.get(i);
                                        String status = obj.get("status").toString().equals("1") ? "恢复(或正常)" : "暂停";
                                        obj.put("statusNeme", status);
                                        RapnList.add(obj);
                                    }
                                }
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    }
                }
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("RapnList", RapnList);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("RapnList", RapnList);
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 物联卡机卡分离状态查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> cardBindStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        String Bindstatus = "";
        String sepTime = "";
        String status = "";
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "cardBindStatus", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            statusCode = 200;
                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            if (Data_1 != null) {
                                if (Data_1.get("result") != null) {
                                    status = Data_1.get("result").toString();
                                    Bindstatus = getBindstatus(status);
                                }
                                sepTime = Data_1.get("sepTime") != null && Data_1.get("sepTime").toString().length() > 0 ? Data_1.get("sepTime").toString() : sepTime;//最近一次的分离时间
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    } else {
                        statusMessage = Data_json.get("message").toString();
                    }
                }


            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("Bindstatus", Bindstatus);
            Outdata.put("sepTime", sepTime);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("Bindstatus", Bindstatus);
            Outdata.put("sepTime", sepTime);
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 移动获取分离状态
     *
     * @param status
     * @return
     */
    public String getBindstatus(String status) {
        String Bindstatus = "";
        if (status.equals("0")) {
            Bindstatus = "已分离";
        } else if (status.equals("1")) {
            Bindstatus = "未分离";
        } else if (status.equals("2")) {
            Bindstatus = "查询失败";
        } else {
            Bindstatus = "未知";
        }
        return Bindstatus;
    }

    /**
     * 单卡状态变更历史查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> simChangeHistory(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        List<Map<String, Object>> changeHistoryList = new ArrayList<>();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "simChangeHistory", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            if (Data_1 != null) {
                                List<Map<String, Object>> changeList = (List<Map<String, Object>>) Data_1.get("changeHistoryList");
                                statusCode = 200;
                                if (changeList != null) {
                                    for (int i = 0; i < changeList.size(); i++) {
                                        Map<String, Object> obj = changeList.get(i);
                                        Integer descStatus = Integer.parseInt(obj.get("descStatus").toString());
                                        Integer targetStatus = Integer.parseInt(obj.get("targetStatus").toString());
                                        Map<String, Object> descStatusMap = cardStatusReplacementUtil.getEcV5CardStatus(descStatus);
                                        Map<String, Object> targetStatusMap = cardStatusReplacementUtil.getEcV5CardStatus(targetStatus);
                                        obj.put("descStatusValue", descStatusMap.get("statusMessage"));
                                        obj.put("targetStatusValue", targetStatusMap.get("statusMessage"));
                                        changeHistoryList.add(obj);
                                    }
                                }
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    } else {
                        statusMessage = Data_json.get("message").toString();
                    }
                }
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("changeHistoryList", changeHistoryList);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("changeHistoryList", changeHistoryList);
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 生命周期 灵活变更
     *
     * @param map
     * @return
     */
    public Map<String, Object> changeCardStatusFlexible(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "changeCardStatusFlexible", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;

            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_DCP")) {
                    //电信 DCP
                    Map<String, Object> Data = (Map<String, Object>) data.get("data");
                    JSONObject jss = JSONObject.parseObject(Data.toString());
                    Object envEnvelope = jss.get("env:Envelope");
                    JSONObject jss1 = JSONObject.parseObject(envEnvelope.toString());
                    Object envBodys = jss1.get("env:Body");
                    JSONObject jss2 = JSONObject.parseObject(envBodys.toString());
                    Object ns2QuerySubscriptionStatusChangeResponse = jss2.get("ns2:QuerySubscriptionStatusChangeResponse");
                    JSONObject jss3 = JSONObject.parseObject(ns2QuerySubscriptionStatusChangeResponse.toString());
                    Object statusRequestResponse = jss3.get("statusRequestResponse");
                    switch (statusRequestResponse.toString()) {
                        case "Completed":
                            statusCode = 200;
                            statusMessage = "操作成功！";
                            break;
                        case "In Progress":
                            statusCode = 200;
                            statusMessage = "执行中！";
                            break;
                        case "Pending":
                            statusCode = 200;
                            statusMessage = "待处理！";
                            break;
                        case "Canceled":
                            statusCode = 204;
                            statusMessage = "已取消！";
                            break;
                        case "Rejected":
                            statusCode = 205;
                            statusMessage = "已拒绝！";
                            break;
                        default:
                            statusMessage = "操作失败！";
                            break;
                    }
                } else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    if (Data.get("result") != null) {
                        statusCode = Integer.parseInt(Data.get("result").toString());
                    } else {
                        String rspcode = Data.get("rspcode").toString();
                        //System.out.println(rspcode);
                        if (rspcode.indexOf("：") != -1) {
                            rspcode = rspcode.substring(0, rspcode.indexOf("："));
                        }
                        statusCode = Integer.parseInt(rspcode);
                        //statusCode = Integer.parseInt(Data.get("rspcode").toString());
                    }
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else if (statusCode == 5) {
                        statusCode = 200;
                        statusMessage = "已是操作生命周期无需操作！";
                    } else {
                        String rspdesc = Data.get("rspdesc").toString();
                        if (rspdesc.indexOf(" 无停机信息，无法复机") != -1) {
                            statusCode = 200;
                            statusMessage = "无停机信息，无法复机！";
                        } else {
                            statusMessage = Data.get("rspdesc").toString();
                        }
                    }

                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    if (data.get("code") != null) {
                        statusCode = Integer.parseInt(data.get("code").toString());
                        statusMessage = data.get("Message").toString();
                    } else {
                        Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                        String Rcode = Data.get("resultCode").toString();
                        if (Rcode.equals("0000") || Rcode.equals("1193")) {
                            statusCode = 200;
                            statusMessage = "操作成功！";
                        } else {
                            statusMessage = Data.get("resultDesc").toString();
                        }
                    }
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("message").toString();
                    }
                } else if (cd_code.equals("YKWL")) {
                    //翼控物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("resultCode").toString();
                    if (code.equals("0")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = JsonData.get("resultMsg").toString();
                    }
                } else if (cd_code.equals("TenngYu")) {
                    //腾宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    String code = JsonData.get("code").toString();
                    if (code.equals("1")) {
                        Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        data.put("Message", JsonData.get("msg"));
                    }
                }

                Outdata.put("Message", statusMessage);
            } catch (Exception e) {
                Outdata.put("code", "500");
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
                return Outdata;
            }
            Outdata.put("code", statusCode);
            Outdata.put("status", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", e.getMessage().toString());
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 单卡本月套餐内流量使用量实时查  [未完成 2022-03-25]
     *
     * @param map
     * @return
     */
    public Map<String, Object> simDataMargin(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "simDataMargin", find_card_route_map);

            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;

            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_DCP")) {

                } else if (cd_code.equals("DianXin_CMP")) {


                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析

                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());

                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Map<String, Object> result = ((List<Map<String, Object>>) Data.get("result")).get(0);
                        List<Map<String, Object>> accmMarginList = (List<Map<String, Object>>) result.get("accmMarginList");


                        Outdata.put("Data", accmMarginList);
                    } else {
                        statusMessage = Data.get("message").toString();
                    }
                }
                Outdata.put("Message", statusMessage);
            } catch (Exception e) {
                Outdata.put("code", "500");
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
                return Outdata;
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", e.getMessage().toString());
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 上游订购套餐查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> queryOffering(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        List<Map<String, Object>> setResult = new ArrayList<>();

        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryOffering", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;

            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            if (Data_1 != null) {
                                setResult = (List<Map<String, Object>>) Data_1.get("offeringInfoList");
                                if (setResult != null) {
                                    statusCode = 200;
                                }
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    } else {
                        statusMessage = Data_json.get("message").toString();
                    }
                } else if (cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo")) {
                    // 移动 EC V2 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = ((List<Map<String, Object>>) Data.get("result")).get(0);
                    setResult = ((List<Map<String, Object>>) Obj.get("prodinfos"));
                    System.out.println(setResult);
                    if (setResult != null) {
                        statusCode = 200;
                    }
                }
                try {
                    if (setResult != null && setResult.size() > 0) {
                        Map<String, Object> sendMap = new HashMap<>();
                        sendMap.put("cd_code", cd_code);
                        sendMap.put("setResult", setResult);
                        sendMap.put("iccid", find_card_route_map.get("iccid"));
                        sendSynApiOfferinginfolist(sendMap);
                    }
                } catch (Exception e) {
                    System.out.println(" sendSynApiOfferinginfolist " + e);
                }

            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("cd_code", cd_code);
            Outdata.put("Message", statusMessage);
            Outdata.put("offeringInfoList", setResult);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("offeringInfoList", setResult);
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 集团群组信息查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> queryGroupInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        List<Map<String, Object>> groupList = new ArrayList<>();
        Integer totalCount = 0;
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryGroupInfo", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            totalCount = Integer.parseInt(Data_1.get("totalCount").toString());

                            if (Data_1 != null) {
                                groupList = (List<Map<String, Object>>) Data_1.get("groupList");
                                if (groupList != null) {
                                    statusCode = 200;
                                }
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    } else {
                        statusMessage = Data_json.get("message").toString();
                    }
                } else if (cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo")) {
                    // 移动 EC V2 解析
                   /* Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = ((List<Map<String, Object>>)Data.get("result")).get(0);
                    groupList = ((List<Map<String, Object>>)Obj.get("prodinfos"));
                    System.out.println(groupList);
                    if (groupList != null) {
                        statusCode = 200;
                    }*/
                }

            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e.getMessage());
            }
            Outdata.put("code", statusCode);
            Outdata.put("cd_code", cd_code);
            Outdata.put("Message", statusMessage);
            Outdata.put("groupList", groupList);
            Outdata.put("totalCount", totalCount);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("groupList", groupList);
            Outdata.put("totalCount", totalCount);
            System.out.println(e.getMessage());
        }
        return Outdata;
    }

    /**
     * 集团群组信息成员查询
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> queryGroupMember(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        List<Map<String, Object>> memberinfoList = new ArrayList<>();
        Integer totalCount = 0;
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryGroupMember", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    if (Data_json.get("result") != null) {
                        statusCode = Integer.parseInt(Data_json.get("status").toString());
                        if (statusCode == 0) {
                            Map<String, Object> Data_1 = ((List<Map<String, Object>>) Data_json.get("result")).get(0);
                            totalCount = Integer.parseInt(Data_1.get("totalCount").toString());

                            if (Data_1 != null) {
                                memberinfoList = (List<Map<String, Object>>) Data_1.get("memberinfoList");
                                if (memberinfoList != null) {
                                    statusCode = 200;
                                }
                            }
                        } else {
                            statusMessage = Data_json.get("message").toString();
                        }
                    } else {
                        statusMessage = Data_json.get("message").toString();
                    }
                } else if (cd_code.equals("DianXin_CMP") || cd_code.equals("DianXin_CMP_5G")) {
                    //电信 CMP 解析
                    JSONObject Data = JSONObject.parseObject(data.get("Data").toString());
                    if (Data.get("description") != null) {
                        Map<String, Object> description = (Map<String, Object>) Data.get("description");
                        if (description.get("simList") != null) {
                            List<Map<String, Object>> simList = (List<Map<String, Object>>) description.get("simList");
                            if (simList != null && simList.size() > 0) {
                                statusCode = 200;
                                memberinfoList = simList;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e.getMessage());
            }
            Outdata.put("code", statusCode);
            Outdata.put("cd_code", cd_code);
            Outdata.put("Message", statusMessage);
            Outdata.put("memberinfoList", memberinfoList);
            Outdata.put("totalCount", totalCount);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("memberinfoList", memberinfoList);
            Outdata.put("totalCount", totalCount);
            System.out.println(e.getMessage());
        }
        return Outdata;
    }

    /**
     * 查询卡详情
     *
     * @param map
     * @param find_card_route_map
     * @return
     */
    public Map<String, Object> queryCardInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        Map<String, Object> rMap = null;
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "queryCardInfo", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            String statusMessage = null;
            Integer statusCode = 500;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    JSONObject Data_json = JSONObject.parseObject(data.get("Data").toString());
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        rMap = ((List<Map<String, Object>>) Data.get("result")).get(0);

                    } else {
                        statusMessage = Data.get("message").toString();
                    }
                }

            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e.getMessage());
            }
            Outdata.put("code", statusCode);
            Outdata.put("cd_code", cd_code);
            Outdata.put("Message", statusMessage);
            Outdata.put("rMap", rMap);
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("rMap", rMap);
            System.out.println(e.getMessage());
        }
        return Outdata;
    }

    /**
     * 达量断网
     *
     * @param map
     * @return
     */
    public Map<String, Object> InternetDisconnection(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        String Rstr = null;
        JSONObject Outdata = new JSONObject();
        try {
            //返回数据解析
            Map<String, Object> Rdata = publicApiService.insideApi(map, "InternetDisconnection", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) Rdata.get("Data");
            int statusCode = 9999;
            String statusMessage = null;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    String rspcode = "";
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    if (Data.get("rspcode") != null) {
                        rspcode = Data.get("rspcode").toString();
                    }
                    if (rspcode.equals("0000")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("rspdesc").toString();
                    }

                } else if (cd_code.equals("DianXin_CMP_5G")) {
                    // DianXin_CMP_5G 解析
                    System.out.println(data);
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String rspcode = Data.get("rspcode").toString();
                    String rspdesc = Data.get("rspdesc") != null ? Data.get("rspdesc").toString() : null;
                    if (rspcode.equals("0000")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        data.put("Message", Data.get("resultMsg"));
                    }
                }
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            System.out.println(e);
        }
        return Outdata;
    }

    public double double_format(double value, int retain) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(retain, RoundingMode.HALF_UP);
        return Double.parseDouble(bd.toString());
    }


    public void sendSynUsageReminder(Map<String, Object> senMap) {
        //同步 上游返回套餐记录信息 路由队列
        String polling_queueName = "admin_sendSynUsageReminder_queue";
        String polling_routingKey = "admin.sendSynUsageReminder.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(senMap), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("发送 同步API套餐 指令失败 " + senMap + " " + e.getMessage());
        }
    }

    public void sendSynApiOfferinginfolist(Map<String, Object> senMap) {
        //同步 上游返回套餐记录信息 路由队列
        String polling_queueName = "admin_sendSynApiOfferinginfolist_queue";
        String polling_routingKey = "admin.sendSynApiOfferinginfolist.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(senMap), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("发送 同步套餐 指令失败 " + senMap + " " + e.getMessage());
        }
    }


    public void sendQuerFlowError(Map<String, Object> senMap) {
        //查询用量接口 失败时 新增到 数据库 比那与后续分析接口错误信息
        String polling_queueName = "admin_sendQuerFlowError_queue";
        String polling_routingKey = "admin.sendQuerFlowError.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(senMap), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("发送 用量同步失败存储 指令失败 " + senMap + " " + e.getMessage());
        }
    }

    /**
     * 补充完整信息
     */
    public Map<String, Object> autocompleteCard(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        try {

            //返回数据解析
            Map<String, Object> queryFlow = publicApiService.insideApi(map, "queryFlow", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) queryFlow.get("Data");
            Map<String, Object> Data = (Map<String, Object>) data.get("Data");

            if (Data.get("resultCode").equals("0000")) {
                Map<String, Object> terminals = ((List<Map<String, Object>>) Data.get("terminals")).get(0);
                Outdata.put("msisdn", terminals.get("msisdn").toString());
                Outdata.put("iccid", terminals.get("iccid").toString());
                Outdata.put("imsi", terminals.get("imsi").toString());
                Outdata.put("imei", terminals.get("imei").toString());
                Outdata.put("used", terminals.get("monthToDateUsage").toString());
                Outdata.put("cd_code", data.get("cd_code").toString());
                Outdata.put("code", "200");
                Outdata.put("Message", data.get("Message").toString());
            } else {
                Outdata.put("code", "500");
                Outdata.put("Message", Data.get("resultDesc").toString());
            }
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            System.out.println(e);
        }
        return Outdata;
    }

    /**
     * 补充完整信息
     */
    public Map<String, Object> autocompleteBatchCard(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        JSONObject Outdata = new JSONObject();
        try {

            //返回数据解析
            Map<String, Object> queryBatchFlow = publicApiService.insideApi(map, "queryBatchFlow", find_card_route_map);
            Map<String, Object> data = (Map<String, Object>) queryBatchFlow.get("Data");
            Map<String, Object> Data = (Map<String, Object>) data.get("Data");

            if (Data.get("resultCode").equals("0000")) {

                Map<String, Object> terminals;
                List<Map<String, Object>> cardInfoArr = new ArrayList<>();
                List<Map<String, Object>> terminalsAll = (List<Map<String, Object>>) Data.get("terminals");
                for (Map<String, Object> stringObjectMap : terminalsAll) {
                    Map<String, Object> cardInfo = new HashMap<>();
                    terminals = stringObjectMap;
                    cardInfo.put("msisdn", terminals.get("msisdn").toString());
                    cardInfo.put("iccid", terminals.get("iccid").toString());
                    cardInfo.put("imsi", terminals.get("imsi").toString());
                    cardInfo.put("imei", terminals.get("imei").toString());
                    cardInfo.put("used", terminals.get("monthToDateUsage").toString());
                    cardInfo.put("realNameStatus", terminals.get("realNameStatus"));
                    cardInfo.put("dateActivated", terminals.get("dateActivated"));
                    cardInfo.put("deliver_date", terminals.get("dateShipped"));
                    int StatusCd = Integer.parseInt(terminals.get("simStatus").toString());
                    cardInfo.put("simStatus", StatusCd);
                    Map<String, Object> LianTong_CMP_CardStatusMap = cardStatusReplacementUtil.getLianTong_CMP_CardStatus(StatusCd);
                    cardInfo.put("status_id", LianTong_CMP_CardStatusMap.get("statusCode").toString());
                    cardInfo.put("statusMessage", LianTong_CMP_CardStatusMap.get("statusMessage").toString());
                    cardInfoArr.add(cardInfo);
                }
                Outdata.put("card_info", cardInfoArr);
                Outdata.put("cd_code", data.get("cd_code").toString());
                Outdata.put("code", "200");
                Outdata.put("Message", data.get("Message").toString());
            } else {
                Outdata.put("code", "500");
                Outdata.put("Message", Data.get("resultDesc").toString());
            }
        } catch (Exception e) {
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            System.out.println(e);
        }
        return Outdata;
    }
}
