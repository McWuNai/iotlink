package com.yunze.iotapi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.mapper.mysql.YzCardRouteMapper;
import com.yunze.iotapi.utils.ResponseJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 回调推送
 */
@Controller
@RequestMapping("/call")
public class Sms {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    /**
     * 发送状态回 [电信CMP5G]
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/dianxin5g/status")
    @ResponseBody
    public JSONObject statusCallback(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("statusCallback--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        String num = null;
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            System.out.println(ParamMap);
            //修改发送状态
            if(ParamMap.get("msgId")!=null && ParamMap.get("msgStatus")!=null){



                Map<String,Object> updMap = new HashMap<>();
                String msgStatus = ParamMap.get("msgStatus").toString();
                String state_id = msgStatus.equals("1300")?"1":"0";
                updMap.put("msgid",ParamMap.get("msgId"));
                updMap.put("state_id",state_id);
                yzCardRouteMapper.updbusiCardSms(updMap);
            }

            return new ResponseJson().error("0","写入成功");
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "写入失败！");
        }
    }




    /**
     * 回传短信推送地址 [电信CMP5G]
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/dianxin5g/returnSMS")
    @ResponseBody
    public JSONObject returnSMSCallback(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("returnSMSCallback--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        String num = null;
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            System.out.println(ParamMap);
            //新增回复记录
            if(ParamMap.get("msgId")!=null && ParamMap.get("phone")!=null && ParamMap.get("returnMsgContent")!=null){
                Map<String,Object> addMap = new HashMap<>();
                addMap.put("msgid",ParamMap.get("msgId"));
                Integer is_ex = yzCardRouteMapper.isbusiCardSms(addMap);
                System.out.println(is_ex);
                if(is_ex!=null && is_ex>0){
                    System.out.println("已存在消息ID"+ParamMap.get("msgId"));
                }else {
                    addMap.put("msisdn",ParamMap.get("phone"));
                    addMap.put("content",ParamMap.get("returnMsgContent"));
                    addMap.put("state_id","1");
                    addMap.put("type","receive");
                    addMap.put("sms_src_TerminalId","");
                    yzCardRouteMapper.addbusiCardSms(addMap);
                }
            }
            return new ResponseJson().error("0","写入成功");
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "写入失败！");
        }
    }


    @RequestMapping(value = "/ecv5/returnSMS")
    @ResponseBody
    public JSONObject returnEcv5SMSCallback(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("returnEcv5SMSCallback--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        String num = null;
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            System.out.println(ParamMap);
            //新增回复记录
            if(ParamMap.get("status")!=null && ParamMap.get("message")!=null && ParamMap.get("info")!=null){
                Map<String,Object> addMap = new HashMap<>();

                Map<String,Object> info = (Map<String, Object>) ParamMap.get("info");
                addMap.put("msgid",info.get("sno"));
                Integer is_ex = yzCardRouteMapper.isbusiCardSms(addMap);
                System.out.println(is_ex);
                if(is_ex!=null && is_ex>0){
                    System.out.println("已存在消息ID"+addMap.get("msgid"));
                }else {
                    String status = ParamMap.get("status").equals("0")?"1":"2";
                    addMap.put("msisdn",info.get("msisdn"));
                    addMap.put("content",info.get("content"));
                    addMap.put("state_id",status);
                    addMap.put("type","receive");
                    addMap.put("sms_src_TerminalId",info.get("msgFmt"));
                    yzCardRouteMapper.addbusiCardSms(addMap);
                }
            }
            return new ResponseJson().error("0","写入成功");
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "写入失败！");
        }
    }


}
