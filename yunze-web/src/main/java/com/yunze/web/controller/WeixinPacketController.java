package com.yunze.web.controller;

import com.alibaba.fastjson.JSON;
import com.yunze.business.annotation.LogAnnotation;

import com.yunze.web.service.impl.WeixinPacketImpl;
import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.cn.service.impl.YzCardServiceImpl;
import com.yunze.cn.service.impl.YzWeChatUserImpl;
import com.yunze.cn.util.AesEncryptUtil;
import com.yunze.cn.util.CheckIsNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.yunze.cn.util.ResponseJson;
import com.yunze.cn.util.Result;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/weixin/pac")
public class WeixinPacketController {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private YzWeChatUserImpl yzWeChatUserImpl;
    @Resource
    private YzCardServiceImpl yzCardServiceImpl;
    @Resource
    private WeixinPacketImpl weixinPacketImpl;


    /**
     * 描述： 公众号查询历史用量
     */
    @RequestMapping(value = "/qu-web-his-flow/{iccid}")
    @ResponseBody
    public Result getCardForWebHis(@PathVariable String iccid){
        if(CheckIsNull.isNull(iccid)){
            return ResponseJson.error("-2","获取信息失败");
        }
        Map<String,Object> Pmap = new HashMap<String,Object>();
        Pmap.put("iccid",iccid);
        Map<String,Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        iccid=IccidMap.get("iccid").toString();
        //查询当前iccid的日历史+月历史
        Map<String,Object> map=new HashMap<String,Object>();
        return ResponseJson.success(map);
    }

    /**
     * 描述： 查看当前流量包是否可以余额支付,微信支付
     */
    @RequestMapping(value = "/querPayType")
    @ResponseBody
    @LogAnnotation(action = "检测支付类型")
    public String querPayType(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid")) || CheckIsNull.isNull(ParamMap.get("packet_id"))){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = weixinPacketImpl.querPayType(ParamMap);
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



    /**
     * 描述： 流量查询
     */
    @RequestMapping(value = "/qu-flow")
    @ResponseBody
    @LogAnnotation(action = "操作查询流量详情")
    public String queryFlowInfo(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid"))){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = weixinPacketImpl.queryFlowInfo(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("queryFlowInfo - 异常 "+e.getMessage());
        }

        return ResponseJson.Myerr("获取用量数据异常！请联系客服！");
    }


    /**
     * auther:
     * return:
     * 描述： 根据iccid查询当前卡应该显示的加包
     * 时间： 2021/12/6 11:34
     */
    @ResponseBody
    @LogAnnotation(action = "查询可订购资费信息")
    @PostMapping(value = "/qu-packet", produces = { "application/json;charset=UTF-8" })
    public String getWxPackets(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("iccid"))){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = weixinPacketImpl.getWxPackets(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("qu-packet - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("获取用量数据异常！请联系客服！");
    }


    /**
     * 获取字典数据
     * @param map
     * @return
     */
    @ResponseBody
    @LogAnnotation(action = "获取字典数据")
    @PostMapping(value = "/getDictionary", produces = { "application/json;charset=UTF-8" })
    public String getDictionary(@RequestBody String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            if(CheckIsNull.isNull(ParamMap.get("openid")) || CheckIsNull.isNull(ParamMap.get("dict_type")) || CheckIsNull.isNull(ParamMap.get("iccid")) ){
                return ResponseJson.Myerr("参数不全操作取消！");
            }
            Map<String,Object> queryMap = weixinPacketImpl.getDictionary(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("getDictionary - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("获取用量数据异常！请联系客服！");
    }




    /**
     * 获取字典数据
     * @param map
     * @return
     */
    @ResponseBody
    @LogAnnotation(action = "获取订购资费计划")
    @PostMapping(value = "/getOrderFlow", produces = { "application/json;charset=UTF-8" })
    public String getOrderFlow(@RequestBody String map){
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
            Map<String,Object> queryMap = weixinPacketImpl.getOrderFlow(ParamMap);
            String code = queryMap.get("code").toString();
            if(code.equals("200")){
                return ResponseJson.MyRetunSuccess(queryMap,null);
            }else {
                return ResponseJson.Myerr(queryMap.get("Message").toString());
            }
        }catch (Exception e){
            System.out.println("getOrderFlow - 异常 "+e.getMessage());
        }
        return ResponseJson.Myerr("获取订购资费计划 异常！请联系客服！");
    }


}
