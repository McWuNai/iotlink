package com.yunze.web.controller.ydhouse;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.ModuleHouse;
import com.yunze.common.core.domain.entity.OutboundRecordsImportTemplate;
import com.yunze.common.core.domain.entity.SysDept;
import com.yunze.common.utils.yunze.Upload;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import com.yunze.system.service.house.IOutboundRecordsService;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.house.OutboundRecordsMapper;
import com.yunze.common.mapper.yunze.house.ModuleHouseMapper;
import com.yunze.web.core.config.MyBaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/house/module")
@Slf4j
public class ModuleYDController extends MyBaseController {
    @Resource
    private IModuleHouseService moduleHouseService;

    @Resource
    private IOutboundRecordsService outboundRecordsService;

    @Autowired
    private TokenService tokenService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;

    @Resource
    private OutboundRecordsMapper outboundRecordsMapper;

    @Resource
    private ModuleHouseMapper moduleHouseMapper;

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

    /**
     * 出库记录-导入模板
     */
    @GetMapping("/outbound/importTemplate")
    public AjaxResult outboundImportTemplate() {
        ExcelUtil<OutboundRecordsImportTemplate> util = new ExcelUtil<OutboundRecordsImportTemplate>(
                OutboundRecordsImportTemplate.class);
        return util.importTemplateExcel("-出库记录导入");
    }

    /**
     * 导入出库记录数据（异步处理）
     */
    @PostMapping("/outbound/importData")
    public AjaxResult outboundImportData(MultipartFile file) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
        SysUser User = loginUser.getUser();
        Parammap.put("User", User);
        Parammap.put("agent_id", User.getDeptId());
        Parammap.put("user_id", User.getUserId());

        try {
            String filename = file.getOriginalFilename();
            String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
            String flieUrlRx = "/upload/importOutboundRecords/";
            ReadName = flieUrlRx + ReadName;

            SysDept Dept = User.getDept();
            String create_by = " [ " + Dept.getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
            String task_name = "导入操作 [批量导入出库记录] ";
            String newName = UUID.randomUUID().toString().replace("-", "") + "_OutboundRecordsImport";
            String UpdBackupName = UUID.randomUUID().toString().replace("-", "") + "_OutboundRecordsImportBackup";

            String SaveUrl = "/getcsv/" + newName + ".csv";
            SaveUrl += ",/getcsv/" + UpdBackupName + ".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth", create_by);
            task_map.put("task_name", task_name);
            task_map.put("url", SaveUrl);
            task_map.put("agent_id", User.getDeptId());
            task_map.put("type", "41"); // 出库记录导入类型
            yzExecutionTaskMapper.add(task_map);// 添加执行任务表

