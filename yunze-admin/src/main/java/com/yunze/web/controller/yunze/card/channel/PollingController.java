package com.yunze.web.controller.yunze.card.channel;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYPollingService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * 通道进度
 * 2021-8-26
 */
@Api("通道进度")
@RestController
@RequestMapping("/yunze/polling")
public class PollingController extends MyBaseController {

    @Autowired
    private IYPollingService iyPollingService;

    /**
     * 执行通道查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:polling:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(iyPollingService.getList(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:polling:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("通道进度 操作失败！");
    }

    /**
     * 手動輪詢
     * @param
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:find')")
    @PostMapping(value = "/plEx", produces = {"application/json;charset=utf-8"})
    public String plEx(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = iyPollingService.plEx(Parammap);
            if(bool){
                return MyRetunSuccess(null,"操作成功！");
            }else {
                return Myerr("操作失败……");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> updPwd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("修改支付密码 操作失败！");
    }


}













