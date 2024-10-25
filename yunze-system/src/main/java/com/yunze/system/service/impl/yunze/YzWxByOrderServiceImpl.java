package com.yunze.system.service.impl.yunze;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByOrderMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.mapper.SysUserMapper;
import com.yunze.system.service.yunze.IYzUserService;
import com.yunze.system.service.yunze.IYzWxByOrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class YzWxByOrderServiceImpl implements IYzWxByOrderService {

    @Resource
    private YzWxByOrderMapper yzWxByOrderMapper;
    @Resource
    private IYzUserService iYzUserService;
    @Resource
    private SysDictDataMapper dictDataMapper;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SysUserMapper userMapper;
    @Resource
    private YzOrderMapper yzOrderMapper;

    @Override
    public Map<String, Object> findWxOrder(Map<String, Object> map) {
        return yzWxByOrderMapper.findWxOrder(map);
    }

    @Override
    public String shopping_export(Map<String, Object> map, SysUser User) {
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
            String newName = UUID.randomUUID().toString().replace("-","")+"_Shoppingexport";

            String  agent_id = User.getDept().getDeptId().toString();
            String task_name ="购物订单 [导出] ";
            String SaveUrl = "/getcsv/"+newName+".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "12");

            //获取字典信息
            List<SysDictData> order_status = dictDataMapper.selectDictDataByType("yunze_order_status");//订单状态
            List<SysDictData> payment_method = dictDataMapper.selectDictDataByType("yunze_card_pay_type");//支付方式
            List<SysDictData> customize_whether = dictDataMapper.selectDictDataByType("yunze_customize_whether");//自定义是否
            List<SysDictData> card_takeEffect = dictDataMapper.selectDictDataByType("yunze_card_takeEffect_type");//资费计划生效类别

            List<String> ord_noArr= new ArrayList<>();
            for(int i =0; i< outCardIccidArr.size();i++){
                ord_noArr.add(outCardIccidArr.get(i).get("ord_no").toString());
            }
            Map<String, Object> FindMap = new HashMap<String, Object>();
            FindMap.put("ord_no",ord_noArr);
            //获取 订单导出信息
            List<Map<String,Object>> shopping_export = yzWxByOrderMapper.shopping_export(FindMap);



            List<Map<String,Object>> deptArr = userMapper.find_flow();



            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_Shoppingexport_queue";
            String polling_routingKey = "admin.Shoppingexport.queue";
            String polling_exchangeName = "admin_exchange";//路由

            try {
                rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr",outCardIccidArr);
                start_type.put("deptArr",deptArr);
                start_type.put("shopping_export",shopping_export);
                start_type.put("order_status", order_status);
                start_type.put("Payment_method",payment_method);
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
                return "购物订单 导入 操作失败！";
            }
        }else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }

}





























