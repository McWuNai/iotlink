package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzCardUsageReminderService;
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
 * 上游返回套餐记录信息
 * 2022-4-11
 */
@Api("上游返回套餐记录信息")
@RestController
@RequestMapping("/yunze/usage")
public class YzCardUsageReminderController extends MyBaseController {

    @Autowired
    private IYzCardUsageReminderService IYzCardUsageReminderService;

    /**
     * 上游返回套餐记录信息 列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardSetmeal:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(IYzCardUsageReminderService.list(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardSetmeal:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("上游返回套餐记录信息 操作失败！");
    }

}
