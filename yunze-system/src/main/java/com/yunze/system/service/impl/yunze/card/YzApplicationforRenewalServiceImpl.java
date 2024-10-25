package com.yunze.system.service.impl.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.Arith;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzAgentPacketMapper;
import com.yunze.common.mapper.yunze.YzCardPacketMapper;
import com.yunze.common.mapper.yunze.YzUserMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryIccidMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryOrdNoMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.card.IYzApplicationforRenewalService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YzApplicationforRenewalServiceImpl implements IYzApplicationforRenewalService {

    @Resource
    private YzApplicationforRenewalMapper yzApplicationforRenewalMapper;
    @Resource
    private YzApplicationforRenewalPrimaryIccidMapper yzApplicationforRenewalPrimaryIccidMapper;
    @Resource
    private YzApplicationforRenewalPrimaryMapper yzApplicationforRenewalPrimaryMapper;
    @Resource
    private YzApplicationforRenewalPrimaryOrdNoMapper yzApplicationforRenewalPrimaryOrdNoMapper;
    @Resource
    private YzUserMapper yzUserMapper;
    @Resource
    private YzCardPacketMapper yzCardPacketMapper;
    @Resource
    private YzAgentPacketMapper yzAgentPacketMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;


    @Override
    public Map<String, Object> selMap(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        //订单号查询
        if(map.get("type") != null && map.get("value") != null && map.get("type").toString().length()>0 && map.get("value").toString().length()>0 ){
            String type = map.get("type").toString();
            String value = map.get("value").toString();
            if(type.equals("0")){
                Map<String, Object> findPidMap = new HashMap<String, Object>();
                findPidMap.put("ord_no",value);
                String id = yzApplicationforRenewalPrimaryOrdNoMapper.findPid(findPidMap);
                if(id!=null && id.length()>0){
                    map.put("id",id);
                }
            }
        }
        Integer rowCount = yzApplicationforRenewalPrimaryMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzApplicationforRenewalPrimaryMapper.selMap(map));
        return omp;
    }



    @Override
    public Map<String, Object> save(Map map, SysUser operateMap) {

        Map<String, Object> Rmap = new HashMap<String, Object>();
        String Message = "操作失败！";
        boolean bool = false;
        List<Map<String, Object>>  subBRCardArr = (List<Map<String, Object>>) map.get("subBRCardArr");
        List<String>  packetIdArr = (List<String>) map.get("packetIdArr");
        Map<String, Object>  BRpacketCardCount = (Map<String, Object>) map.get("BRpacketCardCount");

        if(subBRCardArr!=null && subBRCardArr.size()>0){//续费资费计划数组不能为空
            String login_dept_id = map.get("login_dept_id").toString();

            //0. 续费资费组获取成本价
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
            //Double line_of_credit = DeptAmountMap.get("line_of_credit")!=null&&DeptAmountMap.get("line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("line_of_credit").toString()):0.0;//已授信额度
            Double deposit_amount = DeptAmountMap.get("deposit_amount")!=null&&DeptAmountMap.get("deposit_amount").toString().length()>0?Double.parseDouble(DeptAmountMap.get("deposit_amount").toString()):0.0;//预存金额
            //Double used_line_of_credit = DeptAmountMap.get("used_line_of_credit")!=null&&DeptAmountMap.get("used_line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("used_line_of_credit").toString()):0.0;//已使用授信额度
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


                //修改企业金额信息 路由队列
                String polling_queueName = "admin_updDeptAmount_queue";
                String polling_routingKey = "admin.updDeptAmount.queue";
                String polling_exchangeName = "admin_exchange";//路由
                try {
                    // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                    Map<String, Object> start_type = new HashMap<>();
                    start_type.put("type", "RenewalDebit");//启动类型 续费扣款
                    start_type.put("Pmap", map);//请求参数
                    start_type.put("operateMap", operateMap);//操作人信息
                    rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                        // 设置消息过期时间 30 分钟 过期
                        message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                        return message;
                    });
                    bool = true;
                    Message = " 续费资费 指令发送成功！请稍后刷新查看";
                } catch (Exception e) {
                    System.out.println("续费资费 指令发送 " + e.getMessage());
                    Message =  "续费资费 指令发送 失败！";
                }


            }
        }else {
            Message = "续费资费计划不能为空！";
        }

        Rmap.put("bool",bool);
        Rmap.put("Message",Message);
        return Rmap;
    }

    @Override
    public boolean upd(Map map) {
        boolean bool = yzApplicationforRenewalPrimaryMapper.upd(map)>0;
        return bool;
    }

    @Override
    public Map<String, Object> find(Map map) {
        boolean is_Internal = false;
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (Different.Is_existence(agent_id, 100)) {
                is_Internal = true;//内部人员 部门是 100 的 可看字段增加
            }
        } else {
            is_Internal = true;//内部人员 部门是 100 的 可看字段增加
        }
        map.put("is_Internal", is_Internal);
        Map<String, Object> rMap = new HashMap<>();

        rMap.put("primaryMap",yzApplicationforRenewalPrimaryMapper.find(map));
        rMap.put("packetArr",yzApplicationforRenewalMapper.find(map));
        rMap.put("ordArr",yzApplicationforRenewalPrimaryOrdNoMapper.findOrder(map));
        rMap.put("is_Internal",is_Internal);

        return rMap;
    }

    @Override
    public Map<String, Object> findIccid(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;

        Integer rowCount = yzApplicationforRenewalPrimaryIccidMapper.findCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzApplicationforRenewalPrimaryIccidMapper.find(map));
        return omp;
    }


}
