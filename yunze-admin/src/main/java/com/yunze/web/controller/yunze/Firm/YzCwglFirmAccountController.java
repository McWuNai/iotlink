package com.yunze.web.controller.yunze.Firm;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.cwgl.Firm.IYzCwglFirmAccountService;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 财务管理 公司信息
 */
@RestController
@RequestMapping("/yunze/CwglFirm")
public class YzCwglFirmAccountController extends MyBaseController
{

    @Resource
    private IYzCwglFirmAccountService iYzCwglFirmAccountService;


    /**
     * 公司信息 列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:list')")
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
            return MyRetunSuccess(iYzCwglFirmAccountService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  CwglFirm/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取公司信息 列表 操作失败！");
    }





    /**
     * 公司信息 新增
     * @return
     */
    @Log(title = "公司信息", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:add')")
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
            boolean bool =iYzCwglFirmAccountService.save(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功",null);
            }else{
                return Myerr("操作失败，保存数据操作失败！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CwglFirm:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("新增公司信息 操作失败！");
    }



    /**
     * 公司信息 查询单条
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:find')")
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
            return MyRetunSuccess(iYzCwglFirmAccountService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CwglFirm:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询单条 公司信息 操作失败！");
    }



    /**
     * 公司信息 修改
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:upd')")
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
            boolean bool = iYzCwglFirmAccountService.upd(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功",null);
            }else{
                return Myerr("操作失败，保存数据操作失败！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CwglFirm:upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改 公司信息 操作失败！");
    }


    /**
     * 查询简要 公司信息
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:findArr')")
    @PostMapping(value = "/findArr", produces = { "application/json;charset=utf-8" })
    public String findArr(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzCwglFirmAccountService.findArr(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CwglFirm:upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改 公司信息 操作失败！");
    }

    /**
     * 真实删除 Fat_ID
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:deletefatid')")
    @PostMapping(value = "/deletefatid", produces = { "application/json;charset=utf-8" })
    public String deleteFat_ID(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzCwglFirmAccountService.deletefatid(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CwglFirm:deletefatid  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("删除 公司账户 操作失败！");
    }

    /**
     * 删除 Fat_ID
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:deletefatidarr')")
    @PostMapping(value = "/deletefatidarr", produces = { "application/json;charset=utf-8" })
    public String deleteFat_IDArr(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzCwglFirmAccountService.delId(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CwglFirm:deletefatid  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("真实删除 公司账户 操作失败！");
    }

    /**
     * 查询 公司名称
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:CwglFirm:companyname')")
    @PostMapping(value = "/companyname", produces = { "application/json;charset=utf-8" })
    public String Name(){
        try {
            return MyRetunSuccess(iYzCwglFirmAccountService.sleName(),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:CwglFirm:companyname  " + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询公司名称  操作失败！");
    }


}
