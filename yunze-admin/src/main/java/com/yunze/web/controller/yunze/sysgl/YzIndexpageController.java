package com.yunze.web.controller.yunze.sysgl;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.controller.BaseController;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.sysgl.IYzIndexpageService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 首页数据加载
 */
@Api("首页数据加载")
@RestController
@RequestMapping("/yunze/index")
public class YzIndexpageController extends MyBaseController {

    @Resource
    private IYzIndexpageService iYzIndexpageService;

    /**
     * 首页数据加载
     */
    @PreAuthorize("@ss.hasPermi('yunze:index:findToDay')")
    @PostMapping(value = "/findToDay", produces = {"application/json;charset=UTF-8"})
    public String findToDay(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("dept_id",User.getDeptId());
            Parammap.put("user_id",User.getUserId());
            return MyRetunSuccess(iYzIndexpageService.findToDay(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> index:findToDay  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("首页数据加载 操作失败！");
    }


    /**
     * 资产明细统计图加载
     */
    @PreAuthorize("@ss.hasPermi('yunze:index:assetDetails')")
    @PostMapping(value = "/assetDetails", produces = {"application/json;charset=UTF-8"})
    public String AssetDetails(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("dept_id",User.getDeptId());
            Parammap.put("user_id",User.getUserId());
            return MyRetunSuccess(iYzIndexpageService.assetDetails(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> index:assetDetails  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("资产明细统计图加载 操作失败！");
    }


}













