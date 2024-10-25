package com.yunze.iotapi.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 对外API接受消息 返回转换
 */
@Component
public class ApiMessageTransformation {



    /**
     * 获取当前卡的流量使用 月查询接口
     * @param data
     * @return
     */
    public  Map<String,Object> queryFlow(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
            Double Use = -1d;//使用量  MB

            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String total_bytes_cnt = Data.get("total_bytes_cnt").toString();
                    int len = total_bytes_cnt.length();
                    System.out.println(total_bytes_cnt);
                    total_bytes_cnt = total_bytes_cnt.substring(0, len - 2);
                    System.out.println(total_bytes_cnt);
                    Use = Double.parseDouble(total_bytes_cnt);
                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = ((List<Map<String, Object>>) ((Map<String, Object>) data.get("Data")).get("terminals")).get(0);
                    Use = Double.parseDouble(Data.get("monthToDateUsage").toString());
                    //根据联通接口26账单日数据封装自然月
                    //Use = Use - queryHisFlowSys(iccid);
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    System.out.println(JsonData);
                    Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                    double kb = Double.parseDouble(Data.get("dataAmount").toString());
                    Use = double_format(kb / 1024, 2);//KB 转 MB
                }else if (cd_code.equals("DongXin_ECV2")) {
                    //东信 移动 EC V2 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    System.out.println(JsonData);
                    Map<String, Object> Data = ((List<Map<String, Object>>) ((Map<String, Object>)JsonData.get("result")).get("result")).get(0);
                    double kb = Double.parseDouble(Data.get("total_gprs").toString());
                    Use = double_format(kb / 1024, 2);//KB 转 MB
                }else if (cd_code.equals("ZS_CMP")) {
                    //中翼联通 CMP 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    System.out.println(JsonData);
                    Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                    Use = Double.parseDouble(Data.get("used").toString());
                }else if (cd_code.equals("XuYuWuLian")) {
                    //旭宇物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    //System.out.println(JsonData);
                    Map<String, Object> Data = JSON.parseObject(JsonData.get("resultMap").toString());
                    Use = Double.parseDouble(Data.get("useGprs").toString());
                }else if (cd_code.equals("SDIOT")) {
                    //山东移动老系统 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    //System.out.println(JsonData);
                    Map<String, Object> Data = ((List<Map<String, Object>>) (((Map<String, Object>)((List<Map<String, Object>>)JsonData.get("result")).get(0)).get("gprs"))).get(0);

                    double kb = Double.parseDouble(Data.get("used").toString());
                    Use = double_format(kb / 1024, 2);//KB 转 MB
                }else if (cd_code.equals("YYWL")) {
                    //移远物联 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Use = Double.parseDouble(JsonData.get("flow").toString());
                }else if (cd_code.equals("ShuoLang")) {
                    //硕朗 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                    String code = JsonData.get("code").toString();
                    if(code.equals("0")){
                        double kb = Double.parseDouble(Data.get("used").toString());
                        if(kb>-1){
                            Use = double_format(kb / 1024, 2);//KB 转 MB
                        }
                    }else{
                        data.put("Message",JsonData.get("msg"));
                    }
                }
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                Outdata.put("code", "500");
                Outdata.put("Message", "内部接收消息，解析数据异常！");
                Outdata.put("Use", -1);
                return Outdata;
            }
            Outdata.put("code", "200");
            Outdata.put("Use", Use);
        } catch (Exception e) {
            Outdata.put("Use", -1);
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
        }
        return Outdata;
    }

    /**
     * 单卡历史流量查询
     *
     * @param data
     * @return
     */
    public  Map<String,Object> queryFlowHis(Map<String, Object> data) {
            Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
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
                }else if (cd_code.equals("ZS_CMP")) {
                    //中翼联通 CMP 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Data = JSON.parseObject(data.get("usedInfo").toString());
                    Use = Double.parseDouble(Data.get("totalDataVolume").toString());
                }


                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                Outdata.put("code", "500");
                Outdata.put("Message", "内部接收消息，解析数据异常！");
                Outdata.put("Use", -1);
                return Outdata;
            }
            Outdata.put("code", "200");
            Outdata.put("Use", Use);
        } catch (Exception e) {
            Outdata.put("Use", -1);
            Outdata.put("code", "500");
        }
        return Outdata;
    }

    /**
     * 单卡生命周期查询
     * @param data
     * @return
     */
    public  Map<String,Object> queryCardStatus(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            String code = "200";
            //返回数据解析
            int statusCode = 0;
            String statusMessage = null,Message ="内部接收消息，解析数据异常！";
            String cd_code = data.get("cd_code").toString();
            try {
                if(cd_code.equals("DianXin_DCP")){
                    //电信 DCP
                    Map<String, Object> JsonData = JSON.parseObject(data.get("data").toString());
                    JSONObject j0=JSONObject.parseObject(JsonData.toString());
                    Object Envelope =j0.get("env:Envelope");
                    JSONObject j1=JSONObject.parseObject(Envelope.toString());
                    Object Body = j1.get("env:Body");
                    JSONObject j2=JSONObject.parseObject(Body.toString());
                    Object qsr = j2.get("ns2:QuerySimResourceResponse");
                    JSONObject j3=JSONObject.parseObject(qsr.toString());
                    Object SimResource  = j3.get("SimResource");
                    JSONObject j4=JSONObject.parseObject(SimResource.toString());
                    Object simSubscriptionStatus  = j4.get("simSubscriptionStatus");
                    // Active （ 已 激 活 ）Deactivated （去激活） Paused （ 停 机 保 号 ）Terminated （拆机）
                    switch (simSubscriptionStatus.toString()) {
                        case "Active":
                            statusCode = 1;
                            statusMessage = "正常";
                            break;
                        case "Pause":
                            statusCode = 2;
                            statusMessage = "停机";
                            break;
                        case "Deactivated":
                            statusCode = 7;
                            statusMessage = "待激活";
                            break;
                        case "Terminated":
                            statusCode = 16;
                            statusMessage = "拆机";
                            break;
                        default:
                            break;
                    }
                }else if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> productInfo = ((List<Map<String, Object>>) JsonData.get("productInfo")).get(0);
                    int StatusCd = Integer.parseInt(productInfo.get("productMainStatusCd").toString());
                    // 1：可激活 2：测试激活 3：测试去激活 4：在用 5：停机 6：运营商管理状态 7：拆机
                    switch (StatusCd){
                        case 1:
                            statusCode = 7;
                            statusMessage = "待激活";
                            break;
                        case 2:
                            statusCode = 9;
                            statusMessage = "测试期正常";
                            break;
                        case 3:
                            statusCode = 15;
                            statusMessage = "测试去激活";
                            break;
                        case 4:
                            statusCode = 1;
                            statusMessage = "正常";
                            break;
                        case 5:
                            statusCode = 2;
                            statusMessage = "停机";
                            break;
                        case 6:
                            statusCode = 14;
                            statusMessage = "运营商管理状态";
                            break;
                        case 7:
                            statusCode = 16;
                            statusMessage = "拆机";
                            break;
                        default :
                            statusCode = 8;
                            statusMessage = "未知";
                            break;
                    }
                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    Map<String, Object> Obj = ((List<Map<String, Object>>) Data.get("terminals")).get(0);
                    System.out.println(Obj);
                    int StatusCd = Integer.parseInt(Obj.get("simStatus").toString());
                    // "0": 可测试,"1": 可激活, "2": 已激活, "3": 已停用, "4": 已失效, "5"": 已清除, "6": 已更换, "7": 库存, "8": 开始
                    switch (StatusCd){
                        case 1:
                            statusCode = 7;
                            statusMessage = "待激活";
                            break;
                        case 2:
                            statusCode = 9;
                            statusMessage = "测试期正常";
                            break;
                        case 3:
                            statusCode = 15;
                            statusMessage = "测试去激活";
                            break;
                        case 4:
                            statusCode = 1;
                            statusMessage = "正常";
                            break;
                        case 5:
                            statusCode = 2;
                            statusMessage = "停机";
                            break;
                        case 6:
                            statusCode = 14;
                            statusMessage = "运营商管理状态";
                            break;
                        case 7:
                            statusCode = 16;
                            statusMessage = "拆机";
                            break;
                        default :
                            statusCode = 8;
                            statusMessage = "未知";
                            break;
                    }
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    System.out.println(JsonData);
                    Map<String, Object> Obj = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                    System.out.println(Obj);
                    int StatusCd = Integer.parseInt(Obj.get("cardStatus").toString());
                    //   1：待激活2：已激活4：停机6：可测试7：库存 8：预销户
                    if (StatusCd == 1 ) {
                        statusCode = 7;
                        statusMessage = "待激活";
                    }else if (StatusCd == 6) {
                        statusCode = 17;
                        statusMessage = "可测试";
                    }else if (StatusCd == 7) {
                        statusCode = 19;
                        statusMessage = "库存";
                    } else if (StatusCd == 2) {
                        statusCode = 1;
                        statusMessage = "正常";
                    } else if (StatusCd == 4 ) {
                        statusCode = 2;
                        statusMessage = "停机";
                    }else if (StatusCd == 8) {
                        statusCode = 4;
                        statusMessage = "预销号";
                    }
                }else if (cd_code.equals("DongXin_ECV2")) {
                    //东信移动 EC V2 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    //System.out.println(JsonData);
                    Map<String, Object> Obj = ((List<Map<String, Object>>) ((Map<String, Object>)JsonData.get("result")).get("result")).get(0);
                    //System.out.println(Obj);
                    String StatusCd = Obj.get("STATUS").toString();
                    // ⚫ 00 -正常；⚫  01-单向停机；⚫  02-停机；⚫  03-预销号；⚫  05-过户；⚫  06-休眠；⚫  07-待激活；⚫  99-号码不存在
                    if (StatusCd.equals("07")) {
                        statusCode = 7;
                        statusMessage = "待激活";
                    } else if (StatusCd.equals("00")) {
                        statusCode = 1;
                        statusMessage = "正常";
                    } else if (StatusCd.equals("01")) {
                        statusCode = 3;
                        statusMessage = "单向停机";
                    } else if (StatusCd.equals("03")) {
                        statusCode = 4;
                        statusMessage = "预销号";
                    }  else if (StatusCd.equals("05")) {
                        statusCode = 5;
                        statusMessage = "过户";
                    } else if (StatusCd.equals("06")) {
                        statusCode = 6;
                        statusMessage = "休眠";
                    }else if ( StatusCd.equals("02")) {
                        statusCode = 2;
                        statusMessage = "停机";
                    }
                }else if (cd_code.equals("ZS_CMP")) {
                    //中翼联通 CMP 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = JSON.parseObject(Data.get("data").toString());
                    //System.out.println(Obj);
                    int StatusCd = Integer.parseInt(Obj.get("Interface_statuc").toString());
                    // "0": 待激活,"1": 已激活, "2": 已停用, "3": 已失效,
                    if (StatusCd == 0 || StatusCd == 3) {
                        statusCode = 7;
                        statusMessage = "待激活";
                    } else if (StatusCd == 1) {
                        statusCode = 1;
                        statusMessage = "正常";
                    } else if (StatusCd == 2 ) {
                        statusCode = 2;
                        statusMessage = "停机";
                    }
                }else if (cd_code.equals("XuYuWuLian")) {
                    //旭宇物联 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = JSON.parseObject(Data.get("resultMap").toString());
                    String workingCondition = Obj.get("workingCondition").toString();
                    String PROVIDER = Obj.get("PROVIDER").toString();
                    Map<String, Object> States =getStates(workingCondition);
                    statusCode = Integer.parseInt(States.get("statusCode").toString());
                    statusMessage = States.get("statusMessage").toString();

                }else if (cd_code.equals("SDIOT")) {
                    //山东移动老系统 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Obj = ((List<Map<String, Object>>)Data.get("result")).get(0);
                    String StatusCd = Obj.get("STATUS").toString();
                    // 	00-正常；	01-单向停机；	02-停机；	03-预销号；	04-销号；	05-过户；	06-休眠；	07-待激；	99-号码不存在
                    if (StatusCd.equals("07")) {
                        statusCode = 7;
                        statusMessage = "待激活";
                    } else if (StatusCd.equals("00")) {
                        statusCode = 1;
                        statusMessage = "正常";
                    } else if (StatusCd.equals("01")) {
                        statusCode = 3;
                        statusMessage = "单向停机";
                    } else if (StatusCd.equals("03")) {
                        statusCode = 4;
                        statusMessage = "预销号";
                    } else if (StatusCd.equals("04")) {
                        statusCode = 18;
                        statusMessage = "销号";
                    } else if (StatusCd.equals("05")) {
                        statusCode = 5;
                        statusMessage = "过户";
                    } else if (StatusCd.equals("06")) {
                        statusCode = 6;
                        statusMessage = "休眠";
                    }else if ( StatusCd.equals("02")) {
                        statusCode = 2;
                        statusMessage = "停机";
                    }
                }else if (cd_code.equals("YYWL")) {
                    //移远物联 解析 status
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    //SysstatusCodetem.out.println(Obj);
                    String StatusCd = Data.get("status").toString();
                    Map<String, Object> States =getStates(StatusCd);
                    statusCode = Integer.parseInt(States.get("statusCode").toString());
                    statusMessage = States.get("statusMessage").toString();
                }else if (cd_code.equals("ShuoLang")) {
                    //硕朗 解析 message
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Data = JSON.parseObject(JsonData.get("data").toString());
                    String message = Data.get("message").toString();
                    Map<String, Object> States =getStates(message);
                    statusCode = Integer.parseInt(States.get("statusCode").toString());
                    statusMessage = States.get("statusMessage").toString();
                }
                Message =  data.get("Message").toString();
            } catch (Exception e) {
                Message = "内部接收消息，解析数据异常！";
                code = "500";
            }
            Message = !Message.equals("内部接收消息，解析数据异常！")?Message:"查询成功！";
            Outdata.put("code", code);
            Outdata.put("statusCode", statusCode);
            Outdata.put("statusMessage", statusMessage);
            Outdata.put("Message", Message);
        } catch (Exception e) {
            Outdata.put("Message", "内部接收消息，解析数据异常！");
            Outdata.put("code", "500");
            System.out.println(e);
        }
        return Outdata;
    }


    /**
     * 单卡生命周期 变更
     * @param data
     * @return
     */
    public  Map<String,Object> changeCardStatus(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
            String statusCode = "-1";
            String statusMessage = "内部接收消息，解析数据异常！";
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    if (Data.get("result") != null) {
                        statusCode = Data.get("result").toString();
                    } else {
                        String rspcode = Data.get("rspcode").toString();
                        //System.out.println(rspcode);
                        if(rspcode.indexOf("：")!=-1){
                            rspcode = rspcode.substring(0,rspcode.indexOf("："));
                        }
                        statusCode = rspcode;
                        //statusCode = Integer.parseInt(Data.get("rspcode").toString());
                    }
                    if (statusCode.equals("0") ) {
                        statusCode = "200";
                        statusMessage = "操作成功！";
                    }else if(statusCode.equals("5")){
                        statusCode = "200";
                        statusMessage = "已是操作生命周期无需操作！";
                    } else {
                        statusMessage = Data.get("rspdesc").toString();
                    }

                } else if (cd_code.equals("LianTong_CMP")) {
                    //联通 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    String Rcode = Data.get("resultCode").toString();
                    if (Rcode.equals("0000") || Rcode.equals("1193") ) {
                        statusCode = "200";
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Data.get("status").toString();
                    if (statusCode.equals("0")) {
                        statusCode = "200";
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("message").toString();
                    }
                }else if (cd_code.equals("ZS_CMP")) {
                    //中翼联通 CMP 解析
                    Map<String, Object> obj = JSON.parseObject(data.get("Data").toString());
                    Map<String, Object> Data = JSON.parseObject(obj.get("data").toString());
                    String Rcode = Data.get("state").toString();
                    if (Rcode.equals("0000") || Rcode.equals("1193") ) {
                        statusCode = "200";
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                }
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                statusCode = "500";
                System.out.println(e);
            }
            statusMessage = !statusMessage.equals("内部接收消息，解析数据异常！")?statusMessage:"操作成功！";
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            System.out.println(e);
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
        }
        return Outdata;
    }



    /**
     * 查询是否实名
     * @param data
     * @return
     */
    public  Map<String,Object> queryRealNameStatus(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
            int statusCode = 500;
            String statusMessage = null;
            String Is_status = "0" ;// 实名状态 0未实名-1已实名
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
                        Is_status = "1" ;// 实名状态 0未实名-1已实名
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
                        Is_status = "1" ;// 实名状态 0未实名-1已实名
                        Is_statusName = "已实名";//实名状态 说明
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                }else if (cd_code.equals("ZS_CMP")) {
                    //中翼联通 CMP 解析
                    Map<String, Object> obj = JSON.parseObject(data.get("Data").toString());

                    Map<String, Object> Data = JSON.parseObject(obj.get("data").toString());
                    boolean Rcode = (boolean) Data.get("status");
                    if (Rcode) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Is_status = "1" ;// 实名状态 0未实名-1已实名
                        Is_statusName = "已实名";//实名状态 说明
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                }
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                statusCode = 500;
                System.out.println(e);
            }
            Outdata.put("code", ""+statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("Is_status", Is_status);
            Outdata.put("Is_statusName", Is_statusName);
        } catch (Exception e) {
            System.out.println(e);
            Outdata.put("code", ""+500);
            Outdata.put("Message", "内部接收消息，解析数据异常！");
        }
        return Outdata;
    }




    /**
     * 查询APN设置信息
     * @param data
     * @return
     */
    public  Map<String,Object> queryAPNInfo(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
            int statusCode = 9999;
            String statusMessage = null;
            String APN = "" ;//
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
                }else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());

                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                        Map<String,Object> result = ((List<Map<String,Object>>)Data.get("result")).get(0);
                        Map<String,Object> serviceTypeList =((List<Map<String,Object>>) result.get("serviceTypeList")).get(0);
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
            Outdata.put("code", statusCode);
            Outdata.put("Message", statusMessage);
            Outdata.put("APN", APN);
        } catch (Exception e) {
            System.out.println(e);
        }
        return Outdata;
    }


    /**
     * 单端断网
     * @param data
     * @return
     */
    public  Map<String,Object> FunctionApnStatus(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
            int statusCode = 500;
            String statusMessage = null;
            int rspcode = 99;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> Data = (Map<String, Object>) data.get("Data");
                    if (Data.get("result") != null) {
                        statusCode = Integer.parseInt(Data.get("resultcode").toString());
                    }else {
                        rspcode = Integer.parseInt(Data.get("rspcode").toString());
                    }
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("rspdesc").toString();
                    }
                    if(rspcode==-5){
                        statusCode = 200;
                        statusMessage = "已是该网络状态无需操作！";
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
                }
                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", ""+statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            System.out.println(e);
            Outdata.put("code", "500");
            Outdata.put("status", "内部接收消息，解析数据异常！");
        }
        return Outdata;
    }


    /**
     * 限速
     * @param data
     * @return
     */
    public  Map<String,Object> SpeedLimit(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
            int statusCode = 500;
            String statusMessage = null;
            String cd_code = data.get("cd_code").toString();
            try {//.equals()
                System.out.println(data.get("Data") instanceof String);
                // data.get("Data")
                JSONObject Jobj = null;
                if(data.get("Data") instanceof String){
                    Jobj = JSONObject.parseObject(data.get("Data").toString());
                    //System.out.println(Jobj.get("resultCode"));
                }
                if(data.get("Data") instanceof String && Jobj!=null && Jobj.get("resultCode")==null){
                    Outdata.put("code", ""+statusCode);
                    Outdata.put("Message", data.get("Data"));
                    return Outdata;
                }
                JSONObject Data = JSONObject.parseObject(data.get("Data").toString());

                //Map<String,Object> Data = (Map<String, Object>) data.get("Data");
                //System.out.println(Data);
                //System.out.println(Data.containsKey("resultCode"));
                if(!Data.containsKey("resultCode")){
                    Outdata.put("code", ""+statusCode);
                    Outdata.put("Message", data.get("Data"));
                    return Outdata;
                }
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    String  resultcode = Data.get("resultCode").toString();
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
                    if (Rcode.equals("0000")|| Rcode.equals("6079")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                }else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                    //移动 EC 解析
                    //Map<String, Object> Data = JSON.parseObject(data.get("Data").toString());
                    statusCode = Integer.parseInt(Data.get("status").toString());
                    if (statusCode == 0) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("message").toString();
                    }

                }else if (cd_code.equals("ZS_CMP")) {
                    //中翼联通 CMP 解析
                    Map<String, Object> ZT_LT_data = JSON.parseObject(Data.get("data").toString());
                    String Rcode = ZT_LT_data.get("state").toString();
                    if (Rcode.equals("0000")|| Rcode.equals("6079")) {
                        statusCode = 200;
                        statusMessage = "操作成功！";
                    } else {
                        statusMessage = Data.get("resultDesc").toString();
                    }
                }

                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                System.out.println(e);
            }
            Outdata.put("code", ""+statusCode);
            Outdata.put("Message", statusMessage);
        } catch (Exception e) {
            System.out.println(e);
            Outdata.put("code", "500");
            Outdata.put("Message", "内部接收消息，解析数据异常！");
        }
        return Outdata;
    }


    /**
     * 查询卡是否在网状态
     * @param data
     * @return
     */
    public  Map<String,Object> queryOnlineStatus(Map<String, Object> data) {
        Map<String,Object> Outdata = new HashMap<>();
        try {
            //返回数据解析
            int network_status = -1;
            String statusMessage = null;
            String codd = "500";
            int type = 0;
            String typeName = "";
            String imei = null;
            String cd_code = data.get("cd_code").toString();
            try {
                if (cd_code.equals("DianXin_CMP")) {
                    //电信 CMP 解析
                    Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                    type = 2;
                    typeName = "电信CMP";
                    try{
                        //Map<String, Object> productInfo = ((List<Map<String, Object>>) JsonData.get("description")).get(0);
                        Map<String,Object> productInfo = (Map<String, Object>) JsonData.get("description");
                        int StatusCd = Integer.parseInt(productInfo.get("result").toString());
                        imei = productInfo.get("imei").toString();
                        String  acctStatusType = productInfo.get("acctStatusType").toString();
                        String netModel = productInfo.get("netModel").toString();
                        // 卡停机状态：netModel":"2","acctStatusType":"stop",
                        if ("start".equals(acctStatusType)) {
                            network_status = 1;
                            statusMessage = "开网状态";
                        } else if ("stop".equals(acctStatusType)) {
                            network_status = 0;
                            statusMessage = "断网状态";
                        }
                        codd = "200";
                    }catch (Exception e){
                        statusMessage = JsonData.get("resultMsg").toString();
                    }

                }else if(cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")){
                    type = 1;
                    typeName = "移动EC";
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        //System.out.println(JsonData);
                        Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                        Map<String, Object> simSessionList = ((List<Map<String, Object>>) Data.get("simSessionList")).get(0);
                        Outdata.put("Data",simSessionList);
                        codd = "200";
                    }catch (Exception e){
                        statusMessage = "内部接收消息，解析数据异常！";
                        return Outdata;
                    }
                }else if(cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo") || cd_code.equals("SDIOT")){
                    type = 3;
                    typeName = "移动Pboos";
                    try {
                        Map<String, Object> JsonData = JSON.parseObject(data.get("Data").toString());
                        System.out.println(JsonData);
                        Map<String, Object> Data = ((List<Map<String, Object>>) JsonData.get("result")).get(0);
                        Outdata.put("Data",Data);
                        codd = "200";
                    }catch (Exception e){
                        System.out.println(e);
                        statusMessage = "内部接收消息，解析数据异常！";
                        //return Myerr(data.get("Data").toString());
                    }
                }

                Outdata.put("Message", data.get("Message"));
            } catch (Exception e) {
                statusMessage = "内部接收消息，解析数据异常！";
                //System.out.println(e);
            }
            Outdata.put("code", codd);
            Outdata.put("type", type);
            Outdata.put("typeName", typeName);
            Outdata.put("imei",imei);
            Outdata.put("statusCode", network_status);
            Outdata.put("statusMessage", statusMessage);
        } catch (Exception e) {
            Outdata.put("statusMessage", "内部接收消息，解析数据异常！");
            Outdata.put("code", "500");
        }
        return Outdata;
    }



    public  double double_format(double value, int retain) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(retain, RoundingMode.HALF_UP);
        return Double.parseDouble(bd.toString());
    }



    /**
     * 获取卡状态
     * @param StatusCd
     * @return
     */
    public Map<String,Object> getStates(String StatusCd){
        Map<String,Object> map = new HashMap<>();
        int statusCode = 0;
        String statusMessage ="";
        // 正常• 单项停机• 停机• 预销号• 销号• 过户• 休眠• 待激活• 未知
        switch (StatusCd){
            case "正常":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "单向停机":
                statusCode = 3;
                statusMessage = "单向停机";
                break;
            case "停机":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "预销号":
                statusCode = 4;
                statusMessage = "预销号";
                break;
            case "销号":
                statusCode = 18;
                statusMessage = "销号";
                break;
            case "过户":
                statusCode = 5;
                statusMessage = "过户";
                break;
            case "休眠":
                statusCode = 6;
                statusMessage = "休眠";
                break;
            case "待激活":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "号码不存在":

                break;
            case "已激活":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "激活":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "已停用":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "已失效":
                statusCode = 18;
                statusMessage = "销号";
                break;
            case "可测试":
                statusCode = 17;
                statusMessage = "可测试";
                break;
            case "库存":
                statusCode = 19;
                statusMessage = "库存";
                break;
            case "可激活":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "测试期正常":
                statusCode = 9;
                statusMessage = "测试期正常";
                break;
            case "测试去激活":
                statusCode = 9;
                statusMessage = "测试期正常";
                break;
            case "在用":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "运营商管理状态":
                statusCode = 14;
                statusMessage = "运营商管理状态";
                break;
            case "拆机":
                statusCode = 16;
                statusMessage = "拆机";
                break;
            case "销号/拆机":
                statusCode = 16;
                statusMessage = "拆机";
                break;
            case "违章停机":
                statusCode = 20;
                statusMessage = "违章停机";
                break;
            case "测试激活":
                statusCode = 9;
                statusMessage = "测试期正常";
                break;
            case "断网":
                statusCode = 21;
                statusMessage = "断网";
                break;
        }
        map.put("statusCode",statusCode);
        map.put("statusMessage",statusMessage);
        return map;
    }



}
