package com.yunze.web.controller.yunze.card;

import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.system.service.impl.yunze.YzCardServiceImpl;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("new/traffic")
public class YzTrafficController extends MyBaseController {
    @Autowired
    private YzCardServiceImpl yzCardService;

    @Log(title = "文件上传批量订购", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:import')")
    @PostMapping(value = "/trafficImport", produces = {"application/json;charset=utf-8"})
    public AjaxResult trafficImport(MultipartFile file,@RequestParam Map<String, Object> map) {
        try {
            return yzCardService.trafficImport(file,map);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:import  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("操作失败！");
    }
    @Log(title = "文本订购资费", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:import')")
    @PostMapping(value = "/trafficImportText", produces = {"application/json;charset=utf-8"})
    public AjaxResult trafficImportText(@RequestBody Map<String, Object> map) {
        try {
            return yzCardService.trafficImportText(map);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:import  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("操作失败！");
    }
}
