package com.yunze.task.yunze.outbound;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.house.OutboundRecordsMapper;
import com.yunze.common.utils.poi.ExcelUtil;
import com.yunze.common.core.domain.entity.OutboundRecordsImportTemplate;
import com.yunze.common.mapper.yunze.house.ModuleHouseMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.WriteCSV;
import com.yunze.common.core.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 出库记录导入任务消费者
 * 
 * @author yunze
 * @date 2025-01-11
 */
@Slf4j
@Component
public class OutboundImportTask {

    @Resource
    private OutboundRecordsMapper outboundRecordsMapper;

    @Resource
    private ModuleHouseMapper moduleHouseMapper;

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;

    @Resource
    private WriteCSV writeCSV;

    @Resource
    private RedisCache redisCache;

    private String Outcolumns[] = { "收货单位", "收货地址", "货品名称", "规格型号", "中箱号", "卷盘号", "单位", "数量", "发货日期",
            "收货联系电话", "备注", "快递单号", "供货单位", "供货联系人", "供货联系电话", "执行描述", "执行人", "执行结果" };
    private int OutSize = 50;// 每 50条数据输出一次

    /**
     * 批量导入出库记录
     * 
     * @param msg
     * @param channel
     */
    @SuppressWarnings("unchecked")
    @RabbitHandler
    @RabbitListener(queues = "admin_OutboundImportData_queue")
    public void OutboundImportData(String msg, Channel channel) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();// 项目根目录
            String ReadName = map.get("ReadName").toString();// 上传新文件名
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");// 参数
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");// 登录用户信息

            Map<String, Object> task_map = (Map<String, Object>) map.get("task_map");// 执行任务map
            String newName = map.get("newName").toString();//
            String UpdBackupName = map.get("UpdBackupName").toString();//

