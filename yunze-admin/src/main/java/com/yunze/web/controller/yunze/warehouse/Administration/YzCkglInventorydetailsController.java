package com.yunze.web.controller.yunze.warehouse.Administration;

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
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglInventorydetailsService;
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
 * 库存明细
 * 2021-11-30
 */
@Api("库存明细")
@RestController
@RequestMapping("/yunze/inventorydetails")
public class YzCkglInventorydetailsController extends MyBaseController {

    @Resource
    private YzCkglInventorydetailsService yzCkglInventorydetailsService;

    /**
     * 导入
     */
    @Log(title = "库存明细", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('yunze:inventorydetails:importData')")
    @PostMapping(value = "/importData", produces = {"application/json;charset=utf-8"})
    public AjaxResult importData(MultipartFile file, @RequestParam Map<String, String> map) {
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

            return AjaxResult.success(yzCkglInventorydetailsService.ImportStock(file, map, User, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:inventorydetails:importData  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("上传文件地址 操作失败！");
    }

    /**
     * 查询库存明细列表
     */

    @PreAuthorize("@ss.hasPermi('yunze:inventorydetails:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglInventorydetailsService.inventorydetails(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:inventorydetails:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("库存明细列表 操作失败！");
    }


    /**
     * 导出
     *
     * @return
     */
    @Log(title = "库存明细导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('yunze:inventorydetails:Exportdetails')")
    @PostMapping(value = "/Exportdetails", produces = {"application/json;charset=utf-8"})
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

            return MyRetunSuccess(yzCkglInventorydetailsService.ExportDetails(Parammap, currentUser), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:inventorydetails:Exportdetails  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("库存明细导出 操作失败！");
    }


    /**
     * 导入至卡信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:inventorydetails:cardInfo')")
    @PostMapping(value = "/cardInfo", produces = {"application/json;charset=UTF-8"})
    public String Card(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();

            return MyRetunSuccess(yzCkglInventorydetailsService.ImportCard(Parammap, currentUser), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:inventorydetails:cardInfo  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("导入至卡信息 操作失败！");
    }

    /**
     * 划分 操作
     */
    @PreAuthorize("@ss.hasPermi('yunze:inventorydetails:cardDivide')")
    @PostMapping(value = "/cardDivide", produces = {"application/json;charset=UTF-8"})
    public String Divide(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));

            Object set_dept_id = Parammap.get("set_dept_id");
            Object set_user_id = Parammap.get("set_user_id");
            Object set_dept_name = Parammap.get("set_dept_name");
            Object set_user_name = Parammap.get("set_user_name");
            if (set_dept_id != null && set_dept_id.toString() != "" && set_user_id != null && set_user_id.toString() != ""
                    && set_dept_name != null && set_dept_name.toString() != "" && set_user_name != null && set_user_name.toString() != "") {

                LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                SysUser currentUser = loginUser.getUser();
                String agent_id1 = currentUser.getDeptId().toString();
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
                Parammap.put("TaskAgent_id",agent_id1);
                return MyRetunSuccess(yzCkglInventorydetailsService.detailsDividCard(Parammap), null);
            } else {
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:inventorydetails:cardDivide  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("划分 操作失败！");
    }

}






























