package com.yunze.web.controller.yunze.card;

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
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.IYzCardFlowService;
import com.yunze.system.service.yunze.IYzOrderService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  通道 管理
 *  2021-06-19
 * @author root
 */
@Api("订单管理")
@RestController
@RequestMapping("/yunze/order")
public class YzOrderController extends MyBaseController
{

    @Resource
    private IYzOrderService iYzOrderService;
    @Resource
    private IYzCardFlowService iYzCardFlowService;


    /**
     * 订单 列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:list')")
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
            return MyRetunSuccess(iYzOrderService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取订单 列表信息列表 操作失败！");
    }



    /**
     * 查询 资费计划 信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:list')")
    @PostMapping(value = "/queryPacketSimple", produces = { "application/json;charset=UTF-8" })
    public String queryPacketSimple()
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<String> agent_id = new ArrayList<>();
            if(currentUser.getDeptId()!=100){
                    agent_id.add(currentUser.getDeptId().toString());
            }else{
                agent_id.add(currentUser.getDeptId().toString());
            }

            Parammap.put("agent_id",agent_id);
            return MyRetunSuccess(iYzCardFlowService.queryPacket_simple(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:list  <br/>   ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取 【订单】 查询 资费计划 操作失败！");
    }


    /**
     * 查询 订单详情
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:findOrder')")
    @PostMapping(value = "/findOrder", produces = { "application/json;charset=UTF-8" })
    public String findOrder(@RequestBody String Pstr)
    {
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzOrderService.findOrder(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:findOrder  <br/>   ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询 【订单详情】  操作失败！");
    }


    /**
     * 平台导入充值
     * @param file
     * @return
     */
    @Log(title = "订单管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:order:importRecharge')")
    @PostMapping(value = "/importRecharge", produces = { "application/json;charset=utf-8" })
    public AjaxResult importRecharge(MultipartFile file,@RequestParam Map<String,String> map)
    {

        String Pstr = map.get("Pstr").toString();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User",User);
            Parammap.put("agent_id",User.getDeptId());
            return AjaxResult.success(iYzOrderService.importRecharge(file,Parammap));
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:importRecharge  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return AjaxResult.error("平台导入充值 操作失败！");
    }

    /**
     * 导出
     * @return
     */
    @Log(title = "全部订单导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:order:outOrder')")
    @PostMapping(value = "/outOrder", produces = { "application/json;charset=utf-8" })
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
            return MyRetunSuccess(iYzOrderService.exportallorders(Parammap,currentUser),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:outOrder  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("全部订单导出 操作失败！");
    }


    /**
     * 导入查询、卡号类别
     *
     * @param file
     * @return
     */
    @Log(title = "资费订单导入查询", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:order:cardNumberAge')")
    @PostMapping(value = "/cardNumberAge", produces = {"application/json;charset=utf-8"})
    public AjaxResult CardNumberImport(MultipartFile file, @RequestParam Map<String, String> map) {
        String Pstr = map.get("Pstr").toString();

        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            List<Integer> agent_id = new ArrayList<>();
            if (User.getDeptId() != 100) {
                if (Parammap.get("agent_id") != null) {
                    List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                } else {
                    agent_id.add(Integer.parseInt(User.getDeptId().toString()));
                    Parammap.put("agent_id", agent_id);
                }
            }
            try {
                return AjaxResult.success(AesEncryptUtil.encrypt(JSON.toJSONString(iYzOrderService.CardNumberImport(file, Parammap))));
            } catch (Exception e) {
                return AjaxResult.error(AesEncryptUtil.encrypt(JSON.toJSONString("返回数据加密操作失败")));
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:cardNumberAge  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("资费订单导入查询 操作失败！");
    }


    /**
     * 退订 不操作 下面的内容
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:nobscribe')")
    @PostMapping(value = "/nobscribe", produces = { "application/json;charset=UTF-8" })
    public String NoTbscribe(@RequestBody String Pstr)
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
            Parammap.put("TaskAgent_id", agent_id);
            return MyRetunSuccess(iYzOrderService.NoTbscribe(Parammap,currentUser),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:nobscribe  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取退订 不操作 操作失败！");
    }


    /**
     * 勾选执行加包
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:package')")
    @PostMapping(value = "/package", produces = { "application/json;charset=UTF-8" })
    public String TimeOrder(@RequestBody String Pstr)
    {
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzOrderService.getPackage(Parammap),null);
         }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:package  <br/>   ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("勾选执行加包  操作失败！");
    }



    /**
     * 平台充值 [文本域]操作
     * @return
     */
    @Log(title = "订单管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:order:TextRecharge')")
    @PostMapping(value = "/TextRecharge", produces = { "application/json;charset=utf-8" })
    public String TextRecharge(@RequestBody String Pstr)

    {
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User",User);
            Parammap.put("agent_id",User.getDeptId());
            return MyRetunSuccess(iYzOrderService.TextRecharge(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:importRecharge  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("平台充值 [文本域] 操作失败！");
    }




    /**
     * 平台充值 [文本域]操作-企业
     * @return
     */
    @Log(title = "企业订单", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:order:DeptTextRecharge')")
    @PostMapping(value = "/DeptTextRecharge", produces = { "application/json;charset=utf-8" })
    public String DeptTextRecharge(@RequestBody String Pstr)
    {
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> ParaMap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            ParaMap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            ParaMap.put("User",User);
            ParaMap.put("agent_id",User.getDeptId());
            ParaMap.put("dept_id",User.getDeptId());
            ParaMap.put("login_dept_id",User.getDeptId());
            return MyRetunSuccess(iYzOrderService.DeptTextRecharge(ParaMap,User),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:order:importRecharge  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("平台充值 [文本域] 操作失败！");
    }





    /**
     * 查询已充值的卡号数据
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:DeptTextRecharge')")
    @PostMapping(value = "/findRecharged", produces = { "application/json;charset=utf-8" })
    public String findRecharged(@RequestBody String Pstr)
    {
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(iYzOrderService.findRecharged(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> findRecharged  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询已充值的卡号数据 操作失败！");
    }



    /**
     * API续费订单审核状态 触发回调通知
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:audit')")
    @PostMapping(value = "/orderAudit", produces = { "application/json;charset=utf-8" })
    public String orderAudit(@RequestBody String Pstr)
    {
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Rmap = iYzOrderService.orderAudit(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Message = Rmap.get("Message").toString();
            if(bool){
                return MyRetunSuccess(null,Message);
            }else {
                return Myerr(Message);
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> findRecharged  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询已充值的卡号数据 操作失败！");
    }



}
