            String prefix = "admin_OutboundImportData_queue";
            // 执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + ReadName);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + ReadName, msg, 3, TimeUnit.SECONDS);// 3 秒缓存 避免 重复消费
                execution(filePath, ReadName, Pmap, User, task_map, newName, UpdBackupName);// 执行批量导入出库记录
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量导入出库记录 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 批量导入出库记录
     * 
     * @param msg
     * @param channel
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_OutboundImportData_queue")
    public void dlx_OutboundImportData(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();// 项目根目录
            String ReadName = map.get("ReadName").toString();// 上传新文件名
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");// 参数
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");// 登录用户信息

            Map<String, Object> task_map = (Map<String, Object>) map.get("task_map");// 执行任务map
            String newName = map.get("newName").toString();//
            String UpdBackupName = map.get("UpdBackupName").toString();//

            String prefix = "admin_OutboundImportData_queue";
            // 执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + ReadName);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + ReadName, msg, 3, TimeUnit.SECONDS);// 3 秒缓存 避免 重复消费
                execution(filePath, ReadName, Pmap, User, task_map, newName, UpdBackupName);// 执行批量导入出库记录
            }
        } catch (Exception e) {
            log.error(">>错误 - dlx_ 批量导入出库记录 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 变更 执行
     */
    public void execution(String filePath, String ReadName, Map<String, Object> Pmap, Map<String, Object> User,
            Map<String, Object> task_map, String newName, String UpdBackupName) {
        try {
            // 1.读取 上传文件
            String path = filePath + ReadName;
            ExcelUtil<OutboundRecordsImportTemplate> util = new ExcelUtil<>(OutboundRecordsImportTemplate.class);
            List<OutboundRecordsImportTemplate> list;
            try {
                list = util.importExcel(new FileInputStream(new File(path)));
            } catch (Exception e) {
                log.error("读取Excel文件失败: {}", e.getMessage(), e);
                return;
            }

            // 构建create_by字符串，处理User对象可能为null或字段缺失的情况
            String create_by = " [ 系统 ] - [ 导入任务 ] ";
            try {
                if (User != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> Dept = (Map<String, String>) User.get("dept");
                    String userName = (String) User.get("userName");
                    if (Dept != null && Dept.get("deptName") != null && userName != null) {
                        create_by = " [ " + Dept.get("deptName") + " ] - " + " [ " + userName + " ] ";
                    }
                }
            } catch (Exception e) {
                log.warn("构建create_by失败，使用默认值: {}", e.getMessage());
            }

            // 初始化任务URL，将在生成结果文件后更新
            String SaveUrl = task_map.get("url") != null ? String.valueOf(task_map.get("url")) : "";

            if (list != null && list.size() > 0) {
                int successCount = 0;
                int failCount = 0;
                List<Map<String, Object>> successList = new java.util.ArrayList<>();
                List<Map<String, Object>> failList = new java.util.ArrayList<>();

                log.info("开始处理 {} 条出库记录导入数据", list.size());

                for (int i = 0; i < list.size(); i++) {
                    OutboundRecordsImportTemplate template = list.get(i);
                    try {
                        // 验证中箱号或卷盘号是否存在于模组明细库中且状态为在库状态
                        String boxNumber = template.getBoxNumber();
                        String reelNumber = template.getReelNumber();

                        // 构建验证参数
                        Map<String, Object> validationParam = new HashMap<>();
                        validationParam.put("boxNumber", boxNumber);
                        validationParam.put("reelNumber", reelNumber);

                        // 处理多个值（逗号分隔）
                        if (boxNumber != null && boxNumber.contains(",")) {
                            validationParam.put("boxNumberList", java.util.Arrays.asList(boxNumber.split(",")));
                        } else if (boxNumber != null) {
                            validationParam.put("boxNumberList", java.util.Arrays.asList(boxNumber));
                        }

                        if (reelNumber != null && reelNumber.contains(",")) {
                            validationParam.put("reelNumberList", java.util.Arrays.asList(reelNumber.split(",")));
                        } else if (reelNumber != null) {
                            validationParam.put("reelNumberList", java.util.Arrays.asList(reelNumber));
                        }

                        // 验证所有中箱号和卷盘号是否都在库状态
                        // 分别验证中箱号和卷盘号
                        boolean allInStock = true;
                        String validationError = "";

                        // 验证中箱号
                        if (boxNumber != null && !boxNumber.trim().isEmpty()) {
                            String[] boxNumbers = boxNumber.contains(",") ? boxNumber.split(",")
                                    : new String[] { boxNumber };
                            for (String box : boxNumbers) {
                                if (box != null && !box.trim().isEmpty()) {
                                    Map<String, Object> boxValidationParam = new HashMap<>();
                                    boxValidationParam.put("boxNumber", box.trim());
                                    boxValidationParam.put("boxNumberList", java.util.Arrays.asList(box.trim()));

                                    Map<String, Object> boxResult = moduleHouseMapper
                                            .validateBoxOrReelInStock(boxValidationParam);
                                    if (boxResult == null) {
                                        allInStock = false;
                                        validationError = "中箱号 " + box.trim() + " 不存在或不在库状态";
                                        break;
                                    }
                                }
                            }
                        }

                        // 验证卷盘号
                        if (allInStock && reelNumber != null && !reelNumber.trim().isEmpty()) {
                            String[] reelNumbers = reelNumber.contains(",") ? reelNumber.split(",")
                                    : new String[] { reelNumber };
                            for (String reel : reelNumbers) {
                                if (reel != null && !reel.trim().isEmpty()) {
                                    Map<String, Object> reelValidationParam = new HashMap<>();
                                    reelValidationParam.put("reelNumber", reel.trim());
                                    reelValidationParam.put("reelNumberList", java.util.Arrays.asList(reel.trim()));

                                    Map<String, Object> reelResult = moduleHouseMapper
                                            .validateBoxOrReelInStock(reelValidationParam);
                                    if (reelResult == null) {
                                        allInStock = false;
                                        validationError = "卷盘号 " + reel.trim() + " 不存在或不在库状态";
                                        break;
                                    }
                                }
                            }
                        }

                        if (!allInStock) {
                            failCount++;
                            Map<String, Object> failItem = convertTemplateToMap(template);
                            failItem.put("数量", 0); // 验证失败，数量为0
                            failItem.put("执行描述", validationError);
                            failItem.put("执行人", create_by);
                            failItem.put("执行结果", "操作失败");
                            failList.add(failItem);
                            continue;
                        }

                        // 统计符合条件的中箱号和卷盘号的总数量
                        int totalCount = moduleHouseMapper.countModulesInStock(validationParam);
                        if (totalCount <= 0) {
                            failCount++;
                            Map<String, Object> failItem = convertTemplateToMap(template);
                            failItem.put("数量", 0); // 统计数量为0
                            failItem.put("执行描述", "统计数量为0");
                            failItem.put("执行人", create_by);
                            failItem.put("执行结果", "操作失败");
                            failList.add(failItem);
                            continue;
                        }

                        // 更新模组明细库中的状态为出库
                        Map<String, Object> updateParam = new HashMap<>();
                        // 从Pmap中获取user_id，因为User对象可能不包含user_id字段
                        Object userId = Pmap.get("user_id");
                        if (userId == null && User != null) {
                            // 如果Pmap中没有user_id，尝试从User对象中获取
                            userId = User.get("userId");
                        }
                        if (userId == null) {
                            // 如果都没有，使用默认值
                            userId = "1";
                        }
                        updateParam.put("updateBy", userId.toString());
                        if (boxNumber != null && boxNumber.contains(",")) {
                            updateParam.put("boxNumberList", java.util.Arrays.asList(boxNumber.split(",")));
                        } else if (boxNumber != null) {
                            updateParam.put("boxNumberList", java.util.Arrays.asList(boxNumber));
                        }

                        if (reelNumber != null && reelNumber.contains(",")) {
                            updateParam.put("reelNumberList", java.util.Arrays.asList(reelNumber.split(",")));
                        } else if (reelNumber != null) {
                            updateParam.put("reelNumberList", java.util.Arrays.asList(reelNumber));
                        }

                        int updateResult = moduleHouseMapper.updateModuleStatusToOutbound(updateParam);
                        if (updateResult <= 0) {
                            failCount++;
                            Map<String, Object> failItem = convertTemplateToMap(template);
                            failItem.put("数量", totalCount); // 更新失败，但数量已统计
                            failItem.put("执行描述", "更新模组状态失败");
                            failItem.put("执行人", create_by);
                            failItem.put("执行结果", "操作失败");
                            failList.add(failItem);
                            continue;
                        }

                        // 构建出库记录参数
                        Map<String, Object> recordMap = new HashMap<>();
                        recordMap.put("receivingUnit", template.getReceivingUnit());
                        recordMap.put("receivingAddress", template.getReceivingAddress());
                        recordMap.put("productName", template.getProductName());
                        recordMap.put("specificationModel", template.getSpecificationModel());
                        recordMap.put("boxNumber", boxNumber);
                        recordMap.put("reelNumber", reelNumber);
                        recordMap.put("unit", template.getUnit());
                        recordMap.put("quantity", totalCount);
                        recordMap.put("shippingDate", template.getShippingDate());
                        recordMap.put("contactPhone", template.getContactPhone());
                        recordMap.put("remarks", template.getRemarks());
                        recordMap.put("courierNumber", template.getCourierNumber());
                        recordMap.put("supplierUnit", template.getSupplierUnit());
                        recordMap.put("supplierContactPerson", template.getSupplierContactPerson());
                        recordMap.put("supplierContactPhone", template.getSupplierContactPhone());
                        recordMap.put("createBy", userId);
                        recordMap.put("updateBy", userId);
                        recordMap.put("createTime", new java.util.Date());
                        recordMap.put("updateTime", new java.util.Date());

                        // 插入出库记录
                        int result = outboundRecordsMapper.insertOutboundRecords(recordMap);
                        if (result > 0) {
                            successCount++;
                            Map<String, Object> successItem = convertTemplateToMap(template);
                            successItem.put("数量", totalCount); // 设置实际出库数量
                            successItem.put("执行描述", "导入成功，数量：" + totalCount);
                            successItem.put("执行人", create_by);
                            successItem.put("执行结果", "操作成功");
                            successList.add(successItem);
                        } else {
                            failCount++;
                            Map<String, Object> failItem = convertTemplateToMap(template);
                            failItem.put("数量", totalCount); // 插入失败，但数量已统计
                            failItem.put("执行描述", "插入出库记录失败");
                            failItem.put("执行人", create_by);
                            failItem.put("执行结果", "操作失败");
                            failList.add(failItem);
                        }

                    } catch (Exception e) {
                        failCount++;
                        Map<String, Object> failItem = convertTemplateToMap(template);
                        failItem.put("数量", 0); // 异常情况，数量为0
                        failItem.put("执行描述", e.getMessage());
                        failItem.put("执行人", create_by);
                        failItem.put("执行结果", "操作失败");
                        failList.add(failItem);
                        log.error("处理第{}行数据时发生异常: {}", i + 1, e.getMessage(), e);
                    }
                }

                // 合并所有记录（成功和失败）
                List<Map<String, Object>> allRecordsList = new java.util.ArrayList<>();
                allRecordsList.addAll(successList);
                allRecordsList.addAll(failList);

                // 输出结果文件 - 两个文件都包含所有记录
                if (allRecordsList.size() > 0) {
                    String[] outKeysWithResult = { "收货单位", "收货地址", "货品名称", "规格型号", "中箱号", "卷盘号", "单位", "数量", "发货日期",
                            "收货联系电话", "备注", "快递单号", "供货单位", "供货联系人", "供货联系电话", "执行描述", "执行人", "执行结果" };

                    // 生成主日志文件（包含所有记录）
                    writeCSV.OutCSVObj(allRecordsList, newName, Outcolumns, outKeysWithResult, null, OutSize);

                    // 生成备份日志文件（包含所有记录）
                    writeCSV.OutCSVObj(allRecordsList, UpdBackupName, Outcolumns, outKeysWithResult, null, OutSize);

                    // 添加两个日志路径到任务URL
                    String successPath = "/getcsv/" + newName + ".csv";
                    String failPath = "/getcsv/" + UpdBackupName + ".csv";

                    if (!SaveUrl.contains(successPath)) {
                        SaveUrl = (SaveUrl != null && SaveUrl.length() > 0) ? (SaveUrl + "," + successPath)
                                : successPath;
                    }
                    if (!SaveUrl.contains(failPath)) {
                        SaveUrl = (SaveUrl != null && SaveUrl.length() > 0) ? (SaveUrl + "," + failPath) : failPath;
                    }
                }

                // 更新任务URL，包含所有生成的结果文件
                try {
                    if (SaveUrl.length() > 0) {
                        task_map.put("url", SaveUrl);
                        yzExecutionTaskMapper.upd(task_map);
                        log.info("任务URL已更新: {}", SaveUrl);
                    }
                } catch (Exception e) {
                    log.error("更新任务URL失败: {}", e.getMessage(), e);
                }

                String userName = "未知用户";
                if (User != null && User.get("userName") != null) {
                    userName = User.get("userName").toString();
                }
                log.info("出库记录导入任务完成 - 成功: {}, 失败: {}, 用户: {}", successCount, failCount, userName);

                // 设置任务结束时间
                try {
                    yzExecutionTaskMapper.set_end_time(task_map);
                    log.info("任务结束时间已更新");
                } catch (Exception e) {
                    log.error("更新任务结束时间失败: {}", e.getMessage(), e);
                }

            } else {
                log.warn("导入数据为空");
                // 即使没有数据，也要更新任务URL和设置任务结束时间
                try {
                    if (SaveUrl.length() > 0) {
                        task_map.put("url", SaveUrl);
                        yzExecutionTaskMapper.upd(task_map);
                        log.info("任务URL已更新（无数据）: {}", SaveUrl);
                    }
                    yzExecutionTaskMapper.set_end_time(task_map);
                    log.info("任务结束时间已更新（无数据）");
                } catch (Exception e) {
                    log.error("更新任务状态失败: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("出库记录导入任务执行异常: {}", e.getMessage(), e);
            // 发生异常时也要设置任务结束时间
            try {
                yzExecutionTaskMapper.set_end_time(task_map);
                log.info("任务结束时间已更新（异常情况）");
            } catch (Exception ex) {
                log.error("更新任务结束时间失败: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 将模板对象转换为Map
     */
    private Map<String, Object> convertTemplateToMap(OutboundRecordsImportTemplate template) {
        Map<String, Object> map = new HashMap<>();
        map.put("收货单位", template.getReceivingUnit());
        map.put("收货地址", template.getReceivingAddress());
        map.put("货品名称", template.getProductName());
        map.put("规格型号", template.getSpecificationModel());
        map.put("中箱号", template.getBoxNumber());
        map.put("卷盘号", template.getReelNumber());
        map.put("单位", template.getUnit());
        map.put("数量", ""); // 数量字段将在处理时设置
        map.put("发货日期", template.getShippingDate());
        map.put("收货联系电话", template.getContactPhone());
        map.put("备注", template.getRemarks());
        map.put("快递单号", template.getCourierNumber());
        map.put("供货单位", template.getSupplierUnit());
        map.put("供货联系人", template.getSupplierContactPerson());
        map.put("供货联系电话", template.getSupplierContactPhone());
        return map;
    }
}