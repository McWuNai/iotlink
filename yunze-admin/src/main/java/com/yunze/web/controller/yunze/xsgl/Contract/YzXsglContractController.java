package com.yunze.web.controller.yunze.xsgl.Contract;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.common.utils.yunze.Different;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.xsgl.Contract.IYzXsglContractService;
import com.yunze.system.service.yunze.xsgl.customer.IYzXsglCustomerService;
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
 * 销售管理 销售合同
 */
@RestController
@RequestMapping("/yunze/XsglContract")
public class YzXsglContractController extends MyBaseController
{

    @Resource
    private IYzXsglContractService iYzXsglContractService;
    @Resource
    private IYzXsglCustomerService iYzXsglCustomerService;


    /**
     * 销售合同 列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglContract:list')")
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
            try {
                //获取对当前登录用户 菜单根目录 允许 btn 权限
                LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                SysUser User = loginUser.getUser();
                HashMap<String, Object> AuthorityMap = new HashMap<String, Object>();
                AuthorityMap.put("user_id",User.getUserId());
                List<Map<String,Object>> UserArr = iYzXsglCustomerService.findSalesPartner(AuthorityMap);
                Object Btns = (UserArr.get(0)).get("Btns");
                if(Btns!=null && Btns.toString().length()>0){
                    String arr[] = Btns.toString().split(",");
                    if(Different.Is_existence(arr,"yunze:sys:Sales")){// 销售仅可看自己数据 【是否为销售业务员】
                        List<String> c_userIdArr = new ArrayList<>();
                        c_userIdArr.add(""+User.getUserId());
                        Parammap.put("c_userIdArr",c_userIdArr);
                    }
                }
            }catch (Exception e){
                System.out.println("仅可看所属销售 权限过滤操作失败");
            }
            return MyRetunSuccess(iYzXsglContractService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  XsglContract/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取销售合同 列表 操作失败！");
    }





    /**
     * 销售合同 新增
     * @return
     */
    @Log(title = "销售合同", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:XsglContract:add')")
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
            Map<String,Object> Rmap = iYzXsglContractService.save(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Msg =  Rmap.get("Msg").toString();
            if(bool){
                return MyRetunSuccess(Msg,null);
            }else{
                return Myerr(Msg);
            }

        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglContract:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("新增销售合同 操作失败！");
    }



    /**
     * 销售合同 查询单条
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglContract:find')")
    @PostMapping(value = "/find", produces = { "application/json;charset=utf-8" })
    public String find(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzXsglContractService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglContract:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询单条 销售合同 操作失败！");
    }



    /**
     * 销售合同 修改
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglContract:upd')")
    @PostMapping(value = "/upd", produces = { "application/json;charset=utf-8" })
    public String upd(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String,Object> Rmap = iYzXsglContractService.upd(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Msg =  Rmap.get("Msg").toString();
            if(bool){
                return MyRetunSuccess(Msg,null);
            }else{
                return Myerr(Msg);
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglContract:upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改 销售合同 操作失败！");
    }



    /**
     * 发货申请查询 合同 数据
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:ShippingApplication:add')")
    @PostMapping(value = "/find_FH_data", produces = { "application/json;charset=utf-8" })
    public String find_FH_data(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzXsglContractService.find_FH_data(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> find_FH_data  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("发货申请查询 合同 数据  操作失败！");
    }






}
