package com.yunze.apiCommon.upstreamAPI.LianTongCMP.Inquire;

import com.cu.api.request.CommonJsonRequest;
import com.cu.api.response.CommonJsonResponse;
import com.yunze.apiCommon.upstreamAPI.LianTongCMP.LT_CMP_Api;
import com.yunze.apiCommon.utils.MD5Util;

import java.io.IOException;
import java.util.*;

/**
 * 查询类接口
 */
public class Query_LT extends LT_CMP_Api {


    public Query_LT(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 获取单个卡的流量
     * @param iccid
     * @param
     * @return
     */
    public static Map<String, Object> queryFlow(String iccid) {

        Map<String, Object> rmap = new HashMap<String, Object>();
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url： https://gwapi.10646.cn/api/wsGetTerminalDetails/V1/1Main/vV1.1
            request.setApiName("wsGetTerminalDetails/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> iccids = new ArrayList<String>();
            iccids.add(iccid);
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            rmap = response.getData();
            // //System.out.println(rmap);
            // //System.out.println("rmap ======   "+rmap);
            if(null==rmap){
                System.out.println(iccids+"--查询流量返回报文-- "+response.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rmap;
    }

    /**
     * 项目描述	当月流量查询接口
     * <p>
     * <p>
     * 不再封装！！！！！！！！
     * 注：上月27号 -本月26号晚 一个月。
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> queryBatchFlow(List<String> iccids) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (iccids.size() > 50 || iccids.size() == 0) {
            result.put("Message", "批量查询仅支持50位号码");
            ////System.out.println(result);
            return result;
        }
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url： https://gwapi.10646.cn/api/wsGetTerminalDetails/V1/1Main/vV1.1
            request.setApiName("wsGetTerminalDetails/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<>();
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();
            //System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        ////System.out.println(result);
        return result;
    }

    /**
     * @Author:
     * @Return: 历史流量查询
     * @Description:
     * @Date:
     */
    public static Map<String, Object> queryFlowHis(String iccid, String time) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url： https://gwapi.10646.cn/api/wsGetTerminalDetails/V1/1Main/vV1.1
            request.setApiName("wsGetTerminalUsage/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> iccids = new ArrayList<String>();
            iccids.add(iccid);
            params.put("iccid", iccid);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            params.put("billingCycle", time);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();
            // //System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 项目描述
     * SIM卡状态:
     * "0": 可测试,
     * "1": 可激活,
     * "2": 已激活,
     * "3": 已停用,
     * "4": 已失效,
     * "5"": 已清除,
     * "6": 已更换,
     * "7": 库存,
     * "8": 开始
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> queryCardStatus(String iccid) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url： https://gwapi.10646.cn/api/wsGetTerminalDetails/V1/1Main/vV1.1
            request.setApiName("wsGetTerminalDetails/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */

            Map<String, Object> params = new HashMap<String, Object>();
            List<String> iccids = new ArrayList<String>();
            iccids.add(iccid);
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            //System.out.println(response);
            result = response.getData();

        } catch (Exception e) {
            e.printStackTrace();
        }
        ////System.out.println(result);
        return result;
    }


    /**
     * 项目描述    	查询通讯计划-限速   单卡接口
     * 0-  恢复正常
     * 1-  上下行限速1Mbps
     * 2-  上下行限速2Mbps
     * 4-  上下行限速4Mbps
     * 8-  上下行限速8Mbps
     * -1-  上下行限速0.5Mbps
     * -2-  上下行限速256Kbps
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> queryNetWork(String iccid) {
        Map<String, Object> result = new HashMap<String, Object>();
        int type = 0;
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url：https://gwapi.10646.cn/api/wsGetNetworkAccessconfig/V1/1Main/vV1.1
            request.setApiName("wsGetNetworkAccessconfig/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> iccids = new ArrayList<>();
            iccids.add(iccid);
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(result);
        return result;
    }


    /**
     * 项目描述    	查询通讯计划-限速		批量接口
     * 0-  恢复正常
     * 1-  上下行限速1Mbps
     * 2-  上下行限速2Mbps
     * 4-  上下行限速4Mbps
     * 8-  上下行限速8Mbps
     * -1-  上下行限速0.5Mbps
     * -2-  上下行限速256Kbps
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> queryNetWorkBatch(List<String> iccids) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> result = new HashMap<String, Object>();
        int type = 0;
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url：https://gwapi.10646.cn/api/wsGetNetworkAccessconfig/V1/1Main/vV1.1
            request.setApiName("wsGetNetworkAccessconfig/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 项目描述    	查询APN设置信息
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> queryAPNMessage(String iccid) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url：https://gwapi.10646.cn/api/wsGetNetworkAccessconfig/V1/1Main/vV1.1
            request.setApiName("wsGetNetworkAccessconfig/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> iccids = new ArrayList<String>();
            iccids.add(iccid);
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(result);
        return result;
    }


    /**
     * 项目描述	查询是否需要实名认证
     * 返回 true :已实名   不需要再实名
     * fasle   :未实名   需要实名认证
     *
     * @throws IOException
     */
    public static Map<String, Object> queryRealNameStatus(String iccid) {
        Map<String, Object> result = new HashMap<String, Object>();

        try{
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url： https://gwapi.10646.cn/api/wsGetTerminalDetails/V1/1Main/vV1.1
            request.setApiName("wsGetTerminalDetails/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> iccids = new ArrayList<String>();
            iccids.add(iccid);
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            //System.out.println(params);
            CommonJsonResponse response = client.execute(request);
            //System.out.println(response.getMessage());
            params= response.getData();
            if (response.isSuccess()) {
                result.put("state", params.get("resultCode"));
                result.put("info", "成功");
                List<Map<String, Object>> terminals = (List<Map<String, Object>>) params.get("terminals");
                params = terminals.get(0);
                String realNameStatus  = (String) params.get("realNameStatus");
                switch (realNameStatus) {
                    case "1":
                        result.put("rspcode","0002");
                        break;
                    case "2":
                        result.put("rspcode","0001");
                        break;
                    default:
                        break;
                }
            }else{
                result.put("rspcode","0002");
            }
            //  realNameStatus
        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }








    public static String getSign(Map<String,Object> map,String key){
        List<String> list=new ArrayList<>(map.keySet());

       /* for (int i = 0; i < list.size(); i++) {
        	//System.out.println(list.get(i));
		}*/
        Collections.sort(list);
        ////System.out.println("后----");
       /* for (int i = 0; i < list.size(); i++) {
        	//System.out.println(list.get(i));
		}*/


        StringBuffer sb=new StringBuffer();
        for(int i=0;i<list.size();i++){
            String k =list.get(i);
            String v=(String )map.get(k);
            sb.append(k).append("=").append(v).append("&");
        }
        String signstr=sb.append("key=").append(key).toString();
        String sign = MD5Util.MD5Encode(signstr).toUpperCase();
        return sign;
    }


    /**
     * 项目描述    	查询APN设置信息
     *
     * @throws IOException
     */
    public static Map<String, Object> queryAPNInfo(String iccid) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            CommonJsonRequest request = new CommonJsonRequest();
            //完整的url：https://gwapi.10646.cn/api/wsGetNetworkAccessconfig/V1/1Main/vV1.1
            request.setApiName("wsGetNetworkAccessconfig/V1/1Main");
            request.setApiVer("V1.1");
            /**
             * 业务参数，需要发给能力提供者
             */
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> iccids = new ArrayList<String>();
            iccids.add(iccid);
            params.put("iccids", iccids);
            params.put("messageId", gettrans_id());
            params.put("openId", openId);
            params.put("version", version);
            request.setParams(params);
            CommonJsonResponse response = client.execute(request);
            result = response.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(result);
        return result;
    }

}
