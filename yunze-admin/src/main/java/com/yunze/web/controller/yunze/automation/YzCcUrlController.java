package com.yunze.web.controller.yunze.automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzCcUrlService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 *  自动化
 *  2021-10-19
 */
@Api("自动化推送")
@RestController
@RequestMapping("/yunze/ccurl")
public class YzCcUrlController extends MyBaseController {


    @Autowired
    private IYzCcUrlService IYzCcUrlService;


    /**
     * 执行配置
     */
    @PreAuthorize("@ss.hasPermi('yunze:ccurl:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(IYzCcUrlService.ListCcUrl(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:ccurl:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("自动化配置 操作失败！");
    }

    /**
     * 新增
     */
    @Log(title = "推送", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:ccurl:add')")
    @PostMapping(value = "/add", produces = {"application/json;charset=UTF-8"})
    public String add(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(IYzCcUrlService.insertCcUrl(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:ccurl:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.toString());
        }
        return Myerr(" 操作失败！");
    }

    /**
     * 修改配置
     */

    @PreAuthorize("@ss.hasPermi('yunze:ccurl:update')")
    @PostMapping(value = "/update", produces = {"application/json;charset=UTF-8"})
    public String Update(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(IYzCcUrlService.updateCcUrl(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:ccurl:update  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("配置修改 操作失败！");
    }

    /**
     * 删除 只留最后一位
     */

    @PreAuthorize("@ss.hasPermi('yunze:ccurl:deleteurl')")
    @PostMapping(value = "/deleteurl", produces = {"application/json;charset=UTF-8"})
    public String Delete(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            Map<String,Object> Rmap = IYzCcUrlService.DeleteCcUrl(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Msg =  Rmap.get("Msg").toString();
            if(bool){
                return MyRetunSuccess(Msg,null);
            }else{
                return Myerr(Msg);
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:ccurl:deleteurl  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("删除配置条数 操作失败！");


    }



}








































