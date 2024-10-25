package com.yunze.system.service.impl.yunze;

import com.alibaba.fastjson.JSON;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzCardFlowHisMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YrootlowHisMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzUserService;
import com.yunze.system.service.yunze.IYzCardFlowHisService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Service
public class YzCradFlowHisServiceImpl implements IYzCardFlowHisService {
    @Resource
    private YrootlowHisMapper yrootlowHisMapper;
    @Resource
    private IYzUserService iYzUserService;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzCardFlowHisMapper yzCardFlowHisMapper;


    @Override
    public Map<String, Object> ListHis(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        boolean internalBool = false;
        List<String> IccidArr = null;
        //权限过滤
        if (map.get("agent_idStr") != null) {
            String agent_id = map.get("agent_idStr").toString();
            if (!agent_id.equals("100")) {
                Map<String, Object> CountMap = new HashMap<String, Object>();
                CountMap.put("agent_id", agent_id);

                String  agent_idC = yzCardMapper.queryChildrenAreaInfo(CountMap);
                String[] agent_idArr = null;
                agent_idArr = agent_idC!=null?(agent_idC.substring(1,agent_idC.length())).split(","):agent_idArr;
                map.put("agent_id", agent_idArr);
                map.put("findIccid",map.get("value"));
                IccidArr = yzCardMapper.findBelongingCard(map);
                map.put("IccidArr", IccidArr);
            }else{
                internalBool = true;
            }
        }
        Integer rowCount = null;
        if(internalBool){
            rowCount = yrootlowHisMapper.selMapHis(map);
        }else  if(!internalBool==false && IccidArr!=null && IccidArr.size()>0){
            rowCount = yrootlowHisMapper.selMapHis(map);
        }
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        if(internalBool){
            omp.put("Data", yrootlowHisMapper.ListHis(map));
        }else if(!internalBool && IccidArr!=null && IccidArr.size()>0){
            omp.put("Data", yrootlowHisMapper.ListHis(map));
        }else{
            omp.put("Data", new ArrayList<Map<String,Object>>());
        }
        return omp;
    }

    @Override
    public Map<String, Object> getById(HashMap<String, Object> map) {
        return yrootlowHisMapper.getById(map);
    }

    @Override
    public String FlowHis(Map<String, Object> map, SysUser User) {

        String dept_id = map.get("dept_id").toString();
        boolean IsGeneralGroupbool = dept_id.equals("100")?true:false;


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

        outCardIccidArr = yrootlowHisMapper.exportFlowHis(map);//查询导出内容 用量详情

        if (outCardIccidArr != null && outCardIccidArr.size() > 0) {
            String create_by = " [ " + User.getDept().getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
            String newName = UUID.randomUUID().toString().replace("-", "") + "_ExportHis";

            String agent_id = User.getDept().getDeptId().toString();
            String task_name = "用量详情导出 [导出] ";
            String SaveUrl = "/getcsv/" + newName + ".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth", create_by);
            task_map.put("task_name", task_name);
            task_map.put("url", SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "11");

            List<String> HisIccid = yrootlowHisMapper.exportFlowHisIccid(map);//用量详情导出 group by iccid 用到的查询
            map.put("iccids", HisIccid);
            List<Map<String, Object>> HisArr = yzCardMapper.hisList(map);//查询主表  分组 、备注、发货时间

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_ExportFlowHis_queue";
            String polling_routingKey = "admin.ExportFlowHis.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("IsGeneralGroupbool", IsGeneralGroupbool);
                start_type.put("iccids", HisIccid);
                start_type.put("HisArr", HisArr);
                start_type.put("User", User);
                start_type.put("outCardIccidArr", outCardIccidArr);
                start_type.put("map", map);

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("导出 卡号 失败 " + e.getMessage().toString());
                return "用量详情 导出 操作失败";
            }

        } else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }

        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";

    }
}
















