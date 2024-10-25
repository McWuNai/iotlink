package com.yunze.system.service.impl.yunze;


import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.mapper.SysUserMapper;
import com.yunze.system.service.yunze.IYzCardFlowService;
import com.yunze.system.service.yunze.IYzUserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 订购资费计划 业务实现类
 *
 * @author root
 */
@Service
public class YzCardFlowServiceImpl implements IYzCardFlowService {

    @Resource
    private YzCardFlowMapper cardFlowMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private IYzUserService iYzUserService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private SysDictDataMapper dictDataMapper;
    @Autowired
    private SysUserMapper userMapper;


    @Override
    public List<Map<String, Object>> queryflow(Map<String, Object> map) {
        String agent_id = map.get("agent_id").toString();
        if (agent_id.equals("100")) {
            return cardFlowMapper.queryflow(map);
        } else {
            return cardFlowMapper.queryflow_agent(map);
        }
    }

    @Override
    public Map<String, Object> queryflow_simple(Map<String, Object> map) {
        String agent_id = map.get("agent_id").toString();
        if (agent_id.equals("100")) {
            return cardFlowMapper.queryflow_simple(map);
        } else {
            return cardFlowMapper.queryflowAgent_simple(map);
        }
    }

    @Override
    public List<Map<String, Object>> queryPackage_simple(Map<String, Object> map) {
        String agent_id = map.get("agent_id").toString();
        if (agent_id.equals("100")) {
            return cardFlowMapper.queryPackage_simple(map);
        } else {
            return cardFlowMapper.queryAgentPackage_simple(map);
        }
    }

    @Override
    public List<Map<String, Object>> queryPacket_simple(Map<String, Object> map) {
        List<String> agent_id = (List<String>) map.get("agent_id");
        if (Different.Is_existence(agent_id, "100")) {
            return cardFlowMapper.queryPacket_simple(map);
        } else {
            return cardFlowMapper.queryAgentPacket_simple(map);
        }
    }

    @Override
    public Map<String, Object> FindPacket(Map<String, Object> map) {
        String agent_id = map.get("agent_id").toString();
        if (agent_id.equals("100")) {
            return cardFlowMapper.FindPacket(map);
        } else {
            return cardFlowMapper.FindPacketAgent(map);
        }
    }

    @Override
    public Map<String, Object> getList(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (!Different.Is_existence(agent_id, 100)) {
                List<String> IccidArr = yzCardMapper.findBelongingCard(map);
                map.put("IccidArr", IccidArr);
            }
        }
        Integer rowCount = cardFlowMapper.MapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", cardFlowMapper.getList(map));
        return omp;
    }

    @Override
    public List<Map<String, Object>> sleTime(Map map) {
        return cardFlowMapper.sleTime(map);
    }

    @Override
    public String exportflow(Map<String, Object> map, SysUser User) {
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

        outCardIccidArr = cardFlowMapper.exportArr(map);//查询导出内容

        if (outCardIccidArr != null && outCardIccidArr.size() > 0) {
            String create_by = " [ " + User.getDept().getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
            String newName = UUID.randomUUID().toString().replace("-", "") + "_ExportFlow";

            String agent_id = User.getDept().getDeptId().toString();
            String task_name = "-订购资费 [导出] ";
            String SaveUrl = "/getcsv/" + newName + ".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth", create_by);
            task_map.put("task_name", task_name);
            task_map.put("url", SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "11");

            //获取字典信息
            List<SysDictData> Cardstatus = dictDataMapper.selectDictDataByType("yunze_card_valid_type");//卡状态
            List<SysDictData> packet_type = dictDataMapper.selectDictDataByType("yunze_packet_type");//资费计划类别
            List<SysDictData> takeEffect_type = dictDataMapper.selectDictDataByType("yunze_card_takeEffect_type");//生效类别

            //获取 企业信息
            List<Map<String, Object>> arrorders = cardFlowMapper.exportflow(map);//查询yz_card_packet
            List<Map<String, Object>> deptArr = userMapper.find_flow();//写入企业信息
            List<Map<String, Object>> simpleArr = cardFlowMapper.queryPackage_simple(map);//写入资费组信息
            List<Map<String, Object>> PacketArr = cardFlowMapper.queryPacket_simple(map);//写入资费计划信息

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_ExportFlow_queue";
            String polling_routingKey = "admin.ExportFlow.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
               // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("arrorders", arrorders);//查询yz_card_packet
                start_type.put("User", User);
                start_type.put("outCardIccidArr", outCardIccidArr);
                start_type.put("Cardstatus", Cardstatus);
                start_type.put("packet_type", packet_type);
                start_type.put("takeEffect_type", takeEffect_type);
                start_type.put("deptArr", deptArr);
                start_type.put("PacketArr", PacketArr);
                start_type.put("simpleArr", simpleArr);
                start_type.put("map", map);

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("导出 卡号 失败 " + e.getMessage().toString());
                return "订购资费 导出 操作失败！";
            }
        } else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";

    }


}













