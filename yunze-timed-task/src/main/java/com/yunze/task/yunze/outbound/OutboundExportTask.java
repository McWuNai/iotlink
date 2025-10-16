package com.yunze.task.yunze.outbound;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.house.OutboundRecordsMapper;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 出库记录导出任务消费者
 * 
 * @author yunze
 * @date 2025-01-11
 */
@Slf4j
@Component
public class OutboundExportTask {

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

    private String OutboundRecordColumns[] = { "收货单位", "收货地址", "货品名称", "规格型号", "中箱号", "卷盘号", "单位", "数量", "发货日期",
            "收货联系电话", "备注", "快递单号", "供货单位", "供货联系人", "供货联系电话", "创建者", "创建时间", "更新者", "更新时间" };

    private String ModuleDetailColumns[] = { "序列号", "中箱号", "卷盘号", "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间",
            "客户销售订单号", "最终客户", "物流单号", "收件人", "客户收件人电话", "收件地址", "物料编码", "物料名称", "型号",
            "串号", "设备号", "SN", "原SN", "MAC", "ICCID", "IMSI", "固件版本", "状态" };

    private int OutSize = 50;// 每 50条数据输出一次

    /**
     * 单行出库记录导出
     * 
     * @param msg
     * @param channel
     */
    @SuppressWarnings("unchecked")
    @RabbitHandler
    @RabbitListener(queues = "admin_OutboundRecordExport_queue")
    public void OutboundRecordExport(String msg, Channel channel) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String outboundRecordId = map.get("outboundRecordId").toString();// 出库记录ID
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");// 参数
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");// 登录用户信息

            Map<String, Object> task_map = (Map<String, Object>) map.get("task_map");// 执行任务map
            String newName = map.get("newName").toString();//
            String UpdBackupName = map.get("UpdBackupName").toString();//

