package com.yunze.iotapi.service.impl;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.AesEncryptUtil;
import com.yunze.apiCommon.utils.ApiUtil_NoStatic;
import com.yunze.apiCommon.utils.Arith;
import com.yunze.common.mapper.yunze.YzAgentPacketMapper;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.mapper.yunze.YzUserMapper;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import com.yunze.common.utils.yunze.PlOrder;
import com.yunze.iotapi.service.IOpenApi;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenApiServiceImpl implements IOpenApi {

    @Resource
    private ApiUtil_NoStatic apiUtil_NoStatic;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private PlOrder plOrder;
    @Resource
    private YzAgentPacketMapper yzAgentPacketMapper;
    @Resource
    private YzUserMapper yzUserMapper;
    @Resource
    private YzOrderMapper yzOrderMapper;

    @Override
    public Map<String, Object> simDataUsage(Map<String, Object> Param) {
        boolean bool = false;
        String Message = "";
        Map<String, Object> rMap = new HashMap<String, Object>();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String,Object> Rdata = apiUtil_NoStatic.queryFlow(Param);
        String code = Rdata.get("code").toString();
        if(code.equals("200")){
            returnMap.put("useAmount",Rdata.get("Use"));
            returnMap.put("useUnit","MB");
            bool = true;
            try {
                if(Rdata.get("Use")!=null &&  Rdata.get("Use")!="" &&  Rdata.get("Use").toString().trim().length()>0){
                    Double Use = Double.parseDouble(Rdata.get("Use").toString());
                    if(Use>=0){
                        String polling_queueName = "admin_ApiSynCardUsed_queue";
                        String polling_routingKey = "admin.ApiSynCardUsed.queue";
                        String polling_exchangeName = "admin_exchange";//路由
                        String iccid = Param.get("iccid").toString();
                        Map<String, Object> Data_map = new HashMap<>();
                        Data_map.put("iccid",iccid);
                        Data_map.put("Use",Use);
                        rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(Data_map), message -> {
                            // 设置消息过期时间 30 分钟 过期
                            message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                            return message;
                        });
                    }
                }

            } catch (Exception e) {
                System.out.println("发送 API同步用量 指令失败 " + e.getMessage());
            }
        }else{
            Message = Rdata.get("Message").toString();
        }
        rMap.put("bool",bool);
        rMap.put("code",code);
        rMap.put("Message",Message);
        rMap.put("Data",returnMap);
        return rMap;
    }

    @Override
    public Map<String, Object> simStatus(Map<String, Object> Param) {
        boolean bool = false;
        String Message = "";
        Map<String, Object> rMap = new HashMap<String, Object>();

        Map<String,Object> Rdata = apiUtil_NoStatic.queryCardStatus(Param);
        String code = Rdata.get("code").toString();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if(code.equals("200")){
            String statusCode = Rdata.get("statusCode").toString();
            Map<String,String> Rmap = getShowStatIdArr.GetShowStatIdMap(statusCode);
            String status_ShowId = Rmap.get("status_ShowId");
            String statusMessage = Rmap.get("statusMessage");
            returnMap.put("statusCode", status_ShowId);
            returnMap.put("statusMessage", statusMessage);
            bool = true;
            try {
                if(Rdata.get("statusCode")!=null &&  Rdata.get("statusCode")!="" &&  Rdata.get("statusCode").toString().trim().length()>0){
                    if(!statusCode.equals("0")){
                        String polling_queueName = "admin_ApiSynCardStatus_queue";
                        String polling_routingKey = "admin.ApiSynCardStatus.queue";
                        String polling_exchangeName = "admin_exchange";//路由
                        String iccid = Param.get("iccid").toString();
                        Map<String, Object> Data_map = new HashMap<>();
                        Data_map.put("iccid",iccid);
                        Data_map.put("status_id",statusCode);
                        rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(Data_map), message -> {
                            // 设置消息过期时间 30 分钟 过期
                            message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                            return message;
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println("发送 API同步状态 指令失败 " + e.getMessage());
            }
        }else{
            Message = Rdata.get("Message").toString();
        }
        rMap.put("bool",bool);
        rMap.put("code",code);
        rMap.put("Message",Message);
        rMap.put("Data",returnMap);
        return rMap;
    }

    @Override
    public Map<String, Object> changeSimStatus(Map<String, Object> Param,String Is_Stop) {
        boolean bool = false;
        String Message = "";
        Map<String, Object> rMap = new HashMap<String, Object>();

        Map<String,Object> Rdata = apiUtil_NoStatic.changeCardStatus(Param);
        String code = Rdata.get("code").toString();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if(code.equals("200")){
            bool = true;
            try {
                String polling_queueName = "admin_ApiSynUpdCardStatus_queue";
                String polling_routingKey = "admin.ApiSynUpdCardStatus.queue";
                String polling_exchangeName = "admin_exchange";//路由
                String iccid = Param.get("iccid").toString();
                Map<String, Object> Data_map = new HashMap<>();
                Data_map.put("iccid",iccid);
                Data_map.put("Is_Stop",Is_Stop);
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(Data_map), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("发送 API同步状态 指令失败 " + e.getMessage());
            }
        }else{
            Message = Rdata.get("Message").toString();
        }
        rMap.put("bool",bool);
        rMap.put("code",code);
        rMap.put("Message",Message);
        rMap.put("Data",returnMap);
        return rMap;
    }

    @Override
    public Map<String, Object> simApnFunction(Map<String, Object> Param, String Str_StatusBreak) {
        boolean bool = false;
        String Message = "";
        Map<String, Object> rMap = new HashMap<String, Object>();

        Map<String,Object> Rdata = apiUtil_NoStatic.FunctionApnStatus(Param);
        String code = Rdata.get("code").toString();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if(code.equals("200")){
            bool = true;
            try {
                String polling_queueName = "admin_ApiSynUpdCardConnectionStatus_queue";
                String polling_routingKey = "admin.ApiSynUpdCardConnectionStatus.queue";
                String polling_exchangeName = "admin_exchange";//路由
                String iccid = Param.get("iccid").toString();
                Map<String, Object> Data_map = new HashMap<>();
                Data_map.put("iccid",iccid);
                Data_map.put("Is_Break",Str_StatusBreak);
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(Data_map), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("发送 API同步状态 指令失败 " + e.getMessage());
            }
        }else{
            Message = Rdata.get("Message").toString();
        }
        rMap.put("bool",bool);
        rMap.put("code",code);
        rMap.put("Message",Message);
        rMap.put("Data",returnMap);
        return rMap;
    }

    @Override
    public Map<String, Object> subscribe(Map<String, Object> ParamMap, Map<String, Object> agentAccount) {
        boolean bool = false;
        String Message = "";
        String code = "400";
        Map<String, Object> rMap = new HashMap<String, Object>();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();

        Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
        Object companyId  =  Param.get("companyId");
        Object blocPlantId  =  Param.get("blocPlantId");
        Object effectiveType  =  Param.get("effectiveType");
        String callbackAddress  = "";
        boolean cbBool = true;
        if(Param.get("callbackAddress")!=null){
            cbBool = false;
             callbackAddress  =  Param.get("callbackAddress").toString();
            if(callbackAddress.indexOf("http://") != -1 || callbackAddress.indexOf("https://") != -1){// 回调通知地址 必须是 http:// 或 https:// 开头
                cbBool = true;
            }
        }


        Object iccid  =  Param.get("iccid");
        String agentID = agentAccount.get("agent_id").toString();

        List<String> packetIdArr = new ArrayList<>();
        List<String> iccidArr = new ArrayList<>();
        if(cbBool){
            if(companyId!=null && blocPlantId!=null && effectiveType!=null && iccid!=null
                    && companyId.toString().length()>0 && blocPlantId.toString().length()>0 && effectiveType.toString().length()>0 && iccid.toString().length()>0){
                String Str_effectiveType = effectiveType.toString();
                if(Str_effectiveType.equals("1") || Str_effectiveType.equals("2") || Str_effectiveType.equals("3")){
                    boolean continueBool = true;//是否继续执行
                    //判断 充值卡号 是否首次充值
                    Integer firstOrder = yzOrderMapper.findFirstOrder(Param);
                    firstOrder = firstOrder!=null?firstOrder:0;
                    if(firstOrder>0){
                        if(!Str_effectiveType.equals("3")){//非首次充值 生效类型 非 周期延顺 禁止充值
                            continueBool = false;
                            Message = "非首次充值 生效类型 effectiveType 因填入 3 周期延顺 ！";
                        }
                    }
                    if(continueBool){
                        //判断 传入的 企业编号 是否与操企业编号一致
                        if(agentID.equals(companyId)){
                            Integer count = yzCardRouteMapper.findSubscribe(Param);
                            if(count!=null && count>0){
                                String login_dept_id = agentID;
                                packetIdArr.add(blocPlantId.toString());
                                //0. 充值资费组获取成本价
                                Map<String,Object> findPacketMap = new HashMap<>();
                                findPacketMap.put("login_dept_id",login_dept_id);
                                findPacketMap.put("packetIdArr",packetIdArr);
                                List<Map<String,Object>> packetArr = new ArrayList<>();
                                packetArr = yzAgentPacketMapper.findPacketSingleTable(findPacketMap);

                                Double amount = 0.0;//总计金额
                                Double packet_price = 0.0;//资费金额
                                int card_sumCount = 0;//续费卡总数
                                for (int i = 0; i < packetArr.size(); i++) {//默认是单卡充值所以 直接 赋值 packet_price
                                    Map<String,Object> packe = packetArr.get(i);
                                    Double packet_cost = Double.parseDouble(packe.get("packet_cost").toString());//成本
                                    packet_price = packet_cost;
                                    int packetCardCount = 1;//数量
                                    card_sumCount = (int) Arith.add(card_sumCount,packetCardCount);
                                    amount = Arith.add(amount,Arith.mul(packetCardCount,packet_cost));
                                }
                                //生成订单新增参数
                                iccidArr.add(iccid.toString());
                                map.put("agent_id",companyId);
                                map.put("validate_type",effectiveType);
                                map.put("packet_price",packet_price);
                                map.put("packet_id",blocPlantId);
                                map.put("iccid",iccid);
                                map.put("package_id",packetArr.get(0).get("package_id"));
                                map.put("callbackAddress",callbackAddress);
                                map.put("OrderMap", plOrder.ApiorderOpen(map));

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

                                if(be_usable_line_of_credit<amount && deposit_amount<amount){//如 预计剩余可用预存金额 < 总扣款 且 预存金额 < 总扣款
                                    is_NeedToRecharge = true;
                                }
                                if(forecast_deposit_amount<0 && be_usable_line_of_credit==0){//如 预计剩余可用预存金额 <0 且 可用授信额度 == 0
                                    is_NeedToRecharge = true;
                                }else  if(forecast_deposit_amount<0 && be_usable_line_of_credit>0){//如 预计剩余可用预存金额 <0 且 可用授信额度》0 》》 继续判断可用授信是否足够扣款

                                    forecast_be_usable_line_of_credit = com.yunze.common.utils.Arith.sub(be_usable_line_of_credit,amount);//预计剩余 可用授信 = 剩余可用授信 - 总扣款 【单类型支付时】
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
                                    code = "500";
                                    Map<String,Object> dataMap = new HashMap<>();
                                    dataMap.put("deptName",agentAccount.get("agent_name").toString());
                                    dataMap.put("deptId",login_dept_id);
                                    String pStr = "";
                                    try {
                                        pStr = AesEncryptUtil.encrypt(JSON.toJSONString(dataMap));
                                    }catch (Exception e){

                                    }
                                    Message = "本次操作需扣款 [ "+amount+" ] 还需 [ "+stillNeeded+" ] ,您的账户目前可用预存/授信不足，请点击['去预存'](http://i.5iot.cn/system/deptPrestore/"+pStr+")！";
                                }else{
                                    //续费扣款指令发送
                                    Map<String, Object> primaryMap = new HashMap<>();
                                    primaryMap.put("amount",amount);
                                    primaryMap.put("card_sumCount",card_sumCount);
                                    map.put("primaryMap",primaryMap);
                                    map.put("agent_id",login_dept_id);
                                    map.put("dept_id",login_dept_id);

                                    map.put("deptName",agentAccount.get("agent_name").toString());
                                    map.put("packetArr",packetArr);

                                    //修改企业金额信息 路由队列
                                    String polling_queueName = "admin_updDeptAmount_queue";
                                    String polling_routingKey = "admin.updDeptAmount.queue";
                                    String polling_exchangeName = "admin_exchange";//路由
                                    try {
                                        Map<String, Object> operateMap = new HashMap<>();
                                        Map<String, Object> dept = new HashMap<>();
                                        dept.put("deptName",agentAccount.get("agent_name").toString());
                                        operateMap.put("dept",dept);
                                        Map<String, Object> start_type = new HashMap<>();
                                        start_type.put("type", "ApiRechargeOne");//启动类型 API充值单条数据
                                        start_type.put("Pmap", map);//请求参数
                                        start_type.put("iccidArr", iccidArr);
                                        start_type.put("operateMap", operateMap);//操作人信息.

                                        rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                                            // 设置消息过期时间 30 分钟 过期
                                            message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                                            return message;
                                        });
                                        bool = true;
                                        Message = " 充值资费 指令发送成功！请稍后刷新查看";
                                    } catch (Exception e) {
                                        code = "500";
                                        System.out.println("充值资费 指令发送 " + e.getMessage());
                                        Message =  "充值资费 指令发送 失败！";
                                    }
                                }
                            }else{
                                Message = "未找到 企业编号 【"+companyId+"】下 资费计划 【"+blocPlantId+"】请核实后重试 ！";
                            }
                        }else{
                            Message = "操作企业编号不是您所在的企业请核对后重试！";
                        }
                    }

                }else{
                    Message = "effectiveType 生效类型 错误请核对后重试！";
                }
            }else{
                Message = "参数缺失！请核对文档";
            }
        }else{
            Message = "回调通知地址传入时，回调通知地址 必须是 http:// 或 https:// 开头";
        }

        rMap.put("bool",bool);
        rMap.put("code",code);
        rMap.put("Message",Message);
        rMap.put("Data",returnMap);
        return rMap;
    }
}
