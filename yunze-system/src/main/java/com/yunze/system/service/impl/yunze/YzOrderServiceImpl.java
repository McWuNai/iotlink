package com.yunze.system.service.impl.yunze;


import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.apiCommon.utils.Arith;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysDept;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.*;
import com.yunze.common.mapper.yunze.bulk.YzBulkBusinessMapper;
import com.yunze.common.utils.yunze.*;
import com.yunze.quartz.task.yunze.OrderAddPackageProductionTask;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.mapper.SysUserMapper;
import com.yunze.system.service.yunze.IYzOrderService;
import com.yunze.system.service.yunze.IYzUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 卡信息 业务实现类
 *
 * @author root
 */
@Service
public class YzOrderServiceImpl implements IYzOrderService
{
    private static final Logger log = LoggerFactory.getLogger(YzOrderServiceImpl.class);

    @Resource
    private IYzUserService iYzUserService;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMQConfig mQConfig;
    @Resource
    private SysDictDataMapper dictDataMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private PlOrder plOrder;
    @Resource
    private OrderAddPackageProductionTask orderAddPackageProductionTask;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzBulkBusinessMapper yzBulkBusinessMapper;
    @Resource
    private YzCardPacketMapper yzCardPacketMapper;
    @Resource
    private YzAgentPacketMapper yzAgentPacketMapper;
    @Resource
    private YzUserMapper yzUserMapper;

    @Override
    public Map<String, Object> selMap(Map<String, Object> map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        //权限过滤
        if(map.get("agent_id")!=null){
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if(!Different.Is_existence(agent_id,100)){
                List<String>  user_id =  iYzUserService.getUserID(map);
                map.put("user_id",user_id);
            }
        }
        PageUtil pu = null;
        List<Map<String,Object>> Rlist = null;
        Integer  rowCount = yzOrderMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        pu = new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        Rlist = yzOrderMapper.selMap(map);
        omp.put("Pu", pu);
        omp.put("Pmap", map);
        omp.put("Data",Rlist );
        return omp;
    }

    @Override
    public Map<String, Object> find(Map<String, Object> map) {

        return null;
    }

    @Override
    public List<String> outOrder(Map<String, Object> map) {
        return null;
    }

    @Override
    public Map<String, Object> findOrder(Map<String, Object> map) {
        return yzOrderMapper.findOrder(map);
    }



    @Override
    public String importRecharge(MultipartFile file, Map<String, Object> map) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String newName = UUID.randomUUID().toString().replace("-", "") + "_repeat";
        String flieUrlRx = "/upload/importRecharge/";
        ReadName = flieUrlRx + ReadName;
        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx + "1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            map.put("OrderMap", plOrder.orderOpen(map));
            String agent_id = map.get("agent_id").toString();

            Map<String, Object> bulkMap = new HashMap<>();
            String task_name = "平台批量充值 [充值] ";
            String code = VeDate.getNo(8);
            SysUser User = (SysUser) map.get("User");//登录用户信息
            SysDept Dept = User.getDept();
            String create_by = " [ " + Dept.getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
            bulkMap.put("code", code);
            bulkMap.put("task_name", task_name);
            bulkMap.put("auth", create_by);
            bulkMap.put("agent_id", agent_id);
            bulkMap.put("type", "5");//批量订购 5
            yzBulkBusinessMapper.add(bulkMap);


            //1.创建路由 绑定 生产队列 发送消息
            //路由队列
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_OrderImportRecharge_queue", addOrder_routingKey = "admin.OrderImportRecharge.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                mQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("newName", newName);//输出文件名
                start_type.put("bulkMap", bulkMap);//批量任务主表 信息
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });


            } catch (Exception e) {
                System.out.println("平台导入充值生产指令  失败 " + e.getMessage().toString());
                return ("平台导入充值生产指令 操作失败！");
            }
