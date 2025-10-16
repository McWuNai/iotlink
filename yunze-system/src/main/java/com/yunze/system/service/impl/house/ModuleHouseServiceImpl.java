package com.yunze.system.service.impl.house;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysDept;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.house.ModuleHouseMapper;
import com.yunze.common.utils.StringUtils;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.system.service.house.IModuleHouseService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ModuleHouseServiceImpl implements IModuleHouseService {

    @Resource
    private ModuleHouseMapper moduleHouseMapper;

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public Map<String, Object> list(Map<String, Object> map) {
        Map<String, Object> rMap = new HashMap<>();

        // 获取分页参数
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 1;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;

        // 查询总数
        Integer total = moduleHouseMapper.selMapCount(map);
        total = total != null ? total : 0;

        // 使用PageUtil处理分页
        PageUtil pu = new PageUtil(total, currenPage, pageSize);
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());

        // 查询数据
        List<Map<String, Object>> list = moduleHouseMapper.list(map);

        // 返回结果
        rMap.put("Pu", pu);
        rMap.put("Data", list);
        rMap.put("total", total);

        return rMap;
    }

    @Override
    public void del(Map<String, Object> map) {
        moduleHouseMapper.del(map);
    }

    @Override
    public String importModule(MultipartFile file, HashMap<String, Object> map) {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/upload/importModuleInfo/";
        ReadName = flieUrlRx + ReadName;
        SysUser User = (SysUser) map.get("User");// 登录用户信息
        SysDept Dept = User.getDept();
        String create_by = " [ " + Dept.getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
        String task_name = "导入操作 [批量导入模组信息] ";
        String newName = UUID.randomUUID().toString().replace("-", "") + "_ModuleInfoImport";
        String UpdBackupName = UUID.randomUUID().toString().replace("-", "") + "_ModuleInfoImportBackup";// 设置分组备注前信息备份名称

        String SaveUrl = "/getcsv/" + newName + ".csv";
        SaveUrl += ",/getcsv/" + UpdBackupName + ".csv";

        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth", create_by);
        task_map.put("task_name", task_name);
        task_map.put("url", SaveUrl);
        task_map.put("agent_id", User.getDeptId());
        task_map.put("type", "40");
        yzExecutionTaskMapper.add(task_map);// 添加执行 任务表

        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx + "/1.txt");// tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            // 1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_ModuleImportData_queue",
                    addOrder_routingKey = "admin.ModuleImportData.queue";
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);// 项目根目录
                start_type.put("ReadName", ReadName);// 上传新文件名
                start_type.put("map", map);// 参数
                start_type.put("task_map", task_map);// 参数
                start_type.put("newName", newName);// 参数
                start_type.put("UpdBackupName", UpdBackupName);// 参数

                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type),
                        message -> {
                            // 设置消息过期时间 60 分钟 过期
                            message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                            return message;
                        });
            } catch (Exception e) {
                System.out.println("批量更新卡信息 生产指令  失败 " + e.getMessage().toString());
                return ("批量更新卡信息 生产指令 操作失败！");
            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "批量更新卡信息 指令 已发送，更新卡信息 详细信息请在 【执行日志管理】查询！";

        /*
         * if (StringUtils.isNull(list) || list.size() == 0) {
         * throw new CustomException("导入轮询数据不能为空！");
         * }
         * int successNum = 0;
         * int failureNum = 0;
         * StringBuilder successMsg = new StringBuilder();
         * StringBuilder failureMsg = new StringBuilder();
         * for (ModuleHouse moduleHouse : list) {
         * try {
         * // 验证是否存在这个序列号
         * Map<String,Object> moduleHouseMap =
         * moduleHouseMapper.selectModuleBySerialNumber(moduleHouse);
         * if (StringUtils.isNull(moduleHouseMap)) {//库中不存在这个序列号
         * Map<String,Object> serialNumberMap =
         * moduleHouseMapper.selectModuleBySN(moduleHouse);
         * if (serialNumberMap==null) {
         * moduleHouse.setCreateBy(String.valueOf(userId));
         * moduleHouse.setUpdateBy(String.valueOf(userId));
         * moduleHouseMapper.ins(moduleHouse);
         * successNum++;
         * successMsg.append("<br/>" + successNum + "、序列号 " +
         * moduleHouse.getSerialNumber() + " 导入成功");
         * }else {
         * failureNum++;
         * failureMsg.append("<br/>" + failureNum + "、序列号 " +
         * moduleHouse.getSerialNumber() + " 已存在");
         * }
         * } else {
         * failureNum++;
         * failureMsg.append("<br/>" + failureNum + "、序列号 " +
         * moduleHouse.getSerialNumber() + " 已存在");
         * }
         * } catch (Exception e) {
         * failureNum++;
         * String msg = "<br/>" + failureNum + "、序列号 " + moduleHouse.getSerialNumber() +
         * " 导入失败：";
         * failureMsg.append(msg + e.getMessage());
         * }
         * }
         * if (failureNum > 0) {
         * failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
         * throw new CustomException(failureMsg.toString());
         * } else {
         * successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
         * }
         * return successMsg.toString();
         */
    }

    @Override
    public String exportModule(Map<String, Object> map, SysUser currentUser) {
        // 移除分页参数
        map.remove("pageNum");
        map.remove("pageSize");

        String create_by = " [ " + currentUser.getDept().getDeptName() + " ] - " + " [ " + currentUser.getUserName()
                + " ] ";
        String newName = UUID.randomUUID().toString().replace("-", "") + "_ModuleHouseOut";
        String agent_id = currentUser.getDept().getDeptId().toString();

        String task_name = "模组信息 [导出] ";
        String SaveUrl = "/getcsv/" + newName + ".csv";

        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth", create_by);
        task_map.put("task_name", task_name);
        task_map.put("url", SaveUrl);
        task_map.put("agent_id", agent_id);
        task_map.put("type", "41");

        // 发送队列
        String polling_queueName = "admin_ModuleHouseExport_queue";
        String polling_routingKey = "admin.ModuleHouseExport.queue";
        String polling_exchangeName = "admin_exchange";// 路由

        try {
            yzExecutionTaskMapper.add(task_map);// 添加执行 任务表
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("type", "exportModuleHouse");// 启动类型
            start_type.put("newName", newName);// 输出文件名
            start_type.put("task_map", task_map);//
            start_type.put("create_by", create_by);//
            start_type.put("User", currentUser);
            start_type.put("map", map);

            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type),
                    message -> {
                        // 设置消息过期时间 30 分钟 过期
                        message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                        return message;
                    });
        } catch (Exception e) {
            System.out.println("导出 模组信息 失败 " + e.getMessage().toString());
            return "导出 模组信息 操作失败！";
        }

        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }

    @Override
    public Map<String, Object> validateBoxOrReelInStock(String boxNumber, String reelNumber) {
        Map<String, Object> result = new HashMap<>();

        // 检查参数
        if (StringUtils.isEmpty(boxNumber) && StringUtils.isEmpty(reelNumber)) {
            result.put("valid", false);
            result.put("message", "中箱号和卷盘号不能同时为空");
            return result;
        }

        List<String> allNumbers = new ArrayList<>();
        List<String> failedNumbers = new ArrayList<>();
        List<String> successNumbers = new ArrayList<>();

        try {
            // 处理中箱号（可能包含多个值，用逗号分隔）
            if (StringUtils.isNotEmpty(boxNumber)) {
                String[] boxNumbers = boxNumber.split(",");
                for (String box : boxNumbers) {
                    String trimmedBox = box.trim();
                    if (StringUtils.isNotEmpty(trimmedBox)) {
                        allNumbers.add("中箱号:" + trimmedBox);
                        // 验证单个中箱号
                        Map<String, Object> boxQueryMap = new HashMap<>();
                        boxQueryMap.put("boxNumber", trimmedBox);
                        boxQueryMap.put("boxNumberList", Arrays.asList(trimmedBox));

                        Map<String, Object> boxResult = moduleHouseMapper.validateBoxOrReelInStock(boxQueryMap);
                        if (boxResult != null && !boxResult.isEmpty()) {
                            successNumbers.add("中箱号:" + trimmedBox);
                        } else {
                            failedNumbers.add("中箱号:" + trimmedBox + "(不存在或状态不是库存)");
                        }
                    }
                }
            }

            // 处理卷盘号（可能包含多个值，用逗号分隔）
            if (StringUtils.isNotEmpty(reelNumber)) {
                String[] reelNumbers = reelNumber.split(",");
                for (String reel : reelNumbers) {
                    String trimmedReel = reel.trim();
                    if (StringUtils.isNotEmpty(trimmedReel)) {
                        allNumbers.add("卷盘号:" + trimmedReel);
                        // 验证单个卷盘号
                        Map<String, Object> reelQueryMap = new HashMap<>();
                        reelQueryMap.put("reelNumber", trimmedReel);
                        reelQueryMap.put("reelNumberList", Arrays.asList(trimmedReel));

                        Map<String, Object> reelResult = moduleHouseMapper.validateBoxOrReelInStock(reelQueryMap);
                        if (reelResult != null && !reelResult.isEmpty()) {
                            successNumbers.add("卷盘号:" + trimmedReel);
                        } else {
                            failedNumbers.add("卷盘号:" + trimmedReel + "(不存在或状态不是库存)");
                        }
                    }
                }
            }

            // 判断验证结果
            if (failedNumbers.isEmpty()) {
                // 所有号码都验证通过
                result.put("valid", true);
                result.put("message", "所有号码验证通过，共" + successNumbers.size() + "个号码");
                result.put("successNumbers", successNumbers);
            } else {
                // 有号码验证失败
                result.put("valid", false);
                StringBuilder message = new StringBuilder();
                message.append("验证失败，共").append(failedNumbers.size()).append("个号码不满足要求：");
                for (String failed : failedNumbers) {
                    message.append("\n- ").append(failed);
                }
                if (!successNumbers.isEmpty()) {
                    message.append("\n验证通过的号码：");
                    for (String success : successNumbers) {
                        message.append("\n- ").append(success);
                    }
                }
                result.put("message", message.toString());
                result.put("failedNumbers", failedNumbers);
                result.put("successNumbers", successNumbers);
            }
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "验证过程中发生异常: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> updateModuleStatusToOutbound(String boxNumber, String reelNumber, String updateBy) {
        Map<String, Object> result = new HashMap<>();

        // 检查参数
        if (StringUtils.isEmpty(boxNumber) && StringUtils.isEmpty(reelNumber)) {
            result.put("success", false);
            result.put("message", "中箱号和卷盘号不能同时为空");
            return result;
        }

        List<String> boxNumberList = new ArrayList<>();
        List<String> reelNumberList = new ArrayList<>();
        int totalUpdated = 0;

        try {
            // 处理中箱号（可能包含多个值，用逗号分隔）
            if (StringUtils.isNotEmpty(boxNumber)) {
                String[] boxNumbers = boxNumber.split(",");
                for (String box : boxNumbers) {
                    String trimmedBox = box.trim();
                    if (StringUtils.isNotEmpty(trimmedBox)) {
                        boxNumberList.add(trimmedBox);
                    }
                }
            }

            // 处理卷盘号（可能包含多个值，用逗号分隔）
            if (StringUtils.isNotEmpty(reelNumber)) {
                String[] reelNumbers = reelNumber.split(",");
                for (String reel : reelNumbers) {
                    String trimmedReel = reel.trim();
                    if (StringUtils.isNotEmpty(trimmedReel)) {
                        reelNumberList.add(trimmedReel);
                    }
                }
            }

            // 构建更新参数
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("updateBy", updateBy);

            if (!boxNumberList.isEmpty()) {
                updateMap.put("boxNumberList", boxNumberList);
            }
            if (!reelNumberList.isEmpty()) {
                updateMap.put("reelNumberList", reelNumberList);
            }

            // 执行更新操作
            totalUpdated = moduleHouseMapper.updateModuleStatusToOutbound(updateMap);

            if (totalUpdated > 0) {
                result.put("success", true);
                result.put("message", "成功更新" + totalUpdated + "条记录的状态为出库");
                result.put("updatedCount", totalUpdated);
                result.put("boxNumbers", boxNumberList);
                result.put("reelNumbers", reelNumberList);
            } else {
                result.put("success", false);
                result.put("message", "没有找到需要更新的记录，可能所有号码都已出库或不存在");
                result.put("updatedCount", 0);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新状态过程中发生异常: " + e.getMessage());
            result.put("updatedCount", 0);
        }

        return result;
    }

    @Override
    public Map<String, Object> countModulesInStock(String boxNumber, String reelNumber) {
        Map<String, Object> result = new HashMap<>();

        // 检查参数
        if (StringUtils.isEmpty(boxNumber) && StringUtils.isEmpty(reelNumber)) {
            result.put("success", false);
            result.put("message", "中箱号和卷盘号不能同时为空");
            result.put("count", 0);
            return result;
        }

        List<String> boxNumberList = new ArrayList<>();
        List<String> reelNumberList = new ArrayList<>();

        try {
            // 处理中箱号（可能包含多个值，用逗号分隔）
            if (StringUtils.isNotEmpty(boxNumber)) {
                String[] boxNumbers = boxNumber.split(",");
                for (String box : boxNumbers) {
                    String trimmedBox = box.trim();
                    if (StringUtils.isNotEmpty(trimmedBox)) {
                        boxNumberList.add(trimmedBox);
                    }
                }
            }

            // 处理卷盘号（可能包含多个值，用逗号分隔）
            if (StringUtils.isNotEmpty(reelNumber)) {
                String[] reelNumbers = reelNumber.split(",");
                for (String reel : reelNumbers) {
                    String trimmedReel = reel.trim();
                    if (StringUtils.isNotEmpty(trimmedReel)) {
                        reelNumberList.add(trimmedReel);
                    }
                }
            }

            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();

            if (!boxNumberList.isEmpty()) {
                queryMap.put("boxNumberList", boxNumberList);
            }
            if (!reelNumberList.isEmpty()) {
                queryMap.put("reelNumberList", reelNumberList);
            }

            // 执行统计查询
            int totalCount = moduleHouseMapper.countModulesInStock(queryMap);

            result.put("success", true);
            result.put("message", "统计完成");
            result.put("count", totalCount);
            result.put("boxNumbers", boxNumberList);
            result.put("reelNumbers", reelNumberList);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "统计过程中发生异常: " + e.getMessage());
            result.put("count", 0);
        }

        return result;
    }
}
