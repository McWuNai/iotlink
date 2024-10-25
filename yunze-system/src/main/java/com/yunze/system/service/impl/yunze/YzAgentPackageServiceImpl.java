package com.yunze.system.service.impl.yunze;


import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.DataScope;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzAgentPackageMapper;
import com.yunze.common.mapper.yunze.YzAgentPacketMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.service.yunze.IYzAgentPackageService;
import com.yunze.system.service.yunze.IYzUserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 资费组 业务实现类
 * 
 * @author root
 */
@Service
public class YzAgentPackageServiceImpl implements IYzAgentPackageService
{

   
    @Resource
    private YzAgentPackageMapper yzAgentPackageMapper;
    @Resource
    private YzAgentPacketMapper yzAgentPacketMapper;
    @Resource
    private IYzUserService iYzUserService;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private SysDictDataMapper sysDictDataMapper;



    @Override
    @DataScope(deptAlias = "d" , userAlias = "a" , isMap=true)
    public Map<String, Object> selMap(Map<String, Object> map) {
        Map<String, Object> omp=new HashMap<String, Object>();

        //查询数据前过滤 资费计划 条件数据 判断 是否选择资费计划 查询条件未选择时 不做条件限制
        Object packetType = map.get("packetType");
        Object packetValue = map.get("packetValue");
        boolean Sel = false;
        if(packetValue!=null  && packetType !=null && packetValue.toString().trim().length()>0 && packetType.toString().trim().length()>0){
            Sel = true;
        }
        if(!Sel){
            List<String> packet_valid_type = (List<String>) map.get("packet_valid_type");
            List<String> in_stock = (List<String>) map.get("in_stock");
            List<String> is_month = (List<String>) map.get("is_month");
            List<String> balance_pay = (List<String>) map.get("balance_pay");
            List<String> wechat_pay = (List<String>) map.get("wechat_pay");
            Sel = packet_valid_type!=null && packet_valid_type.size()>0?true:Sel;
            if(!Sel){ Sel = in_stock!=null && in_stock.size()>0?true:Sel;}
            if(!Sel){ Sel = is_month!=null && is_month.size()>0?true:Sel;}
            if(!Sel){ Sel = balance_pay!=null && balance_pay.size()>0?true:Sel;}
            if(!Sel){ Sel = wechat_pay!=null && wechat_pay.size()>0?true:Sel;}
        }

        List<String> package_idArr = null;
        if(Sel){
            Map<String, Object> getPacketMap = map ;
            getPacketMap.remove("pageNum");
            getPacketMap.remove("pageSize");
            package_idArr = yzAgentPacketMapper.getPackage_id(getPacketMap);
            map.put("package_idArr",package_idArr);
            if(package_idArr!=null && package_idArr.size()>0){
            }else{
                omp.put("Pu", new PageUtil(1,1 , 10));
                omp.put("Data", new ArrayList<Map<String,Object>>());
                return  omp;//选择了 资费计划 查询条件 5r未查询到 资费计划时 直接跳出查询返回 null
            }
        }

        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;

        Map<String, Object> CountMap = new HashMap<>();
        CountMap.putAll(map);
        CountMap.remove("pageNum");
        CountMap.remove("pageSize");
        List<String> CountArr = yzAgentPackageMapper.selMapCount(CountMap);
        Integer rowCount = CountArr!=null && CountArr.size()>0?CountArr.size():0;

        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        List<Map<String,Object>> cardPackageArr = yzAgentPackageMapper.selMap(map);
        omp.put("Data", cardPackageArr);
        //如果查询到了资费组信息 获取 资费计划
        if(cardPackageArr!=null && cardPackageArr.size()>0){
            List<String> package_id = new ArrayList<String>();
            //获取查询的 资费组 ID
            for (int i = 0; i <cardPackageArr.size() ; i++) {
                package_id.add(cardPackageArr.get(i).get("package_id").toString());
            }
            map.put("package_id",package_id);
            //添加  资费组 ID 查询条件
            map.put("package_id",package_id);
            map.remove("pageNum");
            map.remove("pageSize");
            omp.put("PackeData", yzAgentPacketMapper.FindList(map));
        }

        return omp;
    }

    @Override
    public boolean add(Map<String, Object> map) {
        map.put("package_id","YZ"+ VeDate.getNo(4));
        return yzAgentPackageMapper.add(map)>0;
    }

    @Override
    public Map<String, Object> find(Map<String, Object> map) {
        List<Map<String, Object>> Rarr = yzAgentPackageMapper.find(map);
        if(Rarr!=null && Rarr.size()>0){
            return yzAgentPackageMapper.find(map).get(0);
        }else{
            return null ;
        }
    }

    @Override
    public List<Map<String, Object>> findPackage(Map<String, Object> map) {
        List<String> agent_id = (List<String>) map.get("agent_id");
        if(Different.Is_existence(agent_id,"100")){
            return yzAgentPackageMapper.find(map);
        }else{
            return  yzAgentPackageMapper.find(map);
        }
    }

    @Override
    public boolean update(Map<String, Object> map) {
        return yzAgentPackageMapper.update(map)>0;
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
        outCardIccidArr = yzAgentPackageMapper.exportallorders(map);

        if(outCardIccidArr!=null && outCardIccidArr.size()>0){
            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            String newName = UUID.randomUUID().toString().replace("-","")+"_ExportAge";//执行任务导出类别
            String  agent_id = User.getDept().getDeptId().toString();
            String task_name = "客户资费导出 [导出]";


            String SaveUrl = "/getcsv/"+newName+".csv";
            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "36");//执行任务类别
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表

            //获取字典信息

            String polling_queueName = "admin_ExportaAge_queue";
            String polling_routingKey = "admin.ExportaAge.queue";
            String polling_exchangeName = "admin_exchange";//路由

            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr",outCardIccidArr);
                start_type.put("map",map);

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            }catch (Exception e){
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return "客户资费导出 导入 操作失败！";
            }
        }else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }




    @Override
    public String exportPacket(Map<String, Object> map, SysUser User) {
        Object MapAgent_id = map.get("agent_id");
        //导出时 未选中 当前 企业编号时 且登录 部门不是 总平台 赋值部门
        String dept_id = User.getDeptId().toString();
        if(MapAgent_id==null && User.getDeptId()!=100){
            List<String> agent_idArr = new ArrayList<String>();
            agent_idArr.add(""+dept_id);
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

        outCardIccidArr = yzAgentPacketMapper.exportPacket(map);

        if(outCardIccidArr!=null && outCardIccidArr.size()>0){
            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            String newName = UUID.randomUUID().toString().replace("-","")+"_ExportPacket";//执行任务导出类别
            String  agent_id = User.getDept().getDeptId().toString();
            String task_name = "企业资费计划导出 [导出]";

            String SaveUrl = "/getcsv/"+newName+".csv";
            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "40");//资费计划导出
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表

            List<SysDictData> customize_whether = sysDictDataMapper.selectDictDataByType("yunze_customize_whether");//系统是否

            String polling_queueName = "admin_ExportPacket_queue";
            String polling_routingKey = "admin.ExportPacket.queue";
            String polling_exchangeName = "admin_exchange";//路由

            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outDataArr",outCardIccidArr);
                start_type.put("map",map);
                start_type.put("customize_whether",customize_whether);

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            }catch (Exception e){
                System.out.println("导出 资费组 失败 " + e.getMessage().toString());
                return "导出 资费组 操作失败！";
            }
        }else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }










}





























