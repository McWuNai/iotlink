package com.yunze.task.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.yunze.PlOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 订单表 检查需要执行API请求执行失败 重复执行【系统最大重试执行次数 默认 100 次】 该任务默认每分钟触发一次
 */
@Slf4j
@Component
public class OrderCardApiRequestTaskMQ {


    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private PlOrder plOrder;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    String addPackage_exchangeName = "polling_addPackage_card", addPackage_queueName = "p_addPackage_card", addPackage_routingKey = "p.addPackage.card";

    @RabbitHandler
    @RabbitListener(queues = "admin_orderCardApiRequest_queue")
    public void orderCardApiRequest(String msg) {
        Execution(msg);
    }


    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_orderCardApiRequest_queue")
    public void dlxOrderCardApiRequest(String msg) {
        Execution(msg);
    }



    public void Execution(String msg) {
        try {
            Map<String,Object> configMap = new HashMap<>();

            configMap.put("config_key","yunze.OrderApiRetry.MaxCount");
            String MaxCount = yzWxByProductAgentMapper.findConfig(configMap);
            MaxCount = MaxCount!=null?MaxCount:"100";//默认 最大重试次数请求 【订购达量断网 开机 开网】100 次

            Map<String,Object> findMap = new HashMap<>();
            findMap.put("MaxCount",MaxCount);
            String  endTime = VeDate.getStringDateShort();
            String  staTime = VeDate.getNextDay(endTime,"-3");//获取前3天的数据 【仅处理仅三天的之前的数据不做处理】
            findMap.put("staTime", staTime );
            findMap.put("endTime",endTime);

            findMap.put("type","1");
            List<Map<String, Object>> ddExArr = yzOrderMapper.findRetryOrderApi(findMap);
            findMap.put("type","2");
            List<Map<String, Object>> bootExArr = yzOrderMapper.findRetryOrderApi(findMap);
            findMap.put("type","3");
            List<Map<String, Object>> openNetExArr = yzOrderMapper.findRetryOrderApi(findMap);

            if(ddExArr!=null && ddExArr.size()>0){
                for (int i = 0; i < ddExArr.size(); i++) {
                    Map<String, Object> exMap = ddExArr.get(i);

                    List<String> operationArr = new ArrayList<String>();
                    operationArr.add("InternetDisconnection");//订购达量断网

                    Map<String, Object> CardDiagnosisMap = new HashMap<>();
                    String cre_type = exMap.get("cre_type").toString();//生成方式
                    String pay_type = exMap.get("pay_type").toString();//支付方式
                    String source_type = "9";//变更来源 PC端充值 9  Api变更 8 C端充值 4
                    if(cre_type.equalsIgnoreCase("api")){
                        source_type = "8";
                    }else {
                        if(pay_type.equalsIgnoreCase("wx")){
                            source_type = "4";
                        }
                    }
                    CardDiagnosisMap.put("source_type", source_type);//变更来源

                    Map<String,Object> fMap = new HashMap<>();
                    fMap.put("iccid",exMap.get("iccid"));
                    List<Map<String, Object>> OrderApiArr = yzOrderMapper.findIccidOrderApi(fMap);
                    boolean sendBool = false;
                    if(OrderApiArr.size()==1){//只有一条需要操作API的时候直接 获取 单条 订单的 套餐流量作为 达量断网阈值
                        if (exMap.get("add_parameter") != null) {
                            try {
                                Map<String, Object> add_parameter = JSON.parseObject(exMap.get("add_parameter").toString());//加包参数
                                String errStr = add_parameter.get("error_flow").toString();
                                int eIndex = errStr.indexOf(".");
                                errStr = eIndex!=-1?errStr.substring(0,eIndex):errStr;
                                Integer error_flow = Integer.parseInt(errStr);//获取资费计划  套餐流量
                                CardDiagnosisMap.put("quota", error_flow);//阈值MB
                            } catch (Exception e) {
                                log.error("orderSave > orderCardApiRequest 获取 add_parameter 异常 {} ====  {}", JSON.toJSONString(exMap), e.getMessage());
                            }
                        } else {
                            log.error("orderSave > orderCardApiRequest 获取 add_parameter==null 达量断网订购取消 {} ", JSON.toJSONString(exMap));
                        }
                    }else{//多个需要 达量断网 套餐时 【多资费订购】【资费叠加等情况】
                        int notPerformed = 0;
                        //0.判断当前未执行加包订单数量
                        for (int j = 0; j < OrderApiArr.size(); j++) {
                            Map<String,Object> oMap = OrderApiArr.get(j);
                            String validate_type =  oMap.get("validate_type")!=null?oMap.get("validate_type").toString():"";
                            String add_package =  oMap.get("add_package")!=null?oMap.get("add_package").toString():"";
                            if(add_package.equals("0")){
                                notPerformed +=1;
                                if(notPerformed>2){
                                    break;//大于两条订单未到账跳过累加 已达到判断条件
                                }
                            }
                        }
                        if(notPerformed==0){
                           //都到账了的情况下获取当前账期可用流量总和 作为 达量断网的 阈值 传入
                            String  []YyyyAndMm = com.yunze.common.utils.yunze.VeDate.getYyyyAndMm();
                            String nowMonthSta = YyyyAndMm[0]+"-"+YyyyAndMm[1]+"-01";
                            String nowMonthEnd = com.yunze.common.utils.yunze.VeDate.getLastDayOfMonth(Integer.parseInt(YyyyAndMm[0]),Integer.parseInt(YyyyAndMm[1]));
                            Map<String,Object> fsumMap = new HashMap<>();
                            fsumMap.put("iccid",exMap.get("iccid"));
                            fsumMap.put("nowMonthSta",nowMonthSta+" 00:00:00");
                            fsumMap.put("nowMonthEnd",nowMonthEnd+" 23:59:59");
                            String SumErrorFlow =  yzCardFlowMapper.findSumErrorFlow(fsumMap);
                            if(SumErrorFlow!=null){
                                int eIndex = SumErrorFlow.indexOf(".");
                                SumErrorFlow = eIndex!=-1?SumErrorFlow.substring(0,eIndex):SumErrorFlow;
                                Integer error_flow = Integer.parseInt(SumErrorFlow);//获取资费计划  套餐流量
                                CardDiagnosisMap.put("quota", error_flow);//阈值MB
                            }
                        }else{//当前卡号 发送加包任务 [超过1个未执行加包时业务暂停， 暂不操作 达量断网]
                            Map<String,Object> findAddPackage_Map = new HashMap<>();
                            findAddPackage_Map.put("iccid",exMap.get("iccid"));
                            List<Map<String,Object>> AddPackageArr = yzOrderMapper.findAddPackage(findAddPackage_Map);
                            if(AddPackageArr!=null && AddPackageArr.size()>0){
                                //卡号放入路由
                                for (int j = 0; j < AddPackageArr.size(); j++) {
                                    Map<String, Object> card = AddPackageArr.get(j);
                                    //生产任务
                                    try {
                                        rabbitTemplate.convertAndSend(addPackage_exchangeName, addPackage_routingKey, JSON.toJSONString(card), message -> {
                                            // 设置消息过期时间 time 分钟 过期
                                            message.getMessageProperties().setExpiration("" + (180 * 1000 * 60));
                                            return message;
                                        });
                                        sendBool = true;
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                            }
                        }
                    }

                    if(CardDiagnosisMap.get("quota")!=null){
                        exCommit(operationArr,CardDiagnosisMap,exMap);
                    }else{
                        log.info("CardDiagnosisMap.get(\"quota\")==null 操作取消 sendBool {}  ==  {}  ",sendBool,exMap);
                    }
                }
            }

            if(bootExArr!=null && bootExArr.size()>0) {
                for (int i = 0; i < bootExArr.size(); i++) {
                    Map<String, Object> exMap = bootExArr.get(i);
                    List<String> operationArr = new ArrayList<String>();
                    operationArr.add("PowerOn");//开机
                    Map<String, Object> CardDiagnosisMap = new HashMap<>();
                    exCommit(operationArr,CardDiagnosisMap,exMap);

                }
            }
            if(openNetExArr!=null && openNetExArr.size()>0) {
                for (int i = 0; i < openNetExArr.size(); i++) {
                    Map<String, Object> exMap = openNetExArr.get(i);
                    List<String> operationArr = new ArrayList<String>();
                    operationArr.add("OpenNetwork");//开网
                    Map<String, Object> CardDiagnosisMap = new HashMap<>();
                    exCommit(operationArr,CardDiagnosisMap,exMap);
                }
            }

        }catch (Exception e){
            log.error(">>错误 - admin_orderCardApiRequest_queue 消费者:{}<<", e.getMessage());
        }
    }




    public void exCommit(List<String> operationArr,Map<String, Object> CardDiagnosisMap,Map<String, Object> exMap){
        CardDiagnosisMap.put("optionType", operationArr);
        CardDiagnosisMap.put("iccid", exMap.get("iccid"));
        Map<String, Object> updOrderMap = new HashMap<>();
        updOrderMap.put("ord_no",exMap.get("ord_no"));
        CardDiagnosisMap.put("updOrderMap", updOrderMap);
        plOrder.orderCardDiagnosisSend(CardDiagnosisMap);
    }




}
