package com.yunze.common.core.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.BuiltinExchangeType;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.utils.Arith;
import com.yunze.common.utils.yunze.VeDate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/03/16:22
 * @Description:
 */
@Component
public class AddPacket {


    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 直接添加folw 表 不生成订单
     * @param ord_no
     * @param iccid
     * @param add_parameter
     * @return
     */
    public Map<String,Object> addFlow(String ord_no,String iccid,Map<String,Object> add_parameter, String validate_type,String create_time,String activate_date){
        //1.查询 需要加包 订单号 与卡号是否已经在数据库中 如果已经在库中 不执行操作
        Map<String,Object> R_Map = new HashMap<>();
        Map<String,Object> findExistence_Map = new HashMap<>();
        findExistence_Map.put("ord_no",ord_no);
        findExistence_Map.put("iccid",iccid);
        Integer Existence =  yzCardFlowMapper.findExistence(findExistence_Map);
        Existence =Existence!=null?Existence:0;
        String code = "200",message = "批量加包成功";
        //2.加包
        if(Existence==0){
            try {
                //加包执行flow
                Map<String,Object> SaveFlowExecuteMap =  SaveFlowExecute(add_parameter,validate_type,create_time,ord_no,activate_date,iccid);
                if(SaveFlowExecuteMap.get("SuccessCount")!=null){
                    int SuccessCount = Integer.parseInt(SaveFlowExecuteMap.get("SuccessCount").toString());
                    String info = "";
                    if(SaveFlowExecuteMap.get("Message")!=null){
                        info = SaveFlowExecuteMap.get("Message").toString();
                    }else {
                        info = SuccessCount>0?"执行加包成功 条数【"+SuccessCount+"】":"执行加包失败【】";
                    }
                    info = info.length()>1500?info.substring(0,1500):info;//切割多余的 错误信息
                    //3.插入flow成功后 变更 订单状态
                    Map<String,Object> updAddPackageInfo_Map = new HashMap<>();
                    updAddPackageInfo_Map.put("iccid",iccid);
                    updAddPackageInfo_Map.put("ord_no",ord_no);
                    updAddPackageInfo_Map.put("info",info);
                    int updOrder = yzOrderMapper.updAddPackageInfo(updAddPackageInfo_Map);
                    System.out.println("变更订单执行加包 执行 状态 "+updOrder);
                }
            }catch (Exception e){
                System.out.println("加包执行 【SaveFlowExecute】 异常 " +e.getMessage());
            }
        }else{
            code = "201";
            message = "资费计划以存在";
        }
        R_Map.put("code",code);
        R_Map.put("message",message);
        return R_Map;
    }


