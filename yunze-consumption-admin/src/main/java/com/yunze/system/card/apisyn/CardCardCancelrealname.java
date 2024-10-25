package com.yunze.system.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
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
 * 批量取消实名
 */
@Slf4j
@Component
public class CardCardCancelrealname {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private BulkUtil bulkUtil;


    private String Outcolumns[] = {"iccid","执行描述","执行人","执行结果"};
    private String keys[] = {"iccid","message","agentName","result"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 批量取消实名
     * @param msg
     * @param channel
     * @throws IOException
     */

    @RabbitHandler
    @RabbitListener(queues = "admin_CardCancelrealname_queue")
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
            bulkUtil.update(bulkMap);//消费者进入变更执行状态 执行中


            String prefix = "admin_CardCancelrealname_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ ReadName);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ ReadName, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                execution(filePath,ReadName,Pmap,User,bulkMap,SaveUrl);//执行特殊操作 查询imie
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量取消实名  消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 查询执行
     * */

    private void execution(String filePath, String ReadName, Map<String, Object> pmap, Map<String, Object> User,Map<String,Object> bulkMap,String SaveUrl) {
        boolean isFailBool = false;//是否有失败的错误数据

        //1.读取 上传文件
        String path = filePath  + ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = {"iccid"};
        List<Map<String, Object>> list = excelConfig.getExcelListMap(path,columns);
        Map<String, String> Dept = (Map<String, String>)User.get("dept");
        String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";

        String newName = UUID.randomUUID().toString().replace("-","")+"_repeat";//对应 执行任务导出类别 CSV 前缀


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
            bulkUtil.Dadd(bulkDtailsMap);


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
                        bulkUtil.DupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","iccid卡号未找到！操作取消！");
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
                                    bulkUtil.DupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","未划分通道！操作取消！");
                                }
                            }
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
                                        bulkUtil.DupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","通道状态 为停用或已删除！操作取消！");
                                    }
                                }
                                map.put("card_arrs", list);//更新 list
                                /**
                                 *  这下面是做  批量取消实名的
                                 * */
                                /*List<Map<String, Object>> SuccessArr = new ArrayList<>();
                                List<Map<String, Object>> failArr = new ArrayList<>();*/
                                List<Map<String, Object>> findRouteArr = yzCardMapper.findRouteArr(map);//查询需要轮询的卡
                                for (int i = 0; i <findRouteArr.size() ; i++) {
                                    Map<String, Object> Obj = new HashMap<>();
                                    String iccid = findRouteArr.get(i).get("iccid").toString();
                                    Obj.put("iccid", iccid);

                                    Map<String, Object> Rmap = internalApiRequest.realNameRemove(Obj, findRouteArr.get(i));
                                    String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                                    String RMessage = Rmap.get("Message")!= null && Rmap.get("Message").toString().length() > 0?Rmap.get("Message").toString():"";
                                    String end_time = "";
                                    String state_id = "2";
                                    if (code.equals("200")) {
                                        //获取 Message
                                        RMessage = RMessage.length()>0?RMessage:"下发指令成功";
                                        Obj.put("message", RMessage);
                                        //SuccessArr.add(Obj);
                                        log.info(">>API - 下发指令成功 statusCode = 0 :{} | {}<<", iccid, Rmap);
                                        state_id = "1";
                                    } else {
                                        RMessage = RMessage.length()>0?RMessage:"下发指令失败";
                                        Obj.put("message", RMessage);
                                        //failArr.add(Obj);
                                        log.error(">>API - 下发指令失败 statusCode = 0 :{} | {}<<", iccid, Rmap);
                                        state_id = "2";
                                    }
                                    end_time = VeDate.getStringDate();
                                    bulkUtil.Dupdate(b_id,iccid,end_time,state_id,RMessage);
                                }
                                /*if(SuccessArr.size()>0){
                                    Map<String, Object> defOutcolumns = new HashMap<>();
                                    defOutcolumns.put("agentName",create_by);
                                    defOutcolumns.put("result","执行成功");
                                    writeCSV.OutCSVObj(SuccessArr, newName,Outcolumns, keys,defOutcolumns,OutSize);
                                }
                                if(failArr.size()>0){

                                }*/
                            }else{
                                bulkUtil.DupdateArr(b_id,list,VeDate.getStringDate(),"2","通道状态 为停用或已删除 取消操作");
                            }
                        }else{
                            bulkUtil.DupdateArr(b_id,list,VeDate.getStringDate(),"2","未划分通道 取消操作");
                        }
                    }
                }catch (DuplicateKeyException e){
                    log.error(">> cardSet-消费者- 上传excel异常 [插入数据 DuplicateKeyException ] :{}<<", e.getMessage().toString());
                }
            }
        }else{
            log.error( "admin_importSetCardInfo_queue-消费者 上传表格无数据！无需执行");
        }
        bulkMap.put("state_id","1");//状态  完成 1
        bulkMap.put("end_time", VeDate.getStringDate());//赋值 结束时间
        if(isFailBool){
            SaveUrl += ",/getcsv/"+newName+".csv";
            bulkMap.put("url", SaveUrl);//url 存储上传文件 原始地址
        }
        bulkUtil.update(bulkMap);//消费者进入变更执行状态 执行中
    }

}

























