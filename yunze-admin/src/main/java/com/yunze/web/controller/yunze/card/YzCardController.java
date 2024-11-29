package com.yunze.web.controller.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.controller.BaseController;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.entity.YzCard;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.StringUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.poi.ExcelUtil;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.*;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.ISysUserService;
import com.yunze.system.service.impl.yunze.YzCardServiceImpl;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡信息
 * 2021-06-05
 *
 * @author root
 */
@Api("卡信息")
@RestController
@RequestMapping("/yunze/card")
public class YzCardController extends MyBaseController {
    @Resource
    private YzCardServiceImpl yzCardServiceImpl;
    @Autowired
    private ISysUserService userService;
    @Resource
    private CardFlowSyn cardFlowSyn;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;
    @Resource
    private InternalApiRequest internalApiRequest;

    @Log(title = "周期备注", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:import')")
    @PostMapping(value = "/otherImport", produces = {"application/json;charset=utf-8"})
    public AjaxResult otherImport(MultipartFile file) {
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            return AjaxResult.success(yzCardServiceImpl.updateOther(file, User));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:import  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("周期备注 操作失败！");
    }

    /**
     * 卡板信息列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String list(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<Integer> agent_id = new ArrayList<>();
            if (currentUser.getDeptId() != 100) {
                if (Parammap.get("agent_id") != null) {
                    List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                } else {
                    agent_id.add(Integer.parseInt(currentUser.getDeptId().toString()));
                    Parammap.put("agent_id", agent_id);
                }
            }
            Map<String, Object> stringObjectMap = yzCardServiceImpl.selMap(Parammap);

            return MyRetunSuccess(stringObjectMap, null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("获取卡板信息列表 操作失败！");
    }


    /**
     * 获取 所属代理下 卡分组
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:list')")
    @PostMapping(value = "/getCardGrouping", produces = {"application/json;charset=UTF-8"})
    public String getCardGrouping(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<Integer> agent_id = new ArrayList<>();
            String StrAgent_id = currentUser.getDeptId().toString() + ",";
            if (Parammap.get("agent_id") != null) {
                List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                for (int i = 0; i < P_agent_id.size(); i++) {
                    StrAgent_id += "" + P_agent_id.get(i) + ",";
                }
            }
            if (currentUser.getDeptId() == 100) {
                if (Parammap.get("agent_id") != null) {
                    Parammap.put("agent_id", StrAgent_id);
                }
            } else {
                Parammap.put("agent_id", StrAgent_id);
            }
            return MyRetunSuccess(yzCardServiceImpl.getCardGrouping(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /yunze/card/getCardGrouping  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("获取 所属代理下 卡分组 操作失败！");
    }


    /**
     * 查询卡板详情
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:find')")
    @PostMapping(value = "/find", produces = {"application/json;charset=utf-8"})//
    public String find(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzCardServiceImpl.find(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询卡板详情 操作失败！");
    }


    /**
     * 导入
     *
     * @param file
     * @param updateSupport
     * @return
     */
    @Log(title = "卡列表", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:import')")
    @PostMapping(value = "/importData", produces = {"application/json;charset=utf-8"})
    public AjaxResult importData(MultipartFile file, boolean updateSupport) {
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            return AjaxResult.success(yzCardServiceImpl.uploadCard(file, updateSupport, User));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:import  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("导入卡列表 操作失败！");
    }


    /**
     * 下载导入模板
     *
     * @return
     */
    @GetMapping("/importTemplate")
    public AjaxResult importTemplate() {
        ExcelUtil<YzCard> util = new ExcelUtil<YzCard>(YzCard.class);
        return util.importTemplateExcel("卡列表数据");
    }


    /**
     * 连接管理获取部门名称
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:list')")
    @PostMapping(value = "/getDeptName", produces = {"application/json;charset=utf-8"})
    public String getDeptName() {
        try {
            return MyRetunSuccess(yzCardServiceImpl.getDeptName(), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:list  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("连接管理获取部门名称 操作失败！");
    }


    /**
     * 导出
     *
     * @return
     */
    @Log(title = "卡列表", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:exportData')")
    @PostMapping(value = "/exportData", produces = {"application/json;charset=utf-8"})
    public String exportData(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {

            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();


            return MyRetunSuccess(yzCardServiceImpl.exportData(Parammap, currentUser), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:exportData  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("导出卡列表 操作失败！");
    }


    /**
     * 查询 企业下所属人员
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:findDeptUser')")
    @PostMapping(value = "/findDeptUser", produces = {"application/json;charset=utf-8"})
    public String findDeptUser(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            String dept_id = Parammap.get("dept_id").toString();
            if (dept_id != null && dept_id != "") {
                return MyRetunSuccess(userService.findDeptUser(Parammap), null);
            } else {
                return Myerr("参数不全请核对后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:findDeptUser  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询 企业下所属人员 操作失败！");
    }


    /**
     * 划分卡信息
     *
     * @return
     */
    @Log(title = "划卡操作", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:card:divide')")
    @PostMapping(value = "/divide", produces = {"application/json;charset=utf-8"})
    public String divide(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Object set_dept_id = Parammap.get("set_dept_id");
            Object set_user_id = Parammap.get("set_user_id");
            Object set_dept_name = Parammap.get("set_dept_name");
            Object set_user_name = Parammap.get("set_user_name");
            if (set_dept_id != null && set_dept_id.toString() != "" && set_user_id != null && set_user_id.toString() != ""
                    && set_dept_name != null && set_dept_name.toString() != "" && set_user_name != null && set_user_name.toString() != "") {
                LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                SysUser currentUser = loginUser.getUser();
                String agent_id = currentUser.getDeptId().toString();
                // if(!set_dept_id.equals(agent_id)){
                //权限 管理 操作必须传入当前操作人 id
                List<Integer> agent_idArr = new ArrayList<>();
                if (currentUser.getDeptId() != 100) {
                    if (Parammap.get("agent_id") != null) {
                        List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                        agent_idArr.addAll(P_agent_id);
                    } else {
                        agent_idArr.add(Integer.parseInt(currentUser.getDeptId().toString()));
                        Parammap.put("agent_id", agent_idArr);
                    }
                }
                Parammap.put("TaskAgent_id", agent_id);
                return MyRetunSuccess(yzCardServiceImpl.dividCard(Parammap), null);
                /*}else{
                    return Myerr("不能操作！");
                }*/
            } else {
                return Myerr("参数不全请核对后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:divide  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("划分卡信息 操作失败！");
    }


    /**
     * 同步用量
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:SynFlow')")
    @PostMapping(value = "/SynFlow", produces = {"application/json;charset=utf-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {

                    Map<String, Object> Rmap = internalApiRequest.queryFlow(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        //获取 卡用量 开卡日期 更新 card info
                        if (Rmap.get("Use") != null && Rmap.get("Use") != "" && Rmap.get("Use").toString().trim().length() > 0) {
                            Double Use = Double.parseDouble(Rmap.get("Use").toString());
                            if (Use >= 0) {
                                try {
                                    Map<String, Object> RMap = cardFlowSyn.CalculationFlow(Parammap.get("iccid").toString(), Use);
                                    return MyRetunSuccess(RMap, "已成功同步用量！");
                                } catch (Exception e) {
                                    return Myerr("用量内部计算错误！" + e.getMessage().toString());
                                }
                            } else {
                                return Myerr("接口超频返回暂无数据返回，请稍后重试！");
                            }
                        }
                    } else {
                        return Myerr("网络繁忙稍后重试！" + Rmap.get("Message").toString());
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("同步用量 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:SynFlow  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("同步用量 操作失败！");
    }


    /**
     * 同步卡状态
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:SynStatus')")
    @PostMapping(value = "/SynStatus", produces = {"application/json;charset=utf-8"})
    public String SynStatus(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.queryCardStatus(Parammap, Route);

                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        //获取 卡状态 开卡日期 更新 card info
                        if (Rmap.get("statusCode") != null && Rmap.get("statusCode") != "" && Rmap.get("statusCode").toString().trim().length() > 0) {
                            String statusCode = Rmap.get("statusCode").toString().trim();
                            if (!statusCode.equals("0")) {
                                Map<String, Object> Upd_Map = new HashMap<>();
                                Upd_Map.put("status_id", statusCode);
                                Upd_Map.put("status_ShowId", getShowStatIdArr.GetShowStatId(statusCode));
                                Upd_Map.put("iccid", Parammap.get("iccid").toString());
                                try {
                                    boolean bool = yzCardServiceImpl.updStatusId(Upd_Map);//变更卡状态
                                    if (bool) {
                                        return MyRetunSuccess("", "已成功同步卡状态！");
                                    } else {
                                        return Myerr("保存状态操作失败:False！");
                                    }
                                } catch (Exception e) {
                                    return Myerr("DB保存状态操作失败！" + e.getMessage().toString());
                                }
                            } else {
                                return Myerr("接口超频返回暂无数据返回，请稍后重试！");
                            }
                        }
                    } else {
                        return Myerr("网络繁忙稍后重试！" + Rmap.get("Message").toString());
                    }

                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("同步卡状态 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:SynStatus  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("同步卡状态 操作失败！");
    }


    /**
     * 同步 激活时间
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:SynActivateDate')")
    @PostMapping(value = "/SynActivateDate", produces = {"application/json;charset=utf-8"})
    public String SynActivateDate(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        String opTypeName = "";
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            String opType = Parammap.get("opType").toString();// 更新激活时间 还是 开卡时间
            opTypeName = opType.equals("activate") ? "激活" : "开卡";
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.queryCardActiveTime(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        //获取 激活时间
                        if (Rmap.get("activateDate") != null && Rmap.get("activateDate") != "" && Rmap.get("activateDate").toString().trim().length() > 0 ||
                                Rmap.get("openDate") != null && Rmap.get("openDate") != "" && Rmap.get("openDate").toString().trim().length() > 0) {
                            Object activateDate_obj = Rmap.get("activateDate") != null && Rmap.get("activateDate") != "" && Rmap.get("activateDate").toString().trim().length() > 0 ?
                                    Rmap.get("activateDate").toString().trim() : "";
                            String activateDate = activateDate_obj.toString().trim();
                            activateDate = activateDate.length() > 10 ? activateDate.substring(0, 10) : activateDate;
                            Object openDate = Rmap.get("openDate") != null && Rmap.get("openDate") != "" && Rmap.get("openDate").toString().trim().length() > 0 ?
                                    Rmap.get("openDate").toString().trim() : null;
                            Map<String, Object> Upd_Map = new HashMap<>();
                            if (activateDate != null && activateDate.length() > 0 && opType.equals("activate")) {
                                Upd_Map.put("activate_date", activateDate);
                            }
                            if (openDate != null) {
                                Upd_Map.put("open_date", openDate);
                            }
                            Upd_Map.put("iccid", Parammap.get("iccid").toString());
                            try {
                                boolean bool = yzCardServiceImpl.updActivate(Upd_Map);//变更 激活时间 和 开卡日期
                                if (bool) {
                                    return MyRetunSuccess("", "已成功同步" + opTypeName + "时间！");
                                } else {
                                    return Myerr("保存" + opTypeName + "时间操作失败:False！");
                                }
                            } catch (Exception e) {
                                return Myerr("DB保存状态操作失败！" + e.getMessage().toString());
                            }
                        } else {
                            System.out.println(Rmap);
                            return Myerr("上游接口返回数据操作失败,稍后重试！");
                        }
                    } else {
                        return Myerr("网络繁忙稍后重试！" + Rmap.get("Message").toString());
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("同步" + opTypeName + "时间 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> SynActivateDate  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("同步" + opTypeName + "时间 操作失败！");
    }


    /**
     * 查询在线信息
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:queryOnlineStatus')")
    @PostMapping(value = "/queryOnlineStatus", produces = {"application/json;charset=utf-8"})
    public String queryOnlineStatus(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.queryOnlineStatus(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        return MyRetunSuccess(Rmap, "查询在线信息成功！");
                    } else {
                        return Myerr("暂未查询到在线信息！");
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("查询在线信息 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:queryOnlineStatus  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询在线信息 操作失败！");
    }


    /**
     * 机卡重绑
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:CardBinding')")
    @PostMapping(value = "/CardBinding", produces = {"application/json;charset=utf-8"})
    public String CardBinding(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.unbundling(Parammap, Route);
                    String code = Rmap.get("code").toString();
                    if (code.equals("200")) {
                        yzCardServiceImpl.CardInfoFlow(Parammap);
                        return MyRetunSuccess(Rmap.get("Message"), "操作完成！");
                    } else {
                        return Myerr(Rmap.get("Message").toString());
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("机卡重绑 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:CardBinding  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("机卡重绑 操作失败！");
    }


    /**
     * 连接管理设置
     *
     * @param file
     * @return
     */
    @Log(title = "连接管理设置", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:card:importSet')")
    @PostMapping(value = "/importSet", produces = {"application/json;charset=utf-8"})
    public AjaxResult importSet(MultipartFile file, @RequestParam Map<String, String> map) {
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
            Parammap.put("agent_id", User.getDeptId());
            return AjaxResult.success(yzCardServiceImpl.importSet(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:import  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("连接管理设置 操作失败！");
    }


    /**
     * 特殊操作查询IMEI
     *
     * @param file
     * @return
     */
    @Log(title = "特殊操作查询IMEI", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:importSelImei')")
    @PostMapping(value = "/importSelImei", produces = {"application/json;charset=utf-8"})
    public AjaxResult importSelImei(MultipartFile file) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            Parammap.put("agent_id", User.getDeptId());
            return AjaxResult.success(yzCardServiceImpl.importSelImei(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:importSelImei  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("特殊操作查询IMEI 操作失败！");
    }


    /**
     * 特殊操作 变更卡分组、备注
     *
     * @param file
     * @return
     */
    @Log(title = "特殊操作变更卡分组、备注", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:importSetCardInfo')")
    @PostMapping(value = "/importSetCardInfo", produces = {"application/json;charset=utf-8"})
    public AjaxResult importSetCardInfo(MultipartFile file) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            Parammap.put("agent_id", User.getDeptId());
            return AjaxResult.success(yzCardServiceImpl.importSetCardInfo(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:importSetCardInfo  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("特殊操作 变更卡分组、备注 操作失败！");
    }


    /**
     * 导入查询、卡号类别
     *
     * @param file
     * @return
     */
    @Log(title = "卡号导入查询", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:ImportQuery')")
    @PostMapping(value = "/cardNumber", produces = {"application/json;charset=utf-8"})
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
                return AjaxResult.success(AesEncryptUtil.encrypt(JSON.toJSONString(yzCardServiceImpl.CardNumberImport(file, Parammap))));
            } catch (Exception e) {
                return AjaxResult.error(AesEncryptUtil.encrypt(JSON.toJSONString("返回数据加密操作失败")));
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:ImportQuery  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("卡号导入查询 操作失败！");
    }


    /**
     * 批量停复机、断开网
     *
     * @param file
     * @return
     */
    @Log(title = "批量停复机、断开网 ", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:ApiUpdBatch')")
    @PostMapping(value = "/status", produces = {"application/json;charset=utf-8"})
    public AjaxResult importSelImei(MultipartFile file, @RequestParam Map<String, String> map) {
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
            Parammap.put("agent_id", User.getDeptId());

            return AjaxResult.success(yzCardServiceImpl.status(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:ApiUpdBatch  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("批量停复机、断开网  操作失败！");
    }

    /**
     * 修改 备注 分组
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:importSetCardInfo')")
    @PostMapping(value = "/updatefill", produces = {"application/json;charset=UTF-8"})
    public String Update(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCardServiceImpl.UpdateFill(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:importSetCardInfo  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("修改 备注 分组 操作失败！");
    }

    /**
     * 下面是 操作
     * */
    /**
     * 停机
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:Stopped')")
    @PostMapping(value = "/stopped", produces = {"application/json;charset=UTF-8"})
    public String StoppedArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            return MyRetunSuccess(yzCardServiceImpl.stoppedarr(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:Stopped  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【停机】 操作失败！");
    }

    /**
     * 复机
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:MachineArr')")
    @PostMapping(value = "/machine", produces = {"application/json;charset=UTF-8"})
    public String MachineArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            return MyRetunSuccess(yzCardServiceImpl.machinearr(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:machine  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【复机】 操作失败！");
    }

    /**
     * 断网
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:DisconnectNetworkArr')")
    @PostMapping(value = "/disconnectNetwork", produces = {"application/json;charset=UTF-8"})
    public String DisconnectNetworkArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            return MyRetunSuccess(yzCardServiceImpl.disconnectNetworkarr(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:disconnectNetwork  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【断网】 操作失败！");
    }

    /**
     * 开网
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:OpenNetworkArr')")
    @PostMapping(value = "/openNetwork", produces = {"application/json;charset=UTF-8"})
    public String OpenNetworkArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            return MyRetunSuccess(yzCardServiceImpl.openNetworkarr(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:OpenNetworkArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【开网】 操作失败！");
    }

    /**
     * 同步流量
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:ConsumptionArr')")
    @PostMapping(value = "/consumption", produces = {"application/json;charset=UTF-8"})
    public String ConsumptionArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            return MyRetunSuccess(yzCardServiceImpl.consumptionarr(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:ConsumptionArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【同步流量】 操作失败！");
    }

    /**
     * 同步状态
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:PublicMethodArr')")
    @PostMapping(value = "/publicmethod", produces = {"application/json;charset=UTF-8"})
    public String PublicMethodArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            return MyRetunSuccess(yzCardServiceImpl.publicmethodarr(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:PublicMethodArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【同步状态】 操作失败！");
    }

    /**
     * 同步状态和用量
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:ConsumptionAndStateArr')")
    @PostMapping(value = "/consumptionandstate", produces = {"application/json;charset=UTF-8"})
    public String ConsumptionAndStateArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            return MyRetunSuccess(yzCardServiceImpl.consumptionandstatearr(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:ConsumptionAndStateArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【同步状态和用量】 操作失败！");
    }

    /**
     * 修改余额
     */
    @Log(title = "修改余额操作", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:card:updateBalance')")
    @PostMapping(value = "/updBalance", produces = {"application/json;charset=utf-8"})
    public String updateBalance(@RequestBody String Pstr) {
        logger.info("Received Str: {}", Pstr); // 添加日志记录
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/"); // 转义 /
        }
        int updCount = 0;
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            HashMap<String, Object> paramMap = new HashMap<>(JSON.parseObject(Pstr));
            logger.info("Parsed paramMap: {}", paramMap); // 添加日志记录

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            if (StringUtils.isNotEmpty(loginUser.getUser().getUserName())) {
                updCount = yzCardServiceImpl.updBalance(paramMap);
                return MyRetunSuccess(updCount, null);
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/card/cardMatch  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e);
        }
        return Myerr("修改余额 操作失败！");
    }

    /**
     * 获取iccid
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:list')")
    @PostMapping(value = "/getIccid", produces = {"application/json;charset=UTF-8"})
    public String getIccid(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));


            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<Integer> agent_id = new ArrayList<>();
            if (currentUser.getDeptId() != 100) {
                if (Parammap.get("agent_id") != null) {
                    List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                } else {
                    agent_id.add(Integer.parseInt(currentUser.getDeptId().toString()));
                    Parammap.put("agent_id", agent_id);
                }
            }

            Map<String, Object> Rmap = yzCardServiceImpl.getIccid(Parammap);
            if (Rmap != null) {
                return MyRetunSuccess(Rmap, null);
            }
            return Myerr("未匹配到相应的数据，请重新输入！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:getIccid  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量 【同步状态和用量】 操作失败！");
    }


    /**
     * 批量取消实名
     *
     * @param file
     * @return
     */
    @Log(title = "批量取消实名", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:cancelrealname')")
    @PostMapping(value = "/cancelrealname", produces = {"application/json;charset=utf-8"})
    public AjaxResult AjaxName(MultipartFile file) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            Parammap.put("agent_id", User.getDeptId());
            return AjaxResult.success(yzCardServiceImpl.cancelrealname(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:cancelrealname  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("批量取消实名 操作失败！");
    }


    /**
     * 智能匹对
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:cardMatch')")
    @PostMapping(value = "/cardMatch", produces = {"application/json;charset=UTF-8"})
    public String cardMatch(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<Integer> agent_id = new ArrayList<>();
            if (currentUser.getDeptId() != 100) {
                if (Parammap.get("agent_id") != null) {
                    List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                } else {
                    agent_id.add(Integer.parseInt(currentUser.getDeptId().toString()));
                    Parammap.put("agent_id", agent_id);
                }
            }

            Map<String, Object> Rmap = yzCardServiceImpl.cardMatch(Parammap);
            if (Rmap != null) {
                return MyRetunSuccess(Rmap, null);
            }
            return Myerr("未匹配到相应的数据，请重新输入！");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/card/cardMatch  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("智能匹对 操作失败！");
    }


    /**
     * 单卡停机原因查询
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:simStopReason')")
    @PostMapping(value = "/simStopReason", produces = {"application/json;charset=utf-8"})
    public String simStopReason(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.simStopReason(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        return MyRetunSuccess(Rmap, "操作成功！");
                    } else {
                        return Myerr("暂未查询到单卡停机原因！");
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("查询单卡停机原因 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:simStopReason  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单卡停机原因查询 操作失败！");
    }


    /**
     * 单卡开关机状态实时查询
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:onOffStatus')")
    @PostMapping(value = "/onOffStatus", produces = {"application/json;charset=utf-8"})
    public String onOffStatus(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.onOffStatus(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        return MyRetunSuccess(Rmap, "操作成功！");
                    } else {
                        System.out.println(Rmap);
                        return Myerr("暂未查询到 单卡开关机状态！");
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("单卡开关机状态实时查询 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:onOffStatus  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单卡开关机状态实时查询 操作失败！");
    }


    /**
     * 单卡已开通APN信息查询
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:apnInfo')")
    @PostMapping(value = "/apnInfo", produces = {"application/json;charset=utf-8"})
    public String apnInfo(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.apnInfo(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        return MyRetunSuccess(Rmap, "操作成功！");
                    } else {
                        System.out.println(Rmap);
                        return Myerr("暂未查询到 单卡已开通APN信息查询！");
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("单卡已开通APN信息查询 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:apnInfo  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单卡已开通APN信息查询 操作失败！");
    }


    /**
     * 物联卡机卡分离状态查询
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:cardBindStatus')")
    @PostMapping(value = "/cardBindStatus", produces = {"application/json;charset=utf-8"})
    public String cardBindStatus(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.cardBindStatus(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        return MyRetunSuccess(Rmap, "操作成功！");
                    } else {
                        System.out.println(Rmap);
                        String msg = Rmap.get("Message") != null && Rmap.get("Message").toString().length() > 0 ? Rmap.get("Message").toString() : "暂未查询到 物联卡机卡分离状态！";
                        return Myerr(msg);
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("物联卡机卡分离状态查询 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:cardBindStatus  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("物联卡机卡分离状态查询 操作失败！");
    }


    /**
     * 单卡状态变更历史查询
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:simChangeHistory')")
    @PostMapping(value = "/simChangeHistory", produces = {"application/json;charset=utf-8"})
    public String simChangeHistory(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.simChangeHistory(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        return MyRetunSuccess(Rmap, "操作成功！");
                    } else {
                        System.out.println(Rmap);
                        return Myerr("暂未查询到 单卡状态变更历史！");
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("单卡状态变更历史查询 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:simChangeHistory  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单卡状态变更历史查询 操作失败！");
    }


    /**
     * 批量更新卡信息
     *
     * @param file
     * @return
     */
    @Log(title = "批量更新卡信息", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:replace')")
    @PostMapping(value = "/importCardReplace", produces = {"application/json;charset=utf-8"})
    public AjaxResult importCardReplace(MultipartFile file, @RequestParam Map<String, String> map) {
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
            Parammap.put("agent_id", User.getDeptId());
            return AjaxResult.success(yzCardServiceImpl.importCardReplace(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:importSelImei  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("批量更新卡信息 操作失败！");
    }


    /**
     * 灵活变更卡状态
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:fbestate')")
    @PostMapping(value = "/fbestate", produces = {"application/json;charset=UTF-8"})
    public String importFle(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();

            return MyRetunSuccess(yzCardServiceImpl.ChangeF(Parammap, currentUser), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:fbestate  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("灵活变更卡状态 操作失败！");
    }


    /**
     * 物联卡 公开查询简要信息
     *
     * @param Pstr
     * @return
     */
    @PostMapping(value = "/selCardOpen", produces = {"application/json;charset=utf-8"})
    public String selCardOpen(@RequestBody String Pstr) {
        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            if (Pstr != null) {
                Pstr = Pstr.replace("%2F", "/");//转义 /
            }
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccessOpen(yzCardServiceImpl.selCardOpen(ip, Parammap), "");
        } catch (Exception e) {
            logger.error("<br/> selCardOpen  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("物联卡 公开查询简要信息 操作失败！");
    }

    @Log(title = "划卡操作", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:card:divide')")
    @PostMapping(value = "/divideOne", produces = {"application/json;charset=utf-8"})
    public String divideOne(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Object set_dept_id = Parammap.get("set_dept_id");
            Object set_user_id = Parammap.get("set_user_id");
            Object set_dept_name = Parammap.get("set_dept_name");
            Object set_user_name = Parammap.get("set_user_name");
            if (set_dept_id != null && set_dept_id.toString() != "" && set_user_id != null && set_user_id.toString() != ""
                    && set_dept_name != null && set_dept_name.toString() != "" && set_user_name != null && set_user_name.toString() != "") {
                LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                SysUser currentUser = loginUser.getUser();
                String agent_id = currentUser.getDeptId().toString();
                // if(!set_dept_id.equals(agent_id)){
                //权限 管理 操作必须传入当前操作人 id
                List<Integer> agent_idArr = new ArrayList<>();
                if (currentUser.getDeptId() != 100) {
                    if (Parammap.get("agent_id") != null) {
                        List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                        agent_idArr.addAll(P_agent_id);
                    } else {
                        agent_idArr.add(Integer.parseInt(currentUser.getDeptId().toString()));
                        Parammap.put("agent_id", agent_idArr);
                    }
                }
                Parammap.put("TaskAgent_id", agent_id);
                return MyRetunSuccess(yzCardServiceImpl.dividCardOne(Parammap), null);
                /*}else{
                    return Myerr("不能操作！");
                }*/
            } else {
                return Myerr("参数不全请核对后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:divide  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("划分卡信息 操作失败！");
    }

    @Log(title = "添加卡至自动轮询", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:card:showhistoryUsed')")
    @PostMapping(value = "/addAutoPolling", produces = {"application/json;charset=utf-8"})
    public String addAutoPolling(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(null, yzCardServiceImpl.addAutoPolling(Parammap));


        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> updPwd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("添加卡至自动轮询 操作失败！");
    }

    /**
     * 获取 上游单卡订购套餐列表
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:selUsage')")
    @PostMapping(value = "/selUsage", produces = {"application/json;charset=UTF-8"})
    public String getListUsage(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCardServiceImpl.getListUsage(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:selUsage  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("上游单卡订购套餐列表 操作失败！");
    }

    /**
     * 获取 上游套餐记录列表
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:reminder')")
    @PostMapping(value = "/reminder", produces = {"application/json;charset=UTF-8"})
    public String getListReminder(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCardServiceImpl.getListReminder(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:reminder  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("上游套餐记录列表 操作失败！");
    }


    /**
     * 单卡同步用量和状态
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:InfoFlow')")
    @PostMapping(value = "/InfoFlow", produces = {"application/json;charset=UTF-8"})
    public String CardInfoFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCardServiceImpl.CardInfoFlow(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:InfoFlow  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单卡同步用量和状态 操作失败！");
    }


    /**
     * 卡详情界面 获取 订单信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:find')")
    @PostMapping(value = "/getOrderCard", produces = {"application/json;charset=utf-8"})//
    public String getOrderCard(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return MyRetunSuccess(yzCardServiceImpl.getOrderCard(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> getOrderCard  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("卡详情界面 获取 订单信息 操作失败！");
    }


    /**
     * 上游订购套餐查询
     *
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:queryOffering')")
    @PostMapping(value = "/queryOffering", produces = {"application/json;charset=utf-8"})
    public String queryOffering(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    Map<String, Object> Rmap = internalApiRequest.queryOffering(Parammap, Route);
                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                    if (code.equals("200")) {
                        return MyRetunSuccess(Rmap, "操作成功！");
                    } else {
                        return Myerr("暂未查询到 上游订购套餐查询！");
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    return Myerr("上游订购套餐查询 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                return Myerr(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> queryOffering  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("上游订购套餐查询 操作失败！");
    }


    /**
     * 单卡 修改 备注 分组
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:singleUpd')")
    @PostMapping(value = "/singleUpd", produces = {"application/json;charset=UTF-8"})
    public String singleCardInfo(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            Parammap.put("iccid", JSON.parseObject(Pstr).get("iccids"));
            Map<String, Object> rMap = new HashMap<>();
            if (Parammap.get("Button").toString().isEmpty()) {
                rMap = yzCardServiceImpl.singleState(Parammap);
            } else {
                Map<String, Object> UpdateSingle = yzCardServiceImpl.UpdateSingle(Parammap);
                rMap.put("bool", UpdateSingle.get("flag"));
                rMap.put("message", UpdateSingle.get("message"));
            }
            if ((boolean) rMap.get("bool")) {
                return MyRetunSuccess(rMap.get("message"), null);
            } else {
                return MyRetunSuccess(rMap.get("message"), rMap.get("message").toString());
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:singleUpd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单卡 修改 备注 分组 操作失败！");
    }

    /*单卡 灵活变更状态*/
    @PreAuthorize("@ss.hasPermi('yunze:card:singleState')")
    @PostMapping(value = "/singleState", produces = {"application/json;charset=UTF-8"})
    public String singFlexibleState(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            Map<String, Object> singleState = yzCardServiceImpl.singleState(Parammap);
            yzCardServiceImpl.CardInfoFlow(Parammap);
            return MyRetunSuccess(singleState, null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:singleState  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单卡 灵活变更状态！");
    }


    /**
     * 查询用户手机号
     *
     * @return
     */
    @PostMapping(value = "/userPhone", produces = {"application/json;charset=UTF-8"})
    public String UserPhone(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = userService.userPhone(Parammap);
            if (bool) {
                return MyRetunSuccess("操作成功！", null);
            } else {
                return Myerr("您的手机号不匹对");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:user:userPhone  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("查询用户手机号 操作失败！");
    }


    /**
     * 验证所属卡号 是否在名下
     */
    @PreAuthorize("@ss.hasPermi('yunze:card:list')")
    @PostMapping(value = "/verifyBelonging", produces = {"application/json;charset=UTF-8"})
    public String verifyBelonging(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            List<Integer> agent_id = new ArrayList<>();
            if (currentUser.getDeptId() != 100) {
                if (Parammap.get("agent_id") != null) {
                    List<Integer> P_agent_id = (List<Integer>) Parammap.get("agent_id");
                    agent_id.addAll(P_agent_id);
                } else {
                    agent_id.add(Integer.parseInt(currentUser.getDeptId().toString()));
                    Parammap.put("agent_id", agent_id);
                }
            }
            return MyRetunSuccess(yzCardServiceImpl.outCardIccid(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> verifyBelonging  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("验证所属卡号 是否在名下 操作失败！");
    }


    /**
     * 批量短信下发
     *
     * @param file
     * @return
     */
    @Log(title = "批量短信下发", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:card:smsCC')")
    @PostMapping(value = "/SmsCC", produces = {"application/json;charset=utf-8"})
    public AjaxResult SmsCC(MultipartFile file, @RequestParam Map<String, String> map) {
        String Pstr = map.get("Pstr").toString();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr.toString()));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser User = loginUser.getUser();
            Parammap.put("User", User);
            Parammap.put("agent_id", User.getDeptId());
            return AjaxResult.success(yzCardServiceImpl.smsCC(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> SmsCC  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("批量短信下发 操作失败！");
    }


}

