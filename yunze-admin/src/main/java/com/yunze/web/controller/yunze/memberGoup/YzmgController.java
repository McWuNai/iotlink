package com.yunze.web.controller.yunze.memberGoup;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.memberGroup.YzMGService;
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
 * 成员组
 * 2022年9月23日15:22:12
 */
@Slf4j
@Api("成员组")
@RestController
@RequestMapping("/yunze/memberGroup")
public class YzmgController extends MyBaseController {

    @Autowired
    private YzMGService yzMGService;

    /**
     * 成员组查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:memberGroup:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess( yzMGService.getList(Parammap), null);
        }catch (Exception e){
            e.printStackTrace();
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:memberGroup:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }

       return Myerr("成员组相关信息查询失败!!");
    }

    @PreAuthorize("@ss.hasPermi('yunze:memberGroup:exportData')")
    @PostMapping(value = "/exportData", produces = { "application/json;charset=utf-8" })
    public String exportData(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
          return MyRetunSuccess(yzMGService.exportData(Parammap,currentUser),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
           // logger.error("<br/> yunze:mg:exportData  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
            e.printStackTrace();
        }
        return Myerr("导出成员组列表 操作失败！");
    }


}













