package com.yunze.web.controller.yunze.sysgl;
import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzLogsService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 *  菜单执行日志
 *  2021-8-19
 */
@Api("菜单执行日志")
@RestController
@RequestMapping("/yunze/sysLog")
public class YzSysLogsController extends MyBaseController {

    @Autowired
    private IYzLogsService iYzLogsService;


   /**
    * 执行日志
    * */
    @PreAuthorize("@ss.hasPermi('yunze:sysLog:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String SynFlow(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(iYzLogsService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:sysLog:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取卡板信息列表 操作失败！");
    }

    /**
     * 根据id查询
     * */
    @PreAuthorize("@ss.hasPermi('yunze:sysLog:findSys')")
    @PostMapping(value = "/findSys", produces = { "application/json;charset=UTF-8" })
    public String findSys(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzLogsService.getById(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:sysLog:findSys  <br/>   ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询 【订单详情】  操作失败！");
    }
}



































































