//            }else{
//                return ("未找到 您的名下 资费计划ID 【"+map.get("packet_id")+"】 充值取消! ");
//            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "资费订购 平台导入充值指令 已发送，充值详细信息请在 【批量业务受理】查询！";
    }

    @Override
    public String exportallorders(Map<String, Object> map, SysUser User) {
        Object MapAgent_id = map.get("agent_id");
        //导出时 未选中 当前 企业编号时 且登录 部门不是 总平台 赋值部门
        if(MapAgent_id==null && User.getDeptId()!=100){
            List<String> agent_idArr = new ArrayList<String>();
            agent_idArr.add(""+User.getDeptId());
            map.put("agent_id",agent_idArr);
        }
        map.remove("pageNum");
        map.remove("pageSize");
        List<Map<String, Object>> outCardIccidArr = null;
        //权限过滤
        if(map.get("agent_id")!=null){
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if(!Different.Is_existence(agent_id,100)){
                List<String>  user_id =  iYzUserService.getUserID(map);
                map.put("user_id",user_id);
            }
        }
        outCardIccidArr = yzOrderMapper.exportallorders(map);

        if(outCardIccidArr!=null && outCardIccidArr.size()>0){
            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            String newName = UUID.randomUUID().toString().replace("-","")+"_Exportallorders";

            String  agent_id = User.getDept().getDeptId().toString();
            String task_name ="订单管理 [导出] ";
            String SaveUrl = "/getcsv/"+newName+".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "11");

            //获取字典信息
            List<SysDictData> orderArr = dictDataMapper.selectDictDataByType("yunze_order_status");//订单状态
            List<SysDictData> pay_type = dictDataMapper.selectDictDataByType("yunze_card_pay_type");//支付方式
            List<SysDictData> customize_whether = dictDataMapper.selectDictDataByType("yunze_customize_whether");//自定义是否
            List<SysDictData> card_takeEffect = dictDataMapper.selectDictDataByType("yunze_card_takeEffect_type");//资费计划生效类别

            //获取 企业信息
            List<Map<String,Object>> arrorders = yzOrderMapper.arrorders(map);
            List<Map<String,Object>> deptArr = sysUserMapper.find_flow();

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_Exportallorders_queue";
            String polling_routingKey = "admin.Exportallorders.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr",outCardIccidArr);
                start_type.put("deptArr",deptArr);
                start_type.put("arrorders",arrorders);
                start_type.put("stateOptions", orderArr);
                start_type.put("pay_type",pay_type);
                start_type.put("customize_whether",customize_whether);
                start_type.put("cardConnection_type",card_takeEffect);
                start_type.put("map",map);

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e){
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return "全部订单 导入 操作失败！";
            }
        }else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }

    @Override
    public Map<String, Object> CardNumberImport(MultipartFile file, Map<String, Object> map) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + "/upload/cardNumberAge/" + ReadName);
            File Url = new File(filePath + "/upload/cardNumberAge/1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            ExcelConfig excelConfig = new ExcelConfig();
            String columns[] = {"cardNumberAge"};
            List<Map<String, Object>> list = excelConfig.getExcelListMap(filePath + "/upload/cardNumberAge/" + ReadName, columns);
            map.put("UpArrList", list);
        } catch (Exception e) {
            System.out.println(e);
        }
        return selMap(map);//是返回的查询列表的数据
    }

    @Override
    public String NoTbscribe(Map map,SysUser User) {
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
        List<Map<String,Object>> Unsubscribe = yzOrderMapper.exportallorders(map);
        if(Unsubscribe != null){

            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            String  task_name = "";
            String  newUpdte = UUID.randomUUID().toString().replace("-","")+"_newUpdte";
            String  newDelete = UUID.randomUUID().toString().replace("-","")+"_newDelete";
            String  newOrder = UUID.randomUUID().toString().replace("-","")+"_newName";
            String  agent_id = User.getDept().getDeptId().toString();
            String obj = map.get("radio").toString();
            switch (obj){
                case "update":
                    task_name ="订单状态[退款]并删除资费计划";
                break;
                case "delete":
                    task_name ="删除订单并删除资费计划";
                break;
            }
            String SaveUrl = "/getcsv/"+newUpdte+".csv";
            SaveUrl += ",/getcsv/"+newDelete+".csv";
            String value = map.get("valueIs").toString();
            if(value.equals("true")) {
                SaveUrl += ",/getcsv/" + newOrder + ".csv";
            }
            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "38");
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表

            map.put("agent_id",agent_id);
            Map<String,Object> OrderMap =  plOrder.orderOpen(map);


            //1.创建路由 绑定 生产队列 发送消息
            String polling_queueName = "admin_Unsubscribe_queue";
            String polling_routingKey = "admin.Unsubscribe.queue";
            String polling_exchangeName = "admin_exchange";//路由

            try {
                rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.putAll(map);
                start_type.put("Unsubscribe", Unsubscribe);//启动类型
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//

                start_type.put("newUpdte", newUpdte);//
                start_type.put("newDelete", newDelete);//
                start_type.put("newOrder", newOrder);//
                start_type.put("OrderMap", OrderMap);//
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("划分 失败 " + e.getMessage().toString());
                return ("物联卡管理 划分 操作失败！");
            }
            Message = "当前筛选条件下需要的数据 [ " + Unsubscribe.size() + " ] 条  指令已下发详情查看 【执行日志管理】 ！";
        }else {
            Message = "当前筛选条件下未找到需要划分的数据！请核对后重试！";
        }
        return Message;
    }

    @Override
    public String getPackage(Map map) {

        Integer time = 60;
        List<String> UpdaArr = (List<String>) map.get("UpdaArr");
        //0.自动 修改已经加包的订单加包状态 [防止重复加包]
        try {
            Map<String,Object> objectMap = new HashMap<>();
            objectMap.put("UpdaArr",UpdaArr);
            yzOrderMapper.updBatch(objectMap);
        }catch (Exception e){
            System.out.println("加包执行失败");
        }

        //1.获取 支付成功 > 用量充值 > 未执行加包的 订单
        Map<String, Object> findAddPackage_Map = new HashMap<>();
        findAddPackage_Map.put("UpdaArr", UpdaArr);
        List<Map<String, Object>> AddPackageArr = yzOrderMapper.findAddPackageNoAddPackage(findAddPackage_Map);

        List<String> list = new ArrayList<>();
        for (Map<String,Object> Map1 : AddPackageArr){
            String Str = Map1.get("iccid").toString();
            list.add(Str);
        }
        int a = 0;
        int b = 0;
        String message = "";
        Map<String,Object> objectMap = new HashMap<>();
        objectMap.put("valueList",list);
        List<Map<String,Object>> stringList = yzCardMapper.itemIccid(objectMap);
        for (Map<String,Object> obj : stringList){

            String activate_date = obj.get("activate_date")+"";
            if(!activate_date.equals("isNull")){
                try {
                    orderAddPackageProductionTask.AddPackag(AddPackageArr, time);
                    message = "执行加包！";
                    a++;
                }catch (Exception e){
                    return "发送加包指令失败……";
                }
            }else {
                message = "执行加包！";
                b++;
            }
        }
        return message+"["+"发送加包指令成功"+a+"条"+"]"+"=========="+"["+"发送加包指令失败"+b+"条"+"]";
    }

    @Override
    public String TextRecharge(Map<String, Object> map) throws IOException {

        String newName = UUID.randomUUID().toString().replace("-", "") + "_repeat";
        String flieUrlRx = "/upload/importRecharge/";
        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File Url = new File(filePath + flieUrlRx + "1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);

            map.put("OrderMap", plOrder.orderOpen(map));
            String agent_id = map.get("agent_id").toString();

            Map<String, Object> bulkMap = new HashMap<>();
            String task_name = "平台批量充值 [充值] ";
            String code = VeDate.getNo(8);
            SysUser User = (SysUser) map.get("User");//登录用户信息
            SysDept Dept = User.getDept();
            String create_by = " [ " + Dept.getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
            bulkMap.put("code", code);
            bulkMap.put("task_name", task_name);
            bulkMap.put("auth", create_by);
            bulkMap.put("agent_id", agent_id);
            bulkMap.put("type", "5");//批量订购 5
            yzBulkBusinessMapper.add(bulkMap);


            //1.创建路由 绑定 生产队列 发送消息
            //路由队列
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_OrderTextRecharge_queue", addOrder_routingKey = "admin.OrderTextRecharge.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                //mQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("newName", newName);//输出文件名
                start_type.put("bulkMap", bulkMap);//批量任务主表 信息
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });


            } catch (Exception e) {
                System.out.println("平台文本域充值指令  失败 " + e.getMessage().toString());
                return ("平台文本域充值指令 操作失败！");
            }
