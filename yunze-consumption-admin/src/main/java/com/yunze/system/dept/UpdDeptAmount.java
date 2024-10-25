package com.yunze.system.dept;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.*;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryIccidMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryOrdNoMapper;
import com.yunze.common.utils.Arith;
import com.yunze.common.utils.yunze.BulkUtil;
import com.yunze.common.utils.yunze.PlOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 企业 预存金额 授信额度 授信支付 消费者
 */
@Slf4j
@Component
public class UpdDeptAmount {

    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzUserMapper yzUserMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzApplicationforRenewalMapper yzApplicationforRenewalMapper;
    @Resource
    private YzApplicationforRenewalPrimaryIccidMapper yzApplicationforRenewalPrimaryIccidMapper;
    @Resource
    private YzApplicationforRenewalPrimaryMapper yzApplicationforRenewalPrimaryMapper;
    @Resource
    private YzApplicationforRenewalPrimaryOrdNoMapper yzApplicationforRenewalPrimaryOrdNoMapper;
    @Resource
    private YzCardPacketMapper yzCardPacketMapper;
    @Resource
    private YzAgentPacketMapper yzAgentPacketMapper;
    @Resource
    private PlOrder plOrder;
    @Resource
    private BulkUtil bulkUtil;
    @Resource
    private RedisCache redisCache;

    /**
     * 企业 预存金额 授信额度 授信支付 消费者
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_updDeptAmount_queue")
    public void updDeptAmount(String msg, Channel channel){
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String type = map.get("type").toString();
            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("Pmap");//请求参数
            Map<String,Object> operateMap =  ( Map<String,Object>)map.get("operateMap");//操作人信息


            switch (type){
                case "updCredit"://变更授信信息
                    updCredit(Pmap,operateMap);//执行连接设置
                    break;
                /*case "Prestore"://代理 预存金额
                    Prestore(Pmap,operateMap);//
                    break;*/
                case "RenewalDebit"://代理 扣款
                    Map<String,Object> primaryMap =  ( Map<String,Object>)Pmap.get("primaryMap");//请求参数
                    Debit(null,null,null,null,null,Pmap,operateMap,primaryMap,"RenewalDebit");//
                    break;

