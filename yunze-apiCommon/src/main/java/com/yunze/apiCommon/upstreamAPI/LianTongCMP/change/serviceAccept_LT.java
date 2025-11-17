package com.yunze.apiCommon.upstreamAPI.LianTongCMP.change;


import com.cu.api.request.CommonJsonRequest;
import com.cu.api.response.CommonJsonResponse;
import com.yunze.apiCommon.upstreamAPI.LianTongCMP.LT_CMP_Api;
import com.yunze.apiCommon.utils.CommonlyUsed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 联通  变更 接口
 */
public class serviceAccept_LT extends LT_CMP_Api {


    public serviceAccept_LT(Map<String, Object> init_map) {
        super(init_map);
    }

    /**
     * 项目描述:  为账户追加资费计划
     * @param accountId 账户ID
     * @param basicPlanName 基础资费名称
     * @param addPlanName 追加资费名称
     * @param addQuantity 追加资费数量
     */
    public Map<String,Object> addRatePlan(String accountId,String basicPlanName, String addPlanName, String addQuantity){
        Map<String,Object> result = new HashMap<String,Object>();
        try{
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url：https://gwapi.10646.cn/api/addRatePlan/V1/1Main/vV1.1
            request.setApiName("addRatePlan/V1/1Main");
            request.setApiVer("V1.1");

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("accountId", accountId);
            params.put("basicPlanName", basicPlanName);
            params.put("addPlanName", addPlanName);
            params.put("addQuantity", addQuantity);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();

        }catch(Exception e){
            e.printStackTrace();
        }
        ////System.out.println(result);
        return result;
    }

    /**
     *
     * 项目描述
     * SIM卡状态:
     "0": 可测试,
     "1": 可激活,
     "2": 已激活,
     "3": 已停用,
     "4": 已失效,
     "5": 已清除,
     "6": 已更换,
     "7": 库存,
     "8": 开始

     * @throws IOException
     */
    public Map<String,Object> changeCardStatus(String iccid,String type){
        Map<String,Object> result = new HashMap<String,Object>();
        try{
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url：https://gwapi.10646.cn/api/wsEditTerminal/V1/1Main/vV1.1
            request.setApiName("wsEditTerminal/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("asynchronous", "0");
            params.put("iccid", iccid);
            params.put("targetValue", type);
            params.put("changeType", "3");
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();

        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 项目描述    	修改通讯计划-限速
     * 0-  恢复正常 21997854 (4M左右)
     * 1-  上下行限速1Mbps 21997289
     * 2-  上下行限速2Mbps 21997290
     * 4-  上下行限速4Mbps 21997291
     * @throws IOException
     */
    public static Map<String,Object> upNetWork(String iccid,String nacId){
        Map<String,Object> result = new HashMap<String,Object>();
        try{
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url：https://gwapi.10646.cn/api/wsEditNetworkAccessConfig/V1/1Main/vV1.1
            request.setApiName("wsEditNetworkAccessConfig/V1/1Main");
            request.setApiVer("V1.1");

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("iccid", iccid);
            params.put("nacId", nacId);
            params.put("asynchronous", "0");
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();

        }catch(Exception e){
            e.printStackTrace();
        }
        ////System.out.println(result);
        return result;
    }

    /**
     * 判断 在限速 标准 中
     * @param speedValue
     * @return bool = true 在标准中
     */
    public Map<String,Object> speedValue_set(String speedValue){
        boolean bool;
        Map<String,Object> map = new HashMap<String,Object>();
        List<String> rule = new ArrayList<String>();
        rule.add("26090663");rule.add("26091603");/*rule.add("21997290");rule.add("21997291");*/
        if(speedValue.length()>0){
            if(speedValue.equals("0")){
                speedValue = "26090663";//不限制
            }else if(speedValue.equals("1")){
                speedValue = "26091603";//上下20Mbps
            }/*else if(speedValue.equals("2")){
                speedValue = "21997290";//2Mbps
            }else if(speedValue.equals("4")){
                speedValue = "21997291";//4Mbps
            }*/
        }/*else{//适配多联通账户匹配
            bool = true;
        }*/
        bool = CommonlyUsed.Val_Is_Arr(rule,speedValue);//判断 传入 类型是否在 已有 标准中
        map.put("speedValue",speedValue);
        map.put("bool",bool);
        return map;
    }




}
