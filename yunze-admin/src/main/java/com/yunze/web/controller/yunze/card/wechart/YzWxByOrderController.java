package com.yunze.web.controller.yunze.card.wechart;


import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.impl.yunze.YzWxByOrderServiceImpl;
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
 *  发货订单
 *  2021-9-22
 */
@Api("发货订单")
@RestController
@RequestMapping("/yunze/WxByOrder")
public class YzWxByOrderController extends MyBaseController {

    @Resource
    private YzWxByOrderServiceImpl yzWxByOrderServiceImpl;

    /**
     * 查询发货订单
     * */
    @PreAuthorize("@ss.hasPermi('yunze:order:findShip')")
    @PostMapping(value = "/findWxOrder", produces = { "application/json;charset=UTF-8" })
    public String findWxOrder(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByOrderServiceImpl.findWxOrder(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/WxByOrder/findWxOrder  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询发货订单 操作失败！");
    }

        /**
         * 购物订单导出
         * */
        @Log(title = "购物订单导出", businessType = BusinessType.EXPORT)
        @PreAuthorize("@ss.hasPermi('yunze:WxByOrder:shoppingexport')")
        @PostMapping(value = "/shoppingexport", produces = { "application/json;charset=utf-8" })
        public String exportData(@RequestBody String Pstr)
        {
            HashMap<String, Object> Parammap = new HashMap<String, Object>();
            if(Pstr!=null){
                Pstr = Pstr.replace("%2F", "/");//转义 /
            }
            try {

                Pstr =  AesEncryptUtil.desEncrypt(Pstr);
                //  System.out.println(map);
                Parammap.putAll(JSON.parseObject((String) Pstr));

                LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                SysUser currentUser = loginUser.getUser();

                return MyRetunSuccess(yzWxByOrderServiceImpl.shopping_export(Parammap,currentUser),null);
            }catch (Exception e){
                String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
                logger.error("<br/> yunze:WxByOrder:shoppingexport  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
            }
            return Myerr("购物订单导出 操作失败！");
        }







}
















































































