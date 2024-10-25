package com.yunze.system.service.impl.yunze.warehouse.Administration;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStorageMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglInventorydetailsMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.system.mapper.SysUserMapper;
import com.yunze.system.service.yunze.IYzUserService;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglInventorydetailsService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;


@Service
public class YzCkglInventorydetailsServiceImpl<SelId> implements YzCkglInventorydetailsService {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private YzCkglCommodityStorageMapper yzCkglCommodityStorageMapper;
    @Resource
    private YzCkglInventorydetailsMapper yzCkglInventorydetailsMapper;


    @Resource
    private IYzUserService iYzUserService;
    @Autowired
    private SysUserMapper userMapper;



    @Override
    public String ImportStock(MultipartFile file, Map<String, String> Pmap, SysUser User, HashMap<String, Object> parammap) {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String newName = UUID.randomUUID().toString().replace("-", "") + "_ImportStock";
        String flieUrlRx = "/upload/uploadStock/";
        ReadName = flieUrlRx + ReadName;
        try {
            /**
             * 获取当前项目的工作路径
             */
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            Map<String, Object> map = new HashMap();
            map.put("CSE_ID", parammap.get("CSE_ID"));

            String CSE_fliieUrl = yzCkglCommodityStorageMapper.sleFile(map);
            if (CSE_fliieUrl != null && CSE_fliieUrl.toString().length() > 0) {
                CSE_fliieUrl += ","  + ReadName;
            } else {
                CSE_fliieUrl =   ReadName;
            }
            map.put("CSE_fliieUrl", CSE_fliieUrl);
            yzCkglCommodityStorageMapper.uploadFile(map);
            File Url = new File(filePath + flieUrlRx+ "1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_ImportStock_queue";
            String polling_routingKey = "admin.ImportStock.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
               // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("newName", newName);//输出文件名
                start_type.put("User", User);//登录用户信息
                start_type.put("Pmap", Pmap);//
                start_type.put("parammap", parammap);//
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return ("仓库管理 导入 操作失败 !");
            }

        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "仓库管理 导入指令 已发送，详细信息请在 【执行日志管理】查询！";

    }

    @Override
    public Map<String, Object> inventorydetails(Map map) {

        //System.out.println("CardRouteServiceImpl > sel_Map = ");
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzCkglInventorydetailsMapper.MapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCkglInventorydetailsMapper.liststorage(map));
        return omp;
    }


    @Override
    public String ExportDetails(Map<String, Object> map, SysUser User) {
        Object MapAgent_id = map.get("agent_id");
        //导出时 未选中 当前 企业编号时 且登录 部门不是 总平台 赋值部门
        if (MapAgent_id == null && User.getDeptId() != 100) {
            List<String> agent_idArr = new ArrayList<String>();
            agent_idArr.add("" + User.getDeptId());
            map.put("agent_id", agent_idArr);
        }
        map.remove("pageNum");
        map.remove("pageSize");
        List<Map<String, Object>> outCardIccidArr = null;
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (!Different.Is_existence(agent_id, 100)) {
                List<String> user_id = iYzUserService.getUserID(map);
                map.put("user_id", user_id);
            }
        }
        //查询库存明细标
        outCardIccidArr = yzCkglInventorydetailsMapper.ExportInvent(map);

        if (outCardIccidArr != null && outCardIccidArr.size() > 0) {
            String create_by = " [ " + User.getDept().getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
            String newName = UUID.randomUUID().toString().replace("-", "") + "_ExportDetails";

            String agent_id = User.getDept().getDeptId().toString();
            String task_name = "库存明细导出 [导出] ";
            String SaveUrl = "/getcsv/" + newName + ".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth", create_by);
            task_map.put("task_name", task_name);
            task_map.put("url", SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "16");


            //获取 用户信息
            List<Map<String, Object>> userArr = userMapper.find_simple();

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_ExportDetails_queue";
            String polling_routingKey = "admin.ExportDetails.queue";
            String polling_exchangeName = "admin_exchange";//路由

            try {
               // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr", outCardIccidArr);
                start_type.put("userArr", userArr);
                start_type.put("map", map);
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return "库存明细导出 导入 操作失败！";
            }
        } else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }


    @Override
    public String ImportCard(Map map, SysUser User) {
        {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_ImportCard_queue", addOrder_routingKey = "admin.ImportCard.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                start_type.put("User", User);
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("导入至卡信息 生产指令  失败 " + e.getMessage().toString());
                return ("导入至卡信息 生产指令 操作失败！");
            }

            return "导入至卡信息 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
        }
    }

    @Override
    public String detailsDividCard(Map<String, Object> map) {
        String Message = "";
        map.remove("pageNum");
        map.remove("pageSize");
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (!Different.Is_existence(agent_id, 100)) {
                List<String> user_id = iYzUserService.getUserID(map);
                map.put("user_id", user_id);
            }
        }

        List<Map<String, Object>> listInv = yzCkglInventorydetailsMapper.BackupAssociate(map);

        if (listInv != null && listInv.size() > 0) {

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_DivideInventorydetails_queue";
            String polling_routingKey = "admin.DivideInventorydetails.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.putAll(map);
                start_type.put("type", "DistributeCard");//启动类型
                start_type.put("listInv", listInv);//启动类型
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("划分 失败 " + e.getMessage().toString());
                return ("物联卡管理 划分 操作失败！");
            }
            Message = "当前筛选条件下需要划分的数据 [ " + listInv.size() + " ] 条 至 [ " + map.get("set_dept_name") + " ] [ " + map.get("set_user_name") + " ] 指令已下发详情查看 【执行日志管理】 ！";
        } else {
            Message = "当前筛选条件下未找到需要划分的数据！请核对后重试！";
        }

        return Message;
    }


}
















































