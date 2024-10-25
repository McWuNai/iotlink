package com.yunze.common.utils.yunze;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单 生成 辅助类
 */
@Component
@Slf4j
public class PlOrder{

    @Resource
    private YzCardFlowMapper cardFlowMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private BulkUtil bulkUtil;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;


    public Map<String, Object> orderOpen(Map<String,Object> map){
        Map<String, Object> OrderMap = new HashMap<>();
        //或充值 资费计划信息
        String agent_id = map.get("agent_id").toString();
        Map<String,Object> PacketMap = null;

        String ord_name = "",pay_type = "";
        if(agent_id.equals("100")){
            PacketMap = cardFlowMapper.FindPacket(map);
            ord_name = "平台-批量充值";
            pay_type = "s";
        }else{
            PacketMap = cardFlowMapper.FindPacketAgent(map);
            ord_name = "企业-批量充值";
            pay_type = "ct";
        }
        if(PacketMap!=null) {
            String ord_type = "2";
            //添加订单生成参数
            PacketMap.put("validate_type", map.get("validate_type"));//生效类型
            PacketMap.put("ord_type", ord_type);//订单类型

            //判断 是否需要订购达量断网
            String is_dd = PacketMap.get("is_dd")!=null? PacketMap.get("is_dd").toString():"0";
            String is_boot_status = "0";
            String is_openNet_status = "0";
            if(map.get("is_boot_status")!=null){
                is_boot_status =  map.get("is_boot_status")!=null? map.get("is_boot_status").toString():"0";
            }else{
                if(is_dd.equals("1")){//需要 订购达量断网时
                    is_boot_status = "1";
                }
            }
            if(map.get("is_openNet_status")!=null){
                is_openNet_status = map.get("is_openNet_status")!=null? map.get("is_openNet_status").toString():"0";
            }else{
                if(is_dd.equals("1")){//需要 订购达量断网时
                    is_openNet_status = "1";
                }
            }
            OrderMap.put("is_dd_status", is_dd);//是否订购达量断网
            OrderMap.put("is_boot_status", is_boot_status);//是否 开机
            OrderMap.put("is_openNet_status", is_openNet_status);//是否 开网

            OrderMap.put("add_parameter", JSON.toJSONString(PacketMap));//加包参数
            OrderMap.put("ord_type", ord_type);//订单类型 用量充值
            OrderMap.put("ord_name", ord_name);//交易名称 用量充值
            OrderMap.put("wx_ord_no", null);//微信订单号
            OrderMap.put("status", "1");//支付状态 支付成功
            OrderMap.put("price", PacketMap.get("packet_price"));//交易金额
            OrderMap.put("account", "0");//交易后金额
            OrderMap.put("pay_type", pay_type);//支付方式 s 平台记录  ct 授信支付
            OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
            OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
            OrderMap.put("show_status", "0");//是否显示交易记录:0否-1显示
            OrderMap.put("packet_id", map.get("packet_id"));//资费计划 ID
            OrderMap.put("validate_type", map.get("validate_type"));//生效类型
            OrderMap.put("open_id", null);//openid 微信支付记录操作人
            OrderMap.put("agent_id", agent_id);// 所属记录企业id
            OrderMap.put("profit_type", "0");//分润类型
            OrderMap.put("info", null);//备注
            OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai

        }
        return OrderMap;
    }

