package com.yunze.web.controller.yunze.card.api;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.card.IYzAgentAccountService;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * API账号 管理
 */
@RestController
@RequestMapping("/yunze/ApiAccount")
public class YzAgentAccountController extends MyBaseController {

    @Resource
    private IYzAgentAccountService iYzAgentAccountService;

    /**
     * 执行API账号查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:apiAccount:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String selList(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            List<Integer> agent_idArr = new ArrayList<>();
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            String deptId = loginUser.getUser().getDeptId().toString();
            if(!deptId.equals("100")){
                if(Parammap.get("agent_id")!=null){
                    List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                    agent_idArr.addAll(P_agent_id);
                }else{
                    agent_idArr.add(Integer.parseInt(deptId));
                    Parammap.put("agent_id",agent_idArr);
                }
            }
            return MyRetunSuccess(iYzAgentAccountService.getList(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:polling:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询 API账号 操作失败！");
    }



    /**
     * 新增
     */
    @Log(title = "API账号新增", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:apiAccount:save')")
    @PostMapping(value = "/save", produces = {"application/json;charset=UTF-8"})
    public String save(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(iYzAgentAccountService.save(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:polling:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("新增 API账号 操作失败！");
    }



    /**
     * 修改
     */
    @Log(title = "API账号修改", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:apiAccount:upd')")
    @PostMapping(value = "/upd", produces = {"application/json;charset=UTF-8"})
    public String upd(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            String deptId = loginUser.getUser().getDeptId().toString();
            if(!deptId.equals("100")){
                String P_agent_id = Parammap.get("agent_id").toString();
                if(!P_agent_id.equals(deptId)){
                    return Myerr("不允许修改非本企业数据信息！");
                }
            }
            return MyRetunSuccess(iYzAgentAccountService.upd(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:polling:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("修改 API账号 操作失败！");
    }


    /**
     * 获取 agent_id 下 appId
     */
    @PreAuthorize("@ss.hasPermi('yunze:apiAccount:find')")
    @PostMapping(value = "/IsExaAgentId", produces = {"application/json;charset=UTF-8"})
    public String IsExaAgentId(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(iYzAgentAccountService.is_exaAgentId(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:apiAccount:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("获取 agent_id 下 appId 操作失败！");
    }

    /**
     * 删除
     */
    @Log(title = "API账号删除", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('yunze:apiAccount:del')")
    @PostMapping(value = "/del", produces = {"application/json;charset=UTF-8"})
    public String Del(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            String deptId = loginUser.getUser().getDeptId().toString();
            if(!deptId.equals("100")){
               return Myerr("非总企业下用户不开放该功能！");
            }
            boolean bool = iYzAgentAccountService.del(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！", null);
            }else{
                return Myerr("删除 操作失败！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:apiAccount:del  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("删除 API账号 操作失败！");
    }





}













