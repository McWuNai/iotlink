package com.yunze.system.service.impl.house;


import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysDept;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.exception.CustomException;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.house.ModuleHouseMapper;
import com.yunze.common.utils.StringUtils;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.system.service.house.IModuleHouseService;
import com.yunze.common.core.domain.entity.ModuleHouse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
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
        Map<String, Object> rMap = new HashMap<String, Object>();
        Integer total = moduleHouseMapper.selMapCount(map);
        rMap.put("total",total);
        int pageNum = (int) map.get("pageNum");
        map.put("pageNum",pageNum-1);
        List<Map<String, Object>> list = moduleHouseMapper.list(map);
        rMap.put("data",list);
//        test();
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

        /*if (StringUtils.isNull(list) || list.size() == 0) {
            throw new CustomException("导入轮询数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (ModuleHouse moduleHouse : list) {
            try {
                // 验证是否存在这个序列号
                Map<String,Object> moduleHouseMap = moduleHouseMapper.selectModuleBySerialNumber(moduleHouse);
                if (StringUtils.isNull(moduleHouseMap)) {//库中不存在这个序列号
                    Map<String,Object> serialNumberMap = moduleHouseMapper.selectModuleBySN(moduleHouse);
                    if (serialNumberMap==null) {
                        moduleHouse.setCreateBy(String.valueOf(userId));
                        moduleHouse.setUpdateBy(String.valueOf(userId));
                        moduleHouseMapper.ins(moduleHouse);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、序列号 " + moduleHouse.getSerialNumber() + " 导入成功");
                    }else {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、序列号 " + moduleHouse.getSerialNumber() + " 已存在");
                    }
                }  else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、序列号 " + moduleHouse.getSerialNumber() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、序列号 " + moduleHouse.getSerialNumber() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new CustomException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();*/
    }
}
