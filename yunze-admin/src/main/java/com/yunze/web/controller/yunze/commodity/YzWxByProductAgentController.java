package com.yunze.web.controller.yunze.commodity;


import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.impl.yunze.commodity.YzWxByProductAgentServiceImpl;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 商品代理
 */
@Api("商品代理")
@RestController
@RequestMapping("/yunze/productAgent")
public class YzWxByProductAgentController extends MyBaseController {

    @Resource
    private YzWxByProductAgentServiceImpl yzWxByProductAgentServiceImpl;

    /**
     * 商品代理 分类
     * */
    @PreAuthorize("@ss.hasPermi('yunze:productAgent:edit')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String list(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByProductAgentServiceImpl.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productAgent/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("商品代理  异常！");
    }





    /**
     * 商品代理 编辑
     * */
    @PreAuthorize("@ss.hasPermi('yunze:productAgent:edit')")
    @PostMapping(value = "/edit", produces = { "application/json;charset=UTF-8" })
    public String edit(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzWxByProductAgentServiceImpl.edit(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("保存数据 异常！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productAgent/edit  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("商品代理 编辑 异常！");
    }














}
















































































