package com.yunze.web.controller.ydhouse;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.ModuleHouse;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.poi.ExcelUtil;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.house.IModuleHouseService;
import com.yunze.web.core.config.MyBaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/house/module")
@Slf4j
public class ModuleYDController extends MyBaseController {
    @Resource
    private IModuleHouseService moduleHouseService;

    @Autowired
    private TokenService tokenService;

    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    @PreAuthorize("@ss.hasPermi('house:module:list')")
    public String getList(@RequestBody String Pstr){

        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject( Pstr));
            return MyRetunSuccess(moduleHouseService.list(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /house/module/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("批量导入模组信息 操作失败!");
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
            moduleHouseService.del(Parammap);
            return MyRetunSuccess("","删除成功");
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /house/module/del  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("删除失败!");
    }

    @GetMapping("/importTemplate")
    public AjaxResult importTemplate()
    {
        ExcelUtil<ModuleHouse> util = new ExcelUtil<ModuleHouse>(ModuleHouse.class);
        return util.importTemplateExcel("-移动模组导入");
    }

    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
        SysUser User = loginUser.getUser();
        Parammap.put("User", User);
        Parammap.put("agent_id", User.getDeptId());
        Parammap.put("user_id", User.getUserId());
        ExcelUtil<ModuleHouse> util = new ExcelUtil<>(ModuleHouse.class);
        //List<ModuleHouse> List = util.importExcel(file.getInputStream());;
        try {

            return AjaxResult.success(moduleHouseService.importModule(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:importData  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("批量导入模组信息 操作失败！");
    }
}
