package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.IYzCardPackageService;
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
 *  资费组 管理
 *  2021-06-22
 * @author root
 */
@Api("资费组管理")
@RestController
@RequestMapping("/yunze/tariffGroup")
public class YzCardPackageController extends MyBaseController
{

    @Autowired
    private IYzCardPackageService cardPackageService;



    /**
     * 平台资费组 列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:list')")
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
            return MyRetunSuccess(cardPackageService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取平台资费组列表 操作失败！");
    }


    /**
     * 平台资费组 新增
     */
    @Log(title = "平台资费组", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:add')")
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
            return MyRetunSuccess(cardPackageService.add(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台资费组 新增 操作失败！");
    }


    /**
     * 平台资费组 详情
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:find')")
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
            return MyRetunSuccess(cardPackageService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台资费组 详情 操作失败！");
    }


    /**
     * 平台资费组 修改
     */
    @Log(title = "平台资费组", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:edit')")
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
            boolean bool = cardPackageService.update(Parammap);
            String msg = bool?"修改 平台资费组成功！":"网络操作失败请稍后重试！！！";
            return MyRetunSuccess(bool,msg);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:edit  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台资费组 修改 操作失败！");
    }

    /**
     * 划分资费组查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:findPackage')")
    @PostMapping(value = "/findPackage", produces = { "application/json;charset=UTF-8" })
    public String findPackage(@RequestBody String Pstr)
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
            List<String> agent_id = new ArrayList<>();
            agent_id.add(currentUser.getDeptId().toString());
            Parammap.put("agent_id",agent_id);
            return MyRetunSuccess(cardPackageService.findPackage(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:findPackage  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("划分资费组查询  操作失败！");
    }



    /**
     * 平台资费组 划分
     */
    @Log(title = "平台资费组>划分资费", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:tariffDivision')")
    @PostMapping(value = "/tariffDivision", produces = { "application/json;charset=UTF-8" })
    public String tariffDivision(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Double set_error_so = 0.0,set_packet_cost = 0.0,set_packet_price = 0.0;
            try {
                set_error_so = Double.parseDouble(Parammap.get("set_error_so").toString());
                set_packet_cost = Double.parseDouble(Parammap.get("set_packet_cost").toString());
                set_packet_price = Double.parseDouble(Parammap.get("set_packet_price").toString());
            }catch (Exception e){
                return Myerr("平台资费组 划分 参数数据 操作失败！");
            }
            if(set_error_so>=1.0){
                if(set_packet_price>=set_packet_cost){
                    //售价大等于 成本
                    //不能设置平台的数据
                    String dept_id =  Parammap.get("dept_id").toString();
                    if(!dept_id.equals("100")){
                        //判断为总部管理才能操作划分资费计划
                        LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                        SysUser currentUser = loginUser.getUser();
                        String ParentDept_id = currentUser.getDeptId().toString();
                        if(ParentDept_id.equals("100")){
                            Parammap.put("ParentDept_id",ParentDept_id);//复制 来源 父类 编号
                            return MyRetunSuccess(true,cardPackageService.tariffDivision(Parammap));
                        }else{
                            return Myerr("非平台总企业不可操作企业资费划分 ！请联系管理员先变更所属企业！");
                        }
                    }else{
                        return Myerr("无需划分资费到总平台 ！");
                    }
                }else{
                    return Myerr("售价  不能小于 成本 ！");
                }
            }else{
                return Myerr("资费计划系数不能 小于 1 ！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:tariffDivision  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("平台资费组 划分 操作失败！");
    }



    /**
     * 企业 资费组 划分
     */
    @Log(title = "企业资费组>划分资费", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:agentTariffDivision')")
    @PostMapping(value = "/agentTariffDivision", produces = { "application/json;charset=UTF-8" })
    public String agentTariffDivision(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Double set_error_so = 0.0,set_packet_cost = 0.0,set_packet_price = 0.0;
            try {
                set_error_so = Double.parseDouble(Parammap.get("set_error_so").toString());
                set_packet_cost = Double.parseDouble(Parammap.get("set_packet_cost").toString());
                set_packet_price = Double.parseDouble(Parammap.get("set_packet_price").toString());
            }catch (Exception e){
                return Myerr("平台资费组 划分 参数数据 操作失败！");
            }
            if(set_error_so>=1.0){
                //售价大等于 成本
                if(set_packet_price>=set_packet_cost){
                    //不能设置平台的数据
                    String dept_id =  Parammap.get("dept_id").toString();
                    if(!dept_id.equals("100")){
                        //不能划分到所属企业名下
                        LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                        SysUser currentUser = loginUser.getUser();
                        String ParentDept_id = currentUser.getDeptId().toString();
                        if(!ParentDept_id.equals(dept_id)){
                            if(!ParentDept_id.equals("100")){
                                Parammap.put("ParentDept_id",ParentDept_id);//复制 来源 父类 编号
                                return MyRetunSuccess(true,cardPackageService.tariffDivision(Parammap));
                            }else{
                                return Myerr("总企业 不能直接 划分 用户 资费 ！！");
                            }
                        }else{
                            return Myerr("不能划分所属企业资费 ！");
                        }
                    }else{
                        return Myerr("无需划分资费到总平台 ！");
                    }
                }else{
                    return Myerr("售价  不能小于 成本 ！");
                }
            }else{
                return Myerr("资费计划系数不能 小于 1 ！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:agentTariffDivision  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("企业 资费组 划分 操作失败！");
    }


    /**
     * 查询通道中间表
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:list')")
    @PostMapping(value = "/through", produces = { "application/json;charset=UTF-8" })
    public String ThroughAssignment(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(cardPackageService.ruepkeFz(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:through  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询通道中间表 操作失败！");
    }

    /**
     * 通道选择后加载出对应的资费计划
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:list')")
    @PostMapping(value = "/package", produces = { "application/json;charset=UTF-8" })
    public String packageName(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(cardPackageService.packageName(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:through  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("通道选择后加载出对应的资费计划 操作失败！");
    }

    /**
     * 选择资费组时加载出对应的通道
     */
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:list')")
    @PostMapping(value = "/channel", produces = { "application/json;charset=UTF-8" })
    public String channelName(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(cardPackageService.channelName(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:through  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("选择资费组时加载出对应的通道 操作失败！");
    }

    /**
     * 导出
     * @return
     */
    @Log(title = "平台资费导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:export')")
    @PostMapping(value = "/export", produces = { "application/json;charset=utf-8" })
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
            SysUser currentUser = loginUser.getUser();
            return MyRetunSuccess(cardPackageService.exportallorders(Parammap,currentUser),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:export  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("平台资费导出 操作失败！");
    }



    @Log(title = "资费计划导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:tariffGroup:export')")
    @PostMapping(value = "/exportPacket", produces = { "application/json;charset=utf-8" })
    public String exportPacket(@RequestBody String Pstr)
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
            return MyRetunSuccess(cardPackageService.exportPacket(Parammap,currentUser),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:tariffGroup:export  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("平台资费导出 操作失败！");
    }



}
