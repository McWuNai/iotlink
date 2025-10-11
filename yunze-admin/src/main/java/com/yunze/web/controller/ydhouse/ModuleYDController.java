package com.yunze.web.controller.ydhouse;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.ModuleHouse;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.poi.ExcelUtil;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import java.util.Date;
import java.sql.Timestamp;
import com.yunze.common.utils.DateUtils;
import com.yunze.common.annotation.Log;
import com.yunze.common.enums.BusinessType;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.house.IModuleHouseService;
import com.yunze.web.core.config.MyBaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/house/module")
@Slf4j
public class ModuleYDController extends MyBaseController {
    @Resource
    private IModuleHouseService moduleHouseService;

    @Autowired
    private TokenService tokenService;

    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    @PreAuthorize("@ss.hasPermi('house:module:list')")
    public String getList(@RequestBody String Pstr) {

        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            Map<String, Object> result = moduleHouseService.list(Parammap);

            // 格式化日期字段
            formatDateFields(result);

            return MyRetunSuccess(result, null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /house/module/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause().toString());
        }
        return Myerr("获取模组信息 操作失败!");
    }

    @PostMapping("/del")
    public String del(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.put("iccid", Pstr);
            moduleHouseService.del(Parammap);
            return MyRetunSuccess("", "删除成功");
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /house/module/del  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause().toString());
        }
        return Myerr("删除失败!");
    }

    @GetMapping("/importTemplate")
    public AjaxResult importTemplate() {
        ExcelUtil<ModuleHouse> util = new ExcelUtil<ModuleHouse>(ModuleHouse.class);
        return util.importTemplateExcel("-移动模组导入");
    }

    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
        SysUser User = loginUser.getUser();
        Parammap.put("User", User);
        Parammap.put("agent_id", User.getDeptId());
        Parammap.put("user_id", User.getUserId());
        ExcelUtil<ModuleHouse> util = new ExcelUtil<>(ModuleHouse.class);
        // List<ModuleHouse> List = util.importExcel(file.getInputStream());;
        try {

            return AjaxResult.success(moduleHouseService.importModule(file, Parammap));
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:card:importData  <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return AjaxResult.error("批量导入模组信息 操作失败！");
    }

    /**
     * 导出模组信息
     */
    @Log(title = "模组信息", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('house:module:export')")
    @PostMapping(value = "/export", produces = { "application/json;charset=utf-8" })
    public String export(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();

            return MyRetunSuccess(moduleHouseService.exportModule(Parammap, currentUser), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /house/module/export  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause().toString());
        }
        return Myerr("导出模组信息 操作失败！");
    }

    /**
     * 格式化日期字段，将时间戳转换为yyyy-MM-dd HH:mm:ss格式（使用项目内DateUtils）
     */
    private void formatDateFields(Map<String, Object> result) {
        if (result == null || !result.containsKey("Data")) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("Data");
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : dataList) {
            // 格式化发货时间字段
            if (item.containsKey("delivery_time")) {
                Object deliveryTime = item.get("delivery_time");
                if (deliveryTime != null) {
                    try {
                        if (deliveryTime instanceof Timestamp) {
                            // Timestamp格式
                            Timestamp timestamp = (Timestamp) deliveryTime;
                            String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, timestamp);
                            item.put("delivery_time", formatted);
                        } else if (deliveryTime instanceof Long) {
                            // 时间戳格式
                            Date date = new Date((Long) deliveryTime);
                            String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                            item.put("delivery_time", formatted);
                        } else if (deliveryTime instanceof String) {
                            // 字符串格式，尝试解析
                            String timeStr = deliveryTime.toString();
                            if (timeStr.matches("\\d+")) {
                                // 纯数字字符串，当作时间戳处理
                                Date date = new Date(Long.parseLong(timeStr));
                                String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                                item.put("delivery_time", formatted);
                            }
                        }
                    } catch (Exception e) {
                        // 格式化失败时保持原值
                        log.warn("发货时间格式化失败: {}", deliveryTime, e);
                    }
                }
            }

            // 格式化其他可能的日期字段
            String[] dateFields = { "create_time", "update_time" };
            for (String field : dateFields) {
                if (item.containsKey(field)) {
                    Object dateValue = item.get(field);
                    if (dateValue != null) {
                        try {
                            if (dateValue instanceof Timestamp) {
                                // Timestamp格式
                                Timestamp timestamp = (Timestamp) dateValue;
                                String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, timestamp);
                                item.put(field, formatted);
                            } else if (dateValue instanceof Long) {
                                Date date = new Date((Long) dateValue);
                                String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                                item.put(field, formatted);
                            } else if (dateValue instanceof String) {
                                String timeStr = dateValue.toString();
                                if (timeStr.matches("\\d+")) {
                                    Date date = new Date(Long.parseLong(timeStr));
                                    String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                                    item.put(field, formatted);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("日期字段 {} 格式化失败: {}", field, dateValue, e);
                        }
                    }
                }
            }
        }
    }
}
