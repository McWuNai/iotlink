package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardBriefMapper;
import com.yunze.common.mapper.yunze.YzCardInfoExpandMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.ExcelConfig;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 批量变更卡信息  消费 者
 */
@Slf4j
@Component
public class CardInfoReplace {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardInfoExpandMapper yzCardInfoExpandMapper;
    @Resource
    private YzCardBriefMapper yzCardBriefMapper;

    private String Outcolumns[] =  {"vid", "msisdn", "iccid", "imsi", "open_date", "activate_date", "agent_id", "channel_id", "is_pool", "batch_date", "remind_ratio", "deliver_date", "customize_grouping", "remarks", "connection_status", "package_id", "imei", "type", "network_type", "is_sms", "sms_number", "gprs", "user_id", "is_polling", "test_period_last_time", "silent_period_last_time","is_NetworkBreak","执行描述","执行人","执行结果"};
    private String keys[] =        {"vid", "msisdn", "iccid", "imsi", "open_date", "activate_date", "agent_id", "channel_id", "is_pool", "batch_date", "remind_ratio", "deliver_date", "customize_grouping", "remarks", "connection_status", "package_id", "imei", "type", "network_type", "is_sms", "sms_number", "gprs", "user_id", "is_polling","test_period_last_time", "silent_period_last_time", "is_NetworkBreak","message","agentName","result"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 批量变更卡信息
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_CardImportReplace_queue")
    public void CardImportReplace(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();//项目根目录
            String ReadName = map.get("ReadName").toString();//上传新文件名
            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
            Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息


            Map<String,Object> task_map =  ( Map<String,Object>)map.get("task_map");//执行任务map
            String newName = map.get("newName").toString();//
            String UpdBackupName = map.get("UpdBackupName").toString();//

            String prefix = "admin_CardImportReplace_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ ReadName);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ ReadName, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                execution(filePath,ReadName,Pmap,User,task_map,newName,UpdBackupName);//执行批量变更卡信息
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量变更卡信息 消费者:{}<<", e.getMessage().toString());
        }
    }




