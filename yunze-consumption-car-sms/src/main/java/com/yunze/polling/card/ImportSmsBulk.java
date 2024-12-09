package com.yunze.polling.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 批量 生成短信下发任务
 */
@Slf4j
@Component
public class ImportSmsBulk {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private BulkUtil bulkUtil;


    private String Outcolumns[] = {"iccid","执行描述","执行人","执行结果"};
    private String keys[] = {"iccid","message","agentName","result"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 批量 生成短信下发任务
     * @param msg
     * @param channel
     * @throws IOException
     */

    @RabbitHandler
    @RabbitListener(queues = "admin_smsCC_queue")
    public void CardCancelrealname(String msg, Channel channel)  {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();//项目根目录
            String ReadName = map.get("ReadName").toString();//上传新文件名
            String SaveUrl = "/getOriginal"+ReadName;

            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
            Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息




            Map<String,Object>  bulkMap = ( Map<String,Object>)map.get("bulkMap");//批量任务主表 信息
            bulkMap.put("state_id","3");//状态  执行中 3
            bulkMap.put("start_time", VeDate.getStringDate());//赋值 开始时间
            bulkMap.put("url", SaveUrl);//url 存储上传文件 原始地址
            bulkUtil.smsUpdate(bulkMap);//消费者进入变更执行状态 执行中


            String prefix = "admin_smsCC_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ ReadName);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ ReadName, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                UpLoadExecution(filePath,ReadName,Pmap,User,bulkMap,SaveUrl);//执行特殊操作 查询imie
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量发送短信  消费者:{}<<", e.getMessage().toString());
        }
    }