    /**
     * 加包执行flow
     * @param add_parameter
     * @param validate_type
     * @param create_time
     * @param ord_no
     * @param activate_date
     * @param iccid
     * @return
     */
    public  Map<String,Object> SaveFlowExecute(Map<String,Object> add_parameter, String validate_type,String create_time,String ord_no,String activate_date,String iccid){
        Map<String,Object> R_Map = new HashMap<>();
        String Message = "";
        List<Map<String,Object>> add_flow = new ArrayList<Map<String,Object>>();

        String start_time = "",end_time = "";
        int Size = 1;
        int SuccessCount = 0;
        //sdate = sdate.substring(0, 8) + "01";

        Integer is_month  = Integer.parseInt(add_parameter.get("is_month").toString());//是否拆分 0 否 1 是
        boolean monthBool = is_month==1?true:false;
        Integer packet_valid_time  = Integer.parseInt(add_parameter.get("packet_valid_time").toString());//加包 时间规格值
        String packet_valid_name  = add_parameter.get("packet_valid_name").toString();//加包 时间规格
        //0. 根据 是否拆分 到账 获取加包 条数
        if(monthBool){
            Size = packet_valid_time;
        }
        List<Map<String,Object>> AddFlow = null;
        Map<String,Object> setFlow =null;//加包设置数据
        if(Size>0){
            //1.判断加包类型 1 当月生效 2 次月生效 3 周期延顺 4 激活生效  获取 订单创建时间
            int validate_type_int = Integer.parseInt(validate_type);
            Map<String,Object> Pmap = new HashMap<String,Object>();
            Pmap.put("iccid",iccid);
            try {
                switch (validate_type_int){
                    case 1://1 当月生效
                        start_time = create_time;
                        break;
                    case 2:
                        start_time = VeDate.getFirstDayOfGivenMonth(VeDate.getMonthDate(create_time,1),"yyyy-MM-dd")+" 00:00:00";
                        break;
                    case 3:
                        //查询最后一个包的到期时间 +1
                        String eTime = yzCardFlowMapper.findEndTime(Pmap);
                        if(eTime!=null){
                            start_time = VeDate.getNextSecond(eTime,1);
                        }else{
                            start_time = create_time;
                        }
                        break;
                    case 4:
                        //激活时间为空时查询激活时间
                        if(activate_date==null){
                            String ActivateDate = yzCardMapper.findActivateDate(Pmap);
                            start_time = ActivateDate;
                        }else{
                            start_time = VeDate.dateToStrLong(VeDate.strToDate(activate_date));
                        }
                        break;
                }
            }catch (Exception e){
                Message ="加包 设置 开始时间异常 "+e.getMessage();
                System.out.println(Message);
            }

            if(start_time!=null && start_time.length()>0){
                try {
                    setFlow  = setFlow(monthBool,add_parameter,packet_valid_time,packet_valid_name,start_time,Size,ord_no,iccid);
                    if(setFlow.get("Rlist")!=null){
                        AddFlow = (List<Map<String, Object>>) setFlow.get("Rlist");
                    }
                }catch (Exception e){
                    Message = "【 获取 加包 flwo setFlow 异常 】"+e.getMessage();
                    System.out.println(Message);
                }
                try {
                    //同步主表数据 如果开始 时间 和 当前 月 时间 一致 剩余用量 += 包用量
                    String Ym = start_time.substring(0,7);
                    String yearM = VeDate.getStringDateShort().substring(0,7);
                    if(yearM.equals(Ym)){
                        Map<String,Object> addInfoRemaining_Map = new HashMap<>();
                        addInfoRemaining_Map.put("flow",add_parameter.get("packet_flow"));
                        addInfoRemaining_Map.put("iccid",iccid);
                        //1.创建路由 绑定 生产队列 发送消息
                        String card_exchangeName = "admin_card_exchange", card_queueName = "admin_CardUpd_queue", card_routingKey = "admin.CardUpd.queue",
                                card_del_exchangeName = "dlx_"+card_exchangeName,card_del_queueName = "dlx_"+card_queueName, card_del_routingKey = "dlx_"+card_routingKey;
                        try {
                            //rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                            addInfoRemaining_Map.put("queueTypeName","admin_CardAddInfoRemaining_queue");
                            rabbitTemplate.convertAndSend(card_exchangeName, card_routingKey, JSON.toJSONString(addInfoRemaining_Map), message -> {
                                // 设置消息过期时间 30 分钟 过期
                                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                                return message;
                            });
                            System.out.println("发送 修改剩余 指令成功 ！");
                        } catch (Exception e) {
                            System.out.println("发送 修改剩余 创建 失败 " + e.getMessage().toString());
                        }
                    }
                }catch (Exception e){
                    Message = "加包 同步主表 Remaining 失败  iccid : "+iccid+" , "+e.getMessage();
                    System.out.println(Message);
                }
            }else{
                System.out.println(" 加包取消  start_time =  "+start_time);
            }

        }

        if(AddFlow!=null){
            Map<String,Object> save_Map = new HashMap<>();
            save_Map.put("flow_arrs",AddFlow);
            SuccessCount = yzCardFlowMapper.saveFlow(save_Map);
            Message = "加包成功  iccid : "+iccid+" SuccessCount "+SuccessCount;
            System.out.println(Message);
        }else{
            Message = "未找到加包数据  iccid : "+iccid+" , add_parameter : "+ JSON.toJSONString(add_parameter);
            System.out.println(Message);
        }
        R_Map.put("SuccessCount",SuccessCount);
        R_Map.put("Message",Message);
        return R_Map;
    }


