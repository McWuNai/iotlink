package com.yunze.web.controller.yunze.xsgl.customer;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.common.utils.yunze.Different;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.xsgl.customer.IYzXsglCustomerService;
import com.yunze.system.service.yunze.xsgl.customer.YzXsglCustomerImgService;
import com.yunze.system.service.yunze.xsgl.customer.YzXsglCustomerPeopleImgService;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售管理 客户信息
 */
@RestController
@RequestMapping("/yunze/XsglCustomer")
public class YzXsglCustomerController extends MyBaseController
{

    @Resource
    private IYzXsglCustomerService iYzXsglCustomerService;
    @Resource
    private YzXsglCustomerImgService yzXsglCustomerImgService;
    @Resource
    private YzXsglCustomerPeopleImgService yzXsglCustomerPeopleImgService;


    /**
     * 客户信息 列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:list')")
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
                        List<String> sales_idArr = new ArrayList<>();
                        sales_idArr.add(""+User.getUserId());
                        Parammap.put("sales_idArr",sales_idArr);
                    }
                }
            }catch (Exception e){
                System.out.println("仅可看所属销售 权限过滤操作失败");
            }
            return MyRetunSuccess(iYzXsglCustomerService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取客户信息 列表 操作失败！");
    }



    /**
     * 客户信息 新增
     * @return
     */
    @Log(title = "客户信息", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:add')")
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
            Map<String,Object> Rmap = iYzXsglCustomerService.save(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Msg =  Rmap.get("Msg").toString();
            if(bool){
                return MyRetunSuccess(Msg,null);
            }else{
                return Myerr(Msg);
            }

        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("新增客户信息 操作失败！");
    }



    /**
     * 客户信息 查询单条
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:find')")
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
            return MyRetunSuccess(iYzXsglCustomerService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询单条 客户信息 操作失败！");
    }

    /**
     * 查询客户图片列表
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:contactsList')")
    @PostMapping(value = "/contactsList", produces = { "application/json;charset=utf-8" })
    public String CountList(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzXsglCustomerImgService.contactsList(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:contactsList  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询客户图片列表  操作失败！");
    }




    /**
     * 客户信息 修改
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:upd')")
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
            Map<String,Object> Rmap = iYzXsglCustomerService.upd(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Msg =  Rmap.get("Msg").toString();
            if(bool){
                return MyRetunSuccess(Msg,null);
            }else{
                return Myerr(Msg);
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改 客户信息 操作失败！");
    }

    /**
     * 修改 图片状态 无效
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:switch')")
    @PostMapping(value = "/switchStatus", produces = { "application/json;charset=utf-8" })
    public String picStatus(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            boolean bool = yzXsglCustomerImgService.picStatus(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功!", null);
            }else {
                return Myerr("变更无效图片失败!");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:switch  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改图片状态  操作失败！");
    }

    /**
     * 客户信息 高级修改
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:updInfo')")
    @PostMapping(value = "/updInfo", produces = { "application/json;charset=utf-8" })
    public String updInfo(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String,Object> Rmap = iYzXsglCustomerService.updInfo(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Msg =  Rmap.get("Msg").toString();
            if(bool){
                return MyRetunSuccess(Msg,null);
            }else{
                return Myerr(Msg);
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:updInfo  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("高级修改 客户信息 操作失败！");
    }

    /**
     * 修改 图片状态 有效
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:effective')")
    @PostMapping(value = "/effective", produces = { "application/json;charset=utf-8" })
    public String Effective(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            boolean bool = yzXsglCustomerImgService.effectiveStatus(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功!", null);
            }else {
                return Myerr("变更有效图片失败!");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:effective  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改图片状态  操作失败！");
    }


    /**
     * 删除单个图片
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:single')")
    @PostMapping(value = "/single", produces = { "application/json;charset=utf-8" })
    public String Single(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            boolean bool = yzXsglCustomerImgService.singleUrl(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功!", null);
            }else {
                return Myerr("删除失败!");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:single  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("删除单个图片  操作失败！");
    }


    /**
     * 查询所属客户 合伙人
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:list')")
    @PostMapping(value = "/findSalesPartner", produces = { "application/json;charset=utf-8" })
    public String findSalesPartner(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));

            return MyRetunSuccess(iYzXsglCustomerService.findSalesPartner(Parammap), "查询所属客户 合伙人成功！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> findSalesPartner  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询所属客户 合伙人 操作失败！");
    }




    /**
     * 查询 总管理 或 非总管理 下  客户
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:Divide')")
    @PostMapping(value = "/CustomerDividefind", produces = { "application/json;charset=utf-8" })
    public String CustomerDividefind(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzXsglCustomerService.IsAdmin(Parammap), "查询 总管理 或 非总管理 下  客户 成功！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> CustomerDividefind  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询 总管理 或 非总管理 下  客户 操作失败！");
    }



    /**
     * 客户划分
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:Divide')")
    @PostMapping(value = "/CustomerDivide", produces = { "application/json;charset=utf-8" })
    public String CustomerDivide(@RequestBody String Pstr) {
        try {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User",User);
            return MyRetunSuccess(iYzXsglCustomerService.Divide(Parammap), "客户划分 成功！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> CustomerDivide  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("客户划分 操作失败！");
    }



    /**
     * 客户 费率设置
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:Rate')")
    @PostMapping(value = "/CustomerRate", produces = { "application/json;charset=utf-8" })
    public String CustomerRate(@RequestBody String Pstr) {
        try {
            HashMap<String, Object> Parammap = new HashMap<String, Object>();
            if (Pstr != null) {
                Pstr = Pstr.replace("%2F", "/");//转义 /
            }
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User",User);
            return MyRetunSuccess(iYzXsglCustomerService.Rate(Parammap), "客户费率设置 成功！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> CustomerRate  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("客户费率设置 操作失败！");
    }




    /**
     * 客户 简要信息查询
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:findCustomerArr')")
    @PostMapping(value = "/findCustomerArr", produces = { "application/json;charset=utf-8" })
    public String findCustomerArr(@RequestBody String Pstr) {
        try {
            HashMap<String, Object> Parammap = new HashMap<String, Object>();
            if (Pstr != null) {
                Pstr = Pstr.replace("%2F", "/");//转义 /
            }
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
                        List<String> sales_idArr = new ArrayList<>();
                        sales_idArr.add(""+User.getUserId());
                        Parammap.put("sales_idArr",sales_idArr);
                    }
                }
            }catch (Exception e){
                System.out.println("仅可看所属销售 权限过滤操作失败");
            }
            return MyRetunSuccess(iYzXsglCustomerService.findCustomerArr(Parammap), "简要信息查询 成功！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> findCustomerArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("客户 简要信息查询 操作失败！");
    }



    /**
     * 客户 简要联系人信息
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:fnidPeopleArr')")
    @PostMapping(value = "/fnidPeopleArr", produces = { "application/json;charset=utf-8" })
    public String fnidPeopleArr(@RequestBody String Pstr) {
        try {
            HashMap<String, Object> Parammap = new HashMap<String, Object>();
            if (Pstr != null) {
                Pstr = Pstr.replace("%2F", "/");//转义 /
            }
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzXsglCustomerService.fnidPeopleArr(Parammap), "查询 简要联系人信息 成功！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> fnidPeopleArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询  简要联系人信息 操作失败！");
    }



    /**
     *  客户地址简要信息
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:fnidAccountArr')")
    @PostMapping(value = "/fnidAccountArr", produces = { "application/json;charset=utf-8" })
    public String fnidAccountArr(@RequestBody String Pstr) {
        try {
            HashMap<String, Object> Parammap = new HashMap<String, Object>();
            if (Pstr != null) {
                Pstr = Pstr.replace("%2F", "/");//转义 /
            }
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzXsglCustomerService.fnidAccountArr(Parammap), "查询 客户地址简要信息 成功！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> fnidAccountArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询 客户地址简要信息 操作失败！");
    }



    /**
     * 导入
     * @param file
     * @param updateSupport
     * @return
     */
    @Log(title = "客户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:import')")
    @PostMapping(value = "/importData", produces = { "application/json;charset=utf-8" })
    public AjaxResult importData(MultipartFile file, boolean updateSupport)
    {
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            return AjaxResult.success(iYzXsglCustomerService.uploadCustomer(file,updateSupport,User));
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /yunze/XsglCustomer/importData  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return AjaxResult.error("导入 客户管理 操作失败！");
    }



    /**
     * 客户图片 编辑
     * @param file
     * @return
     */
    @Log(title = "客户图片", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:guest')")
    @PostMapping(value = "/guest", produces = { "application/json;charset=utf-8" })
    public AjaxResult Guest(MultipartFile file[], @RequestParam Map<String,String> map)
    {
        String PstrFrom = map.get("PstrFrom").toString();
        if(PstrFrom!=null){
            PstrFrom = PstrFrom.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            PstrFrom =  AesEncryptUtil.desEncrypt(PstrFrom);
            Parammap.putAll(JSON.parseObject((String) PstrFrom));
            return AjaxResult.success(yzXsglCustomerImgService.guestFile(file,Parammap));
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/XsglCustomer/guest  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return AjaxResult.error("客户图片 操作失败！");
    }


    /**
     * 加载图片信息
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:urlPicture')")
    @PostMapping(value = "/urlPicture", produces = { "application/json;charset=utf-8" })
    public String urlPicture(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzXsglCustomerImgService.urlID(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:XsglCustomer:urlPicture  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("加载图片信息  操作失败！");
    }




    /**
     * 客户联系人图片 编辑
     * @param file
     * @return
     */
    @Log(title = "客户联系人图片", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:Prdedit')")
    @PostMapping(value = "/Prdedit", produces = { "application/json;charset=utf-8" })
    public AjaxResult Prdedit(MultipartFile file[], @RequestParam Map<String,String> map)
    {
        String PstrArr = map.get("PstrArr").toString();
        if(PstrArr!=null){
            PstrArr = PstrArr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            PstrArr =  AesEncryptUtil.desEncrypt(PstrArr);
            Parammap.putAll(JSON.parseObject((String) PstrArr));
            return AjaxResult.success(yzXsglCustomerPeopleImgService.Prdedit(file,Parammap));
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/XsglCustomer/Prdedit  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return AjaxResult.error("客户联系人图片 操作失败！");
    }

    /**
     * 获取联系人 图片
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:contacts')")
    @PostMapping(value = "/contacts", produces = {"application/json;charset=UTF-8"})
    public String findConcise(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzXsglCustomerPeopleImgService.contactsList(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/XsglCustomer/contacts  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("获取联系人 图片 操作失败！");
    }




    /**
     * 查询联系人列表 图片
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:ContactsPicture:list')")
    @PostMapping(value = "/liaison", produces = {"application/json;charset=UTF-8"})
    public String ListArr (@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzXsglCustomerPeopleImgService.liaisonList(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/XsglCustomer/liaison  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询联系人图片列表 操作失败！");
    }


    /**
     *  删除联系人单个图片
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:deurl')")
    @PostMapping(value = "/deurl", produces = {"application/json;charset=UTF-8"})
    public String deleteUrl (@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzXsglCustomerPeopleImgService.deleteUrl(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功!",null);
            }else {
                return Myerr("删除失败");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/XsglCustomer/deurl  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("删除联系人单个图片 操作失败！");
    }


    /**
     *  修改 无效状态
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:invalid')")
    @PostMapping(value = "/invalid", produces = {"application/json;charset=UTF-8"})
    public String upInvalid (@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzXsglCustomerPeopleImgService.upInvalid(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功!",null);
            }else {
                return Myerr("修改无效状态失败");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/XsglCustomer/invalid  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("修改 无效状态 操作失败！");
    }


    /**
     *  修改 有效状态
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:XsglCustomer:effectivesta')")
    @PostMapping(value = "/effectivesta", produces = {"application/json;charset=UTF-8"})
    public String upEffective (@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzXsglCustomerPeopleImgService.upEffective(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功!",null);
            }else {
                return Myerr("修改有效状态失败");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/XsglCustomer/effectivesta  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("修改 有效状态 操作失败！");
    }








}
