package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzCardApiOfferinginfolistService;
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
 * 上游接口返回已订购资费信息
 * 表名：yz_card_api_offeringinfolist
 * 2022-4-25
 */
@Api("上游已订购套餐")
@RestController
@RequestMapping("/yunze/folist")
public class YzCardApiOfferinginfolistController extends MyBaseController {

    @Autowired
    private IYzCardApiOfferinginfolistService IYzCardApiOfferinginfolistService;

    /**
     * 执行 上游已订购套餐
     */
    @PreAuthorize("@ss.hasPermi('yunze:offeringinfolist:list')")
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
            return MyRetunSuccess(IYzCardApiOfferinginfolistService.Listfolist(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:offeringinfolist:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("上游已订购套餐 操作失败！");
    }






}
