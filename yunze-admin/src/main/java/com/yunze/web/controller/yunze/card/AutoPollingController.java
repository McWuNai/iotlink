package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.SysAutoPolling;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.poi.ExcelUtil;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.card.IAutoPollingService;
import com.yunze.web.core.config.MyBaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/yunze/autoPolling")
@Slf4j
public class AutoPollingController extends MyBaseController {
    @Resource
    private IAutoPollingService autoPollingService;


    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    @PreAuthorize("@ss.hasPermi('yunze:card:auto')")
    public String getList(@RequestBody String Pstr){

        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject( Pstr));
            return MyRetunSuccess(autoPollingService.list(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /yunze/applyUseCard/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取 自动轮询 列表 操作失败!");
    }


    @PostMapping("/del")
    public String del(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.put("iccid",Pstr);
            autoPollingService.del(Parammap);
            return MyRetunSuccess("","删除成功");
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /yunze/applyUseCard/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("删除失败!");
    }

    @GetMapping("/importTemplate")
    public AjaxResult importTemplate()
    {
        ExcelUtil<SysAutoPolling> util = new ExcelUtil<SysAutoPolling>(SysAutoPolling.class);
        return util.importTemplateExcel("轮询数据");
    }

    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<SysAutoPolling> util = new ExcelUtil<SysAutoPolling>(SysAutoPolling.class);
        List<SysAutoPolling> List = util.importExcel(file.getInputStream());
        String message = autoPollingService.importAuto(List);
        return AjaxResult.success(message);
    }
}
