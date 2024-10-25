package com.yunze.web.controller;

import com.yunze.business.annotation.LogAnnotation;
import com.yunze.business.config.WxConfigInit;
import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.cn.util.CheckIsNull;
import com.yunze.cn.util.ResponseJson;
import com.yunze.cn.util.Result;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxOAuth2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WeixinAuthController {

    private static Logger logger= LoggerFactory.getLogger(WeixinAuthController.class);

    @Resource
    private WxMpService wxMpService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private WxConfigInit wxConfigInit;

    /**
     * auther:
     * return: 网页授权
     * 描述： 
     */
    @GetMapping(value = "/authorize/{appId}", produces = { "application/json;charset=UTF-8" })
    @ResponseBody
    @LogAnnotation(action = "操作网页授权")
    public Result authorize(@RequestParam String returnurl, @PathVariable String appId) throws UnsupportedEncodingException, WxErrorException {
        if(CheckIsNull.isNull(returnurl)){
            return ResponseJson.error("-2","网页授权失败");
        }
        //String urlstr=URLDecoder.decode(returnurl,"utf-8");
        //String appId=urlstr.substring(urlstr.lastIndexOf("/")+1,urlstr.length());
        System.out.println(appId);
        WxMpService wx=wxConfigInit.wxMpService().switchoverTo(appId);
        WxOAuth2Service w2=wx.getOAuth2Service();
        String url=w2.buildAuthorizationUrl(returnurl, WxConsts.OAuth2Scope.SNSAPI_USERINFO, null);
        url=URLDecoder.decode(url,"utf-8");
        logger.info("微信网页授权:获取code,redirectURL={}",returnurl);
        Map<String,Object> map=new HashMap<>();
        map.put("returnurl",url);
        map.put("appId",appId);
        return ResponseJson.success(map);
    }

    @RequestMapping(value = "/gt-info/{appId}")
    @ResponseBody
    @LogAnnotation(action = "操作获取用户信息")
    public Result getUserInfo(@RequestParam String code,@PathVariable String appId) throws WxErrorException {
        if(CheckIsNull.isNull(code) || CheckIsNull.isNull(appId)){
            return ResponseJson.error("-2","获取用户信息失败");
        }
        WxMpService wx=wxConfigInit.wxMpService().switchoverTo(appId);
        WxOAuth2AccessToken wToken=wx.getOAuth2Service().getAccessToken(code);
        if(CheckIsNull.isNull(redisUtil.get("accessToken"))){
            wToken=wx.getOAuth2Service().refreshAccessToken(wToken.getRefreshToken());
            redisUtil.setAccessToken(wToken.getAccessToken());
        }
        WxOAuth2UserInfo wxMpUser=wx.getOAuth2Service().getUserInfo(wToken,null);
        logger.info("获取当前用户信息：{}",wxMpUser);
        //保存到redis用户信息
        redisUtil.set(wxMpUser.getOpenid(),wxMpUser);
        return ResponseJson.success(wxMpUser.getOpenid());
    }
}
