package com.yunze.web.controller.yunze.Firm;

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
import com.yunze.system.service.yunze.cwgl.Firm.CwglDepositInfoService;
import com.yunze.system.service.yunze.xsgl.customer.IYzXsglCustomerService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
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

@Api("入款记录")
@RestController
@RequestMapping("/yunze/remittance")
public class CwglDepositInfoController extends MyBaseController {

    @Resource
    private CwglDepositInfoService cwglDepositInfoService;
    @Resource
    private IYzXsglCustomerService iYzXsglCustomerService;
    /**
     * 入款记录列表 查询
     * */
    @PreAuthorize("@ss.hasPermi('yunze:remittance:list')")
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
            return MyRetunSuccess(cwglDepositInfoService.sel_CT_data_DS(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  remittance/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("入款记录列表 列表 操作失败！");
    }


    /**
     * 修改入款记录
     * */
    @PreAuthorize("@ss.hasPermi('yunze:remittance:addIncome')")
    @PostMapping(value = "/addIncome", produces = { "application/json;charset=UTF-8" })
    public String addIncome (@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(cwglDepositInfoService.add_CDIO(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  remittance/addIncome  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改入款记录 操作失败！");
    }

    /**
     * 查询 入款信息详情表
     * */
    @PreAuthorize("@ss.hasPermi('yunze:remittance:slefind')")
    @PostMapping(value = "/slefind", produces = { "application/json;charset=UTF-8" })
    public String SleList (@RequestBody String Pstr)
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
            return MyRetunSuccess(cwglDepositInfoService.find_CDIDS(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  remittance/slefind  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询入款详情 操作失败！");
    }


    /**
     * 查询本年入款总计和为入款
     * */
    @PreAuthorize("@ss.hasPermi('yunze:remittance:Totalfortheyear')")
    @PostMapping(value = "/Totalfortheyear", produces = { "application/json;charset=UTF-8" })
    public String DeleteList (@RequestBody String Pstr)
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
            return MyRetunSuccess(cwglDepositInfoService.sel_Year_data_CT(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  remittance/Totalfortheyear  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("本年入款总计和为入款 操作失败！");
    }


    /**
     * 入款详情查看
     * */
    @PreAuthorize("@ss.hasPermi('yunze:remittance:sledeteils')")
    @PostMapping(value = "/sledeteils", produces = { "application/json;charset=UTF-8" })
    public String DetailsList (@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {

            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(cwglDepositInfoService.sleDetails(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  remittance/sledeteils  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("入款详情查看 操作失败！");
    }

    /**
     * 入款 月 月入款 月未到款 金额合计
     * */
    @PreAuthorize("@ss.hasPermi('yunze:remittance:amountofmoney')")
    @PostMapping(value = "/amountofmoney", produces = { "application/json;charset=UTF-8" })
    public String Year_data (@RequestBody String Pstr)
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
            return MyRetunSuccess(cwglDepositInfoService.sel_Month_data_CT(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  remittance/amountofmoney  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("入款金额 操作失败！");
    }


    /**
     * 入款信息导出
     * @return
     */
    @Log(title = "入款信息导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:remittance:ExportRemittance')")
    @PostMapping(value = "/ExportRemittance", produces = { "application/json;charset=utf-8" })
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
            SysUser User = loginUser.getUser();
            try {
                //获取对当前登录用户 菜单根目录 允许 btn 权限
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
            return MyRetunSuccess(cwglDepositInfoService.ExportRemittance(Parammap,User),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:remittance:ExportRemittance  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("入款信息导出 操作失败！");
    }


    /**
     * 入款详情导出
     * @return
     */
    @Log(title = "入款详情导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:remittance:ExportIncome')")
    @PostMapping(value = "/ExportIncome", produces = { "application/json;charset=utf-8" })
    public String exportDataI(@RequestBody String Pstr)
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
            SysUser User = loginUser.getUser();
            try {
                //获取对当前登录用户 菜单根目录 允许 btn 权限
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
            return MyRetunSuccess(cwglDepositInfoService.ExportIncome(Parammap,User),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:remittance:ExportIncome  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("入款详情导出 操作失败！");
    }


}


















