package com.yunze.web.controller;

import com.alibaba.fastjson.JSON;
import com.yunze.business.annotation.LogAnnotation;
import com.yunze.business.util.CheckIsNull;

import com.yunze.web.service.impl.WeixinPacketImpl;
import com.yunze.cn.util.AesEncryptUtil;
import com.yunze.cn.util.ResponseJson;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WeixinCardController {

    @Resource
    private WeixinPacketImpl weixinPacketImpl;






    /**
     * 描述： 获取当前iccid的详细信息
     * 时间： 2021/12/11 13:28
     */
    @RequestMapping(value = "/qu-balance/{iccid}")
    @ResponseBody
    @LogAnnotation(action = "操作查询卡号信息")
    public String getIccidInfo(@PathVariable String iccid){
      /*  if(CheckIsNull.isNull(iccid)){
            return ResponseJson.error("-2","获取该卡号余额失败");
        }
        CardIndex cardIndex=getIccid.getIccid(iccid);
        iccid=cardIndex.getIccid();
        String tableName = cardIndex.getCardType();
        Double balance=cardTypeService.queryZiBiaoByIccid(iccid,tableName).getBalance();
        return ResponseJson.success(balance);*/
        return null;
    }

    /**
     * return: 解绑iccid
     */
    @RequestMapping(value = "/un-bind")
    @ResponseBody
    @LogAnnotation(action = "操作解绑卡号")
    public String unbind(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid")) ) {
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = weixinPacketImpl.unbind(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("un-bind - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("解绑 异常！请联系客服！");
    }

    /**
     * 开始绑定卡号,必须 卡号只能绑定一个公众号，不能两个公众号存在同一张卡号
      * @param map
     * @return
     * @throws WxErrorException
     */
    @RequestMapping(value = "bind-card")
    @ResponseBody
    @LogAnnotation(action = "操作绑定卡号")
    public String bindCard(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid")) || CheckIsNull.isNull(ParamMap.get("appid"))){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = weixinPacketImpl.bindCard(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("bind-card - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("操作绑定卡号 异常！请联系客服！");
    }

    /**
     * 获取当前卡的信息,进入相应的界面
     */
    @RequestMapping(value = "/go-index")
    @ResponseBody
    @LogAnnotation(action = "操作进入主页")
    public String goWebIndex(@RequestBody String map){
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
            Map<String,Object> queryMap = weixinPacketImpl.goWebIndex(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("go-index - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("操作进入主页 异常！请联系客服！");
    }
    
    /**
     * return: 查询当前openid是否已经绑定了卡
     */
    @RequestMapping(value = "/ck-bind")
    @ResponseBody
    @LogAnnotation(action = "操作检测是否绑定")
    public String checkBind(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid"))){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = weixinPacketImpl.checkBind(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("ck-bind - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("操作检测是否绑定 异常！请联系客服！");

    }


}
