package com.yunze.web.controller;

import com.alibaba.fastjson.JSON;
import com.yunze.business.annotation.LogAnnotation;
import com.yunze.business.token.TokenProcess;
import com.yunze.web.service.impl.CommodityImpl;
import com.yunze.cn.util.AesEncryptUtil;
import com.yunze.cn.util.CheckIsNull;
import com.yunze.cn.util.RedisUtil_Yz;
import com.yunze.cn.util.ResponseJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/weixin/shopping")
public class WeixinShoppingController {

    @Resource
    private CommodityImpl commodityImpl;
    @Resource
    private TokenProcess tokenProcess;
    @Resource
    private RedisUtil_Yz redisUtilYz;

    /**
     * 获取商品分类
     */
    @RequestMapping(value = "/cy-category")
    @ResponseBody
    @LogAnnotation(action = "获取商品分类")
    public String Category(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) ) {
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = commodityImpl.QuerCategory(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("cy-category - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("获取商品分类 异常！请联系客服！");
    }


    /**
     * 获取商品图片
     */
    @RequestMapping(value = "/cy-img")
    @ResponseBody
    @LogAnnotation(action = "获取商品图片")
    public String Img(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("product_id"))) {
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = commodityImpl.QuerImg(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("cy-img - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("获取商品图片 异常！请联系客服！");
    }




    /**
     * 下载图片
     */
    @RequestMapping(value = "/cy-readImage")
    @ResponseBody
    @LogAnnotation(action = "下载图片")
    public void readImage(HttpServletRequest request, HttpServletResponse response){
        String map = request.getParameter("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
            map = map.replace(" ", "+");//转义 ' '
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("imagePath"))) {
                System.out.println("参数不全操作取消！");
            }
            String fileName = ParamMap.get("imagePath").toString();
            File cat = new File("");
            String filePath = cat.getCanonicalPath();
            String imagePath =  filePath + fileName;
            if(imagePath != null){
                File file = new File(imagePath);
                if (file.exists()) {
                    DataOutputStream temps = new DataOutputStream(response
                            .getOutputStream());
                    DataInputStream in = new DataInputStream(
                            new FileInputStream(imagePath));
                    byte[] b = new byte[2048];
                    while ((in.read(b)) != -1) {
                        temps.write(b);
                    }
                    temps.flush();
                    in.close();
                    temps.close();
                }
            }
        }catch (Exception e){
            System.out.println("cy-readImage - 异常 "+e.getMessage());
        }
    }




    /**
     * 购买支付核对
     */
    @RequestMapping(value = "/PayCheck")
    @ResponseBody
    @LogAnnotation(action = "购买支付核对")
    public String PayCheck(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("product_id")) ||
                    CheckIsNull.isNull(ParamMap.get("count")) || CheckIsNull.isNull(ParamMap.get("appId"))
                    || CheckIsNull.isNull(ParamMap.get("userName")) || CheckIsNull.isNull(ParamMap.get("userPhone"))
                    || CheckIsNull.isNull(ParamMap.get("city")) || CheckIsNull.isNull(ParamMap.get("province"))
                    || CheckIsNull.isNull(ParamMap.get("district")) || CheckIsNull.isNull(ParamMap.get("price"))
            ) {
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = commodityImpl.PayCheck(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("PayCheck - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("购买支付核对 异常！请联系客服！");
    }



    /**
     * 生成webToken
     */
    @RequestMapping(value = "/setToken")
    @ResponseBody
    @LogAnnotation(action = "生成webToken")
    public String setToken(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("appId"))
            ) {
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = new HashMap<>();
            String openid =  ParamMap.get("openid").toString();
            //微信公众号缓存token
            String webToken =openid+"-_yunzeweb_-"+tokenProcess.makeToken();
            redisUtilYz.setKey(openid+"-yzAuth-web-token",webToken);
            queryMap.put("webToken",webToken);
            return ResponseJson.MyRetunSuccess(queryMap,null);
        }catch (Exception e){
            System.out.println("setToken - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("生成webToken 异常！请联系客服！");
    }







}