            try {
                // 获取当前项目的工作路径
                File file2 = new File("");
                String filePath = file2.getCanonicalPath();
                File newFile = new File(filePath + ReadName);
                File Url = new File(filePath + flieUrlRx + "/1.txt");// tomcat 生成路径
                Upload.mkdirsmy(Url);
                file.transferTo(newFile);

                // 创建路由绑定生产队列发送消息
                String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_OutboundImportData_queue",
                        addOrder_routingKey = "admin.OutboundImportData.queue";
                try {
                    Map<String, Object> start_type = new HashMap<>();
                    start_type.put("filePath", filePath);// 项目根目录
                    start_type.put("ReadName", ReadName);// 上传新文件名
                    start_type.put("map", Parammap);// 参数
                    start_type.put("task_map", task_map);// 参数
                    start_type.put("newName", newName);// 参数
                    start_type.put("UpdBackupName", UpdBackupName);// 参数

                    rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey,
                            JSON.toJSONString(start_type),
                            message -> {
                                // 设置消息过期时间 60 分钟 过期
                                message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                                return message;
                            });
                } catch (Exception e) {
                    log.error("批量导入出库记录 生产指令 失败: {}", e.getMessage());
                    return AjaxResult.error("批量导入出库记录 生产指令 操作失败！");
                }
            } catch (Exception e) {
                log.error("上传excel异常: {}", e.getMessage());
                return AjaxResult.error("上传excel异常");
            }

            return AjaxResult.success("批量导入出库记录 指令 已发送，详细信息请在 【执行日志管理】查询！");

        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            log.error("出库记录导入任务提交失败: {}", e.getMessage(), e);
            logger.error("<br/> /house/module/outbound/importData  <br/> ip =  " + ip + " <br/> ",
                    e.getCause() != null ? e.getCause().toString() : e.getMessage());
        }
        return AjaxResult.error("导入任务提交失败！");
    }

    /**
     * 查询出库记录列表
     */
    @PostMapping(value = "/outbound/list", produces = { "application/json;charset=UTF-8" })
    @PreAuthorize("@ss.hasPermi('house:module:outbound:list')")
    public String getOutboundRecordsList(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            // 处理日期范围查询
            if (Parammap.get("shippingDateStart") != null && Parammap.get("shippingDateEnd") != null) {
                Parammap.put("shippingDateStart", Parammap.get("shippingDateStart"));
                Parammap.put("shippingDateEnd", Parammap.get("shippingDateEnd"));
            }

            // 处理通用查询条件
            if (Parammap.get("type") != null && Parammap.get("value") != null) {
                Parammap.put("type", Parammap.get("type"));
                Parammap.put("value", Parammap.get("value"));
            }

            Map<String, Object> result = outboundRecordsService.selectOutboundRecordsListWithPage(Parammap);

            // 格式化日期字段
            formatOutboundDateFields(result);

            return MyRetunSuccess(result, null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> /house/module/outbound/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause() != null ? e.getCause().toString() : e.getMessage());
        }
        return Myerr("获取出库记录列表 操作失败!");
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
     * 新增出库记录
     */
    @Log(title = "出库记录", businessType = BusinessType.INSERT)
    @PostMapping(value = "/outbound/add", produces = { "application/json;charset=UTF-8" })
    public String addOutboundRecord(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            // 添加调试信息
            log.info("接收到的原始数据: {}", Pstr);
            log.info("数据长度: {}", Pstr != null ? Pstr.length() : 0);

            // 尝试解密
            try {
                // 验证Base64格式
                if (Pstr != null && !Pstr.matches("^[A-Za-z0-9+/]*={0,2}$")) {
                    log.error("数据不是有效的Base64格式: {}", Pstr);
                    throw new IllegalArgumentException("数据不是有效的Base64格式");
                }

                // 计算Base64解码后的长度
                if (Pstr != null) {
                    int padding = 0;
                    if (Pstr.endsWith("=="))
                        padding = 2;
                    else if (Pstr.endsWith("="))
                        padding = 1;
                    int decodedLength = (Pstr.length() * 3) / 4 - padding;
                    log.info("Base64解码后长度: {}", decodedLength);
                    log.info("是否为16的倍数: {}", decodedLength % 16 == 0);
                }

                Pstr = AesEncryptUtil.desEncrypt(Pstr);
                log.info("解密成功，解密后数据: {}", Pstr);
            } catch (Exception decryptException) {
                log.error("AES解密失败: {}", decryptException.getMessage());
                log.error("解密异常详情: ", decryptException);
                // 如果解密失败，尝试直接解析JSON（可能是未加密的数据）
                if (Pstr != null && Pstr.trim().startsWith("{")) {
                    log.info("尝试直接解析JSON数据");
                } else {
                    throw decryptException;
                }
            }

            Parammap.putAll(JSON.parseObject(Pstr));

            // 获取当前登录用户信息
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();

            // 验证中箱号或卷盘号是否存在于模组明细库中且状态为在库状态
            String boxNumber = (String) Parammap.get("boxNumber");
            String reelNumber = (String) Parammap.get("reelNumber");

            log.info("开始验证中箱号[{}]和卷盘号[{}]", boxNumber, reelNumber);
            Map<String, Object> validationResult = moduleHouseService.validateBoxOrReelInStock(boxNumber, reelNumber);

            if (!(Boolean) validationResult.get("valid")) {
                log.warn("验证失败: {}", validationResult.get("message"));
                return Myerr(validationResult.get("message").toString());
            }

            log.info("验证通过: {}", validationResult.get("message"));

            // 验证通过后，统计符合条件的中箱号和卷盘号的总数量
            log.info("开始统计符合条件的中箱号和卷盘号总数量");
            Map<String, Object> countResult = moduleHouseService.countModulesInStock(boxNumber, reelNumber);

            if (!(Boolean) countResult.get("success")) {
                log.error("统计数量失败: {}", countResult.get("message"));
                return Myerr("统计数量失败: " + countResult.get("message").toString());
            }

            int totalCount = (Integer) countResult.get("count");
            log.info("统计完成，总数量: {}", totalCount);

            // 验证通过后，更新模组明细库中的状态为出库
            log.info("开始更新模组明细库状态为出库");
            Map<String, Object> updateResult = moduleHouseService.updateModuleStatusToOutbound(
                    boxNumber, reelNumber, currentUser.getUserId().toString());

            if (!(Boolean) updateResult.get("success")) {
                log.error("更新模组明细库状态失败: {}", updateResult.get("message"));
                return Myerr("更新模组明细库状态失败: " + updateResult.get("message").toString());
            }

            log.info("模组明细库状态更新成功: {}", updateResult.get("message"));

            // 设置创建人信息和自动统计的数量
            Parammap.put("createBy", currentUser.getUserId());
            Parammap.put("updateBy", currentUser.getUserId());
            Parammap.put("createTime", new java.util.Date());
            Parammap.put("updateTime", new java.util.Date());
            Parammap.put("quantity", totalCount); // 自动设置数量为统计的总数

            // 执行插入操作
            log.info("准备执行数据库插入操作，参数: {}", Parammap);
            int result = outboundRecordsService.insertOutboundRecords(Parammap);
            log.info("数据库插入操作返回值: {}", result);

            if (result > 0) {
                log.info("出库记录新增成功");
                return MyRetunSuccess("", "出库记录新增成功");
            } else {
                log.warn("出库记录新增失败，返回值: {}", result);
                return Myerr("出库记录新增失败");
            }

        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            log.error("出库记录新增异常: {}", e.getMessage(), e);
            logger.error("<br/> /house/module/outbound  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause() != null ? e.getCause().toString() : e.getMessage());
        }
        return Myerr("出库记录新增操作失败!");
    }

    /**
     * 单行出库记录导出
     */
    @Log(title = "出库记录导出", businessType = BusinessType.EXPORT)
    @PostMapping(value = "/outbound/export", produces = { "application/json;charset=UTF-8" })
    public String exportOutboundRecord(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            // 获取当前登录用户信息
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            Parammap.put("User", currentUser);
            Parammap.put("agent_id", currentUser.getDeptId());
            Parammap.put("user_id", currentUser.getUserId());

            // 验证必填参数
            Object id = Parammap.get("id");
            if (id == null || id.toString().trim().isEmpty()) {
                return Myerr("缺少出库记录ID");
            }

            // 创建执行任务记录
            SysDept Dept = currentUser.getDept();
            String create_by = " [ " + Dept.getDeptName() + " ] - " + " [ " + currentUser.getUserName() + " ] ";
            String task_name = "导出操作 [单行出库记录导出] ";
            String newName = UUID.randomUUID().toString().replace("-", "") + "_OutboundRecordExport";
            String UpdBackupName = UUID.randomUUID().toString().replace("-", "") + "_OutboundRecordExportBackup";

            String SaveUrl = "/getcsv/" + newName + "_出库记录.csv";
            SaveUrl += ",/getcsv/" + UpdBackupName + "_出库记录.csv";
            SaveUrl += ",/getcsv/" + newName + "_模组明细.csv";
            SaveUrl += ",/getcsv/" + UpdBackupName + "_模组明细.csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth", create_by);
            task_map.put("task_name", task_name);
            task_map.put("url", SaveUrl);
            task_map.put("agent_id", currentUser.getDeptId());
            task_map.put("type", "42"); // 出库记录导出类型
            yzExecutionTaskMapper.add(task_map);// 添加执行任务表

            // 创建路由绑定生产队列发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_OutboundRecordExport_queue",
                    addOrder_routingKey = "admin.OutboundRecordExport.queue";
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("outboundRecordId", id.toString());// 出库记录ID
                start_type.put("map", Parammap);// 参数
                start_type.put("task_map", task_map);// 执行任务参数
                start_type.put("newName", newName);// 文件名
                start_type.put("UpdBackupName", UpdBackupName);// 备份文件名

                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey,
                        JSON.toJSONString(start_type),
                        message -> {
                            // 设置消息过期时间 60 分钟 过期
                            message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                            return message;
                        });
            } catch (Exception e) {
                log.error("单行出库记录导出 生产指令 失败: {}", e.getMessage());
                return Myerr("单行出库记录导出 生产指令 操作失败！");
            }

            return MyRetunSuccess("", "单行出库记录导出 指令 已发送，详细信息请在 【执行日志管理】查询！");

        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            log.error("单行出库记录导出任务提交失败: {}", e.getMessage(), e);
            logger.error("<br/> /house/module/outbound/export  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause() != null ? e.getCause().toString() : e.getMessage());
        }
        return Myerr("单行出库记录导出任务提交失败！");
    }

    /**
     * 删除出库记录
     */
    @Log(title = "出库记录", businessType = BusinessType.DELETE)
    @PostMapping(value = "/outbound/del", produces = { "application/json;charset=UTF-8" })
    public String delOutboundRecord(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            // 获取当前登录用户信息
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            Parammap.put("User", currentUser);
            Parammap.put("agent_id", currentUser.getDeptId());
            Parammap.put("user_id", currentUser.getUserId());

            // 验证必填参数
            Object id = Parammap.get("id");
            if (id == null) {
                return Myerr("缺少出库记录ID");
            }

            // 处理单个ID或ID数组
            List<Long> idList = new ArrayList<>();
            if (id instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> idObjList = (List<Object>) id;
                for (Object idObj : idObjList) {
                    if (idObj != null) {
                        idList.add(Long.parseLong(idObj.toString()));
                    }
                }
            } else {
                idList.add(Long.parseLong(id.toString()));
            }

            if (idList.isEmpty()) {
                return Myerr("出库记录ID不能为空");
            }

            // 1. 批量获取出库记录详细数据
            List<Map<String, Object>> outboundRecords = new ArrayList<>();
            for (Long recordId : idList) {
                Map<String, Object> record = outboundRecordsMapper.selectOutboundRecordsById(recordId);
                if (record != null) {
                    outboundRecords.add(record);
                }
            }

            if (outboundRecords.isEmpty()) {
                return Myerr("未找到要删除的出库记录");
            }

            // 2. 根据中箱号、卷盘号将库存明细表状态改为0（在库）
            for (Map<String, Object> record : outboundRecords) {
                String boxNumber = (String) record.get("box_number");
                String reelNumber = (String) record.get("reel_number");

                if ((boxNumber != null && !boxNumber.trim().isEmpty()) ||
                        (reelNumber != null && !reelNumber.trim().isEmpty())) {

                    Map<String, Object> updateParam = new HashMap<>();
                    updateParam.put("status", "0"); // 设置为在库状态
                    updateParam.put("update_by", currentUser.getUserName());
                    updateParam.put("update_time", new java.util.Date());

                    if (boxNumber != null && !boxNumber.trim().isEmpty()) {
                        if (boxNumber.contains(",")) {
                            updateParam.put("boxNumberList", java.util.Arrays.asList(boxNumber.split(",")));
                        } else {
                            updateParam.put("boxNumberList", java.util.Arrays.asList(boxNumber));
                        }
                    }
                    if (reelNumber != null && !reelNumber.trim().isEmpty()) {
                        if (reelNumber.contains(",")) {
                            updateParam.put("reelNumberList", java.util.Arrays.asList(reelNumber.split(",")));
                        } else {
                            updateParam.put("reelNumberList", java.util.Arrays.asList(reelNumber));
                        }
                    }

                    // 更新库存明细表状态
                    moduleHouseMapper.updateModuleStatusToInStock(updateParam);
                }
            }

            // 3. 删除出库记录表中的数据
            int deleteCount = 0;
            for (Long recordId : idList) {
                int result = outboundRecordsMapper.deleteOutboundRecordsById(recordId);
                if (result > 0) {
                    deleteCount++;
                }
            }

            if (deleteCount > 0) {
                return MyRetunSuccess("", "成功删除 " + deleteCount + " 条出库记录，相关库存已恢复为在库状态");
            } else {
                return Myerr("删除出库记录失败");
            }

        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            log.error("删除出库记录失败: {}", e.getMessage(), e);
            logger.error("<br/> /house/module/outbound/del  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause() != null ? e.getCause().toString() : e.getMessage());
        }
        return Myerr("删除出库记录失败！");
    }

    /**
     * 修改出库记录
     */
    @Log(title = "出库记录", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/outbound/update", produces = { "application/json;charset=UTF-8" })
    public String updateOutboundRecord(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");// 转义 /
        }
        try {
            // 调试日志
            log.info("接收到的原始数据(更新): {}", Pstr);
            log.info("数据长度(更新): {}", Pstr != null ? Pstr.length() : 0);

            // 解密处理（与新增一致的容错策略）
            try {
                if (Pstr != null && !Pstr.matches("^[A-Za-z0-9+/]*={0,2}$")) {
                    log.error("数据不是有效的Base64格式(更新): {}", Pstr);
                    throw new IllegalArgumentException("数据不是有效的Base64格式");
                }
                if (Pstr != null) {
                    int padding = 0;
                    if (Pstr.endsWith("=="))
                        padding = 2;
                    else if (Pstr.endsWith("="))
                        padding = 1;
                    int decodedLength = (Pstr.length() * 3) / 4 - padding;
                    log.info("Base64解码后长度(更新): {}", decodedLength);
                    log.info("是否为16的倍数(更新): {}", decodedLength % 16 == 0);
                }
                Pstr = AesEncryptUtil.desEncrypt(Pstr);
                log.info("解密成功(更新)，解密后数据: {}", Pstr);
            } catch (Exception decryptException) {
                log.error("AES解密失败(更新): {}", decryptException.getMessage());
                log.error("解密异常详情(更新): ", decryptException);
                if (Pstr != null && Pstr.trim().startsWith("{")) {
                    log.info("尝试直接解析JSON数据(更新)");
                } else {
                    throw decryptException;
                }
            }

            Parammap.putAll(JSON.parseObject(Pstr));

            // 必填参数校验：id
            Object id = Parammap.get("id");
            if (id == null || id.toString().trim().isEmpty()) {
                return Myerr("缺少记录ID");
            }

            // 数量由后端统计，不接受前端传入
            Parammap.remove("quantity");
            // 中箱号/卷盘号为只读，不允许修改（移除以避免被更新）
            Parammap.remove("boxNumber");
            Parammap.remove("reelNumber");

            // 获取当前登录用户信息
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();

            // 只更新维护信息
            Parammap.put("updateBy", currentUser.getUserId());
            // 数量保持不变，不做任何写入

            log.info("准备执行数据库更新操作，参数: {}", Parammap);
            int result = outboundRecordsService.updateOutboundRecords(Parammap);
            log.info("数据库更新操作返回值: {}", result);

            if (result > 0) {
                log.info("出库记录更新成功");
                return MyRetunSuccess("", "出库记录更新成功");
            } else {
                log.warn("出库记录更新失败，返回值: {}", result);
                return Myerr("出库记录更新失败");
            }

        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            log.error("出库记录更新异常: {}", e.getMessage(), e);
            logger.error("<br/> /house/module/outbound/update  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",
                    e.getCause() != null ? e.getCause().toString() : e.getMessage());
        }
        return Myerr("出库记录更新操作失败!");
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

    /**
     * 格式化出库记录日期字段，将时间戳转换为yyyy-MM-dd HH:mm:ss格式
     */
    private void formatOutboundDateFields(Map<String, Object> result) {
        if (result == null || !result.containsKey("Data")) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("Data");
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : dataList) {
            // 格式化发货日期字段
            if (item.containsKey("shipping_date")) {
                Object shippingDate = item.get("shipping_date");
                if (shippingDate != null) {
                    try {
                        if (shippingDate instanceof Timestamp) {
                            // Timestamp格式
                            Timestamp timestamp = (Timestamp) shippingDate;
                            String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, timestamp);
                            item.put("shipping_date", formatted);
                        } else if (shippingDate instanceof Long) {
                            // 时间戳格式
                            Date date = new Date((Long) shippingDate);
                            String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                            item.put("shipping_date", formatted);
                        } else if (shippingDate instanceof String) {
                            // 字符串格式，尝试解析
                            String timeStr = shippingDate.toString();
                            if (timeStr.matches("\\d+")) {
                                // 纯数字字符串，当作时间戳处理
                                Date date = new Date(Long.parseLong(timeStr));
                                String formatted = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                                item.put("shipping_date", formatted);
                            }
                        }
                    } catch (Exception e) {
                        // 格式化失败时保持原值
                        log.warn("发货日期格式化失败: {}", shippingDate, e);
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