    /**
     * 设置flow
     * @param add_parameter
     * @param packet_valid_time
     * @param packet_valid_name
     * @param start_time
     * @param Size
     * @param ord_no
     * @return
     * @throws ParseException
     */
    public  Map<String,Object> setFlow(boolean monthBool,Map<String,Object> add_parameter,Integer packet_valid_time, String packet_valid_name, String start_time,int Size,String ord_no,String iccid ) throws ParseException {
        Map<String,Object> R_map = new HashMap<>();
        List<Map<String,Object>> Rlist = new ArrayList<>();
        int valid_time = 0;
        Double FlowSize = 0.0;
        //判断 时间规格
        String ord_type = "";
        if(packet_valid_name.equals("月")){
            ord_type = "1";
            valid_time = packet_valid_time;
        }else if(packet_valid_name.equals("年")){
            ord_type = "2";
            valid_time = packet_valid_time;
        }
        int month = 1;
        if(ord_type.equals("2")){//年包
            if(monthBool){//年包拆分为 1年 单位
                month = 1*12;
            }else{
                month = valid_time*12;
            }
        }
            String sTime = start_time,eTime = null;
            for (int i = 0; i < Size; i++) {
                eTime = VeDate.getMonthDate(sTime,month-1);// 如 周期 1 月 = 当前时间 至 本月月底 所以 -1
                eTime = VeDate.getEndDateOfMonth_yyyy_MM_DD(eTime.split(" ")[0])+" 23:59:59";//获取月底 日
                Map<String,Object> FlowMap = new HashMap<>();
                FlowMap.put("package_id",add_parameter.get("package_id"));
                FlowMap.put("packet_id",add_parameter.get("packet_id"));
                FlowMap.put("ord_no",ord_no);
                FlowMap.put("true_flow",add_parameter.get("packet_flow"));
                FlowMap.put("error_flow",add_parameter.get("error_flow"));
                FlowMap.put("start_time",sTime);
                FlowMap.put("end_time",eTime);
                FlowMap.put("ord_type",ord_type);
                FlowMap.put("status","1");
                //解决老数据缺少加包参数加包失败问题
                String packet_type = null;
                if(add_parameter.get("packet_type")!=null && add_parameter.get("packet_type").toString().length()>0){
                     packet_type = add_parameter.get("packet_type").toString() ;
                }else{
                    Map<String,Object> FindPacketTypeMap =  yzCardFlowMapper.findPacketType(add_parameter);
                    packet_type = FindPacketTypeMap.get("packet_type").toString();
                }
                FlowMap.put("packet_type",packet_type);
                FlowMap.put("error_time",add_parameter.get("error_so"));
                Object validate_type = add_parameter.get("validate_type");
                validate_type = validate_type!=null?validate_type:"1";
                FlowMap.put("validate_type",validate_type);
                FlowMap.put("iccid",iccid);
                Rlist.add(FlowMap);
                //重新赋值 sTime 下个包的开始时间 等于 当前包 往后 推一秒
                sTime = VeDate.getNextSecond(eTime,1);
               /* if(add_parameter.get("packet_flow")!=null){
                    FlowSize = Arith.add(FlowSize,Double.parseDouble(add_parameter.get("packet_flow").toString()));
                }*/
            }
        R_map.put("Rlist",Rlist);
        //R_map.put("FlowSize",FlowSize);
        return R_map;
    }





    }