    /**
     * API订单充值生成
     * @param map
     * @return
     */
    public Map<String, Object> ApiorderOpen(Map<String,Object> map){
        Map<String, Object> OrderMap = new HashMap<>();
        //或充值 资费计划信息
        String agent_id = map.get("agent_id").toString();
        Map<String,Object> PacketMap = null;

        String ord_name = "",pay_type = "";
        PacketMap = cardFlowMapper.FindPacketAgent(map);
        ord_name = "企业-API充值";
        pay_type = "ct";
        if(PacketMap!=null) {
            String ord_type = "2";
            //添加订单生成参数
            PacketMap.put("validate_type", map.get("validate_type"));//生效类型
            PacketMap.put("ord_type", ord_type);//订单类型

            //判断 是否需要订购达量断网
            String is_dd = PacketMap.get("is_dd")!=null? PacketMap.get("is_dd").toString():"0";
            String is_boot_status = "0";
            String is_openNet_status = "0";
            if(map.get("is_boot_status")!=null){
                is_boot_status =  map.get("is_boot_status")!=null? map.get("is_boot_status").toString():"0";
            }else{
                if(is_dd.equals("1")){//需要 订购达量断网时
                    is_boot_status = "1";
                }
            }
            if(map.get("is_openNet_status")!=null){
                is_openNet_status = map.get("is_openNet_status")!=null? map.get("is_openNet_status").toString():"0";
            }else{
                if(is_dd.equals("1")){//需要 订购达量断网时
                    is_openNet_status = "1";
                }
            }
            OrderMap.put("is_dd_status", is_dd);//是否订购达量断网
            OrderMap.put("is_boot_status", is_boot_status);//是否 开机
            OrderMap.put("is_openNet_status", is_openNet_status);//是否 开网


            OrderMap.put("add_parameter", JSON.toJSONString(PacketMap));//加包参数
            OrderMap.put("ord_type", ord_type);//订单类型 用量充值
            OrderMap.put("ord_name", ord_name);//交易名称 用量充值
            OrderMap.put("wx_ord_no", null);//微信订单号
            OrderMap.put("status", "1");//支付状态 支付成功
            OrderMap.put("price", PacketMap.get("packet_price"));//交易金额
            OrderMap.put("account", "0");//交易后金额
            OrderMap.put("pay_type", pay_type);//支付方式 s 平台记录  ct 授信支付
            OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
            OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
            OrderMap.put("show_status", "0");//是否显示交易记录:0否-1显示
            OrderMap.put("packet_id", map.get("packet_id"));//资费计划 ID
            OrderMap.put("validate_type", map.get("validate_type"));//生效类型
            OrderMap.put("open_id", null);//openid 微信支付记录操作人
            OrderMap.put("agent_id", agent_id);// 所属记录企业id
            OrderMap.put("profit_type", "0");//分润类型
            OrderMap.put("info", null);//备注
            OrderMap.put("iccid", map.get("iccid"));
            OrderMap.put("cre_type", "api");//生成方式  接口 api
            OrderMap.put("is_audit", "0");//是否已审核通过 0否 1 是
            OrderMap.put("callback_address", map.get("callbackAddress"));//回调地址
        }
        return OrderMap;
    }








    /**
     * 生成订单 文本域充值
     * @param filePath
     * @param ReadName
     * @param newName
     * @param Pmap
     * @param User
     * @param OrderMap
     */
    public void TextAddOrder(List<String> iccidArr, String filePath, String ReadName, String newName, Map<String,Object> Pmap, Map<String,Object> User, Map<String,Object> OrderMap, Map<String,Object> bulkMap, String SaveUrl){
        //1.读取 上传文件
        List<Map<String, Object>> list = new ArrayList<>();

        List<String> Exlist = new ArrayList<String>();//记录 已生成的订单编号避免重复
        //
        for (int i = 0; i < iccidArr.size(); i++) {
            Map<String,Object> addMap = new HashMap<>();
            addMap.putAll(OrderMap);
            String ord_no = VeDate.getNo(8);
            while (true){
                if(!CommonlyUsed.Val_Is_Arr(Exlist,ord_no)){
                    Exlist.add(ord_no);
                    break;
                }else{
                    ord_no = VeDate.getNo(8);
                }
            }
            addMap.put("ord_no",ord_no);
            addMap.put("iccid",iccidArr.get(i));
            list.add(addMap);
        }

        if(list!=null && list.size()>0){
            PlusPackageCommon(list,newName,Pmap,User,OrderMap,bulkMap,SaveUrl);
        }else{
            log.error( "admin-消费者 上传表格无数据！无需执行充值");
        }
    }