            String prefix = "admin_OutboundRecordExport_queue";
            // 执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + outboundRecordId);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + outboundRecordId, msg, 3, TimeUnit.SECONDS);// 3 秒缓存 避免 重复消费
                execution(outboundRecordId, Pmap, User, task_map, newName, UpdBackupName);// 执行单行出库记录导出
            }
        } catch (Exception e) {
            log.error(">>错误 - 单行出库记录导出 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 单行出库记录导出
     * 
     * @param msg
     * @param channel
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_OutboundRecordExport_queue")
    public void dlx_OutboundRecordExport(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> map = JSON.parseObject(msg);
            String outboundRecordId = map.get("outboundRecordId").toString();// 出库记录ID
            Map<String, Object> Pmap = (Map<String, Object>) map.get("map");// 参数
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");// 登录用户信息

            Map<String, Object> task_map = (Map<String, Object>) map.get("task_map");// 执行任务map
            String newName = map.get("newName").toString();//
            String UpdBackupName = map.get("UpdBackupName").toString();//

            String prefix = "admin_OutboundRecordExport_queue";
            // 执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object isExecute = redisCache.getCacheObject(prefix + ":" + outboundRecordId);
            if (isExecute == null) {
                redisCache.setCacheObject(prefix + ":" + outboundRecordId, msg, 3, TimeUnit.SECONDS);// 3 秒缓存 避免 重复消费
                execution(outboundRecordId, Pmap, User, task_map, newName, UpdBackupName);// 执行单行出库记录导出
            }
        } catch (Exception e) {
            log.error(">>错误 - dlx_ 单行出库记录导出 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 执行导出
     */
    public void execution(String outboundRecordId, Map<String, Object> Pmap, Map<String, Object> User,
            Map<String, Object> task_map, String newName, String UpdBackupName) {
        try {
            log.info("开始导出出库记录ID: {}", outboundRecordId);

            // 构建create_by字符串，处理User对象可能为null或字段缺失的情况
            String create_by = " [ 系统 ] - [ 导出任务 ] ";
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

            // 1. 查询出库记录基本信息
            Map<String, Object> outboundRecord = outboundRecordsMapper
                    .selectOutboundRecordsById(Long.parseLong(outboundRecordId));
            if (outboundRecord == null) {
                log.error("出库记录不存在，ID: {}", outboundRecordId);
                // 设置任务结束时间
                try {
                    yzExecutionTaskMapper.set_end_time(task_map);
                    log.info("任务结束时间已更新（记录不存在）");
                } catch (Exception e) {
                    log.error("更新任务结束时间失败: {}", e.getMessage(), e);
                }
                return;
            }

            // 2. 构建出库记录基本信息列表
            List<Map<String, Object>> outboundRecordList = new java.util.ArrayList<>();
            Map<String, Object> recordItem = new HashMap<>();
            recordItem.put("收货单位", outboundRecord.get("receiving_unit"));
            recordItem.put("收货地址", outboundRecord.get("receiving_address"));
            recordItem.put("货品名称", outboundRecord.get("product_name"));
            recordItem.put("规格型号", outboundRecord.get("specification_model"));
            recordItem.put("中箱号", outboundRecord.get("box_number"));
            recordItem.put("卷盘号", outboundRecord.get("reel_number"));
            recordItem.put("单位", outboundRecord.get("unit"));
            recordItem.put("数量", outboundRecord.get("quantity"));
            recordItem.put("发货日期", outboundRecord.get("shipping_date"));
            recordItem.put("收货联系电话", outboundRecord.get("contact_phone"));
            recordItem.put("备注", outboundRecord.get("remarks"));
            recordItem.put("快递单号", outboundRecord.get("courier_number"));
            recordItem.put("供货单位", outboundRecord.get("supplier_unit"));
            recordItem.put("供货联系人", outboundRecord.get("supplier_contact_person"));
            recordItem.put("供货联系电话", outboundRecord.get("supplier_contact_phone"));
            recordItem.put("创建者", outboundRecord.get("create_by"));
            recordItem.put("创建时间", outboundRecord.get("create_time"));
            recordItem.put("更新者", outboundRecord.get("update_by"));
            recordItem.put("更新时间", outboundRecord.get("update_time"));
            outboundRecordList.add(recordItem);

            // 3. 查询对应的模组明细数据
            String boxNumber = (String) outboundRecord.get("box_number");
            String reelNumber = (String) outboundRecord.get("reel_number");

            List<Map<String, Object>> moduleDetailList = new java.util.ArrayList<>();
            if ((boxNumber != null && !boxNumber.trim().isEmpty()) ||
                    (reelNumber != null && !reelNumber.trim().isEmpty())) {

                Map<String, Object> queryParam = new HashMap<>();
                if (boxNumber != null && !boxNumber.trim().isEmpty()) {
                    if (boxNumber.contains(",")) {
                        queryParam.put("boxNumberList", java.util.Arrays.asList(boxNumber.split(",")));
                    } else {
                        queryParam.put("boxNumberList", java.util.Arrays.asList(boxNumber));
                    }
                }
                if (reelNumber != null && !reelNumber.trim().isEmpty()) {
                    if (reelNumber.contains(",")) {
                        queryParam.put("reelNumberList", java.util.Arrays.asList(reelNumber.split(",")));
                    } else {
                        queryParam.put("reelNumberList", java.util.Arrays.asList(reelNumber));
                    }
                }

                // 查询模组明细数据
                List<Map<String, Object>> modules = moduleHouseMapper.selectModulesByBoxOrReel(queryParam);
                if (modules != null && !modules.isEmpty()) {
                    for (Map<String, Object> module : modules) {
                        Map<String, Object> moduleItem = new HashMap<>();
                        moduleItem.put("序列号", module.get("serial_number"));
                        moduleItem.put("中箱号", module.get("middle_box_number"));
                        moduleItem.put("卷盘号", module.get("reel_number"));
                        moduleItem.put("销售员", module.get("salesperson"));
                        moduleItem.put("客户名称", module.get("customer_name"));
                        moduleItem.put("销售订单号", module.get("sales_order_number"));
                        moduleItem.put("发货单单号", module.get("delivery_order_number"));
                        moduleItem.put("发货时间", module.get("delivery_time"));
                        moduleItem.put("客户销售订单号", module.get("customer_sales_order_number"));
                        moduleItem.put("最终客户", module.get("end_customer"));
                        moduleItem.put("物流单号", module.get("logistics_number"));
                        moduleItem.put("收件人", module.get("consignee"));
                        moduleItem.put("客户收件人电话", module.get("consignee_phone"));
                        moduleItem.put("收件地址", module.get("delivery_address"));
                        moduleItem.put("物料编码", module.get("material_code"));
                        moduleItem.put("物料名称", module.get("material_name"));
                        moduleItem.put("型号", module.get("model"));
                        moduleItem.put("串号", module.get("device_serial"));
                        moduleItem.put("设备号", module.get("device_id"));
                        moduleItem.put("SN", module.get("sn"));
                        moduleItem.put("原SN", module.get("original_sn"));
                        moduleItem.put("MAC", module.get("mac"));
                        moduleItem.put("ICCID", module.get("iccid"));
                        moduleItem.put("IMSI", module.get("imsi"));
                        moduleItem.put("固件版本", module.get("firmware_version"));
                        moduleItem.put("状态", module.get("status"));
                        moduleDetailList.add(moduleItem);
                    }
                }
            }

            // 4. 生成CSV文件（分别导出两个工作表）
            if (!outboundRecordList.isEmpty() || !moduleDetailList.isEmpty()) {
                try {
                    // 生成出库记录CSV文件
                    if (!outboundRecordList.isEmpty()) {
                        String[] outboundKeys = { "收货单位", "收货地址", "货品名称", "规格型号", "中箱号", "卷盘号", "单位", "数量", "发货日期",
                                "收货联系电话", "备注", "快递单号", "供货单位", "供货联系人", "供货联系电话", "创建者", "创建时间", "更新者", "更新时间" };

                        // 生成主文件
                        writeCSV.OutCSVObj(outboundRecordList, newName + "_出库记录", OutboundRecordColumns, outboundKeys,
                                null, OutSize);

                        // 生成备份文件
                        writeCSV.OutCSVObj(outboundRecordList, UpdBackupName + "_出库记录", OutboundRecordColumns,
                                outboundKeys, null, OutSize);
                    }

                    // 生成模组明细CSV文件
                    if (!moduleDetailList.isEmpty()) {
                        String[] moduleKeys = { "序列号", "中箱号", "卷盘号", "销售员", "客户名称", "销售订单号", "发货单单号", "发货时间",
                                "客户销售订单号", "最终客户", "物流单号", "收件人", "客户收件人电话", "收件地址", "物料编码", "物料名称", "型号",
                                "串号", "设备号", "SN", "原SN", "MAC", "ICCID", "IMSI", "固件版本", "状态" };

                        // 生成主文件
                        writeCSV.OutCSVObj(moduleDetailList, newName + "_模组明细", ModuleDetailColumns, moduleKeys, null,
                                OutSize);

                        // 生成备份文件
                        writeCSV.OutCSVObj(moduleDetailList, UpdBackupName + "_模组明细", ModuleDetailColumns, moduleKeys,
                                null, OutSize);
                    }

                    // 添加所有生成的文件路径到任务URL
                    if (!outboundRecordList.isEmpty()) {
                        String outboundSuccessPath = "/getcsv/" + newName + "_出库记录.csv";
                        String outboundFailPath = "/getcsv/" + UpdBackupName + "_出库记录.csv";

                        if (!SaveUrl.contains(outboundSuccessPath)) {
                            SaveUrl = (SaveUrl != null && SaveUrl.length() > 0) ? (SaveUrl + "," + outboundSuccessPath)
                                    : outboundSuccessPath;
                        }
                        if (!SaveUrl.contains(outboundFailPath)) {
                            SaveUrl = (SaveUrl != null && SaveUrl.length() > 0) ? (SaveUrl + "," + outboundFailPath)
                                    : outboundFailPath;
                        }
                    }

                    if (!moduleDetailList.isEmpty()) {
                        String moduleSuccessPath = "/getcsv/" + newName + "_模组明细.csv";
                        String moduleFailPath = "/getcsv/" + UpdBackupName + "_模组明细.csv";

                        if (!SaveUrl.contains(moduleSuccessPath)) {
                            SaveUrl = (SaveUrl != null && SaveUrl.length() > 0) ? (SaveUrl + "," + moduleSuccessPath)
                                    : moduleSuccessPath;
                        }
                        if (!SaveUrl.contains(moduleFailPath)) {
                            SaveUrl = (SaveUrl != null && SaveUrl.length() > 0) ? (SaveUrl + "," + moduleFailPath)
                                    : moduleFailPath;
                        }
                    }

                } catch (Exception e) {
                    log.error("生成CSV文件失败: {}", e.getMessage(), e);
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
            log.info("出库记录导出任务完成 - 出库记录: 1条, 模组明细: {}条, 用户: {}", moduleDetailList.size(), userName);

            // 设置任务结束时间
            try {
                yzExecutionTaskMapper.set_end_time(task_map);
                log.info("任务结束时间已更新");
            } catch (Exception e) {
                log.error("更新任务结束时间失败: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("出库记录导出任务执行异常: {}", e.getMessage(), e);
            // 发生异常时也要设置任务结束时间
            try {
                yzExecutionTaskMapper.set_end_time(task_map);
                log.info("任务结束时间已更新（异常情况）");
            } catch (Exception ex) {
                log.error("更新任务结束时间失败: {}", ex.getMessage(), ex);
            }
        }
    }

}