                case "Recharge"://企业批量充值
                    String filePath = map.get("filePath").toString();//项目根目录
                    String newName = map.get("newName").toString();//输出文件名
                    Map<String,Object> primaryMap_1 =  ( Map<String,Object>)Pmap.get("primaryMap");//请求参数
                    Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息
                    Map<String,Object> OrderMap =  ( Map<String,Object>)Pmap.get("OrderMap");//添加订单生成参数
                    Map<String,Object>  bulkMap = ( Map<String,Object>)map.get("bulkMap");//批量任务主表 信息
                    List<String> iccidArr = (List<String>) Pmap.get("iccidArr");//文本域卡号
                    Debit(filePath,newName,OrderMap,bulkMap,iccidArr,Pmap,operateMap,primaryMap_1,"Recharge");
                    break;
                case "ApiRechargeOne"://企业 API充值
                    Map<String,Object> primaryMap_2 =  ( Map<String,Object>)Pmap.get("primaryMap");//请求参数
                    Map<String,Object> OrderMap_1 =  ( Map<String,Object>)Pmap.get("OrderMap");//添加订单生成参数
                    List<String> iccidArr_1 = (List<String>) Pmap.get("iccidArr");//文本域卡号
                    Debit("","",OrderMap_1,null,iccidArr_1,Pmap,operateMap,primaryMap_2,"ApiRechargeOne");
                    break;
                default:
                    log.info(">> switch - default   mytype:{}  msg:{}<<", type,msg);
                    break;
            }


        } catch (Exception e) {
            log.error(">>错误 - 企业 预存金额 授信额度 授信支付 消费者 :{} :{}<<", msg,e.getMessage());
        }
    }





    /**
     * 企业 预存金额 修改 消费者 [充值 扣款都 走这里]
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_UpdDepositAmount_queue")
    public void UpdDepositAmount(String msg, Channel channel){
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("Pmap");//请求参数
            Map<String,Object> operateMap =  ( Map<String,Object>)map.get("operateMap");//操作人信息

            UpdDAmount(Pmap,operateMap);

        } catch (Exception e) {
            log.error(">>错误 - 企业 预存金额 授信额度 授信支付 消费者 :{} :{}<<", msg,e.getMessage());
        }
    }




    /**
     * 修改授信额度
     * @param Pmap
     * @param operateMap
     */
    public void updCredit( Map<String,Object> Pmap ,Map<String,Object> operateMap){


        if(Pmap.get("agent_id")!=null && Pmap.get("agent_id").toString().length()>0 && Pmap.get("line_of_credit")!=null && Pmap.get("line_of_credit").toString().length()>0){
            String agent_id = Pmap.get("agent_id").toString();
            Double line_of_credit = Double.parseDouble(Pmap.get("line_of_credit").toString());

            //1. 授信额度 - （ 获取 订单 SUM(交易类型为 [用量充值] & 支付方式[授信支付]) - SUM(预存抵扣) ） = 已使用额度

            Double D_CreditPrice = 0.0;
            Double D_DeductPrice = 0.0;

            List<String> agent_idArr =  new ArrayList<>();
            agent_idArr.add(agent_id);

            Map<String,Object> OrderpMap = new HashMap<>();
            OrderpMap.put("ord_type","2");
            OrderpMap.put("pay_type","ct");
            OrderpMap.put("status","1");//支付状态 = 支付成功
            OrderpMap.put("agent_id",agent_idArr);
            String SumCreditPrice = yzOrderMapper.getSumPrice(OrderpMap);
            D_CreditPrice = SumCreditPrice!=null?Double.parseDouble(SumCreditPrice):D_CreditPrice;

            Map<String,Object> deductMap = new HashMap<>();
            deductMap.put("ord_type","6");
            deductMap.put("agent_id",agent_idArr);
            deductMap.put("status","1");//支付状态 = 支付成功
            deductMap.put("pay_type","ct");
            String SumDeductPrice = yzOrderMapper.getSumPrice(deductMap);
            D_DeductPrice = SumDeductPrice!=null?Double.parseDouble(SumDeductPrice):D_DeductPrice;


            Double used_line_of_credit = Arith.sub(D_CreditPrice,D_DeductPrice);//已使用授信额度
            Double be_usable_line_of_credit = Arith.sub(line_of_credit,used_line_of_credit);//可使用授信额度

            String result = "1";
            String remarks = "";


            //2.准备   金额变更记录表 新增数据
            Map<String,Object> FindDeptAmountMap = new HashMap<>();
            FindDeptAmountMap.put("dept_id",agent_id);
            Map<String,Object> DeptAmountMap =  yzUserMapper.findDeptAmount(FindDeptAmountMap);

            try {
                Map<String,Object> updCreditMap = new HashMap<>();
                updCreditMap.put("used_line_of_credit",used_line_of_credit);
                updCreditMap.put("be_usable_line_of_credit",be_usable_line_of_credit);
                updCreditMap.put("line_of_credit",line_of_credit);
                updCreditMap.put("dept_id",agent_id);
                int updCOunt = yzUserMapper.updCredit(updCreditMap);
                remarks = "操作成功 updCOunt "+updCOunt;
            }catch (Exception e){
                result = "2";
                remarks = e.getMessage()!=null?e.getMessage():remarks;
                remarks = remarks.length()>150?remarks.substring(0,150):remarks;
                log.error("yzUserMapper.updCredit Exception {}",e.getMessage());

            }

            Map<String,Object> par_valMap = new HashMap<>();
            par_valMap.put("Pmap",Pmap);
            par_valMap.put("operateMap",operateMap);


            String mytable_name = "sys_dept";
            String mytype = "2";
            String myfunction_name = "updCredit";
            String par_val = JSON.toJSONString(par_valMap);
            String sel_key = "dept_id";
            String sel_val = agent_id;


            Map<String,Object> add_common = new HashMap<>();
            add_common.put("mytable_name",mytable_name);
            add_common.put("sel_key",sel_key);
            add_common.put("sel_val",sel_val);
            add_common.put("result",result);
            add_common.put("par_val",par_val);
            add_common.put("remarks",remarks);
            add_common.put("myfunction_name",myfunction_name);
            add_common.put("mytype",mytype);


            Map<String,Object> add_row1 = new HashMap<>();
            add_row1.put("mybefore",DeptAmountMap.get("line_of_credit")!=null?DeptAmountMap.get("line_of_credit").toString():"");
            add_row1.put("change_value",line_of_credit.toString());
            add_row1.put("field_key","line_of_credit");
            add_row1.put("field_val","已授信额度");

            Map<String,Object> add_row2 = new HashMap<>();
            add_row2.put("mybefore",DeptAmountMap.get("used_line_of_credit")!=null?DeptAmountMap.get("used_line_of_credit").toString():"");
            add_row2.put("change_value",used_line_of_credit.toString());
            add_row2.put("field_key","used_line_of_credit");
            add_row2.put("field_val","已使用授信额度");

            Map<String,Object> add_row3 = new HashMap<>();
            add_row3.put("mybefore",DeptAmountMap.get("be_usable_line_of_credit")!=null?DeptAmountMap.get("be_usable_line_of_credit").toString():"");
            add_row3.put("change_value",be_usable_line_of_credit.toString());
            add_row3.put("field_key","be_usable_line_of_credit");
            add_row3.put("field_val","可使用授信额度");


            List<Map<String,Object>> insertArr = new ArrayList<>();

            insertArr.add(add_row1);
            insertArr.add(add_row2);
            insertArr.add(add_row3);

            add_common.put("saveArr",insertArr);

            //修改企业金额信息 路由队列
            String polling_routingKey = "admin.insertMoneyChangeRecord.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                Map<String, Object> start_mytype = new HashMap<>();
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(add_common), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                log.error("admin_insertMoneyChangeRecord_queue 发送 失败 {} {}",add_common,e.getMessage() );
            }

        }else{
            log.error(">>updCredit 参数不全取消操作 Pmap:{} operateMap:{}<<", Pmap,operateMap);
        }


    }

    /**
     * 修改 预存金额
     * @param Pmap
     * @param operateMap
     */
    public void UpdDAmount( Map<String,Object> Pmap ,Map<String,Object> operateMap){
        if(Pmap.get("agent_id")!=null && Pmap.get("agent_id").toString().length()>0 && Pmap.get("deposit_amount")!=null && Pmap.get("deposit_amount").toString().length()>0
                && Pmap.get("operateType")!=null && Pmap.get("operateType").toString().length()>0) {
            String agent_id = Pmap.get("agent_id").toString();
            Double deposit_amount = Double.parseDouble(Pmap.get("deposit_amount").toString());
            String operateType = Pmap.get("operateType").toString();

            //准备   金额变更记录表 新增数据
            Map<String,Object> FindDeptAmountMap = new HashMap<>();
            FindDeptAmountMap.put("dept_id",agent_id);
            Map<String,Object> DeptAmountMap =  yzUserMapper.findDeptAmount(FindDeptAmountMap);
            String result = "1";
            String remarks = "";

            try {
                int updCOunt =  yzUserMapper.updDepositAmount(Pmap);
                remarks = ""+operateType+" 操作成功 updCOunt "+updCOunt;
            }catch (Exception e){
                result = "2";
                remarks = e.getMessage()!=null?e.getMessage():remarks;
                remarks = remarks.length()>150?remarks.substring(0,150):remarks;
                remarks = ""+operateType+" "+remarks;
                log.error("yzUserMapper.updCredit Exception {}",e.getMessage());

            }
            String mytable_name = "sys_dept";
            String mytype = "2";
            String myfunction_name = "updDepositAmount";
            String sel_key = "dept_id";
            String sel_val = agent_id;
            String mybefore = DeptAmountMap.get("deposit_amount")!=null?DeptAmountMap.get("deposit_amount").toString():"";
            String change_value = deposit_amount.toString();
            String field_key = "deposit_amount";
            String field_val = Pmap.get("field_val")!=null?Pmap.get("field_val").toString():"预存金额";

            insertMoneyChangeRecord(Pmap,operateMap,result,remarks,mybefore,change_value,mytable_name,mytype,myfunction_name,sel_key,sel_val,field_key,field_val);


        }


    }


    /**
     * 新增 金额变更记录表
     * @param Pmap 请求参数
     * @param operateMap 操作人信息
     * @param result 执行结果
     * @param remarks 备注
     * @param mybefore 变更前
     * @param change_value 变更值
     * @param mytable_name 变更表名
     * @param mytype 变更类型
     * @param myfunction_name 函数名
     * @param sel_key 关联查询 字段
     * @param sel_val 关联查询 字段值
     * @param field_key 变更字段
     * @param field_val 变更字段介绍
     */
    public void insertMoneyChangeRecord(Map<String,Object> Pmap ,Map<String,Object> operateMap,String result,String remarks,String mybefore,String change_value,
                                        String mytable_name,String mytype,String myfunction_name,String sel_key,String sel_val,String field_key,String field_val){
        Map<String,Object> par_valMap = new HashMap<>();
        par_valMap.put("Pmap",Pmap);
        par_valMap.put("operateMap",operateMap);

        String par_val = JSON.toJSONString(par_valMap);


        Map<String,Object> add_common = new HashMap<>();
        add_common.put("mytable_name",mytable_name);
        add_common.put("sel_key",sel_key);
        add_common.put("sel_val",sel_val);
        add_common.put("result",result);
        add_common.put("par_val",par_val);
        add_common.put("remarks",remarks);
        add_common.put("myfunction_name",myfunction_name);
        add_common.put("mytype",mytype);


        Map<String,Object> add_row1 = new HashMap<>();
        add_row1.put("mybefore",mybefore);
        add_row1.put("change_value",change_value);
        add_row1.put("field_key",field_key);
        add_row1.put("field_val",field_val);

        List<Map<String,Object>> insertArr = new ArrayList<>();

        insertArr.add(add_row1);

        add_common.put("saveArr",insertArr);

        //修改企业金额信息 路由队列
        String polling_routingKey = "admin.insertMoneyChangeRecord.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(add_common), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            log.error("admin_insertMoneyChangeRecord_queue 发送 失败 {} {}",add_common,e.getMessage() );
        }

    }























    /**
     * 代理 预存金额
     * @param Pmap
     * @param operateMap
     */
    @Transactional
    public void Prestore( Map<String,Object> Pmap ,Map<String,Object> operateMap){


        if(Pmap.get("agent_id")!=null && Pmap.get("agent_id").toString().length()>0 && Pmap.get("deposit_amount")!=null && Pmap.get("deposit_amount").toString().length()>0){
            String agent_id = Pmap.get("agent_id").toString();
            Double deposit_amount = Double.parseDouble(Pmap.get("deposit_amount").toString());//预存金额

            String result = "1";
            String remarks = "";


            //1. 授信
            Map<String,Object> creditMap =  getCredit(agent_id);
            Double D_CreditPrice = Double.parseDouble(creditMap.get("D_CreditPrice").toString());//已用授信
            Double D_DeductPrice =  Double.parseDouble(creditMap.get("D_DeductPrice").toString());//预存抵扣
            Double used_line_of_credit = Double.parseDouble(creditMap.get("used_line_of_credit").toString());//已使用授信额度

            //查询企业下金额信息
            Map<String,Object> FindDeptAmountMap = new HashMap<>();
            FindDeptAmountMap.put("dept_id",agent_id);
            Map<String,Object> DeptAmountMap =  yzUserMapper.findDeptAmount(FindDeptAmountMap);
            Double line_of_credit = DeptAmountMap.get("line_of_credit")!=null&&DeptAmountMap.get("line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("line_of_credit").toString()):0.0;//已授信额度


            int updCredit = 0;
            int updAmount = 0;

            String operateType = "add";

            try {
                //2.该次预存金额 - 已使用额度 =  实际预存金额
                Double actualDeposit_amount = Arith.sub(deposit_amount,used_line_of_credit);
                if(used_line_of_credit>0){//如果有已使用授信额度 预存时因优先扣除  已使用授信额度

                    String ordNo = VeDate.getNo(8);
                    Double paySum = 0.0;
                    if(actualDeposit_amount<0){//实际预存金额 < 0 [说明 该次预存没有达到已使用授信（还是充钱了还是欠账）]
                        actualDeposit_amount = 0.0;//预存金额 0
                        paySum = deposit_amount;//交易金额
                    }else{//实际预存金额 >= 0 [说明 该次预存 大于 已使用的授信额度 ]
                        paySum = used_line_of_credit;//交易金额 = 已使用的授信额度 [抵扣]
                        used_line_of_credit = 0.0;//已使用的授信额度 清零
                    }

                    //添加订单生成参数
                    Map<String, Object> OrderMap = new HashMap<>();

                    OrderMap.put("ord_type", 6);//订单类型 企业预存 4  预存抵扣 6
                    OrderMap.put("ord_name", "预存抵扣");//交易名称
                    OrderMap.put("wx_ord_no", null);//微信订单号
                    OrderMap.put("status", "1");//支付状态 未支付
                    OrderMap.put("price", paySum);//交易金额
                    OrderMap.put("account", "0");//交易后金额
                    OrderMap.put("pay_type", "pe");//支付方式 预存支付 pe
                    OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
                    OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
                    OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
                    OrderMap.put("open_id", null);//openid 微信支付记录操作人
                    OrderMap.put("agent_id", agent_id);// 所属记录企业id
                    OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
                    OrderMap.put("iccid", "");
                    OrderMap.put("ord_no", ordNo);
                    OrderMap.put("info", "");
                    //生成交易订单
                    if(plOrder.orderSave(OrderMap)>0){
                        //修改授信额度信息
                        Double be_usable_line_of_credit =  Arith.sub(line_of_credit,used_line_of_credit);//可使用授信额度 = 已授信额度 - 已使用授信额度[本次交易]

                        Map<String,Object> updCreditMap = new HashMap<>();
                        updCreditMap.put("used_line_of_credit",used_line_of_credit);
                        updCreditMap.put("be_usable_line_of_credit",be_usable_line_of_credit);
                        updCreditMap.put("line_of_credit",line_of_credit);//授信额度不变
                        updCreditMap.put("dept_id",agent_id);
                        updCredit =  yzUserMapper.updCredit(updCreditMap);
                    }
                }
                Map<String,Object> updDepositAmountMap = new HashMap<>();
                updDepositAmountMap.put("operateType",operateType);
                updDepositAmountMap.put("deposit_amount",actualDeposit_amount);
                updDepositAmountMap.put("dept_id",agent_id);

                updAmount =  yzUserMapper.updDepositAmount(updDepositAmountMap);
                remarks = ""+operateType+" 操作成功 updAmount "+updAmount;
            }catch (Exception e){
                result = "2";
                remarks = e.getMessage()!=null?e.getMessage():remarks;
                remarks = remarks.length()>150?remarks.substring(0,150):remarks;
                remarks = ""+operateType+" "+remarks;
                log.error("yzUserMapper.updCredit Exception {}",e.getMessage());
            }

            try {
                String mytable_name = "sys_dept";
                String mytype = "2";
                String myfunction_name = "updDepositAmount";
                String sel_key = "dept_id";
                String sel_val = agent_id;
                String mybefore = DeptAmountMap.get("deposit_amount")!=null?DeptAmountMap.get("deposit_amount").toString():"";
                String change_value = deposit_amount.toString();
                String field_key = "deposit_amount";
                String field_val = "预存金额";
                insertMoneyChangeRecord(Pmap,operateMap,result,remarks,mybefore,change_value,mytable_name,mytype,myfunction_name,sel_key,sel_val,field_key,field_val);

            }catch (Exception e){
                log.error(">>insertMoneyChangeRecord 操作异常 Pmap:{} operateMap:{}<<", Pmap,operateMap);
            }

            log.info(">>Prestore 成功 Pmap:{} operateMap:{} updAmount {} updCredit {}<<", Pmap,operateMap,updAmount,updCredit);
        }else{
            log.error(">>Prestore 参数不全取消操作 Pmap:{} operateMap:{}<<", Pmap,operateMap);
        }


    }


    /**
     * 获取 企业预存信息
     * @param agent_id
     * @return
     */
    public Map<String,Object> getCredit(String agent_id){
        Map<String,Object> rMap = new HashMap<>();

        //1. 授信额度 - （ 获取 订单 SUM(交易类型为 [企业续费] & 支付方式[授信支付]) - SUM(预存抵扣) ） = 已使用额度
        Double D_CreditPrice = 0.0;//已用授信
        Double D_DeductPrice = 0.0;//预存抵扣

        List<String> agent_idArr =  new ArrayList<>();
        agent_idArr.add(agent_id);

        Map<String,Object> OrderpMap = new HashMap<>();
        OrderpMap.put("ord_type","7");
        OrderpMap.put("pay_type","ct");
        OrderpMap.put("agent_id",agent_idArr);
        String SumCreditPrice = yzOrderMapper.getSumPrice(OrderpMap);
        D_CreditPrice = SumCreditPrice!=null?Double.parseDouble(SumCreditPrice):D_CreditPrice;

        Map<String,Object> deductMap = new HashMap<>();
        deductMap.put("ord_type","6");
        deductMap.put("agent_id",agent_idArr);
        String SumDeductPrice = yzOrderMapper.getSumPrice(deductMap);
        D_DeductPrice = SumDeductPrice!=null?Double.parseDouble(SumDeductPrice):D_DeductPrice;

        Double used_line_of_credit = Arith.sub(D_CreditPrice,D_DeductPrice) ;//已使用授信额度
        rMap.put("D_CreditPrice",D_CreditPrice);
        rMap.put("D_DeductPrice",D_DeductPrice);
        rMap.put("used_line_of_credit",used_line_of_credit);

        return rMap;
    }


    /**
     * 企业账户续费扣款
     * @param Pmap
     * @param operateMap
     * @param primaryMap
     */
    @Transactional
    public void RenewalDebit( Map<String,Object> Pmap ,Map<String,Object> operateMap,Map<String,Object> primaryMap){
        if(Pmap.get("dept_id")!=null && Pmap.get("dept_id").toString().length()>0 && primaryMap.get("amount")!=null && primaryMap.get("amount").toString().length()>0){
            String agent_id = Pmap.get("dept_id").toString();
            Map<String,Object> dept = (Map<String, Object>) operateMap.get("dept");
            String deptName = dept.get("deptName").toString();
            Double amount = Double.parseDouble(primaryMap.get("amount").toString());//扣款金额

            int renewalCount = 0,iccidCount = 0,ordnoCount = 0,primaryCount = 0;

            //1. 获取授信
            Map<String,Object> creditMap =  getCredit(agent_id);
            Double D_CreditPrice = Double.parseDouble(creditMap.get("D_CreditPrice").toString());//已用授信
            Double D_DeductPrice =  Double.parseDouble(creditMap.get("D_DeductPrice").toString());//预存抵扣
            Double used_line_of_credit = Double.parseDouble(creditMap.get("used_line_of_credit").toString());//已使用授信额度
            Map<String,Object> FindDeptAmountMap = new HashMap<>();
            FindDeptAmountMap.put("dept_id",agent_id);
            Map<String,Object> DeptAmountMap =  yzUserMapper.findDeptAmount(FindDeptAmountMap);

            Double deposit_amount = DeptAmountMap.get("deposit_amount")!=null&&DeptAmountMap.get("deposit_amount").toString().length()>0?Double.parseDouble(DeptAmountMap.get("deposit_amount").toString()):0.0;//预存金额
            Double be_usable_line_of_credit = DeptAmountMap.get("be_usable_line_of_credit")!=null&&DeptAmountMap.get("be_usable_line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("be_usable_line_of_credit").toString()):0.0;//可使用授信额度
            Double line_of_credit = DeptAmountMap.get("line_of_credit")!=null&&DeptAmountMap.get("line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("line_of_credit").toString()):0.0;//已授信额度


            double forecast_deposit_amount = 0.0;//预计剩余可用预存金额
            double forecast_be_usable_line_of_credit = 0.0;//预计剩余 可用授信 [默认 0 ]
            forecast_deposit_amount = Arith.sub(deposit_amount,amount);//预计剩余可用预存金额 =  可用预存金额 - 总扣款
            boolean is_NeedToRecharge = false;//判断扣款是否不足以支付该次 操作进行提示

            if(forecast_deposit_amount<0 && be_usable_line_of_credit==0){//如 预计剩余可用预存金额 <0 且 可用授信额度 == 0
                is_NeedToRecharge = true;
            }else  if(forecast_deposit_amount<0 && be_usable_line_of_credit>0){//如 预计剩余可用预存金额 <0 且 可用授信额度》0 》》 继续判断可用授信是否足够扣款
                forecast_be_usable_line_of_credit = Arith.add(be_usable_line_of_credit,forecast_deposit_amount);//预计剩余 可用授信 = 剩余可用授信 - 预计剩余可用预存金额
                forecast_deposit_amount = 0;
                if(forecast_be_usable_line_of_credit>0) {//如 预计剩余 可用授信 >0  变更 预计剩余可用预存金额 = 0

                }else{
                    is_NeedToRecharge = true;
                }
            }else{
                forecast_be_usable_line_of_credit = be_usable_line_of_credit;
            }

            if(is_NeedToRecharge){ //不足以扣款时
                double stillNeeded =  Arith.add(forecast_deposit_amount,forecast_be_usable_line_of_credit);//还需充值金额
                stillNeeded = Math.abs(stillNeeded);
                log.error(">>本次操作需扣款 [ {} ] 还需 [ {} ] ,{} 账户目前可用预存/授信不足，请点击['去预存']！<<", amount,stillNeeded,deptName);
            }else{
                //0.账户扣款 【1.预计剩余可用预存金额》0 且 预存金额 》0 】
                List<String> OrdArrs = new ArrayList<>();
                int orderSaveCount = 0;
                Double debit = 0.0;//已经扣款的金额 【预存授信混合支付时做减法】
                if(deposit_amount>0){//预存里面可以扣 优先扣预存再扣授信
                    Double paySum = 0.0;
                    if(forecast_deposit_amount>0){
                        paySum = amount;//预计剩余可用预存金额 > 0 交易金额 =  amount 扣款金额
                    }else{
                        paySum = deposit_amount;//交易金额 =  deposit_amount 现有可用预存金额
                    }
                    debit = Arith.add(debit,paySum);//已经扣除
                    //添加订单生成参数
                    Map<String, Object> OrderMap = new HashMap<>();
                    String ordNo = VeDate.getNo(8);
                    OrdArrs.add(ordNo);
                    OrderMap.put("ord_type", 7);//订单类型 企业续费 7
                    OrderMap.put("ord_name", "企业续费");//交易名称
                    OrderMap.put("wx_ord_no", null);//微信订单号
                    OrderMap.put("status", "1");//支付状态 已支付
                    OrderMap.put("price", paySum);//交易金额
                    OrderMap.put("account", forecast_deposit_amount);//交易后金额 = 预计剩余可用预存金额
                    OrderMap.put("pay_type", "pe");//支付方式 预存支付 pe
                    OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
                    OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
                    OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
                    OrderMap.put("open_id", null);//openid 微信支付记录操作人
                    OrderMap.put("agent_id", agent_id);// 所属记录企业id
                    OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
                    OrderMap.put("iccid", "");
                    OrderMap.put("ord_no", ordNo);
                    OrderMap.put("info", "");

                    Map<String, Object> setDAMap = new HashMap<>();
                    setDAMap.put("deposit_amount",forecast_deposit_amount);
                    setDAMap.put("dept_id",agent_id);
                    if(yzUserMapper.setDepositAmount(setDAMap)>0){
                        //生成交易订单
                        orderSaveCount += plOrder.orderSave(OrderMap);
                    }
                }

                if(be_usable_line_of_credit>0){//可使用授信额度 》 0
                    Double paySum = 0.0;
                    if(forecast_be_usable_line_of_credit>=0){
                        paySum = amount;//预计剩余 可用授信 》=0 交易金额 =  amount 扣款金额
                    }else{
                        paySum = forecast_be_usable_line_of_credit;//交易金额 =  forecast_be_usable_line_of_credit 可使用授信额度
                    }
                    paySum = Arith.sub(paySum,debit);//交易金额减去已经扣款的部分

                    //添加订单生成参数
                    Map<String, Object> OrderMap = new HashMap<>();
                    String ordNo = VeDate.getNo(8);
                    OrdArrs.add(ordNo);
                    OrderMap.put("ord_type", 7);//订单类型 企业续费 7
                    OrderMap.put("ord_name", "企业续费");//交易名称
                    OrderMap.put("wx_ord_no", null);//微信订单号
                    OrderMap.put("status", "1");//支付状态 已支付
                    OrderMap.put("price", paySum);//交易金额
                    OrderMap.put("account", forecast_be_usable_line_of_credit);//交易后金额 = 预计剩余可用授信
                    OrderMap.put("pay_type", "ct");//支付方式 授信支付 ct
                    OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
                    OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
                    OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
                    OrderMap.put("open_id", null);//openid 微信支付记录操作人
                    OrderMap.put("agent_id", agent_id);// 所属记录企业id
                    OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
                    OrderMap.put("iccid", "");
                    OrderMap.put("ord_no", ordNo);
                    OrderMap.put("info", "");

                    used_line_of_credit = Arith.add(paySum,used_line_of_credit);//已使用授信额度 = //结算出的历史已使用授信额度+本次使用的

                    Map<String, Object> updCreditMap = new HashMap<>();
                    updCreditMap.put("line_of_credit",line_of_credit);//已授信额度 不变
                    updCreditMap.put("used_line_of_credit",used_line_of_credit);//已使用授信额度
                    updCreditMap.put("be_usable_line_of_credit",forecast_be_usable_line_of_credit);//可使用授信额度
                    updCreditMap.put("dept_id",agent_id);
                    if(yzUserMapper.updCredit(updCreditMap)>0){
                        //生成交易订单
                        orderSaveCount += plOrder.orderSave(OrderMap);
                    }
                }
                //2.生成 续费申请
                if(orderSaveCount>0){

                    //2.1 续费申请主表
                    primaryMap.put("info",Pmap.get("info"));
                    primaryMap.put("dept_id",agent_id);
                    primaryCount = yzApplicationforRenewalPrimaryMapper.save(primaryMap);
                    if(primaryCount>0){
                        String p_id = primaryMap.get("id").toString();
                        //2.1 订单表
                        Map<String, Object> OrdNoMap = new HashMap<>();
                        OrdNoMap.put("p_id",p_id);
                        OrdNoMap.put("OrdArrs",OrdArrs);

                        ordnoCount = yzApplicationforRenewalPrimaryOrdNoMapper.save(OrdNoMap);

                        List<Map<String, Object>>  packetArr = (List<Map<String, Object>>) Pmap.get("packetArr");
                        Map<String, Object>  BRpacketCardCount = (Map<String, Object>) Pmap.get("BRpacketCardCount");






                        List<Map<String, Object>>  packet_arrs = new ArrayList<>();
                        for (int i = 0; i < packetArr.size(); i++) {
                            Map<String, Object> packetObj = packetArr.get(i);
                            Map<String,Object> findPacketMap = new HashMap<>();
                            String packet_id = packetObj.get("packet_id").toString();
                            findPacketMap.put("dept_id",agent_id);
                            findPacketMap.put("packet_id",packet_id);
                            Map<String, Object> packet_info = new HashMap<>();
                            if(agent_id.equals("100")){
                                packet_info = yzCardPacketMapper.findOnePacket(findPacketMap);
                            }else{
                                packet_info = yzAgentPacketMapper.findOnePacket(findPacketMap);
                            }
                            packetObj.put("packet_info",JSON.toJSONString(packet_info));
                            List<Map<String, Object>>  cardObj = (List<Map<String, Object>>) BRpacketCardCount.get(""+packet_id);
                            packetObj.put("card_count",cardObj.size());
                            packet_arrs.add(packetObj);
                        }
                        for (int i = 0; i < packet_arrs.size(); i++) {//续费申请详情卡号 新增 指定资费下 卡号信息
                            Map<String, Object> packetObj = packet_arrs.get(i);
                            packetObj.put("p_id",p_id);
                            packetObj.put("dept_id",agent_id);

                            int rCount = yzApplicationforRenewalMapper.save(packetObj);
                            renewalCount += rCount;
                            if(rCount>0){
                                Map<String, Object> IccidMap = new HashMap<>();
                                String packet_id = packetObj.get("packet_id").toString();
                                String b_id = packetObj.get("id").toString();
                                List<Map<String, Object>>  cardArrs = (List<Map<String, Object>>) BRpacketCardCount.get(""+packet_id);
                                IccidMap.put("b_id",b_id);
                                IccidMap.put("cardArrs",cardArrs);
                                iccidCount = yzApplicationforRenewalPrimaryIccidMapper.save(IccidMap);
                            }

                        }
                    }
                }
            }

            log.info(">>RenewalDebit 成功 Pmap:{} operateMap:{} renewalCount {} iccidCount {} ordnoCount {} primaryCount {}<<", Pmap,operateMap,renewalCount,iccidCount,ordnoCount,primaryCount);
        }else{
            log.error(">>RenewalDebit 参数不全取消操作 Pmap:{} operateMap:{}<<", Pmap,operateMap);
        }


    }





    /**
     * 企业账户续费扣款
     * @param Pmap
     * @param operateMap
     * @param primaryMap
     */
    @Transactional
    public void Debit(String filePath, String newName,Map<String,Object> POrderMap,Map<String,Object>  bulkMap,List<String> iccidArr,  Map<String,Object> Pmap ,Map<String,Object> operateMap,Map<String,Object> primaryMap,String opType){
        if(Pmap.get("dept_id")!=null && Pmap.get("dept_id").toString().length()>0 && primaryMap.get("amount")!=null && primaryMap.get("amount").toString().length()>0){
            String agent_id = Pmap.get("dept_id").toString();
            Map<String,Object> dept = (Map<String, Object>) operateMap.get("dept");
            String deptName = dept.get("deptName").toString();
            Double amount = Double.parseDouble(primaryMap.get("amount").toString());//扣款金额

            int renewalCount = 0,iccidCount = 0,ordnoCount = 0,primaryCount = 0;
            boolean singlePayment = false;//是否 仅允许单类型支付 【禁止混合支付】

            String ord_name = "";
            switch (opType){
                case "RenewalDebit":
                    ord_name = "企业续费";
                    break;
                case "Recharge":
                    ord_name = "企业充值";
                    break;
                case "ApiRechargeOne":
                    ord_name = "企业-API充值";
                    singlePayment = true;
                    break;
            }

            //1. 获取授信
            Map<String,Object> creditMap =  getCredit(agent_id);
            Double D_CreditPrice = Double.parseDouble(creditMap.get("D_CreditPrice").toString());//已用授信
            Double D_DeductPrice =  Double.parseDouble(creditMap.get("D_DeductPrice").toString());//预存抵扣
            Double used_line_of_credit = Double.parseDouble(creditMap.get("used_line_of_credit").toString());//已使用授信额度
            Map<String,Object> FindDeptAmountMap = new HashMap<>();
            FindDeptAmountMap.put("dept_id",agent_id);
            Map<String,Object> DeptAmountMap =  yzUserMapper.findDeptAmount(FindDeptAmountMap);

            Double deposit_amount = DeptAmountMap.get("deposit_amount")!=null&&DeptAmountMap.get("deposit_amount").toString().length()>0?Double.parseDouble(DeptAmountMap.get("deposit_amount").toString()):0.0;//预存金额
            Double be_usable_line_of_credit = DeptAmountMap.get("be_usable_line_of_credit")!=null&&DeptAmountMap.get("be_usable_line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("be_usable_line_of_credit").toString()):0.0;//可使用授信额度
            Double line_of_credit = DeptAmountMap.get("line_of_credit")!=null&&DeptAmountMap.get("line_of_credit").toString().length()>0?Double.parseDouble(DeptAmountMap.get("line_of_credit").toString()):0.0;//已授信额度

            double forecast_deposit_amount = 0.0;//预计剩余可用预存金额
            double forecast_be_usable_line_of_credit = 0.0;//预计剩余 可用授信 [默认 0 ]
            forecast_deposit_amount = Arith.sub(deposit_amount,amount);//预计剩余可用预存金额 =  可用预存金额 - 总扣款
            boolean is_NeedToRecharge = false;//判断扣款是否不足以支付该次 操作进行提示

            if(singlePayment){//单类型支付时
                if(be_usable_line_of_credit<amount && deposit_amount<amount){//如 预计剩余可用预存金额 < 总扣款 且 预存金额 < 总扣款
                    is_NeedToRecharge = true;
                }else{
                    if(forecast_deposit_amount<0 && be_usable_line_of_credit>0){
                        forecast_be_usable_line_of_credit = Arith.sub(be_usable_line_of_credit,amount);//预计剩余 可用授信 = 剩余可用授信 - 总扣款 【单类型支付时】
                        forecast_deposit_amount = 0;
                        if(forecast_be_usable_line_of_credit>0) {//如 预计剩余 可用授信 >0  变更 预计剩余可用预存金额 = 0

                        }else{
                            is_NeedToRecharge = true;
                        }
                    }
                }
            }else{
                if(forecast_deposit_amount<0 && be_usable_line_of_credit==0){//如 预计剩余可用预存金额 <0 且 可用授信额度 == 0
                    is_NeedToRecharge = true;
                }else  if(forecast_deposit_amount<0 && be_usable_line_of_credit>0){//如 预计剩余可用预存金额 <0 且 可用授信额度》0 》》 继续判断可用授信是否足够扣款
                    forecast_be_usable_line_of_credit = Arith.add(be_usable_line_of_credit,forecast_deposit_amount);//预计剩余 可用授信 = 剩余可用授信 - 预计剩余可用预存金额
                    forecast_deposit_amount = 0;
                    if(forecast_be_usable_line_of_credit>0) {//如 预计剩余 可用授信 >0  变更 预计剩余可用预存金额 = 0

                    }else{
                        is_NeedToRecharge = true;
                    }
                }else{
                    forecast_be_usable_line_of_credit = be_usable_line_of_credit;
                }
            }

            if(is_NeedToRecharge){ //不足以扣款时
                double stillNeeded =  Arith.add(forecast_deposit_amount,forecast_be_usable_line_of_credit);//还需充值金额
                stillNeeded = Math.abs(stillNeeded);
                log.error(">>本次操作需扣款 [ {} ] 还需 [ {} ] ,{} 账户目前可用预存/授信不足，请点击['去预存']！<<", amount,stillNeeded,deptName);
            }else{
                //0.账户扣款 【1.预计剩余可用预存金额》0 且 预存金额 》0 】
                List<String> OrdArrs = new ArrayList<>();
                int orderSaveCount = 0;
                Double debit = 0.0;//已经扣款的金额 【预存授信混合支付时做减法】
                if(deposit_amount>0 ){//预存里面可以扣 优先扣预存再扣授信
                    Map<String,Object> rMap = new HashMap<>();
                    if(singlePayment) {//单类型支付时
                        if(deposit_amount>=amount){//预存金额 >= 当前扣款 才进行操作
                            rMap = DepositDebit(forecast_deposit_amount,amount,deposit_amount,debit,opType,POrderMap,OrdArrs,ord_name,agent_id,orderSaveCount);
                            debit = Double.parseDouble(rMap.get("debit").toString());
                            orderSaveCount = Integer.parseInt(rMap.get("orderSaveCount").toString());
                        }
                    }else {
                        rMap = DepositDebit(forecast_deposit_amount,amount,deposit_amount,debit,opType,POrderMap,OrdArrs,ord_name,agent_id,orderSaveCount);
                        debit = Double.parseDouble(rMap.get("debit").toString());
                        orderSaveCount = Integer.parseInt(rMap.get("orderSaveCount").toString());
                    }
                }
                if(be_usable_line_of_credit>0 && Arith.sub(amount,debit)>0){//可使用授信额度 》 0   && 当前扣款 - 已经扣款的金额 》0 （说明还需要扣款，否则已经扣款完成了）
                    Map<String,Object> rMap = new HashMap<>();
                    if(singlePayment) {//单类型支付时
                        if(be_usable_line_of_credit>=amount) {//可使用授信额度 >= 当前扣款 才进行操作
                            rMap = creditDeduction(forecast_be_usable_line_of_credit,amount,used_line_of_credit,debit,opType,POrderMap,OrdArrs,ord_name,agent_id,orderSaveCount,line_of_credit);
                            orderSaveCount = Integer.parseInt(rMap.get("orderSaveCount").toString());
                        }
                    }else {
                        rMap = creditDeduction(forecast_be_usable_line_of_credit,amount,used_line_of_credit,debit,opType,POrderMap,OrdArrs,ord_name,agent_id,orderSaveCount,line_of_credit);
                        orderSaveCount = Integer.parseInt(rMap.get("orderSaveCount").toString());
                    }
                }
                //2.
                if(orderSaveCount>0){
                    if(opType.equals("RenewalDebit")){
                        //生成 续费申请
                        Renewal(Pmap,operateMap,primaryMap,agent_id,OrdArrs,primaryCount,orderSaveCount,renewalCount,iccidCount);
                    }else if(opType.equals("Recharge")){
                        //文本域批量充值
                        TextExecution(filePath,newName,Pmap,operateMap,POrderMap,bulkMap,iccidArr);
                    }else if(opType.equals("ApiRechargeOne")){
                        //接口单卡号充值 [这里不需要执行什么]

                    }


                }
            }


        }else{
            log.error(">>RenewalDebit 参数不全取消操作 Pmap:{} operateMap:{}<<", Pmap,operateMap);
        }


    }

    /**
     * 预存支付
     * @param forecast_deposit_amount
     * @param amount
     * @param deposit_amount
     * @param debit
     * @param opType
     * @param POrderMap
     * @param OrdArrs
     * @param ord_name
     * @param agent_id
     * @param orderSaveCount
     * @return
     */
    public Map<String,Object> DepositDebit(Double forecast_deposit_amount,Double amount,Double deposit_amount,Double debit,String opType,Map<String,Object> POrderMap, List<String> OrdArrs,String ord_name,String agent_id,int orderSaveCount){
        Map<String,Object> rMap = new HashMap<>();
        Double paySum = 0.0;
        if(forecast_deposit_amount>0){
            paySum = amount;//预计剩余可用预存金额 > 0 交易金额 =  amount 扣款金额
        }else{
            paySum = deposit_amount;//交易金额 =  deposit_amount 现有可用预存金额
        }
        debit = Arith.add(debit,paySum);//已经扣除
        //添加订单生成参数
        Map<String, Object> OrderMap = new HashMap<>();
        String ordNo = VeDate.getNo(8);

        if(opType.equals("ApiRechargeOne")){
            OrderMap = POrderMap;
            OrderMap.put("ord_no", ordNo);
            OrderMap.put("price", paySum);//交易金额
            OrderMap.put("account", forecast_deposit_amount);//交易后金额 = 预计剩余可用预存金额
            OrderMap.put("pay_type", "pe");//支付方式 预存支付 pe
        }else{
            OrderMap.put("ord_no", ordNo);
            OrdArrs.add(ordNo);
            OrderMap.put("ord_type", 7);//订单类型 企业续费 7
            OrderMap.put("ord_name", ord_name);//交易名称
            OrderMap.put("wx_ord_no", null);//微信订单号
            OrderMap.put("status", "1");//支付状态 已支付
            OrderMap.put("price", paySum);//交易金额
            OrderMap.put("account", forecast_deposit_amount);//交易后金额 = 预计剩余可用预存金额
            OrderMap.put("pay_type", "pe");//支付方式 预存支付 pe
            OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
            OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
            OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
            OrderMap.put("open_id", null);//openid 微信支付记录操作人
            OrderMap.put("agent_id", agent_id);// 所属记录企业id
            OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
            OrderMap.put("iccid", "");
            OrderMap.put("info", "");
        }
        Map<String, Object> setDAMap = new HashMap<>();
        setDAMap.put("deposit_amount",forecast_deposit_amount);
        setDAMap.put("dept_id",agent_id);
        if(yzUserMapper.setDepositAmount(setDAMap)>0){
            //生成交易订单
            orderSaveCount += plOrder.orderSave(OrderMap);
        }
        rMap.put("debit",debit);
        rMap.put("orderSaveCount",orderSaveCount);
        return  rMap;
    }

    /**
     * 授信支付
     * @param forecast_be_usable_line_of_credit
     * @param amount
     * @param used_line_of_credit
     * @param debit
     * @param opType
     * @param POrderMap
     * @param OrdArrs
     * @param ord_name
     * @param agent_id
     * @param orderSaveCount
     * @param line_of_credit
     * @return
     */
    public Map<String,Object> creditDeduction(Double forecast_be_usable_line_of_credit,Double amount,Double used_line_of_credit,Double debit,String opType,Map<String,Object> POrderMap, List<String> OrdArrs,String ord_name,String agent_id,int orderSaveCount,Double line_of_credit) {
        Map<String,Object> rMap = new HashMap<>();
        Double paySum = 0.0;
        if(forecast_be_usable_line_of_credit>=0){
            paySum = amount;//预计剩余 可用授信 》=0 交易金额 =  amount 扣款金额
        }else{
            paySum = forecast_be_usable_line_of_credit;//交易金额 =  forecast_be_usable_line_of_credit 可使用授信额度
        }
        paySum = Arith.sub(paySum,debit);//交易金额减去已经扣款的部分

        //添加订单生成参数
        Map<String, Object> OrderMap = new HashMap<>();
        String ordNo = VeDate.getNo(8);
        OrdArrs.add(ordNo);
        if(opType.equals("ApiRechargeOne")){
            OrderMap = POrderMap;
            OrderMap.put("ord_no", ordNo);
            OrderMap.put("price", paySum);//交易金额
            OrderMap.put("account", forecast_be_usable_line_of_credit);//交易后金额 = 预计剩余可用预存金额
            OrderMap.put("pay_type", "ct");//支付方式 授信支付 ct
        }else {
            OrderMap.put("ord_no", ordNo);
            OrderMap.put("ord_type", 7);//订单类型 企业续费 7
            OrderMap.put("ord_name", ord_name);//交易名称
            OrderMap.put("wx_ord_no", null);//微信订单号
            OrderMap.put("status", "1");//支付状态 已支付
            OrderMap.put("price", paySum);//交易金额
            OrderMap.put("account", forecast_be_usable_line_of_credit);//交易后金额 = 预计剩余可用授信
            OrderMap.put("pay_type", "ct");//支付方式 授信支付 ct
            OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
            OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
            OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
            OrderMap.put("open_id", null);//openid 微信支付记录操作人
            OrderMap.put("agent_id", agent_id);// 所属记录企业id
            OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
            OrderMap.put("iccid", "");
            OrderMap.put("info", "");
        }
        used_line_of_credit = Arith.add(paySum,used_line_of_credit);//已使用授信额度 = //结算出的历史已使用授信额度+本次使用的

        Map<String, Object> updCreditMap = new HashMap<>();
        updCreditMap.put("line_of_credit",line_of_credit);//已授信额度 不变
        updCreditMap.put("used_line_of_credit",used_line_of_credit);//已使用授信额度
        updCreditMap.put("be_usable_line_of_credit",forecast_be_usable_line_of_credit);//可使用授信额度
        updCreditMap.put("dept_id",agent_id);
        if(yzUserMapper.updCredit(updCreditMap)>0){
            //生成交易订单
            orderSaveCount += plOrder.orderSave(OrderMap);
        }
        rMap.put("orderSaveCount",orderSaveCount);
        return  rMap;
    }




    /**
     * 续费
     * @param Pmap
     * @param operateMap
     * @param primaryMap
     * @param agent_id
     * @param OrdArrs
     * @param primaryCount
     * @param ordnoCount
     * @param renewalCount
     * @param iccidCount
     */
    public void Renewal(Map<String,Object> Pmap , Map<String,Object> operateMap,Map<String,Object> primaryMap,String agent_id,List<String> OrdArrs,int primaryCount,int ordnoCount,int renewalCount,int iccidCount){
        //2.1 续费申请主表
        primaryMap.put("info",Pmap.get("info"));
        primaryMap.put("dept_id",agent_id);
        primaryCount = yzApplicationforRenewalPrimaryMapper.save(primaryMap);
        if(primaryCount>0){
            String p_id = primaryMap.get("id").toString();
            //2.1 订单表
            Map<String, Object> OrdNoMap = new HashMap<>();
            OrdNoMap.put("p_id",p_id);
            OrdNoMap.put("OrdArrs",OrdArrs);
            ordnoCount = yzApplicationforRenewalPrimaryOrdNoMapper.save(OrdNoMap);
            List<Map<String, Object>>  packetArr = (List<Map<String, Object>>) Pmap.get("packetArr");
            Map<String, Object>  BRpacketCardCount = (Map<String, Object>) Pmap.get("BRpacketCardCount");

            List<Map<String, Object>>  packet_arrs = new ArrayList<>();
            for (int i = 0; i < packetArr.size(); i++) {
                Map<String, Object> packetObj = packetArr.get(i);
                Map<String,Object> findPacketMap = new HashMap<>();
                String packet_id = packetObj.get("packet_id").toString();
                findPacketMap.put("dept_id",agent_id);
                findPacketMap.put("packet_id",packet_id);
                Map<String, Object> packet_info = new HashMap<>();
                if(agent_id.equals("100")){
                    packet_info = yzCardPacketMapper.findOnePacket(findPacketMap);
                }else{
                    packet_info = yzAgentPacketMapper.findOnePacket(findPacketMap);
                }
                packetObj.put("packet_info",JSON.toJSONString(packet_info));
                List<Map<String, Object>>  cardObj = (List<Map<String, Object>>) BRpacketCardCount.get(""+packet_id);
                packetObj.put("card_count",cardObj.size());
                packet_arrs.add(packetObj);
            }
            for (int i = 0; i < packet_arrs.size(); i++) {//续费申请详情卡号 新增 指定资费下 卡号信息
                Map<String, Object> packetObj = packet_arrs.get(i);
                packetObj.put("p_id",p_id);
                packetObj.put("dept_id",agent_id);

                int rCount = yzApplicationforRenewalMapper.save(packetObj);
                renewalCount += rCount;
                if(rCount>0){
                    Map<String, Object> IccidMap = new HashMap<>();
                    String packet_id = packetObj.get("packet_id").toString();
                    String b_id = packetObj.get("id").toString();
                    List<Map<String, Object>>  cardArrs = (List<Map<String, Object>>) BRpacketCardCount.get(""+packet_id);
                    IccidMap.put("b_id",b_id);
                    IccidMap.put("cardArrs",cardArrs);
                    iccidCount = yzApplicationforRenewalPrimaryIccidMapper.save(IccidMap);
                }

            }
        }
        log.info(">>RenewalDebit 成功 Pmap:{} operateMap:{} renewalCount {} iccidCount {} ordnoCount {} primaryCount {}<<", Pmap,operateMap,renewalCount,iccidCount,ordnoCount,primaryCount);
    }


    /**
     * 文本域批量充值
     * @param filePath
     * @param newName
     * @param Pmap
     * @param User
     * @param OrderMap
     * @param bulkMap
     * @param iccidArr
     */
    public void TextExecution(String filePath, String newName, Map<String,Object> Pmap,Map<String,Object> User,Map<String,Object> OrderMap, Map<String,Object>  bulkMap,List<String> iccidArr){

        bulkMap.put("state_id","3");//状态  执行中 3
        bulkMap.put("start_time", VeDate.getStringDate());//赋值 开始时间
        bulkMap.put("url", "");//url

        bulkUtil.update(bulkMap);//消费者进入变更执行状态 执行中

        String prefix = "admin_OrderTextRecharge_queue";
        //执行前判断 redis 是否存在 执行数据 存在时 不执行
        Object  isExecute = redisCache.getCacheObject(prefix+":"+ newName);
        if(isExecute==null){
            //System.out.println("SUCCESS");
            redisCache.setCacheObject(prefix+":"+ newName, JSON.toJSONString(Pmap), 30, TimeUnit.SECONDS);//30 秒缓存 避免 重复消费
            plOrder.TextAddOrder(iccidArr,filePath,"",newName,Pmap,User,OrderMap,bulkMap,"");//生成订单
        }
    }




}
