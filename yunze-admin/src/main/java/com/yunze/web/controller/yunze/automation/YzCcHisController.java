package com.yunze.web.controller.yunze.automation;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzCcHisService;
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
 *  自动化配置
 *  2021-10-19
 */
@Api("自动化配置")
@RestController
@RequestMapping("/yunze/cchis")
public class YzCcHisController extends MyBaseController {

    @Autowired
    private IYzCcHisService IYzCcHisService;
    /**
     * 执行 自动化配置
     */
    @PreAuthorize("@ss.hasPermi('yunze:cchis:list')")
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
            return MyRetunSuccess(IYzCcHisService.ListCcHis(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cchis:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("自动化配置 操作失败！");
    }
}