    @RabbitHandler
    @RabbitListener(queues = "admin_smsCCTextField_queue")
    public void smsCCTextField(String msg, Channel channel)  {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String SaveUrl = "";

            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
            Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息
            List<Map<String, Object>> list  =  ( List<Map<String, Object>>)map.get("list");//文本域卡号

            Map<String,Object>  bulkMap = ( Map<String,Object>)map.get("bulkMap");//批量任务主表 信息
            bulkMap.put("state_id","3");//状态  执行中 3
            bulkMap.put("start_time", VeDate.getStringDate());//赋值 开始时间
            bulkMap.put("url", SaveUrl);//url 存储上传文件 原始地址
            bulkUtil.smsUpdate(bulkMap);//消费者进入变更执行状态 执行中
            String deptId = User.get("deptId").toString();

            String prefix = "admin_smsCCTextField_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ deptId);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ deptId, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                execution(User,bulkMap,SaveUrl,list);//执行特殊操作 查询imie
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量发送短信文本域  消费者:{}<<", e.getMessage().toString());
        }
    }




    private void UpLoadExecution(String filePath, String ReadName, Map<String, Object> pmap, Map<String, Object> User,Map<String,Object> bulkMap,String SaveUrl) {
        //1.读取 上传文件
        String path = filePath  + ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = {"iccid"};
        List<Map<String, Object>> list = excelConfig.getExcelListMap(path,columns);
        execution(User, bulkMap, SaveUrl,list);
    }







    /**
     * 查询执行
     * */
    private void execution(Map<String, Object> User,Map<String,Object> bulkMap,String SaveUrl,List<Map<String, Object>> list) {
        boolean isFailBool = false;//是否有失败的错误数据


        Map<String, String> Dept = (Map<String, String>)User.get("dept");
        String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";

        String newName = UUID.randomUUID().toString().replace("-","")+"_repeat";//对应 执行任务导出类别 CSV 前缀

        String state_id = "1";//执行任务状态
        if(list!=null && list.size()>0) {

            //筛选出  iccid 卡号 重复项
            Map<String, Object> getNotRepeatingMap =  Different.getNotRepeating(list,"iccid");//获取 筛选不重复的某列值 和 重复的
            list = (List<Map<String, Object>>) getNotRepeatingMap.get("Rlist");//更新 查询数据
            List<Map<String, Object>> Repeatlist = (List<Map<String, Object>>) getNotRepeatingMap.get("Repeatlist");
            if(Repeatlist.size()>0) {
                isFailBool = true;
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("message", "ICCID重复查询失败！同一ICCID同批次，无需多次查询！");
                defOutcolumns.put("agentName", create_by);
                defOutcolumns.put("result", "查询失败");
                writeCSV.OutCSVObj(Repeatlist, newName, Outcolumns, keys, defOutcolumns, OutSize);
            }

            //查询数据库中 匹对iccid 是否存在
            HashMap<String, Object> map = new HashMap<>();
            //添加修改数据
            map.put("card_arrs",list);
            map.put("type","3");
            List<String>  iccidarr = yzCardMapper.isExistence(map);


            //新增批量操作详情
            Map<String, Object> bulkDtailsMap = new HashMap<>();
            String b_id =  bulkMap.get("id").toString();
            bulkDtailsMap.put("b_id",b_id);
            bulkDtailsMap.put("bus_arrs",list);
            bulkUtil.smsDadd(bulkDtailsMap);


            if (iccidarr != null && iccidarr.size() > 0) {
                if (!(iccidarr.size() == list.size())) {
                    //上传数据>数据库查询 赛选出
                    List<String> list1 = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        list1.add(list.get(i).get("iccid").toString());
                    }
                    // 获取 数组去重数据 和 重复值
                    Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                    list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                    List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                    //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                    if (Out_list_Different.size() > 0) {
                        bulkUtil.smsDupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","iccid卡号未找到！操作取消！");
                    }
                }
                map.put("card_arrs", list);//更新 list
                try {
                    if(list!=null && list.size()>0) {
                        //筛选出未划分通道的
                        map.put("channel_idType","notNull");
                        List<String>  Channel_iccidarr = yzCardMapper.isExistence(map);
                        if (Channel_iccidarr != null && Channel_iccidarr.size() > 0) {
                            if (!(Channel_iccidarr.size() == list.size())) {
                                //上传数据>数据库查询 赛选出
                                List<String> list1 = new ArrayList<>();
                                for (int i = 0; i < list.size(); i++) {
                                    list1.add(list.get(i).get("iccid").toString());
                                }
                                // 获取 数组去重数据 和 重复值
                                Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, Channel_iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                                list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                                List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                                //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                                if (Out_list_Different.size() > 0) {
                                    bulkUtil.smsDupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","未划分通道！操作取消！");
                                }
                            }

                            //筛选出 未开通短信服务或没有短信服务号码的


                            List<String>  Sms_iccidarr =  yzCardMapper.findSmsArr(map);
                            if (Sms_iccidarr != null && Sms_iccidarr.size() > 0) {



                                map.put("card_arrs", list);//更新 list
                                List<Map<String, Object>> find_crArr = yzCardRouteMapper.find_cr();//查询 通道简要信息  状态为 正常
                                List<Object> channel_idArr = new ArrayList<>();
                                if(find_crArr!=null && find_crArr.size()>0){
                                    for (int i = 0; i < find_crArr.size(); i++) {
                                        channel_idArr.add(find_crArr.get(i).get("dictValue"));
                                    }
                                }else{
                                    channel_idArr.add("0");
                                }
                                //筛选出通道正常的进行 查询
                                map.put("channel_idArr",channel_idArr);
                                List<String>  normalChannel_iccidarr = yzCardMapper.isExistence(map);
                                if(normalChannel_iccidarr!=null && normalChannel_iccidarr.size()>0){
                                    if (!(normalChannel_iccidarr.size() == list.size())) {
                                        //上传数据>数据库查询 赛选出
                                        List<String> list1 = new ArrayList<>();
                                        for (int i = 0; i < list.size(); i++) {
                                            list1.add(list.get(i).get("iccid").toString());
                                        }
                                        // 获取 数组去重数据 和 重复值
                                        Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, normalChannel_iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                                        list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                                        List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                                        //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                                        if (Out_list_Different.size() > 0) {
                                            bulkUtil.smsDupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","通道状态 为停用或已删除！操作取消！");
                                        }
                                    }
                                    map.put("card_arrs", list);//更新 list
                                    map.put("sms_code", "notNull");//通道配置不能为空

                                    /**
                                     *  这下面是做 通道检查是否配置了 短信下发的信息没有配置的直接执行失败 [其他卡号进入待处理状态，等待定时任务下发执行短信抄送]
                                     * */
                                    List<Map<String, Object>> findRouteArr = yzCardMapper.findRouteArr(map);//查询需要轮询的卡
                                    if(findRouteArr!=null && findRouteArr.size()>0){
                                        if (!(findRouteArr.size() == list.size())) {
                                            //上传数据>数据库查询 赛选出
                                            List<String> list1 = new ArrayList<>();
                                            for (int i = 0; i < list.size(); i++) {
                                                list1.add(list.get(i).get("iccid").toString());
                                            }
                                            List<String> list_findRouteArr = new ArrayList<>();
                                            for (int i = 0; i < findRouteArr.size(); i++) {
                                                list_findRouteArr.add(findRouteArr.get(i).get("iccid").toString());
                                            }
                                            // 获取 数组去重数据 和 重复值
                                            Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, list_findRouteArr, "iccid");//获取 筛选不重复的某列值 和 重复的
                                            list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                                            List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                                            //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                                            if (Out_list_Different.size() > 0) {
                                                bulkUtil.smsDupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","通道状态 为停用或已删除！操作取消！");
                                            }

                                        }
                                        if(list!=null && list.size()>0){
                                            state_id = "3";//状态 为 3 执行中，因为还未执行下发短信实际操作 等待定时任务触发短信下发执行
                                            //开始 赋值 下发 短信号
                                            for (int i = 0; i < list.size(); i++) {
                                                Map<String, Object> card = list.get(i);
                                                Map<String, Object> updMap = new HashMap<>();
                                                String iccid = card.get("iccid").toString();

                                                updMap.put("b_id",b_id);
                                                updMap.put("card_number",iccid);
                                                String send_number = "";
                                                for (int j = 0; j < findRouteArr.size(); j++) {
                                                    Map<String, Object> cardRoute = findRouteArr.get(j);
                                                    String rIccid = cardRoute.get("iccid").toString();
                                                    String sms_number = cardRoute.get("sms_number").toString();
                                                    if(iccid.equals(rIccid)){
                                                        send_number = sms_number;//赋值短信号
                                                        break;
                                                    }
                                                }
                                                updMap.put("send_number",send_number);
                                                bulkUtil.smsDupdate(updMap);
                                            }
                                        }
                                    }else{
                                        bulkUtil.smsDupdateArr(b_id,list,VeDate.getStringDate(),"2","通道未配置短信接口信息 取消操作");
                                    }
                                }else{
                                    bulkUtil.smsDupdateArr(b_id,list,VeDate.getStringDate(),"2","通道状态 为停用或已删除 取消操作");
                                }
                            }else{
                                bulkUtil.smsDupdateArr(b_id,list,VeDate.getStringDate(),"2","未开通短信服务或未配置短信服务号码！请检查配置信息！");
                            }
                        }else{
                            bulkUtil.smsDupdateArr(b_id,list,VeDate.getStringDate(),"2","未划分通道 取消操作");
                        }
                    }
                }catch (DuplicateKeyException e){
                    log.error(">> cardSet-消费者- 上传excel异常 [插入数据 DuplicateKeyException ] :{}<<", e.getMessage().toString());
                }
            }
        }else{
            log.error( "admin_importSetCardInfo_queue-消费者 上传表格无数据！无需执行");
        }

        bulkMap.put("state_id",state_id);//状态  完成 1
        if(state_id.equals("1")){
            bulkMap.put("end_time", VeDate.getStringDate());//赋值 结束时间
        }
        if(isFailBool){
            SaveUrl += ",/getcsv/"+newName+".csv";
            bulkMap.put("url", SaveUrl);//url 存储上传文件 原始地址
        }
        bulkUtil.smsUpdate(bulkMap);//消费者进入变更执行状态
    }

}

