    /**
     * 批量变更卡信息
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_CardImportReplace_queue")
    public void dlx_CardImportReplace(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();//项目根目录
            String ReadName = map.get("ReadName").toString();//上传新文件名
            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
            Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息

            Map<String,Object> task_map =  ( Map<String,Object>)map.get("task_map");//执行任务map
            String newName = map.get("newName").toString();//
            String UpdBackupName = map.get("UpdBackupName").toString();//


            String prefix = "admin_CardImportReplace_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ ReadName);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ ReadName, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                execution(filePath,ReadName,Pmap,User,task_map,newName,UpdBackupName);//执行批量变更卡信息
            }
        } catch (Exception e) {
            log.error(">>错误 - dlx_ 批量变更卡信息 消费者:{}<<", e.getMessage().toString());
        }
    }




    /**
     * 变更 执行
     */
    public void execution(String filePath,String ReadName,Map<String,Object> Pmap,Map<String,Object> User,Map<String,Object> task_map,String newName,String UpdBackupName){
        String Message = "";
        //1.读取 上传文件
        String path = filePath +  ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = {"vid", "msisdn", "iccid", "imsi", "open_date", "activate_date", "agent_id", "channel_id", "is_pool", "batch_date", "remind_ratio", "deliver_date", "customize_grouping", "remarks", "connection_status", "package_id", "imei", "type", "network_type", "is_sms", "sms_number", "gprs", "user_id", "is_polling","test_period_last_time", "silent_period_last_time","is_NetworkBreak"};
        List<Map<String, Object>> list = excelConfig.getExcelListMap(path,columns);
        Map<String, String> Dept = (Map<String, String>)User.get("dept");
        String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";


        String newExpandName = UUID.randomUUID().toString().replace("-","")+"_CardInfoExpandReplace";
        String UpdBackupExpandName = UUID.randomUUID().toString().replace("-","")+"__CardInfoReplaceExpandBackup";//设置分组备注前信息备份名称
        String deptId  = User.get("deptId").toString();

        String SaveUrl = task_map.get("url").toString();
       Integer type = Integer.parseInt(Pmap.get("myUpdType").toString());
        type+=1;// cardNumber 字典装换 +1

        String lie = "";
        switch (type){
            case 1:
                lie = "vid";
                break;
            case 2:
                lie = "msisdn";
                break;
            case 3:
                lie = "iccid";
                break;
            case 4:
                lie = "imsi";
                break;
        }

        if(list!=null && list.size()>0) {
            //筛选出  iccid 卡号 重复项
            Map<String, Object> getNotRepeatingMap =  Different.getNotRepeating(list,lie);//获取 筛选不重复的某列值 和 重复的
            list = (List<Map<String, Object>>) getNotRepeatingMap.get("Rlist");//更新 查询数据
            List<Map<String, Object>> Repeatlist = (List<Map<String, Object>>) getNotRepeatingMap.get("Repeatlist");
            if(Repeatlist.size()>0){
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("message"," ["+lie+"] 重复操作失败！同一 ["+lie+"] 同批次，无需多次操作！");
                defOutcolumns.put("agentName",create_by);
                defOutcolumns.put("result","操作失败");
                writeCSV.OutCSVObj(Repeatlist, newName,Outcolumns, keys,defOutcolumns,OutSize);
            }
            //查询数据库中 匹对iccid 是否存在
            HashMap<String, Object> map = new HashMap<>();
            //添加修改数据
            map.put("card_arrs",list);
            String myUpdType = Pmap.get("myUpdType").toString();
            map.put("myUpdType",myUpdType);
            map.put("type",type);
            List<String>  iccidarr = yzCardMapper.isExistence(map);
            if (iccidarr != null && iccidarr.size() > 0) {
                //1.判断 查询卡号是否都在库里
                    //库中查询出卡号与上传卡号数量 不一致 说明有卡号不在数据库中
                    if (!(iccidarr.size() == list.size())) {
                        // 获取 数组去重数据 和 重复值
                        Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, iccidarr, lie);//获取 筛选不重复的某列值 和 重复的
                        list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                        List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                        //找出与数据库已存在 相同 lie 去除 重复 lie
                        if (Out_list_Different.size() > 0) {
                            Map<String, Object> defOutcolumns = new HashMap<>();
                            defOutcolumns.put("message","["+lie+"] 不在数据库中！请核对["+lie+"]卡号！");
                            defOutcolumns.put("agentName",create_by);
                            defOutcolumns.put("result","操作失败");
                            writeCSV.OutCSVObj(Out_list_Different, newName,Outcolumns, keys,defOutcolumns,OutSize);
                        }
                    }
                    map.put("card_arrs", list);//更新 list
                    try {
                        if(list!=null && list.size()>0) {
                            //判断当前 操作用户所属企业 过滤不属于自己企业的卡
                            if(!deptId.equals("100")){
                                Map<String, Object> CountMap = new HashMap<>();
                                CountMap.putAll(map);
                                CountMap.put("agent_id",deptId);
                                CountMap.put("FindAgent_id",yzCardMapper.queryChildrenAreaInfo(CountMap));//查询代理所属下级代理
                                List<String> AgentIccidArr = yzCardMapper.findAgentIccid(CountMap);
                                //库中查询出【该代理旗下】卡号 与上传卡号数量 不一致 说明有卡号不在 其名下
                                if (!(AgentIccidArr.size() == list.size())) {
                                    // 获取 数组去重数据 和 重复值
                                    Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, AgentIccidArr, "iccid");//获取 筛选不重复的某列值 和 重复的
                                    list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                                    List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                                    //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                                    if (Out_list_Different.size() > 0) {
                                        Map<String, Object> defOutcolumns = new HashMap<>();
                                        defOutcolumns.put("message","越权操作！ iccid 不在操作人名下！请核对iccid卡号！");
                                        defOutcolumns.put("agentName",create_by);
                                        defOutcolumns.put("result","操作失败");
                                        writeCSV.OutCSVObj(Out_list_Different, newName,Outcolumns, keys,defOutcolumns,OutSize);
                                    }
                                }
                            }
                            map.put("card_arrs", list);//更新 list


                            if(list.size()>0){
                                //1.备份 修改前数据 主表
                                List<Map<String, Object>> BakUpArr =  yzCardMapper.findReplaceBk(map);
                                String Card_Outcolumns[] = {"vid", "msisdn", "iccid", "imsi", "open_date", "activate_date", "agent_id", "channel_id", "is_pool", "batch_date", "remind_ratio", "deliver_date", "customize_grouping","remarks", "connection_status", "package_id", "imei", "type", "network_type", "is_sms", "sms_number", "gprs", "user_id","is_polling","is_NetworkBreak", "执行描述","执行人","执行结果"};
                                String Card_keys[] = {"vid", "msisdn", "iccid", "imsi", "open_date", "activate_date", "agent_id", "channel_id", "is_pool", "batch_date", "remind_ratio", "deliver_date", "customize_grouping",  "remarks", "connection_status", "package_id", "imei", "type", "network_type", "is_sms", "sms_number", "gprs", "user_id","is_polling","is_NetworkBreak","message","agentName","result"};
                                if(BakUpArr!=null && BakUpArr.size()>0){
                                    Map<String, Object> defOutcolumns = new HashMap<>();
                                    defOutcolumns.put("message","操作前数据备份 操作时间："+ VeDate.getStringDate());
                                    defOutcolumns.put("agentName",create_by);
                                    defOutcolumns.put("result","操作成功");
                                    //主表备份导出列
                                   writeCSV.OutCSVObj(BakUpArr, UpdBackupName,Card_Outcolumns, Card_keys,defOutcolumns,OutSize);
                                }

                                int updCount = 0;
                                int updCard = 0;
                                List<Map<String, Object>> outCardArr = new ArrayList<>();
                                //2.修改主表
                                for (int i = 0; i < list.size(); i++) {
                                    Map<String, Object> uObj = list.get(i);
                                    String Msg = "",result = "";
                                    try {
                                        //System.out.println(uObj);
                                        uObj.put("myUpdType",myUpdType);
                                        updCount += yzCardMapper.updCardReplace(uObj);//同步主表信息
//                                        updCard += yzCardBriefMapper.batchUpd(uObj);
                                        result = "操作成功";
                                    }catch (Exception e){
                                        Msg = e.getMessage();
                                        log.info(">>错误 - yzCardMapper.updCardReplace :{} | {}<<", Msg);
                                        Msg = Msg.length()>100?Msg.substring(0,100):Msg;
                                        result = "操作失败";
                                    }
                                    uObj.put("message",Msg);
                                    uObj.put("agentName",create_by);
                                    uObj.put("result",result);
                                    outCardArr.add(uObj);
                                }
                                if(outCardArr.size()>0){
                                    writeCSV.OutCSVObj(list, newName,Card_Outcolumns, Card_keys,null,OutSize);
                                }
                                //3.筛选出需要同步的 拓展表信息


                                try {//有 测试期最后时间 或 沉默期最后时间 存入 卡列表拓展表
                                    List<Map<String, Object>> addExpandArr = new ArrayList<>();

                                    for (int i = 0; i < list.size(); i++) {
                                        Map<String, Object> card = list.get(i);
                                        boolean addBool = false;
                                        if(card.get("test_period_last_time")!=null && card.get("test_period_last_time")!="" && card.get("test_period_last_time").toString().length()>0){
                                            addBool = true;
                                        }
                                        if(card.get("silent_period_last_time")!=null && card.get("silent_period_last_time")!="" && card.get("silent_period_last_time").toString().length()>0){
                                            addBool = true;
                                        }
                                        if(addBool){
                                            addExpandArr.add(card);
                                        }
                                    }
                                    if(addExpandArr.size()>0){
                                        SaveUrl += ",/getcsv/"+newExpandName+".csv";
                                        SaveUrl += ",/getcsv/"+UpdBackupExpandName+".csv";

                                        task_map.put("url",SaveUrl);
                                        yzExecutionTaskMapper.upd(task_map);//修改文件路径

                                        if(!type.equals("3")){//非 iccid 为修改 依据情况下 获取 iccid
                                            List<Map<String, Object>> CardIccid =  yzCardMapper.findCardIccid(map);
                                            List<Map<String, Object>> setIccidArr = new ArrayList<>();

                                            if(CardIccid!=null && CardIccid.size()>0){
                                                for (int i = 0; i < list.size(); i++) {
                                                    Map<String, Object> obj = list.get(i);
                                                    String keyLie = obj.get(lie).toString();
                                                    for (int j = 0; j < CardIccid.size(); j++) {
                                                        Map<String, Object> getObj = CardIccid.get(j);
                                                        if(keyLie.equals(getObj.get(lie).toString())){
                                                            obj.put("iccid",getObj.get("iccid"));
                                                            break;
                                                        }
                                                    }
                                                    setIccidArr.add(obj);
                                                }
                                                map.put("card_arrs", setIccidArr);//更新 list
                                            }

                                        }
                                        List<Map<String, Object>> BakUpExpandArr =  yzCardInfoExpandMapper.findReplaceBk(map);
                                        String cardExpand_Outcolumns[] = {"iccid",  "测试期最后时间", "沉默期最后时间", "执行描述","执行人","执行结果"};
                                        String cardExpand_keys[] = {"iccid","gprs", "user_id","test_period_last_time","silent_period_last_time","result"};
                                        if(BakUpExpandArr!=null && BakUpExpandArr.size()>0){
                                            Map<String, Object> defOutcolumns = new HashMap<>();
                                            defOutcolumns.put("message","操作前数据备份 操作时间："+ VeDate.getStringDate());
                                            defOutcolumns.put("agentName",create_by);
                                            defOutcolumns.put("result","操作成功");
                                            //主表备份导出列
                                            writeCSV.OutCSVObj(BakUpExpandArr, UpdBackupExpandName,cardExpand_Outcolumns, cardExpand_keys,defOutcolumns,OutSize);
                                        }
                                        List<Map<String, Object>> outCardExpandArr =  cardExpandSyn(addExpandArr,create_by);//同步卡拓展信息表数据
                                        if(outCardExpandArr.size()>0){
                                            writeCSV.OutCSVObj(outCardExpandArr, newExpandName,cardExpand_Outcolumns, cardExpand_keys,null,OutSize);
                                        }
                                    }
                                }catch (Exception e){
                                    System.out.println("卡列表拓展表存入异常 "+e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Map<String, Object> defOutcolumns = new HashMap<>();
                        defOutcolumns.put("message",e.getCause().toString());
                        defOutcolumns.put("agentName",create_by);
                        defOutcolumns.put("result","操作失败");
                        writeCSV.OutCSVObj(list, newName,Outcolumns, keys,defOutcolumns,OutSize);
                        log.error(">> cardSet-消费者- 上传excel异常 [ Exception  ] :{}<<", e.getMessage());
                    }
            } else {
                log.info("上传 "+lie+" 不在数据库中！请核对后重试！");
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("message","操作 "+lie+" 平台中未找到！查询取消！");
                defOutcolumns.put("agentName",create_by);
                defOutcolumns.put("result","操作失败");
                writeCSV.OutCSVObj(list, newName,Outcolumns, keys,defOutcolumns,OutSize);
            }
            yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        }else{
            log.error( "admin_CardImportReplace_queue-消费者 上传表格无数据！无需执行");
        }

    }



    /**
     * 同步卡拓展信息表数据
     * @param addExpandArr
     */
    public List<Map<String, Object>> cardExpandSyn( List<Map<String, Object>> addExpandArr,String create_by){
        Map<String, Object> findExpandMap = new HashMap<>();
        findExpandMap.put("card_arrs",addExpandArr);
        //查询是否存在 拓展信息
        List<String> exExpandArr =  yzCardInfoExpandMapper.findInfoExpandArr(findExpandMap);
        List<Map<String, Object>> addEArr = new ArrayList<>();
        List<Map<String, Object>> updEArr = new ArrayList<>();
        Map<String, Object> addExpandMap = new HashMap<>();
        if(exExpandArr!=null && exExpandArr.size()>0){

            //匹对出以存在拓展信息 加入修改数组 否则 加入 新增数组
            for (int i = 0; i < addExpandArr.size(); i++) {
                Map<String, Object> cObj = addExpandArr.get(i);
                String iccid = cObj.get("iccid").toString();
                boolean ex = false;
                for (int j = 0; j < exExpandArr.size(); j++) {
                    String exIccid = exExpandArr.get(j);
                    if(iccid.equals(exIccid)){
                        ex = true;
                        break;
                    }
                }
                if(ex){
                    updEArr.add(cObj);
                }else {
                    addEArr.add(cObj);
                }
            }
        }else{
            addEArr = addExpandArr;
        }
        int addExpanCount = 0;
        int updExpanCount = 0;

        List<Map<String, Object>> outCardArr = new ArrayList<>();

        if(addEArr!=null && addEArr.size()>0){
            addExpandMap.put("card_arrs",addEArr);
            String Msg = "",result = "";
            try {
                addExpanCount = yzCardInfoExpandMapper.saveInfoExpand(addExpandMap);
                result = "操作成功";
            }catch (Exception e){
                Msg = e.getMessage();
                log.info(">>错误 - yzCardMapper.saveInfoExpand :{} | {}<<", Msg);
                Msg = Msg.length()>100?Msg.substring(0,100):Msg;
                result = "操作失败";
            }
            for (int i = 0; i < addEArr.size() ; i++) {
                Map<String, Object> outObj = addEArr.get(i);
                outObj.put("message",Msg);
                outObj.put("agentName",create_by);
                outObj.put("result",result);
                outCardArr.add(outObj);
            }
        }
        if(updEArr!=null && updEArr.size()>0){
            for (int i = 0; i < updEArr.size(); i++) {
                Map<String, Object> outObj = updEArr.get(i);
                String Msg = "",result = "";
                try {
                    updExpanCount += yzCardInfoExpandMapper.updInfoExpand(outObj);
                }catch (Exception e){
                        Msg = e.getMessage();
                        log.info(">>错误 - yzCardMapper.saveInfoExpand :{} | {}<<", Msg);
                        Msg = Msg.length()>100?Msg.substring(0,100):Msg;
                        result = "操作失败";
                }
                outObj.put("message",Msg);
                outObj.put("agentName",create_by);
                outObj.put("result",result);
                outCardArr.add(outObj);
            }
        }
        System.out.println("卡信息拓展表需 同步 ："+addExpandArr.size()+" 实际新增 ："+addExpanCount+" 实际修改 "+ updExpanCount);
        return outCardArr;
    }






}
