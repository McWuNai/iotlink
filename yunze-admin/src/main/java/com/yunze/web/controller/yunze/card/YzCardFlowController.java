package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.controller.BaseController;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.IYzCardFlowService;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 资费信息
 *  2021-06-09
 * @author root
 */
@RestController
@RequestMapping("/yunze/cardFlow")
public class YzCardFlowController extends MyBaseController
{

    @Autowired
    private IYzCardFlowService cardFlowService;



    /**
     * 查询套餐信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:flow:list')")
    @PostMapping(value = "/queryflow", produces = { "application/json;charset=utf-8" })
    public String queryflow(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(cardFlowService.queryflow(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            System.err.println(e);
        }
        return Myerr("查询套餐信息 操作失败！");
    }



    /**
     * 查询当前套餐简要信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardFlow:queryflowSimple')")
    @PostMapping(value = "/queryflowSimple", produces = { "application/json;charset=utf-8" })
    public String queryflowSimple(@RequestBody String Pstr)
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
            Parammap.put("agent_id",currentUser.getDeptId());
            return MyRetunSuccess(cardFlowService.queryflow_simple(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardFlow:queryflowSimple  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询当前套餐简要信息 操作失败！");
    }



    /**
    * 查询 简要 资费计划 名称
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardFlow:queryPackageSimple')")
    @PostMapping(value = "/queryPackageSimple", produces = { "application/json;charset=utf-8" })
    public String queryPackageSimple()
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            Parammap.put("agent_id",currentUser.getDeptId());
            return MyRetunSuccess(cardFlowService.queryPackage_simple(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardFlow:queryPackageSimple  " + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询套餐信息 操作失败！");
    }


    /**
     * 查询单条 资费计划  简要
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:cardFlow:FindPacket')")
    @PostMapping(value = "/FindPacket", produces = { "application/json;charset=utf-8" })
    public String FindPacket(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            Parammap.put("agent_id",currentUser.getDeptId());
            return MyRetunSuccess(cardFlowService.FindPacket(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:cardFlow:FindPacket  " + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询单条 资费计划  简要 操作失败！");
    }

    /**
     * 订购资费查询
     * */
    @PreAuthorize("@ss.hasPermi('yunze:flow:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String SynFlow(@RequestBody String Pstr){
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
            List<Integer> agent_id = new ArrayList<>();
            if(currentUser.getDeptId()!=100){
                if(Parammap.get("agent_id")!=null){
                    List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                }else{
                    agent_id.add(Integer.parseInt(currentUser.getDeptId().toString()));
                    Parammap.put("agent_id",agent_id);
                }
            }


            return MyRetunSuccess(cardFlowService.getList(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:flow:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("订购资费查询 操作失败！");
    }

    /**
     * 订购资费导出
     * */
    @Log(title = "订购资费导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:flow:exportflow')")
    @PostMapping(value = "/exportflow", produces = { "application/json;charset=utf-8" })
    public String exportFlow(@RequestBody String Pstr)
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

            return MyRetunSuccess(cardFlowService.exportflow(Parammap,currentUser),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:flow:exportflow  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("订购资费导出 操作失败！");
    }










}
