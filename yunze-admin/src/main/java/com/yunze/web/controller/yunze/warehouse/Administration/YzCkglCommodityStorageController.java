package com.yunze.web.controller.yunze.warehouse.Administration;
import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityStorageService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 新增入库信息表
*/

@Api("新增入库信息表")
@RestController
@RequestMapping("yunze/Warehousing")
public class YzCkglCommodityStorageController extends MyBaseController {

    @Autowired
    private YzCkglCommodityStorageService yzCkglCommodityStorageService;

    @PreAuthorize("@ss.hasPermi('yunze:Warehousing:information')")
    @PostMapping(value = "/information", produces = { "application/json;charset=UTF-8" })
    public String Information(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {

            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityStorageService.warehousingadd(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:Warehousing:information  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("新增入库信息表 操作失败！");
    }

    /**查询商品信息列表*/
    @PreAuthorize("@ss.hasPermi('yunze:Warehousing:liststorage')")
    @PostMapping(value = "/liststorage", produces = { "application/json;charset=UTF-8" })
    public String SynStorage (@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityStorageService.slestorage(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:Warehousing:liststorage  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询商品信息列表 操作失败！");
    }

    /**
     * 查询详情查看
     */
    @PreAuthorize("@ss.hasPermi('yunze:Warehousing:detailed')")
    @PostMapping(value = "/detailed", produces = { "application/json;charset=UTF-8" })
    public String Detailed(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {

            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityStorageService.detailedId(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:Warehousing:detailed  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("详情查看 异常！");
    }

    /**
     * 导入
     */
    @Log(title = "库存详情", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:Warehousing:importData')")
    @PostMapping(value = "/importData", produces = {"application/json;charset=utf-8"})
    public AjaxResult importData(MultipartFile file, @RequestParam Map<String,String> map) {
        String Pstr = map.get("Pstr").toString();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();

            return AjaxResult.success(yzCkglCommodityStorageService.ImportStock(file, map, User,Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:Warehousing:importData  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("导入库存详情 异常！");
    }


}




















