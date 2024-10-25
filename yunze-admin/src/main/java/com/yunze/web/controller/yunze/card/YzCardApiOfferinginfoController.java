package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzCardApiOfferinginfoService;
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
 * 上游套餐分类
 * 表名：yz_card_api_offeringinfo
 * 2022-4-25
 */
@Api("上游资费组管理")
@RestController
@RequestMapping("/yunze/info")
public class YzCardApiOfferinginfoController extends MyBaseController {

    @Autowired
    private IYzCardApiOfferinginfoService IYzCardApiOfferinginfoService;

    /**
     * 执行  上游资费组管理
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
            return MyRetunSuccess(IYzCardApiOfferinginfoService.getList(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:offeringinfo:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr(" 上游资费组管理 操作失败！");
    }

    /**
     * 删除ID
     */
    @PreAuthorize("@ss.hasPermi('yunze:offeringinfo:delete')")
    @PostMapping(value = "/delete", produces = {"application/json;charset=UTF-8"})
    public String Delete(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = IYzCardApiOfferinginfoService.deleteInfo(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("删除失败！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:offeringinfo:delete  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr(" 删除ID 操作失败！");
    }


    /**
     * 修改
     */
    @PreAuthorize("@ss.hasPermi('yunze:offeringinfo:update')")
    @PostMapping(value = "/update", produces = {"application/json;charset=UTF-8"})
    public String Update (@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = IYzCardApiOfferinginfoService.updateInfo(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("修改失败！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:offeringinfo:update  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr(" 修改 操作失败！");
    }


}
