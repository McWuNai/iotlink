package com.yunze.web.controller.yunze.xsgl.CustomerContact;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.xsgl.CustomerContact.YzXsglCustomerCallLogService;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 客户联系记录  查询
 */
@RestController
@RequestMapping("/yunze/CustomerContact")
public class YzXsglCustomerCallLogController extends MyBaseController {

    @Resource
    private YzXsglCustomerCallLogService yzXsglCustomerCallLogService;


    /**
     * 客户联系记录 新增
     */
    @PreAuthorize("@ss.hasPermi('yunze:CustomerContact:NewCustomer')")
    @PostMapping(value = "/NewCustomer", produces = { "application/json;charset=utf-8" })
    public String NewCustomerAdd(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            boolean bool = yzXsglCustomerCallLogService.NewCustomerAdd(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("网络操作失败请稍后重试！！！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CustomerContact:NewCustomer  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("客户联系记录新增 操作失败！");
    }

    /**
     * 客户联系记录 查询列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:CustomerContact:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=utf-8" })
    public String getList (@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzXsglCustomerCallLogService.getList(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CustomerContact:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("客户联系记录 操作失败！");
    }

    /**
     * 删除 客户联系记录 单条
     */
    @PreAuthorize("@ss.hasPermi('yunze:CustomerContact:delId')")
    @PostMapping(value = "/delId", produces = { "application/json;charset=utf-8" })
    public String DeleteId (@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzXsglCustomerCallLogService.deleteId(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CustomerContact:delId  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("删除客户联系记录 操作失败！");
    }

    /**
     * 修改 客户联系记录
     */
    @PreAuthorize("@ss.hasPermi('yunze:CustomerContact:updateId')")
    @PostMapping(value = "/updateId", produces = { "application/json;charset=utf-8" })
    public String UpdateId (@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzXsglCustomerCallLogService.updateId(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CustomerContact:updateId  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改客户联系记录 操作失败！");
    }

    /**
     * 赋值 企业名称 管理员
     */
    @PreAuthorize("@ss.hasPermi('yunze:CustomerContact:assignment')")
    @PostMapping(value = "/assignment", produces = { "application/json;charset=utf-8" })
    public String AssignmentID (@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzXsglCustomerCallLogService.assignmentID(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CustomerContact:assignment  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("赋值 企业名称 管理员 操作失败！");
    }


}
