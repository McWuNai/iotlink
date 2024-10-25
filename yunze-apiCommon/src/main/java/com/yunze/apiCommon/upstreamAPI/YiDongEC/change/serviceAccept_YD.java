package com.yunze.apiCommon.upstreamAPI.YiDongEC.change;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.YD_EC_Api;
import com.yunze.apiCommon.utils.CommonlyUsed;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 移动  变更 接口
 */
public class serviceAccept_YD extends YD_EC_Api {


    public serviceAccept_YD(Map<String, Object> init_map,String cd_code) {
        super(init_map,cd_code);
    }


    /**
     * 项目描述	单卡卡状态操作
     * 0:申请停机(已激活转已停机)
     1:申请复机(已停机转已激活)
     2:库存转已激活
     3:可测试转库存
     4:可测试转待激活
     5:可测试转已激活
     6:待激活转已激活
     * @param card_no
     * @param flag
     * @return
     * @throws IOException
     */
    public String changeCardStatus(String card_no, String flag){
        String functionNm =  "/change/sim-status";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
            String url = server_Ip+functionNm;
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
    }


    /**
     *
     * @Title: FunctionApnStatus
     * @Description:单卡apn 开停
     * @param @param card_no
     * @param @param operType  0:开 1:停
     * @param @param 单卡apn信息
     * @param @return    参数
     * @return JSONObject    返回类型
     * @throws
     * @author
     * @date 2020年5月25日
     */
    public  String FunctionApnStatus(String card_no,String operType,Map<String, Object> Jobj){
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        String url = server_Ip+"/operate/sim-apn-function";
        String Str = null;

        Map<String, Object> Data = ((List<Map<String, Object>>) Jobj.get("result")).get(0);
        Map<String, Object> serviceTypeList = ((List<Map<String, Object>>) Data.get("serviceTypeList")).get(0);
        String apnName=serviceTypeList.get("apnName").toString();
        String serviceStatus =""; // 当前apn状态
        //获取 查询 apn 数据

        if(Jobj.get("status").toString().equals("0")){
            serviceStatus = serviceTypeList.get("serviceStatus").toString(); // 0：暂停  1：恢复
        }
            if("1".equals(serviceStatus) &&"0".equals(operType)){
                retult.put("status", "0");
                retult.put("message", "操作成功");
                Str = JSON.toJSONString(retult);
            }else if("0".equals(serviceStatus)&&"1".equals(operType)){
                retult.put("status", "0");
                retult.put("message", "操作成功");
                Str = JSON.toJSONString(retult);
            }else{
                json.put("transid", transid);
                json.put("token", token);
                json.put("msisdn", card_no);
                json.put("apnName", apnName);
                json.put("operType", operType);
                Str = send(url,json);
            }
        return repeat(Str,json,url);
    }

    /**
     * @param card_no
     * @param apnName
     * @param serviceUsageState
     * 1:速率恢复
     * 91:APN-AMBR=2Mbps（月初不自动恢复）
     * 92:APN-AMBR=1Mbps（月初不自动恢复
     * 93:APN-AMBR=512Kbps（月初不自动恢复)
     * 94:APN-AMBR=128Kbps（月初不自动恢复)
     * 95:APN-AMBR=2Mbps（月初自动恢复）
     * 96:APN-AMBR=1Mbps（月初自动恢复）
     * 97:APN-AMBR=512Kbps（月初自动恢复）
     * 98:APN-AMBR=128Kbps（月初自动恢复）
     * @return
     */
    public String speed(String card_no, String apnName,String serviceUsageState){
        String functionNm =  "/operate/network-speed";
        JSONObject retult = new JSONObject();
        JSONObject json = new JSONObject();
        String url = server_Ip+functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
            json.put("apnName", apnName);
            json.put("serviceUsageState", serviceUsageState);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            requestParams.put("apnName", apnName);
            requestParams.put("serviceUsageState", serviceUsageState);
            json.put("requestParams", requestParams);
        }

        String Str = send(url,json);
        return repeat(Str,json,url);
    }

    /**
     * 判断 在限速 标准 中
     * @param speedValue
     * @param AutomaticRecovery 月初 是否 自动恢复 0 自动 恢复 1 不自动恢复
     * @return bool = true 在标准中
     */
    public Map<String,Object> speedValue_set(String speedValue,String AutomaticRecovery){
        boolean bool = false;
        Map<String,Object> map = new HashMap<String,Object>();
        List<String> rule = new ArrayList<String>();
        rule.add("1");rule.add("91");rule.add("92");rule.add("93");
        rule.add("94");rule.add("95");rule.add("96");rule.add("97");rule.add("98");
        //自动恢复
        if(AutomaticRecovery!=null && AutomaticRecovery!="" && AutomaticRecovery=="0"){
            if(speedValue.equals("1")){
                speedValue = "96";//1Mbps
            }else if(speedValue.equals("2")){
                speedValue = "95";//2Mbps
            }else if(speedValue.equals("11")){
                speedValue = "97";//512Kbps
            }else if(speedValue.equals("5")){
                speedValue = "98";//128Kbps
            }
        }else{
            if(speedValue.equals("1")){
                speedValue = "92";//1Mbps
            }else if(speedValue.equals("2")){
                speedValue = "91";//2Mbps
            }else if(speedValue.equals("11")){
                speedValue = "93";//512Kbps
            }else if(speedValue.equals("5")){
                speedValue = "94";//128Kbps
            }
        }
        if(speedValue.equals("0")){
            speedValue = "1";//不限制
        }
        bool = CommonlyUsed.Val_Is_Arr(rule,speedValue);//判断 传入 类型是否在 已有 标准中
        map.put("speedValue",speedValue);
        map.put("bool",bool);
        return map;
    }


    /**
     * V5.9.4 2021-11-26 ⚫ 下架“机卡绑定类”商品包及其包内所含接口
     * 单卡机卡绑定/变更/解绑（网络侧）
     * @param card_no
     * @param operType 机卡操作：
     * 1：机卡绑定
     * 2：机卡解绑
     * 3：绑定变更
     * @param imei
     * @return
     */
    public String cardBind(String card_no, String operType,String imei){
        String functionNm =  "/operate/card-bind-by-web";
        JSONObject json = new JSONObject();
        String url = server_Ip+functionNm;
        if(!cd_code.equals("YiDong_EC_TengYu")){
            json.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));
            json.put("token", token);
            json.put("msisdn", card_no);
            json.put("operType", operType);
            json.put("imei", imei);
        }else{
            json.put("interface", functionNm);
            json.put("appid", appId);
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put("msisdn",card_no);
            requestParams.put("operType", operType);
            requestParams.put("imei", imei);
            json.put("requestParams", requestParams);
        }

        String Str = send(url,json);
        return repeat(Str,json,url);
    }





}
