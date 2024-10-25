package com.yunze.web.controller.yunze.card.channel;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.IYzCardRouteService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 *  通道 管理
 *  2021-06-19
 * @author root
 */
@Api("上游通道管理")
@Slf4j
@RestController
@RequestMapping("/yunze/cardRoute")
public class YzCardRouteController extends MyBaseController
{

    @Resource
    private IYzCardRouteService iYzCardRouteService;



    /**
     * 查询简要 通道信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:findSp')")
    @PostMapping(value = "/findSp", produces = { "application/json;charset=utf-8" })
    public String findSp()
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            return MyRetunSuccess(iYzCardRouteService.find_sp(),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:findSp  " + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询简要 通道信息 操作失败！");
    }


    /**
     * 查询 通道简要信息  状态为 正常 划分通道用
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:findCr')")
    @PostMapping(value = "/findCr", produces = { "application/json;charset=utf-8" })
    public String findCr()
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            return MyRetunSuccess(iYzCardRouteService.find_cr(),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:findSp  " + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询 通道简要信息  状态为 正常 划分通道用  操作失败！");
    }


    /**
     * API通道信息列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String list(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzCardRouteService.sel_Map(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取API通道信息列表 操作失败！");
    }



    /**
     * 查询API通道详情
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:find')")
    @PostMapping(value = "/find", produces = { "application/json;charset=utf-8" })//
    public String find(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzCardRouteService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询API通道详情 操作失败！");
    }



    /**
     * 新增 API通道信息
     */
    @Log(title = "上游通道", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:add')")
    @PostMapping(value = "/add", produces = { "application/json;charset=utf-8" })
    public String add(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("cd_agent_id",User.getDeptId());
            boolean bool = iYzCardRouteService.add(Parammap);
        //    log.info("add方法参数>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+Parammap);

            String msg = bool?"新增 上游通道成功！":"网络操作失败请稍后重试！！！";
            return MyRetunSuccess(bool,msg);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("新增 API通道信息 操作失败！");
    }


    /**
     * 编辑 API通道信息
     */
    @Log(title = "上游通道", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:edit')")
    @PostMapping(value = "/edit", produces = { "application/json;charset=utf-8" })
    public String edit(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("cd_agent_id",User.getDeptId());
            boolean bool = iYzCardRouteService.update(Parammap);
            String msg = bool?"编辑 上游通道成功！":"网络操作失败请稍后重试！！！";
            return MyRetunSuccess(bool,msg);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:edit  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("编辑 API通道信息 操作失败！");
    }


    /**
     * 删除 API通道信息 （改为 删除 状态）
     */
    @Log(title = "上游通道", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:delRoute')")
    @PostMapping(value = "/delRoute", produces = { "application/json;charset=utf-8" })
    public String delRoute(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("cd_agent_id",User.getDeptId());
            boolean bool = iYzCardRouteService.update_cd_status(Parammap);
            String msg = bool?"删除 上游通道成功！":"网络操作失败请稍后重试！！！";
            return MyRetunSuccess(bool,msg);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:delRoute  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("删除 API通道信息 操作失败！");
    }



    /**
     * 导出
     * @return
     */
    @Log(title = "上游通道", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:exportData')")
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
            return MyRetunSuccess(iYzCardRouteService.exportData(Parammap,currentUser),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:exportData  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("导出上游通道 操作失败！");
    }



    /**
     * 查询自费组树结构,进行赋值
     * */
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:packageRoute')")
    @PostMapping(value = "/packageRoute", produces = { "application/json;charset=utf-8" })//
    public String ThroughRoute(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzCardRouteService.RouteList(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:packageRoute  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("树结构默认赋值 操作失败！");
    }


    /*
    *  选择运营商时加载出对应的通道
    * */
    @PreAuthorize("@ss.hasPermi('yunze:cardRoute:operator')")
    @PostMapping(value = "/operator", produces = { "application/json;charset=UTF-8" })
    public String operatorName(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(iYzCardRouteService.operatorName(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardRoute:operator  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("选择运营商时加载出对应的通道 操作失败！");
    }



}
























