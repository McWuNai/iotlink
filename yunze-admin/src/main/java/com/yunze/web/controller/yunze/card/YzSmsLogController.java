package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzSmsLogService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * 短信日志
 * 2022年9月2日10:57:25
 */
@Api("短信日志")
@RestController
@RequestMapping("/yunze/smslog")
@Slf4j
public class YzSmsLogController extends MyBaseController {

    @Autowired
    private IYzSmsLogService iYzSmsLogService;

    /**
     * 获取展示在首页的数据
     * 2022年9月2日10:59:49
     */
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    @PreAuthorize("@ss.hasPermi('yunze:sysLog:list')")
    public String getList(@RequestBody String Pstr){

        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            System.out.println(Parammap);
            System.out.println(">>>>>endController");
            return MyRetunSuccess(iYzSmsLogService.getList(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }

        return Myerr("获取短信日志列表 操作失败!");
    }


}