//            }else{
//                return ("未找到 您的名下 资费计划ID 【"+map.get("packet_id")+"】 充值取消! ");
//            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "资费订购 平台文本域充值指令 已发送，充值详细信息请在 【批量业务受理】查询！";
    }

    @Override
    public Map<String,Object> DeptTextRecharge(Map<String, Object> map, SysUser operateMap) throws IOException {
        String newName = UUID.randomUUID().toString().replace("-", "") + "_repeat";
        String flieUrlRx = "/upload/importRecharge/";

        // 获取当前项目的工作路径
        File file2 = new File("");
        String filePath = file2.getCanonicalPath();
        File Url = new File(filePath + flieUrlRx + "1.txt");//tomcat 生成路径
        Upload.mkdirsmy(Url);

        map.put("OrderMap", plOrder.orderOpen(map));
        String agent_id = map.get("agent_id").toString();

        Map<String, Object> Rmap = new HashMap<String, Object>();
        String Message = "操作失败！";
        boolean bool = false;
        List<Map<String, Object>>  subBRCardArr = (List<Map<String, Object>>) map.get("subBRCardArr");
        List<String>  packetIdArr = (List<String>) map.get("packetIdArr");
        Map<String, Object>  BRpacketCardCount = (Map<String, Object>) map.get("BRpacketCardCount");
        List<String>  iccidArr = (List<String>) map.get("iccidArr");



        if(subBRCardArr!=null && subBRCardArr.size()>0){//充值资费计划数组不能为空
            String login_dept_id = map.get("login_dept_id").toString();

            //0. 充值资费组获取成本价
            Map<String,Object> findPacketMap = new HashMap<>();
            findPacketMap.put("login_dept_id",login_dept_id);
            findPacketMap.put("packetIdArr",packetIdArr);
            List<Map<String,Object>> packetArr = new ArrayList<>();
            if(login_dept_id.equals("100")){
                packetArr = yzCardPacketMapper.findPacketSingleTable(findPacketMap);
            }else{
                packetArr = yzAgentPacketMapper.findPacketSingleTable(findPacketMap);
            }

            Double amount = 0.0;//总计金额
            int card_sumCount = 0;//续费卡总数
            for (int i = 0; i < packetArr.size(); i++) {
                Map<String,Object> packe = packetArr.get(i);
                Double packet_cost = Double.parseDouble(packe.get("packet_cost").toString());//成本
                String packet_id = packe.get("packet_id").toString();
                List<Map<String, Object>>  cardObj = (List<Map<String, Object>>) BRpacketCardCount.get(""+packet_id);
                int packetCardCount = cardObj.size();//数量
                card_sumCount = (int) Arith.add(card_sumCount,packetCardCount);
                amount = Arith.add(amount,Arith.mul(packetCardCount,packet_cost));
            }

            //1.判断企业金额信息是或否支持续费操作
            Map<String,Object> FindDeptAmountMap = new HashMap<>();
            FindDeptAmountMap.put("dept_id",login_dept_id);
            Map<String,Object> DeptAmountMap =  yzUserMapper.findDeptAmount(FindDeptAmountMap);
            Double deposit_amount = DeptAmountMap.get("deposit_amount")!=null&&DeptAmountMap.get("deposit_amount").toString().length()>0?Double.parseDouble(DeptAmountMap.get("deposit_amount").toString()):0.0;//预存金额
            Double be_usable_line_of_credit = DeptAmountMap.get("be_usable_line_of_credit")!=null&&DeptAmountMap.get("be_usable_line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("be_usable_line_of_credit").toString()):0.0;//可使用授信额度

            double forecast_deposit_amount = 0.0;//预计剩余可用预存金额
            double forecast_be_usable_line_of_credit = 0.0;//预计剩余 可用授信 [默认 0 ]
            forecast_deposit_amount = Arith.sub(deposit_amount,amount);//预计剩余可用预存金额 =  可用预存金额 - 总扣款
            boolean is_NeedToRecharge = false;//判断扣款是否不足以支付该次 操作进行提示

            if(forecast_deposit_amount<=0 && be_usable_line_of_credit==0){//如 预计剩余可用预存金额 <0 且 可用授信额度 == 0
                is_NeedToRecharge = true;
            }else  if(forecast_deposit_amount<0 && be_usable_line_of_credit>0){//如 预计剩余可用预存金额 <0 且 可用授信额度》0 》》 继续判断可用授信是否足够扣款
                forecast_be_usable_line_of_credit = Arith.add(be_usable_line_of_credit,forecast_deposit_amount);//预计剩余 可用授信 = 剩余可用授信 - 预计剩余可用预存金额
                forecast_deposit_amount = 0;
                if(forecast_be_usable_line_of_credit>=0) {//如 预计剩余 可用授信 >=0  变更 预计剩余可用预存金额 = 0

                }else{
                    is_NeedToRecharge = true;
                }
            }else{
                forecast_be_usable_line_of_credit = be_usable_line_of_credit;
            }

            if(is_NeedToRecharge){ //不足以扣款时
                double stillNeeded =  Arith.add(forecast_deposit_amount,forecast_be_usable_line_of_credit);//还需充值金额
                stillNeeded = Math.abs(stillNeeded);
                Message = "本次操作需扣款 [ "+amount+" ] 还需 [ "+stillNeeded+" ] ,您的账户目前可用预存/授信不足，请点击['去预存']！";
            }else{
                //续费扣款指令发送
                Map<String, Object> primaryMap = new HashMap<>();
                primaryMap.put("amount",amount);
                primaryMap.put("card_sumCount",card_sumCount);
                primaryMap.put("user_id",operateMap.getUserId());
                map.put("primaryMap",primaryMap);
                map.put("agent_id",login_dept_id);
                map.put("deptName",operateMap.getDept().getDeptName());
                map.put("packetArr",packetArr);

                Map<String, Object> bulkMap = new HashMap<>();
                String task_name = "企业批量充值 [充值] ";
                String code = VeDate.getNo(8);
                SysUser User = (SysUser) map.get("User");//登录用户信息
                SysDept Dept = User.getDept();
                String create_by = " [ " + Dept.getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
                bulkMap.put("code", code);
                bulkMap.put("task_name", task_name);
                bulkMap.put("auth", create_by);
                bulkMap.put("agent_id", agent_id);
                bulkMap.put("type", "5");//批量订购 5
                yzBulkBusinessMapper.add(bulkMap);

                //修改企业金额信息 路由队列
                String polling_queueName = "admin_updDeptAmount_queue";
                String polling_routingKey = "admin.updDeptAmount.queue";
                String polling_exchangeName = "admin_exchange";//路由
                try {
                    Map<String, Object> start_type = new HashMap<>();
                    start_type.put("type", "Recharge");//启动类型 批量充值
                    start_type.put("Pmap", map);//请求参数
                    start_type.put("operateMap", operateMap);//操作人信息.
                    start_type.put("filePath", filePath);//项目根目录
                    start_type.put("newName", newName);//输出文件名
                    start_type.put("bulkMap", bulkMap);//批量任务主表 信息
                    start_type.put("iccidArr", iccidArr);

                    rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                        // 设置消息过期时间 30 分钟 过期
                        message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                        return message;
                    });
                    bool = true;
                    Message = " 充值资费 指令发送成功！请稍后刷新查看";
                } catch (Exception e) {
                    System.out.println("充值资费 指令发送 " + e.getMessage());
                    Message =  "充值资费 指令发送 失败！";
                }


            }
        }else {
            Message = "充值资费计划不能为空！";
        }

        Rmap.put("bool",bool);
        Rmap.put("Message",Message);
        return Rmap;
    }

    @Override
    public List<String> findRecharged(Map<String, Object> map) {
        return yzOrderMapper.findRecharged(map);
    }

    @Override
    public Map<String, Object> orderAudit(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<String, Object>();
        String Message = "操作失败！";
        boolean bool = false;
        boolean noticeBool = (boolean) map.get("notice");
        int updCount = yzOrderMapper.updaudit(map); //修改订单 审核状态
        if(updCount>0){
            bool = true;
            Message = "操作成功！";
            if(noticeBool){//进行回调通知
                String polling_queueName = "admin_updOrderAudit_queue";
                String polling_routingKey = "admin.updOrderAudit.queue";
                String polling_exchangeName = "admin_exchange";//路由
                try {
                    Map<String, Object> start_type = new HashMap<>();
                    start_type.put("Pmap", map);//请求参数
                    rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                        // 设置消息过期时间 30 分钟 过期
                        message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                        return message;
                    });
                    Message = " 审核状态已修改 通知指令发送成功！";
                } catch (Exception e) {
                    System.out.println("充值资费 指令发送 " + e.getMessage());
                    Message =  "审核状态已修改 通知指令发送失败！！！";
                }
            }
        }
        Rmap.put("bool",bool);
        Rmap.put("Message",Message);
        return Rmap;
    }
}






















