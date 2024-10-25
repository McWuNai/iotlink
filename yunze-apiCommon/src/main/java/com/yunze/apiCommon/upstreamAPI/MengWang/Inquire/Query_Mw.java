package com.yunze.apiCommon.upstreamAPI.MengWang.Inquire;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.MengWang.Mw_Api;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.MD5Util;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.apiCommon.utils.XmlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询类接口
 */
public class Query_Mw extends Mw_Api {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
       /* map.put("cd_username", "sdzywlw20180510");
        map.put("cd_pwd", "sdwlwzy@2018!");*/

        map.put("cd_username", "C0000103");
        map.put("cd_pwd", "mengwang,5YNesygirK0ZAOM");
        map.put("cd_key", "https://tx.phone580.com/iotcard-buss/api/out-open");

        Query_Mw Qy = new Query_Mw(map);

        for (int i = 0; i < 1; i++) {
            System.out.println(Qy.queryInfo("1440900887334"));
        }


    }


    public Query_Mw(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 获取单卡的流量 [物联网卡信息查询接口]
     * @param cardNo 接入号
     * @param
     * @return
     */
    public static String queryInfo(String cardNo) {
        String rMapStr = null;
        try {
            List<Map<String,Object>> cardList = new ArrayList<>();
            Map<String, Object> params = new HashMap<String, Object>();
            String txnDate = VeDate.getStringDate();
            params.put("channelNo", channelNo);
            params.put("txnDate", txnDate);
            params.put("version", version);
            String str = channelNo +txnDate+ version+ MD5Util.MD5Encode(userpws);
            setSign(txnDate,str);
            params.put("sign", sign);
            Map<String,Object> cardInfo = new HashMap<>();
            cardInfo.put("cardNo",cardNo);
            cardList.add(cardInfo);

            String rootNme = "cardTariffQueryReq";
            String url = server_Ip + "/card/query";
            String paramsStr = getXmlStr(params,rootNme,cardList);
            String Rstr = HttpUtil.postXml(url, paramsStr);
            System.out.println(Rstr);
            try {
                rMapStr = JSON.toJSONString(XmlUtil.xmlToMap(Rstr));
            } catch (Exception e) {
                System.err.println(" [Query_Mw queryFlow] 解析xml异常 "+e+" e "+e.getMessage()+"  ||||   "+Rstr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rMapStr;
    }



    /**
     * 停复机 11：停用 12： 启用
     * @param cardNo 接入号
     * @param
     * @return
     */
    public static String ChangeState(String cardNo,String operate) {
        String rMapStr = null;
        try {
            String txnDate = VeDate.getStringDate();

            String orderId = VeDate.getNo(18);
            List<Map<String,Object>> cardList = new ArrayList<>();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("channelNo", channelNo);
            params.put("txnDate", txnDate);
            params.put("operate", operate);
            params.put("orderId", orderId);
            params.put("version", version);
            String str = channelNo + txnDate +version+ operate +orderId+ MD5Util.MD5Encode(userpws);
            setSign(txnDate,str);
            params.put("sign", sign);
            Map<String,Object> cardInfo = new HashMap<>();
            cardInfo.put("cardNo",cardNo);
            cardList.add(cardInfo);
            String rootNme = "cardStatusReq";
            String url = server_Ip + "/card/status";
            String paramsStr = getXmlStr(params,rootNme,cardList);
            System.out.println(paramsStr);
            String Rstr = HttpUtil.postXml(url, paramsStr);
            try {
                rMapStr = JSON.toJSONString(XmlUtil.xmlToMap(Rstr));
            } catch (Exception e) {
                System.err.println(" [Query_Mw ChangeState] 解析xml异常 "+e+" e "+e.getMessage()+"  ||||   "+Rstr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rMapStr;
    }


    /**
     * 生成xmlString
     * @param params
     * @param rootNme
     * @param cardList
     * @return
     */
    public  static String getXmlStr(Map<String, Object> params,String rootNme,List<Map<String,Object>> cardList){
        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xmlStr += "<"+rootNme+">";
        for(String key:params.keySet()){
            xmlStr += "<"+key+">"+params.get(key).toString()+"</"+key+">";
        }
        if(cardList!=null && cardList.size()>0){
            xmlStr += "<cardList>";
            for (int i = 0; i < cardList.size(); i++) {
                Map<String,Object> cardInfo = cardList.get(i);
                xmlStr += "<cardInfo>";
                for(String key:cardInfo.keySet()){
                    xmlStr += "<"+key+">"+cardInfo.get(key).toString()+"</"+key+">";
                }
                xmlStr += "</cardInfo>";
            }
            xmlStr += "</cardList>";
        }
        xmlStr += "</"+rootNme+">";
        return xmlStr;
    }





}
