package com.yunze.web.controller.yunze.card;


import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.IYzInfoChangeService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
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
 *  卡信息变更
 *  2021-9-22
 */
@Api("卡信息变更")
@RestController
@RequestMapping("/yunze/change")
public class YzYzInfoChangeController extends MyBaseController {

    @Autowired
    private IYzInfoChangeService iYzInfoChangeService;

    /**
     * 获取卡信息变更列表
     * */
    @PreAuthorize("@ss.hasPermi('yunze:change:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String list(@RequestBody String Pstr){
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
            return MyRetunSuccess(iYzInfoChangeService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> info:change:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取卡信息变更列表 操作失败！");
    }

    /**
     * 时间降序查询
     * */
    @PreAuthorize("@ss.hasPermi('yunze:change:list')")
    @PostMapping(value = "/info", produces = { "application/json;charset=UTF-8" })
    public String findSys(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzInfoChangeService.selTime(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:change:info  <br/>   ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询 【时间降序】  操作失败！");
    }






}
















































































