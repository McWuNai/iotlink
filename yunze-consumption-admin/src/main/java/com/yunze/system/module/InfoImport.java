package com.yunze.system.module;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.core.domain.entity.ModuleHouse;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.house.ModuleHouseMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.ExcelConfig;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class InfoImport {
    @Resource
    private ModuleHouseMapper moduleHouseMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;

    private String Outcolumns[] = { "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间", "客户销售订单号", "最终客户", "物流单号", "收件人",
            "客户收件人电话", "收件地址", "物料编码", "物料名称", "型号", "序列号", "中箱号", "串号", "设备号", "SN", "原SN", "MAC", "ICCID", "IMSI",
            "卷盘号", "固件版本", "执行描述", "执行人", "执行结果" };
    private String keys[] = { "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间", "客户销售订单号", "最终客户", "物流单号", "收件人", "客户收件人电话",
            "收件地址", "物料编码", "物料名称", "型号", "序列号", "中箱号", "串号", "设备号", "SN", "原SN", "MAC", "ICCID", "IMSI", "卷盘号",
            "固件版本" };
    private int OutSize = 50;// 每 50条数据输出一次

    /**
     * 批量导入模组信息
     * 
     * @param msg
     * @param channel
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_ModuleImportData_queue")
    public void CardImportReplace(String msg, Channel channel) {
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

            String prefix = "admin_ModuleImportData_queue";
            // 执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + ReadName);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + ReadName, msg, 3, TimeUnit.SECONDS);// 3 秒缓存 避免 重复消费
                execution(filePath, ReadName, Pmap, User, task_map, newName, UpdBackupName);// 执行批量导入模组信息
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量批量导入模组信息 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 批量导入模组信息
     * 
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_ModuleImportData_queue")
    public void dlx_CardImportReplace(String msg, Channel channel) throws IOException {
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

            String prefix = "admin_ModuleImportData_queue";
            // 执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + ReadName);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + ReadName, msg, 3, TimeUnit.SECONDS);// 3 秒缓存 避免 重复消费
                execution(filePath, ReadName, Pmap, User, task_map, newName, UpdBackupName);// 执行批量批量导入模组信息
            }
        } catch (Exception e) {
            log.error(">>错误 - dlx_ 批量批量导入模组信息 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 变更 执行
     */
    public void execution(String filePath, String ReadName, Map<String, Object> Pmap, Map<String, Object> User,
            Map<String, Object> task_map, String newName, String UpdBackupName) {
        // 1.读取 上传文件
        String path = filePath + ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = { "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间", "客户销售订单号", "最终客户", "物流单号", "收件人", "客户收件人电话",
                "收件地址", "物料编码", "物料名称", "型号", "序列号", "中箱号", "串号", "设备号", "SN", "原SN", "MAC", "ICCID", "IMSI", "卷盘号",
                "固件版本" };
        List<Map<String, Object>> list = excelConfig.getExcelListMap(path, columns);
        Map<String, String> Dept = (Map<String, String>) User.get("dept");
        String create_by = " [ " + Dept.get("deptName") + " ] - " + " [ " + User.get("userName") + " ] ";

        String newExpandName = UUID.randomUUID().toString().replace("-", "") + "_ModuleImportData";

        // 原任务文件保存地址如需写回，请在需要时再启用

        // 确保将导入结果文件加入任务下载地址（/getcsv/{newName}.csv）
        try {
            String SaveUrl = task_map.get("url") != null ? String.valueOf(task_map.get("url")) : "";
            String resultPath = "/getcsv/" + newName + ".csv";
            if (!SaveUrl.contains(resultPath)) {
                SaveUrl = (SaveUrl != null && SaveUrl.length() > 0) ? (SaveUrl + "," + resultPath) : resultPath;
                task_map.put("url", SaveUrl);
                yzExecutionTaskMapper.upd(task_map);
            }
        } catch (Exception ignore) {
        }

        // 模组唯一性按 序列号 判定
        String lie = "序列号";

        if (list != null && list.size() > 0) {
            // 1) 批内重复（按 序列号）
            Map<String, Object> getNotRepeatingMap = Different.getNotRepeating(list, lie);
            List<Map<String, Object>> Rlist = (List<Map<String, Object>>) getNotRepeatingMap.get("Rlist");
            List<Map<String, Object>> Repeatlist = (List<Map<String, Object>>) getNotRepeatingMap.get("Repeatlist");
            if (Repeatlist.size() > 0) {
                // 输出重复记录
                String[] outKeysWithResult = { "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间", "客户销售订单号", "最终客户", "物流单号",
                        "收件人", "客户收件人电话", "收件地址", "物料编码", "物料名称", "型号", "序列号", "中箱号", "串号", "设备号", "SN", "原SN", "MAC",
                        "ICCID", "IMSI", "卷盘号", "固件版本", "message", "agentName", "result" };
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("message", " [" + lie + "] 重复！同批次无需多次导入");
                defOutcolumns.put("agentName", create_by);
                defOutcolumns.put("result", "操作失败");
                writeCSV.OutCSVObj(Repeatlist, newName, Outcolumns, outKeysWithResult, defOutcolumns, OutSize);
            }
            // 注意：不再用 Rlist 覆盖原 list，避免批内去重导致仅剩一条

            // 2) 数据库已存在（按 序列号）
            HashMap<String, Object> map = new HashMap<>();
            // 规范化序列号（不在此处去重，避免错误把多行归并一条）
            List<String> serialNumbers = new ArrayList<>();
            for (Map<String, Object> row : list) {
                String sv = normalizeSerial(row);
                if (sv.length() == 0) {
                    continue;
                }
                serialNumbers.add(sv);
            }
            // 仅用于 DB 查询的去重，保持 list 原样用于后续逐行插入
            List<String> serialNumbersDistinct = new ArrayList<>(new java.util.LinkedHashSet<>(serialNumbers));
            map.put("serialNumbers", serialNumbersDistinct);
            List<String> exists = serialNumbersDistinct.isEmpty() ? Collections.emptyList()
                    : moduleHouseMapper.isExistence(map);
            List<Map<String, Object>> existsList = new ArrayList<>();
            List<Map<String, Object>> toInsert = new ArrayList<>();
            // 精准区分：逐行按标准化序列号是否在 exists 里分流
            if (exists != null && exists.size() > 0) {
                for (Map<String, Object> row : list) {
                    String sv = normalizeSerial(row);
                    if (sv.length() == 0) {
                        continue;
                    }
                    if (exists.contains(sv)) {
                        existsList.add(row);
                    } else {
                        toInsert.add(row);
                    }
                }
            } else {
                toInsert = list;
            }

            if (existsList.size() > 0) {
                String[] outKeysWithResult = { "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间", "客户销售订单号", "最终客户", "物流单号",
                        "收件人", "客户收件人电话", "收件地址", "物料编码", "物料名称", "型号", "序列号", "中箱号", "串号", "设备号", "SN", "原SN", "MAC",
                        "ICCID", "IMSI", "卷盘号", "固件版本", "message", "agentName", "result" };
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("message", "[" + lie + "] 已存在，跳过导入");
                defOutcolumns.put("agentName", create_by);
                defOutcolumns.put("result", "操作失败");
                writeCSV.OutCSVObj(existsList, newName, Outcolumns, outKeysWithResult, defOutcolumns, OutSize);
            }

            // 3) 执行插入
            if (toInsert != null && toInsert.size() > 0) {
                List<Map<String, Object>> outArr = new ArrayList<>();
                for (int i = 0; i < toInsert.size(); i++) {
                    Map<String, Object> m = toInsert.get(i);
                    String msg = "";
                    String result = "操作成功";
                    try {
                        String serial = normalizeSerial(m);
                        if (serial == null || serial.isEmpty()) {
                            throw new IllegalArgumentException("序列号为空（或未找到列：序列号/SN）");
                        }
                        ModuleHouse entity = new ModuleHouse();
                        entity.setSerialNumber(serial);
                        entity.setSalesperson(Objects.toString(m.get("销售员"), ""));
                        entity.setCustomerName(Objects.toString(m.get("客户名称"), ""));
                        entity.setSalesOrderNumber(Objects.toString(m.get("销售订单号"), ""));
                        entity.setDeliveryOrderNumber(Objects.toString(m.get("发货单单号"), ""));
                        entity.setDeliveryTime(handleDateTimeField(Objects.toString(m.get("发货时间"), "")));
                        entity.setCustomerSalesOrderNumber(Objects.toString(m.get("客户销售订单号"), ""));
                        entity.setEndCustomer(Objects.toString(m.get("最终客户"), ""));
                        entity.setLogisticsNumber(Objects.toString(m.get("物流单号"), ""));
                        entity.setConsignee(Objects.toString(m.get("收件人"), ""));
                        entity.setConsigneePhone(Objects.toString(m.get("客户收件人电话"), ""));
                        entity.setDeliveryAddress(Objects.toString(m.get("收件地址"), ""));
                        entity.setMaterialCode(Objects.toString(m.get("物料编码"), ""));
                        entity.setMaterialName(Objects.toString(m.get("物料名称"), ""));
                        entity.setModel(Objects.toString(m.get("型号"), ""));
                        entity.setMiddleBoxNumber(Objects.toString(m.get("中箱号"), ""));
                        entity.setDeviceSerial(Objects.toString(m.get("串号"), ""));
                        entity.setDeviceID(Objects.toString(m.get("设备号"), ""));
                        entity.setSn(Objects.toString(m.get("SN"), ""));
                        entity.setOriginalSN(Objects.toString(m.get("原SN"), ""));
                        entity.setMac(Objects.toString(m.get("MAC"), ""));
                        entity.setIccid(Objects.toString(m.get("ICCID"), ""));
                        entity.setImsi(Objects.toString(m.get("IMSI"), ""));
                        entity.setReelNumber(Objects.toString(m.get("卷盘号"), ""));
                        entity.setFirmwareVersion(Objects.toString(m.get("固件版本"), ""));
                        entity.setCreateBy(Objects.toString(m.get("createBy"), Pmap.get("user_id").toString()));
                        entity.setUpdateBy(Objects.toString(m.get("updateBy"), Pmap.get("user_id").toString()));
                        moduleHouseMapper.ins(entity);
                    } catch (Exception e) {
                        msg = e.getMessage();
                        msg = msg != null && msg.length() > 100 ? msg.substring(0, 100) : msg;
                        result = "操作失败";
                    }
                    m.put("message", msg);
                    m.put("agentName", create_by);
                    m.put("result", result);
                    outArr.add(m);
                }
                String[] outKeysWithResult = { "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间", "客户销售订单号", "最终客户", "物流单号",
                        "收件人", "客户收件人电话", "收件地址", "物料编码", "物料名称", "型号", "序列号", "中箱号", "串号", "设备号", "SN", "原SN", "MAC",
                        "ICCID", "IMSI", "卷盘号", "固件版本", "message", "agentName", "result" };
                writeCSV.OutCSVObj(outArr, newName, Outcolumns, outKeysWithResult, null, OutSize);
            }

            yzExecutionTaskMapper.set_end_time(task_map);// 任务结束
        } else {
            log.error("admin_CardImportReplace_queue-消费者 上传表格无数据！无需执行");
        }
    }

    /**
     * 规范化序列号：
     * - 优先取列「序列号」，若不存在则回退取「SN」
     * - 去前后空白与所有空白字符（包含全角空格）
     * - 全角转半角，统一为字符串
     */
    private String normalizeSerial(Map<String, Object> row) {
        Object v = row.get("序列号");
        if (v == null || String.valueOf(v).trim().length() == 0) {
            v = row.get("SN");
        }
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v);
        // 去所有空白（含全角空格）
        s = s.replace('\u3000', ' ').replaceAll("\\s+", "").trim();
        // 全角转半角
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c >= 65281 && c <= 65374) {
                sb.append((char) (c - 65248));
            } else if (c == 12288) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 导出模组信息
     * 
     * @param msg
     * @param channel
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_ModuleHouseExport_queue")
    public void exportModuleHouse(String msg, Channel channel) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String type = map.get("type").toString();
            String newName = map.get("newName").toString();
            Map<String, Object> task_map = (Map<String, Object>) map.get("task_map");
            String create_by = map.get("create_by").toString();
            Map<String, Object> User = (Map<String, Object>) map.get("User");
            Map<String, Object> queryMap = (Map<String, Object>) map.get("map");

            String prefix = "admin_ModuleHouseExport_queue";
            // 执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + newName);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + newName, "1", 30, TimeUnit.MINUTES);
                exportModuleData(queryMap, User, newName, task_map, create_by);
            }
        } catch (Exception e) {
            log.error(">>错误 - 模组信息导出 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 导出模组数据
     */
    private void exportModuleData(Map<String, Object> queryMap, Map<String, Object> User, String newName,
            Map<String, Object> task_map, String create_by) {
        try {
            // 先查询总数
            Integer totalCount = moduleHouseMapper.selMapCount(queryMap);
            if (totalCount == null || totalCount == 0) {
                log.info("模组信息导出：没有数据可导出");
                yzExecutionTaskMapper.set_end_time(task_map);
                return;
            }

            log.info("模组信息导出开始，总数据量: {} 条", totalCount);

            // 导出列定义
            String[] exportColumns = { "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间", "客户销售订单号", "最终客户", "物流单号", "收件人",
                    "客户收件人电话", "收件地址", "物料编码", "物料名称", "型号", "序列号", "中箱号", "串号", "设备号", "SN", "原SN", "MAC", "ICCID",
                    "IMSI", "卷盘号", "固件版本" };

            String[] exportKeys = { "salesperson", "customer_name", "sales_order_number", "delivery_order_number",
                    "delivery_time",
                    "customer_sales_order_number", "end_customer", "logistics_number", "consignee", "consignee_phone",
                    "delivery_address", "material_code", "material_name", "model", "serial_number", "middle_box_number",
                    "device_serial", "device_id", "sn", "original_sn", "mac", "iccid", "imsi", "reel_number",
                    "firmware_version" };

            // 分批次处理参数
            int batchSize = 1000; // 每批次处理1000条数据
            int totalBatches = (totalCount + batchSize - 1) / batchSize; // 计算总批次数
            int exportedCount = 0;
            boolean isFirstBatch = true;

            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                try {
                    // 设置当前批次的分页参数
                    int startRow = batchIndex * batchSize;
                    queryMap.put("StarRow", startRow);
                    queryMap.put("PageSize", batchSize);

                    // 查询当前批次数据
                    List<Map<String, Object>> batchData = moduleHouseMapper.list(queryMap);

                    if (batchData != null && !batchData.isEmpty()) {
                        // 格式化日期字段
                        formatDateFieldsForExport(batchData);

                        // 写入CSV文件（第一次写入表头，后续追加）
                        if (isFirstBatch) {
                            writeCSV.Write(newName, batchData, exportColumns, null, exportKeys);
                            isFirstBatch = false;
                        } else {
                            // 追加写入，不包含表头
                            writeCSV.Write(newName, batchData, null, null, exportKeys);
                        }

                        exportedCount += batchData.size();
                        log.info("模组信息导出进度: {}/{} 批次完成，已导出 {} 条记录",
                                batchIndex + 1, totalBatches, exportedCount);
                    }

                    // 移除分页参数，为下一批次准备
                    queryMap.remove("StarRow");
                    queryMap.remove("PageSize");

                } catch (Exception batchException) {
                    log.error("第 {} 批次导出失败: {}", batchIndex + 1, batchException.getMessage(), batchException);
                    throw batchException;
                }
            }

            log.info("模组信息导出完成，共导出 {} 条记录", exportedCount);
            yzExecutionTaskMapper.set_end_time(task_map);

        } catch (Exception e) {
            log.error("模组信息导出异常: {}", e.getMessage(), e);
            yzExecutionTaskMapper.set_end_time(task_map);
        }
    }

    /**
     * 处理日期时间字段，将空字符串转换为null
     * 
     * @param dateTimeStr 日期时间字符串
     * @return 处理后的字符串，空字符串返回null
     */
    private String handleDateTimeField(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty() ||
                "null".equals(dateTimeStr) || "NULL".equals(dateTimeStr) ||
                "".equals(dateTimeStr.trim())) {
            return null;
        }
        return dateTimeStr.trim();
    }

    /**
     * 为导出格式化日期字段
     */
    private void formatDateFieldsForExport(List<Map<String, Object>> dataList) {
        for (Map<String, Object> item : dataList) {
            // 格式化发货时间字段
            if (item.containsKey("delivery_time")) {
                Object deliveryTime = item.get("delivery_time");
                if (deliveryTime != null) {
                    try {
                        if (deliveryTime instanceof java.sql.Timestamp) {
                            java.sql.Timestamp timestamp = (java.sql.Timestamp) deliveryTime;
                            String formatted = com.yunze.common.utils.DateUtils.parseDateToStr(
                                    com.yunze.common.utils.DateUtils.YYYY_MM_DD_HH_MM_SS, timestamp);
                            item.put("delivery_time", formatted);
                        } else if (deliveryTime instanceof Long) {
                            java.util.Date date = new java.util.Date((Long) deliveryTime);
                            String formatted = com.yunze.common.utils.DateUtils.parseDateToStr(
                                    com.yunze.common.utils.DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                            item.put("delivery_time", formatted);
                        }
                    } catch (Exception e) {
                        // 格式化失败时保持原值
                    }
                }
            }

            // 格式化其他日期字段
            String[] dateFields = { "create_time", "update_time" };
            for (String field : dateFields) {
                if (item.containsKey(field)) {
                    Object dateValue = item.get(field);
                    if (dateValue != null) {
                        try {
                            if (dateValue instanceof java.sql.Timestamp) {
                                java.sql.Timestamp timestamp = (java.sql.Timestamp) dateValue;
                                String formatted = com.yunze.common.utils.DateUtils.parseDateToStr(
                                        com.yunze.common.utils.DateUtils.YYYY_MM_DD_HH_MM_SS, timestamp);
                                item.put(field, formatted);
                            } else if (dateValue instanceof Long) {
                                java.util.Date date = new java.util.Date((Long) dateValue);
                                String formatted = com.yunze.common.utils.DateUtils.parseDateToStr(
                                        com.yunze.common.utils.DateUtils.YYYY_MM_DD_HH_MM_SS, date);
                                item.put(field, formatted);
                            }
                        } catch (Exception e) {
                            // 格式化失败时保持原值
                        }
                    }
                }
            }
        }
    }
}
