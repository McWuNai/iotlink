package com.yunze.web.controller;

import com.alibaba.fastjson.JSON;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.yunze.business.annotation.LogAnnotation;
import com.yunze.web.service.impl.CommodityImpl;
import com.yunze.cn.util.*;


import com.yunze.web.config.WxPayServiceInterface;
import com.yunze.web.service.impl.WeixinPacketImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;


@Controller
@RequestMapping(value = "/weixin/pay")
public class WeixinPayController {

    @Resource
    private WxPayServiceInterface wxPayServiceInterface;
    @Resource
    private WeixinPacketImpl weixinPacketImpl;
    @Resource
    private CommodityImpl commodityImpl;







    /**
     * return: 微信统一下单
     */
    @RequestMapping(value = "/weixinPay", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @LogAnnotation(action = "操作微信下单")
    public String weixinPay(@RequestBody String map) {

        Map<String, Object> ParamMap = new HashMap<String, Object>();
        map = map.replace("%2F", "/");//转义 /
        try {
            map = AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String,Object> queryMap = weixinPacketImpl.weixinPay(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap.get("Data"),null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("weixinPay - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("微信支付 异常！请联系客服反馈！");
    }




    /**
     * return: 购买下单
     */
    @RequestMapping(value = "/ShoppingPay", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @LogAnnotation(action = "购买下单")
    public String ShoppingPay(@RequestBody String map) {

        Map<String, Object> ParamMap = new HashMap<String, Object>();
        map = map.replace("%2F", "/");//转义 /
        try {
            map = AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("product_id")) ||
                    CheckIsNull.isNull(ParamMap.get("count")) || CheckIsNull.isNull(ParamMap.get("appId"))) {
                return ResponseJson.Myerr("参数不全操作取消！");
            }

            Map<String,Object> queryMap = commodityImpl.ShoppingPay(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap.get("Data"),null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("ShoppingPay - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("购买下单 异常！请联系客服反馈！");
    }









    /**
     * 描述： 1元钱转为 100分
     */
    private int yuanToFee(BigDecimal bigDecimal) {
        return bigDecimal.multiply(new BigDecimal(100)).intValue();
    }

    /**
     * 描述： 微信回调url
     */
    @RequestMapping(value = "/weixinPayNotify/{appId}")
    @ResponseBody
    @LogAnnotation(action = "操作微信回调")
    public String weChatNotify(@RequestBody String xmlData, @PathVariable String appId) {
        try {
            Map<String,Object> queryMap = weixinPacketImpl.weChatNotify(xmlData,appId);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return WxPayNotifyResponse.success(queryMap.get("Message").toString());
            }else {
                return WxPayNotifyResponse.fail(queryMap.get("Message").toString());
            }
        } catch (Exception e) {
            System.out.println("微信回调异常：" + e.getMessage());
            return WxPayNotifyResponse.fail("回调有误!");
        }
    }


    private String createTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