    /**
     * 生成订单
     * @param filePath
     * @param ReadName
     * @param newName
     * @param Pmap
     * @param User
     * @param OrderMap
     */
    public void AddOrder(String filePath,String ReadName,String newName,Map<String,Object> Pmap,Map<String,Object> User,Map<String,Object> OrderMap,Map<String,Object> bulkMap,String SaveUrl){
        //1.读取 上传文件
        String path = filePath  + ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = {"iccid"};

        List<Map<String, Object>> list = excelConfig.getExcelListMap(path,columns,OrderMap,"order");


        if(list!=null && list.size()>0){
            PlusPackageCommon(list,newName,Pmap,User,OrderMap,bulkMap,SaveUrl);
        }else{
            log.error( "admin-消费者 上传表格无数据！无需执行充值");
        }
    }


    /**
     * 上传加包公共执行
     * @param list
     * @param newName
     * @param Pmap
     * @param User
     * @param OrderMap
     * @param bulkMap
     * @param SaveUrl
     */
    public void PlusPackageCommon (List<Map<String, Object>> list,String newName,Map<String,Object> Pmap,Map<String,Object> User,Map<String,Object> OrderMap,Map<String,Object> bulkMap,String SaveUrl){
        boolean isFailBool = false;//是否有失败的错误数据

        String  create_by = bulkMap.get("auth").toString();
        String Outcolumns[] = {"iccid","操作描述","执行时间","执行结果","执行人"};
        String keys[] = {"iccid","description","time","result","agentName"};

        //筛选出  iccid 卡号 重复项
        Map<String, Object> getNotRepeatingMap =  Different.getNotRepeating(list,"iccid");//获取 筛选不重复的某列值 和 重复的
        list = (List<Map<String, Object>>) getNotRepeatingMap.get("Rlist");//更新 充值数据
        List<Map<String, Object>> Repeatlist = (List<Map<String, Object>>) getNotRepeatingMap.get("Repeatlist");
        if(Repeatlist.size()>0){
            isFailBool = true;
            writeCSV.OutCSVObj(Repeatlist,newName,"ICCID重复充值失败！同一ICCID同批次，多次充值不允许！",create_by,"充值失败",Outcolumns,keys);
        }
        //查询数据库中 匹对iccid 是否存在
        HashMap<String, Object> map = new HashMap<>();
        map.put("order_arrs",list);
        map.put("card_arrs",list);
        map.put("type","3");
        List<String>  iccidarr = yzCardMapper.isExistence(map);

        //新增批量操作详情
        Map<String, Object> bulkDtailsMap = new HashMap<>();
        String b_id =  bulkMap.get("id").toString();
        bulkDtailsMap.put("b_id",b_id);
        bulkDtailsMap.put("bus_arrs",list);
        bulkUtil.Dadd(bulkDtailsMap);

        //1.判断 充值卡号是否都在库里
        if(iccidarr.size()>0){
            //库中查询出卡号与上传卡号数量 不一致 说明有卡号不在数据库中
            if(!(iccidarr.size()==list.size())){
                //上传数据>数据库查询 赛选出
                List<String> list1 = new ArrayList<>();
                for (int i = 0; i <list.size() ; i++) {
                    list1.add(list.get(i).get("iccid").toString());
                }
                // 获取 数组去重数据 和 重复值
                Map<String, Object> getNotRepeatingMap_DB =  Different.getNotRepeating(list,iccidarr,"iccid");//获取 筛选不重复的某列值 和 重复的
                list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 充值数据
                List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 充值数据
                //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                if(Out_list_Different.size()>0){
                    bulkUtil.DupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","iccid卡号未找到！操作取消！");

                }
            }
            map.put("order_arrs",list);//更新 list
            map.put("create_by",create_by);
            try {
                if(list.size()>0){
                    int  sInt = importOrder(map);
                    if(sInt>0){
                        bulkUtil.DupdateArr(b_id,list,VeDate.getStringDate(),"1","");

                    }
                }
            }catch (DuplicateKeyException e){
                bulkUtil.DupdateArr(b_id,list,VeDate.getStringDate(),"2","充值失败");
                log.error(">> order-消费者- 上传excel异常 [插入数据 DuplicateKeyException ] :{}<<", e.getMessage());
            }catch (Exception e){
                bulkUtil.DupdateArr(b_id,list,VeDate.getStringDate(),"2","充值失败");
                log.error(">>order-消费者- 批量充值消费者:{}<<", e.getMessage().toString());
            }
        }else{
            isFailBool = true;
            bulkUtil.DupdateArr(b_id,list,VeDate.getStringDate(),"2","iccid卡号未找到！操作取消！");
            //上传卡号不在数据库中
            //writeCSV.OutCSVObj(list,newName,"充值iccid卡号平台中未找到！充值取消！",create_by,"充值失败",Outcolumns,keys);
        }
        bulkMap.put("state_id","1");//状态  完成 1
        bulkMap.put("end_time", VeDate.getStringDate());//赋值 结束时间
        if(isFailBool){
            if(SaveUrl.length()>0){
                SaveUrl += ",/getcsv/"+newName+".csv";
            }else{
                SaveUrl += "/getcsv/"+newName+".csv";
            }
            bulkMap.put("url", SaveUrl);//url 存储上传文件 原始地址
        }
        bulkUtil.update(bulkMap);//消费者进入变更执行状态 执行中
    }


    /**
     * 批量新增订单时 判断是否 需要 订购达量断网  是否 开机 是否 开网 需要的整理卡号发送
     * @param pMap
     * @return
     */
    public int importOrder(Map<String, Object> pMap){
        int count = 0;
        count = yzOrderMapper.importOrder(pMap);
        if(count>0 ){
            List<Map<String, Object>> order_arrs = (List<Map<String, Object>>) pMap.get("order_arrs");
            for (int i = 0; i < order_arrs.size(); i++) {
                Map<String, Object> map = order_arrs.get(i);
                String status = map.get("status").toString();//支付状态 == 1 //支付成功时 触发
                if(status.equals("1")){
                    try {
                        String is_dd_status = map.get("is_dd_status") != null ? map.get("is_dd_status").toString() : "";
                        String is_boot_status = map.get("is_boot_status") != null ? map.get("is_boot_status").toString() : "";
                        String is_openNet_status = map.get("is_openNet_status") != null ? map.get("is_openNet_status").toString() : "";
                        if (is_dd_status.equals("1") || is_boot_status.equals("1") || is_openNet_status.equals("1")) {
                            Map<String, Object> CardDiagnosisMap = new HashMap<>();
                            List<String> operationArr = new ArrayList<String>();
                            /*if (is_dd_status.equals("1")) {
                                operationArr.add("InternetDisconnection");//订购达量断网

                                String cre_type = map.get("cre_type").toString();//生成方式
                                String pay_type = map.get("pay_type").toString();//支付方式
                                String source_type = "9";//变更来源 PC端充值 9  Api变更 8 C端充值 4
                                if(cre_type.equalsIgnoreCase("api")){
                                    source_type = "8";
                                }else {
                                    if(pay_type.equalsIgnoreCase("wx")){
                                        source_type = "4";
                                    }
                                }
                                CardDiagnosisMap.put("source_type", source_type);//变更来源
                                if (map.get("add_parameter") != null) {
                                    try {
                                        Map<String, Object> add_parameter = JSON.parseObject(map.get("add_parameter").toString());//加包参数
                                        String errStr = add_parameter.get("error_flow").toString();
                                        int eIndex = errStr.indexOf(".");
                                        errStr = eIndex!=-1?errStr.substring(0,eIndex):errStr;
                                        Integer error_flow = Integer.parseInt(errStr);//获取资费计划  套餐流量
                                        CardDiagnosisMap.put("quota", error_flow);//阈值MB
                                    } catch (Exception e) {
                                        log.error("orderSave > InternetDisconnection 获取 add_parameter 异常 {} ====  {}", JSON.toJSONString(map), e.getMessage());
                                    }
                                } else {
                                    log.error("orderSave > InternetDisconnection 获取 add_parameter==null 达量断网订购取消 {} ", JSON.toJSONString(map));
                                }
                            }*/
                            if (is_openNet_status.equals("1")) {
                                operationArr.add("OpenNetwork");//开网
                            }
                            if (is_boot_status.equals("1")) {
                                operationArr.add("PowerOn");//开机
                            }
                            CardDiagnosisMap.put("optionType", operationArr);
                            CardDiagnosisMap.put("iccid", map.get("iccid"));
                            Map<String, Object> updOrderMap = new HashMap<>();
                            updOrderMap.put("ord_no",map.get("ord_no"));
                            CardDiagnosisMap.put("updOrderMap", updOrderMap);

                            orderCardDiagnosisSend(CardDiagnosisMap);

                        }
                    } catch (Exception e) {
                        System.out.println("importOrder 判断是否需要 【订购达量断网】【开机】【开网】异常 " + e.getMessage());
                    }
                }

            }
        }
        return count;
    }






    /**
     * 批量新增订单时 判断是否 需要 订购达量断网  是否 开机 是否 开网 需要的整理卡号发送
     * @param map
     * @return
     */
    public int orderSave(Map<String, Object> map){
        int count = 0;
        count = yzOrderMapper.save(map);
        String status = map.get("status").toString();//支付状态 == 1 //支付成功时 触发
        if(count>0 && status.equals("1")){
            try {
                String is_dd_status = map.get("is_dd_status")!=null?map.get("is_dd_status").toString():"";
                String is_boot_status = map.get("is_boot_status")!=null?map.get("is_boot_status").toString():"";
                String is_openNet_status = map.get("is_openNet_status")!=null?map.get("is_openNet_status").toString():"";
                if(is_dd_status.equals("1") || is_boot_status.equals("1") || is_openNet_status.equals("1") ){
                    Map<String,Object> CardDiagnosisMap = new HashMap<>();

                    List<String> operationArr = new  ArrayList<String>();
                   /* if(is_dd_status.equals("1")){
                        operationArr.add("InternetDisconnection");//订购达量断网

                        String cre_type = map.get("cre_type").toString();//生成方式
                        String pay_type = map.get("pay_type").toString();//支付方式
                        String source_type = "9";//变更来源 PC端充值 9  Api变更 8 C端充值 4
                        if(cre_type.equalsIgnoreCase("api")){
                            source_type = "8";
                        }else {
                            if(pay_type.equalsIgnoreCase("wx")){
                                source_type = "4";
                            }
                        }
                        CardDiagnosisMap.put("source_type", source_type);//变更来源
                        if(map.get("add_parameter")!=null){
                            try {
                                Map<String,Object> add_parameter = JSON.parseObject(map.get("add_parameter").toString());//加包参数
                                String errStr = add_parameter.get("error_flow").toString();
                                int eIndex = errStr.indexOf(".");
                                errStr = eIndex!=-1?errStr.substring(0,eIndex):errStr;
                                Integer error_flow = Integer.parseInt(errStr);//获取资费计划  套餐流量
                                CardDiagnosisMap.put("quota", error_flow);//阈值MB
                            }catch (Exception e){
                                log.error("orderSave > InternetDisconnection 获取 add_parameter 异常 {} ====  {}",JSON.toJSONString(map),e.getMessage());
                            }
                        }else {
                            log.error("orderSave > InternetDisconnection 获取 add_parameter==null 达量断网订购取消 {} ",JSON.toJSONString(map));
                        }
                    }*/
                    if(is_openNet_status.equals("1")){
                        operationArr.add("OpenNetwork");//开网
                    }
                    if(is_boot_status.equals("1")){
                        operationArr.add("PowerOn");//开机
                    }
                    CardDiagnosisMap.put("optionType",operationArr);
                    CardDiagnosisMap.put("iccid",map.get("iccid"));
                    Map<String, Object> updOrderMap = new HashMap<>();
                    updOrderMap.put("ord_no",map.get("ord_no"));
                    CardDiagnosisMap.put("updOrderMap", updOrderMap);
                    orderCardDiagnosisSend(CardDiagnosisMap);
                }
            }catch (Exception e){
                System.out.println("orderSave 判断是否需要 【订购达量断网】【开机】【开网】异常 "+e.getMessage());
            }
        }
        return count;
    }


    /**
     * 开机 开网 达量断网诊断发送
     * @param CardDiagnosisMap
     */
    public void orderCardDiagnosisSend(Map<String, Object> CardDiagnosisMap){
        String polling_queueName = "admin_orderCardDiagnosis_queue";
        String polling_routingKey = "admin.orderCardDiagnosis.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(CardDiagnosisMap), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("orderCardDiagnosisSend 【订购达量断网】【开机】【开网】 指令发送异常 " + e.getMessage());
        }
    }


}
