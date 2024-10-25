package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.controller.BaseController;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.YzHandAddPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/hand")
public class YzHandAddPackageController extends BaseController {
    //创建生效类型分支
    @Autowired
    private YzHandAddPackageService yzHandAddPackageService;

    @PostMapping("/addPackage")
    public AjaxResult handAddPackage(@RequestBody String Pstr) {
        Map<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = com.yunze.common.utils.yunze.AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return AjaxResult.success(AesEncryptUtil.encrypt(JSON.toJSONString(yzHandAddPackageService.handAddPackage(Parammap))));
        } catch (Exception e) {
            return AjaxResult.error("加包失败 ！");
        }
    }

    @Log(title = "文件上传批量订购", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:import')")
    @PostMapping(value = "/addPackageExcel", produces = {"application/json;charset=utf-8"})
    public AjaxResult addPackage(MultipartFile file) {
        try {
            return yzHandAddPackageService.addPackage(file);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:import  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("操作失败！");
    }
}