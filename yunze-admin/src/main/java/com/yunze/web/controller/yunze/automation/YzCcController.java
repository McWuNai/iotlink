package com.yunze.web.controller.yunze.automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzCcService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
/**
 *  自动化
 *  2021-10-19
 */
@Api("自动化")
@RestController
@RequestMapping("/yunze/cc")

public class YzCcController extends MyBaseController {

    @Autowired
    private IYzCcService IYzCcService;


    /**
     * 执行自动化
     */
    @PreAuthorize("@ss.hasPermi('yunze:cc:list')")
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
            return MyRetunSuccess(IYzCcService.ListCc(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cc:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("自动化 操作失败！");
    }

    /**
     *  自动化新增
     */
    @Log(title = "自动化新增", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:cc:add')")
    @PostMapping(value = "/add", produces = { "application/json;charset=UTF-8" })
    public String add(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(IYzCcService.YzCcAdd(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cc:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.toString());
        }
        return Myerr(" 操作失败！");
    }

    /**
     * 查询 对应自动化规则表  名字 对应 自动化管理抄送名字
     */
    @PreAuthorize("@ss.hasPermi('yunze:cc:nameadd')")
    @PostMapping(value = "/nameadd", produces = { "application/json;charset=utf-8" })
    public String findCr()
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            return MyRetunSuccess(IYzCcService.NameAdd(),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cc:nameadd  " + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询 对应自动化规则表  名字 正常 自动化管理抄送名字  操作失败！");
    }

    /**
     * 修改自动化
     */

    @PreAuthorize("@ss.hasPermi('yunze:cc:ccupdate')")
    @PostMapping(value = "/ccupdate", produces = {"application/json;charset=UTF-8"})
    public String Update(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(IYzCcService.UpdateCc(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cc:update  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("配置自动化 操作失败！");
    }


}



































