package com.yunze.web.controller;

import com.alibaba.fastjson.JSON;
import com.yunze.business.annotation.LogAnnotation;
import com.yunze.cn.util.CheckIsNull;
import com.yunze.web.service.impl.OrderImpl;
import com.yunze.cn.util.AesEncryptUtil;
import com.yunze.cn.util.ResponseJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping(value = "/weixin/ord")
public class WeixinOrderController {

    @Resource
    private OrderImpl orderImpl;





    /**
     * 描述： 查询当前卡号的订单
     */
    @RequestMapping(value = "/qu-ord")
    @ResponseBody
    @LogAnnotation(action = "操作查询卡号订单")
    public String queryOrders(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid")) ){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = orderImpl.queryOrders(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("queryFlowInfo - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("检测支付类型 异常！请联系客服！");
    }





    @RequestMapping(value = "/queryPackage")
    @ResponseBody
    @LogAnnotation(action = "微信订单获取资费名称")
    public String queryPackage(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject(map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid"))){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = orderImpl.queryPackage(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("queryPackage - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("微信订单获取资费名称 异常！请联系客服！");
    }



    @RequestMapping(value = "/WxfindOrderDs")
    @ResponseBody
    @LogAnnotation(action = "微信订单详情")
    public String WxfindOrderDs(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject(map));
            if(CheckIsNull.isNull(ParamMap.get("id")) || CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid"))){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = orderImpl.WxfindOrderDs(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("WxfindOrderDs - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("微信订单详情 异常！请联系客服！");
    }



}
