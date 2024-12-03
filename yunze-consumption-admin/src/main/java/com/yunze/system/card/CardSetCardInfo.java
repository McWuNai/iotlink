package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
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
 * 特殊操作 变更卡分组、备注  消费 者
 */
@Slf4j
@Component
public class CardSetCardInfo {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;


    private String Outcolumns[] = {"iccid","分组","发货日期","备注","执行描述","执行人","执行结果"};
    private String keys[] = {"iccid","customize_grouping","deliver_date","remarks","message","agentName","result"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 特殊操作 变更卡分组、备注
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_importSetCardInfo_queue")
    public void SetCardInfo(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();//项目根目录
            String ReadName = map.get("ReadName").toString();//上传新文件名
            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
            Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息

            String prefix = "admin_importSetCardInfo_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ ReadName);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ ReadName, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                execution(filePath,ReadName,Pmap,User);//执行特殊操作 变更卡分组、备注
            }
        } catch (Exception e) {
            log.error(">>错误 - 特殊操作 变更卡分组、备注 消费者:{}<<", e.toString());
        }
    }

    /**
     * 变更 执行
     */
    public void execution(String filePath,String ReadName,Map<String,Object> Pmap,Map<String,Object> User){
        String Message = "";
        //1.读取 上传文件
        String path = filePath  + ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = {"iccid","customize_grouping","remarks","deliver_date"};
        List<Map<String, Object>> list = excelConfig.getExcelListMap(path,columns);
        Map<String, String> Dept = (Map<String, String>)User.get("dept");
        String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";

        String newName = UUID.randomUUID().toString().replace("-","")+"_CardSetCardInfo";
        String UpdBackupName = UUID.randomUUID().toString().replace("-","")+"_CardSetCardInfoBackup";//设置分组备注前信息备份名称
        String deptId  = User.get("deptId").toString();
        String task_name ="特殊操作 [特殊操作 变更卡分组、备注] ";
        String SaveUrl = "/getcsv/"+newName+".csv";
        SaveUrl += ",/getcsv/"+UpdBackupName+".csv";

        Map<String, Object> task_map = new HashMap<>();
        task_map.put("auth",create_by);
        task_map.put("task_name",task_name);
        task_map.put("url",SaveUrl);
        task_map.put("agent_id",User.get("deptId") );
        task_map.put("type", "8");
        
        if(list!=null && list.size()>0) {
            //筛选出  iccid 卡号 重复项
            Map<String, Object> getNotRepeatingMap =  Different.getNotRepeating(list,"iccid");//获取 筛选不重复的某列值 和 重复的
            list = (List<Map<String, Object>>) getNotRepeatingMap.get("Rlist");//更新 查询数据
            List<Map<String, Object>> Repeatlist = (List<Map<String, Object>>) getNotRepeatingMap.get("Repeatlist");
            if(Repeatlist.size()>0){
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("message","ICCID重复操作失败！同一ICCID同批次，无需多次修改！");
                defOutcolumns.put("agentName",create_by);
                defOutcolumns.put("result","操作失败");
                writeCSV.OutCSVObj(Repeatlist, newName,Outcolumns, keys,defOutcolumns,OutSize);
            }
            //查询数据库中 匹对iccid 是否存在
            HashMap<String, Object> map = new HashMap<>();
            //添加修改数据
            map.put("card_arrs",list);
            map.put("type","3");
            List<String>  iccidarr = yzCardMapper.isExistence(map);
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表
            if (iccidarr != null && iccidarr.size() > 0) {
                //1.判断 查询卡号是否都在库里
                    //库中查询出卡号与上传卡号数量 不一致 说明有卡号不在数据库中
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
                            Map<String, Object> defOutcolumns = new HashMap<>();
                            defOutcolumns.put("message","iccid 不在数据库中！请核对iccid卡号！");
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
                               //1.备份 修改前数据
                               List<Map<String, Object>> BakUpArr =  yzCardMapper.findGroupingAndRemarks(map);
                               if(BakUpArr!=null && BakUpArr.size()>0){
                                   Map<String, Object> defOutcolumns = new HashMap<>();
                                   defOutcolumns.put("message","操作前数据备份 操作时间："+ VeDate.getStringDate());
                                   defOutcolumns.put("agentName",create_by);
                                   defOutcolumns.put("result","操作成功");
                                   writeCSV.OutCSVObj(BakUpArr, UpdBackupName,Outcolumns, keys,defOutcolumns,OutSize);
                               }
                                int updCount = 0;
                                try {
                                    updCount =  yzCardMapper.updGroupingAndRemarkArrs((List<Map<String, Object>>) map.get("card_arrs"));//变更 分组、备注 队列
                                    Map<String, Object> defOutcolumns = new HashMap<>();
                                    defOutcolumns.put("message","操作成功 修改数量 "+updCount);
                                    defOutcolumns.put("agentName",create_by);
                                    defOutcolumns.put("result","操作成功");
                                    writeCSV.OutCSVObj(list, newName,Outcolumns, keys,defOutcolumns,OutSize);
                                }catch (DuplicateKeyException e){
                                    log.info(">>错误 - yzCardMapper.updGroupingAndRemarks :{} | {}<<", e.getMessage());
                                    String Msg = e.getMessage();
                                    Msg = Msg.length()>100?Msg.substring(0,100):Msg;
                                    Map<String, Object> defOutcolumns = new HashMap<>();
                                    defOutcolumns.put("message",Msg);
                                    defOutcolumns.put("agentName",create_by);
                                    defOutcolumns.put("result","操作失败");
                                    writeCSV.OutCSVObj(list, newName,Outcolumns, keys,defOutcolumns,OutSize);
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
                log.info("上传ICCID卡号不在数据库中！请核对后重试！");
                Map<String, Object> defOutcolumns = new HashMap<>();
                defOutcolumns.put("message","操作iccid卡号平台中未找到！查询取消！");
                defOutcolumns.put("agentName",create_by);
                defOutcolumns.put("result","操作失败");
                writeCSV.OutCSVObj(list, newName,Outcolumns, keys,defOutcolumns,OutSize);
            }
            yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        }else{
            log.error( "admin_importSetCardInfo_queue-消费者 上传表格无数据！无需执行");
        }


    }









}
