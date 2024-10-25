package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.mapper.yunze.YzUserMapper;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.card.IYzApplicationforRenewalService;
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
import java.util.Map;

/**
 * 续费申请 管理
 */
@RestController
@RequestMapping("/yunze/renewal")
public class YzApplicationforRenewalController extends MyBaseController {

    @Resource
    private IYzApplicationforRenewalService iYzApplicationforRenewalService;
    @Resource
    private YzUserMapper yzUserMapper;
    /**
     * 续费申请 查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:renewal:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String selList(@RequestBody String Pstr) {
        HashMap<String, Object> ParaMap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            ParaMap.putAll(JSON.parseObject(Pstr));
            List<Integer> agent_idArr = new ArrayList<>();
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            String deptId = loginUser.getUser().getDeptId().toString();
            if(!deptId.equals("100")){
                if(ParaMap.get("agent_id")!=null){
                    List<Integer> P_agent_id = (List<Integer>) ParaMap.get("agent_id");
                    agent_idArr.addAll(P_agent_id);
                }else{
                    agent_idArr.add(Integer.parseInt(deptId));
                    ParaMap.put("agent_id",agent_idArr);
                }
            }
            return MyRetunSuccess(iYzApplicationforRenewalService.selMap(ParaMap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:renewal:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询 续费申请 操作失败！");
    }


    /**
     *  查询 企业账户账户金额信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:renewal:list')")
    @PostMapping(value = "/getDeptPrestore", produces = {"application/json;charset=UTF-8"})
    public String getDeptPrestore(@RequestBody String Pstr) {
        HashMap<String, Object> ParaMap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            ParaMap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            String deptId = loginUser.getUser().getDeptId().toString();
            ParaMap.put("dept_id",deptId);
            Map<String, Object> rMap = yzUserMapper.findDeptAmount(ParaMap);
            return MyRetunSuccess(rMap, null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:renewal:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询 企业账户账户金额信息 操作失败！");
    }
    
    


    /**
     * 新增
     */
    @Log(title = "新增续费申请", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:renewal:add')")
    @PostMapping(value = "/save", produces = {"application/json;charset=UTF-8"})
    public String save(@RequestBody String Pstr) {
        HashMap<String, Object> ParaMap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            ParaMap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser sysUser= loginUser.getUser();
            String deptId = sysUser.getDeptId().toString();
            ParaMap.put("dept_id",deptId);
            ParaMap.put("login_dept_id",deptId);
            return MyRetunSuccess(iYzApplicationforRenewalService.save(ParaMap,sysUser), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:polling:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("新增 续费申请 操作失败！");
    }



    /**
     * 修改
     */
    @Log(title = "修改续费详情", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:renewal:upd')")
    @PostMapping(value = "/upd", produces = {"application/json;charset=UTF-8"})
    public String upd(@RequestBody String Pstr) {
        HashMap<String, Object> ParaMap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            ParaMap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            String deptId = loginUser.getUser().getDeptId().toString();
            if(!deptId.equals("100")){
                String P_agent_id = ParaMap.get("agent_id").toString();
                if(!P_agent_id.equals(deptId)){
                    return Myerr("不允许修改非本企业数据信息！");
                }
            }
            return MyRetunSuccess(iYzApplicationforRenewalService.upd(ParaMap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:renewal:upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("续费详情修改 操作失败！");
    }



    /**
     * 查询单条
     */
    @PreAuthorize("@ss.hasPermi('yunze:renewal:find')")
    @PostMapping(value = "/find", produces = {"application/json;charset=UTF-8"})
    public String find(@RequestBody String Pstr) {
        HashMap<String, Object> ParaMap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            ParaMap.putAll(JSON.parseObject(Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<Integer> agent_id = new ArrayList<>();
            if (currentUser.getDeptId() != 100) {
                if (ParaMap.get("agent_id") != null) {
                    List<Integer> P_agent_id = (List<Integer>) ParaMap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                } else {
                    agent_id.add(Integer.parseInt(currentUser.getDeptId().toString()));
                    ParaMap.put("agent_id", agent_id);
                }
            }
            return MyRetunSuccess(iYzApplicationforRenewalService.find(ParaMap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:renewal:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询续费详情 操作失败！");
    }



    /**
     * 查询单条 续费资费计划下 iccid
     */
    @PreAuthorize("@ss.hasPermi('yunze:renewal:find')")
    @PostMapping(value = "/findIccid", produces = {"application/json;charset=UTF-8"})
    public String findIccid(@RequestBody String Pstr) {
        HashMap<String, Object> ParaMap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            ParaMap.putAll(JSON.parseObject(Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<Integer> agent_id = new ArrayList<>();
            if (currentUser.getDeptId() != 100) {
                if (ParaMap.get("agent_id") != null) {
                    List<Integer> P_agent_id = (List<Integer>) ParaMap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                } else {
                    agent_id.add(Integer.parseInt(currentUser.getDeptId().toString()));
                    ParaMap.put("agent_id", agent_id);
                }
            }
            return MyRetunSuccess(iYzApplicationforRenewalService.findIccid(ParaMap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /findIccid <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询 续费资费计划下 iccid 操作失败！");
    }



}













