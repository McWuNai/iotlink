package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.Arith;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.IYzCardPacketService;
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
 *  平台资费计划 管理
 *  2021-06-24
 * @author root
 */
@Api("平台资费计划管理")
@RestController
@RequestMapping("/yunze/tariffPlan")
public class YzCardPacketController extends MyBaseController
{

    @Autowired
    private IYzCardPacketService cardPacketService;




    /**
     * 平台资费计划 新增
     */
    @Log(title = "平台资费计划", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:tariffPlan:add')")
    @PostMapping(value = "/add", produces = { "application/json;charset=UTF-8" })
    public String add(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Double error_so = Double.parseDouble(Parammap.get("error_so").toString());
            Double packet_flow = Double.parseDouble(Parammap.get("packet_flow").toString());
            Double error_flow = Double.parseDouble(Parammap.get("error_flow").toString());

            if(error_so>=1){
                if(packet_flow>=error_flow) {

                    //校验计算 用量
                    Parammap.put("error_flow",Arith.div(packet_flow,error_so,0));
                    return MyRetunSuccess(cardPacketService.add(Parammap),null);
                }else{
                    return Myerr("资费计划 流量 不能小于 资费计划 用量  ！");
                }
            }else{
                return Myerr("资费计划系数不能 小于 1 ！");
            }

        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffPlan:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台资费计划 新增 异常！");
    }



    /**
     * 平台资费计划 详情
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffPlan:find')")
    @PostMapping(value = "/find", produces = { "application/json;charset=UTF-8" })
    public String find(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            //校验计算 用量
            return MyRetunSuccess(cardPacketService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffPlan:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台资费计划 详情 异常！");
    }








    /**
     * 平台资费计划 编辑
     */
    @Log(title = "平台资费计划", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:tariffPlan:edit')")
    @PostMapping(value = "/edit", produces = { "application/json;charset=UTF-8" })
    public String edit(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Double error_so = Double.parseDouble(Parammap.get("error_so").toString());
            Double packet_flow = Double.parseDouble(Parammap.get("packet_flow").toString());
            Double error_flow = Double.parseDouble(Parammap.get("error_flow").toString());

            Double packet_price = Double.parseDouble(Parammap.get("packet_price").toString());
            Double packet_cost = Double.parseDouble(Parammap.get("packet_cost").toString());

            if(error_so>=1){
                if(packet_flow>=error_flow) {
                    //校验计算 用量
                    if(packet_price>=packet_cost){
                        //售价大等于 成本
                        Parammap.put("error_flow",Arith.div(packet_flow,error_so,0));
                        return MyRetunSuccess(cardPacketService.update(Parammap),null);
                    }else{
                        return Myerr("售价  不能小于 成本 ！");
                    }
                }else{
                    return Myerr("资费计划 流量 不能小于 资费计划 用量  ！");
                }
            }else{
                return Myerr("资费计划系数不能 小于 1 ！");
            }

        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffPlan:edit  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台资费计划 编辑 异常！");
    }



    /**
     * 平台 划分资费计划 查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffPlan:findPacket')")
    @PostMapping(value = "/findPacket", produces = { "application/json;charset=UTF-8" })
    public String findPacket(@RequestBody String Pstr)
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
            Parammap.put("agent_idStr",currentUser.getDeptId());
            return MyRetunSuccess(cardPacketService.findPacket(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffPlan:findPacket  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台 划分资费计划 查询  异常！");
    }




    /**
     * 续费查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:renewal:list')")
    @PostMapping(value = "/findToBR", produces = { "application/json;charset=UTF-8" })
    public String findToBR(@RequestBody String Pstr)
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
            Parammap.put("login_dept_id",currentUser.getDeptId());
            return MyRetunSuccess(cardPacketService.findToBR(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffPlan:findPacket  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台 划分资费计划 查询  异常！");
    }



}
