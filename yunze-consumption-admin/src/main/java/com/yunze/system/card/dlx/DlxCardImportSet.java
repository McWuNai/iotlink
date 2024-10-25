package com.yunze.system.card.dlx;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.ExcelConfig;
import com.yunze.common.utils.yunze.WriteCSV;
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
 * 连接设置 【资费组设置、通道设置 、超量停机阈值设定】 消费 者
 */
@Slf4j
@Component
public class DlxCardImportSet {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;

    private String Outcolumns[] = {"id","set_dept_id","set_user_id","执行时间","执行人"};
    private String keys[] = {"id","set_dept_id","set_user_id","time","agentName"};
    private int OutSize = 50;//每 50条数据输出一次

    /**
     * 连接设置
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_CardImportSet_queue")
    public void CardSet(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();//项目根目录
            String ReadName = map.get("ReadName").toString();//上传新文件名
            Map<String,Object> Pmap =  ( Map<String,Object>)map.get("map");//参数
            Map<String,Object> User =  ( Map<String,Object>)Pmap.get("User");//登录用户信息

            String prefix = "dlx_admin_CardImportSet_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ ReadName);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ ReadName, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                execution(filePath,ReadName,Pmap,User);//执行连接设置
            }
        } catch (Exception e) {
            log.error(">>错误 - 连接设置 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 设置执行
     */
    public void execution(String filePath,String ReadName,Map<String,Object> Pmap,Map<String,Object> User){
        String Message = "";
        //1.读取 上传文件
        String path = filePath + "/upload/importSet/" + ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = {"iccid"};



        List<Map<String, Object>> list = excelConfig.getExcelListMap(path,columns);
        Map<String, String> Dept = (Map<String, String>)User.get("dept");
        String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";
        String Outcolumns[] = {"iccid","操作描述","执行结果","执行人","执行时间"};
        String keys[] = {"iccid","description","result","agentName","OptionTime"};

        String newName = UUID.randomUUID().toString().replace("-","")+"_CardImportSet";
        String DistributeBackupName = UUID.randomUUID().toString().replace("-","")+"_CardImportSetBackup";
        String task_name ="设备管理 [连接设置] ";
        String SaveUrl = "/getcsv/"+newName+".csv";
        SaveUrl = "/getcsv/"+newName+".csv";
        SaveUrl += ",/getcsv/"+DistributeBackupName+".csv";
        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth",create_by);
        task_map.put("task_name",task_name);
        task_map.put("url",SaveUrl);
        task_map.put("agent_id",User.get("deptId") );
        task_map.put("type", "6");
        
        if(list!=null && list.size()>0) {
            //筛选出  iccid 卡号 重复项
            Map<String, Object> getNotRepeatingMap =  Different.getNotRepeating(list,"iccid");//获取 筛选不重复的某列值 和 重复的
            list = (List<Map<String, Object>>) getNotRepeatingMap.get("Rlist");//更新 设置数据
            List<Map<String, Object>> Repeatlist = (List<Map<String, Object>>) getNotRepeatingMap.get("Repeatlist");
            if(Repeatlist.size()>0){
                writeCSV.OutCSVObj(Repeatlist,newName,"ICCID重复设置失败！同一ICCID同批次，无需多次设置！",create_by,"设置失败",Outcolumns,keys,OutSize);
            }
            //查询数据库中 匹对iccid 是否存在
            HashMap<String, Object> map = new HashMap<>();
            //添加修改数据
            map.put("package_id",Pmap.get("package_id"));
            map.put("channel_id",Pmap.get("channel_id"));
            map.put("Is_remind_ratio",Pmap.get("Is_remind_ratio"));
            map.put("remind_ratio",Pmap.get("remind_ratio"));

            map.put("card_arrs",list);
            map.put("type","3");
            List<String>  iccidarr = yzCardMapper.isExistence(map);
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表
            if (iccidarr != null && iccidarr.size() > 0) {
                //1.判断 设置卡号是否都在库里
                    //库中查询出卡号与上传卡号数量 不一致 说明有卡号不在数据库中
                    if (!(iccidarr.size() == list.size())) {
                        //上传数据>数据库查询 赛选出
                        List<String> list1 = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            list1.add(list.get(i).get("iccid").toString());
                        }
                        // 获取 数组去重数据 和 重复值
                        Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(list, iccidarr, "iccid");//获取 筛选不重复的某列值 和 重复的
                        list = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 设置数据
                        List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 设置数据
                        //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                        if (Out_list_Different.size() > 0) {
                            writeCSV.OutCSVObj(Out_list_Different, newName, "iccid 不在数据库中！请核对iccid卡号！", create_by, "设置失败", Outcolumns, keys,OutSize);
                        }
                    }
                    map.put("card_arrs", list);//更新 list
                    try {
                        if (list.size() > 0) {
                            //备份设置前属性
                            try {
                                List<Map<String,Object>> Bklist =  yzCardMapper.SetBackupAssociate(map);
                                if(Bklist!=null && Bklist.size()>0){
                                    String BK_keys[] = {"id","iccid","remind_ratio","channel_id","package_id","OptionTime"};
                                    writeCSV.OutCSVObj(Bklist, DistributeBackupName, BK_keys, BK_keys,OutSize);
                                }
                            }catch (Exception e){
                                log.error(">> cardSet-消费者- 备份 异常  :{}<<", e.getMessage());
                            }
                            int sInt = yzCardMapper.SetCard(map);
                            if (sInt > 0) {
                                writeCSV.OutCSVObj(list, newName, "批量 设置 成功 数据 [" + sInt + "] 条", create_by, "设置成功", Outcolumns, keys,OutSize);
                                yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                            }
                        }
                    } catch (DuplicateKeyException e) {
                        String[] solit = e.getCause().toString().split("'");
                        writeCSV.OutCSVObj(list, newName, e.getCause().toString(), create_by, "设置失败", Outcolumns, keys,OutSize);
                        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                        log.error(">> cardSet-消费者- 上传excel异常 [插入数据 DuplicateKeyException ] :{}<<", e.getMessage().toString());
                    } catch (Exception e) {
                        writeCSV.OutCSVObj(list, newName, e.getCause().toString(), create_by, "设置失败", Outcolumns, keys,OutSize);
                        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                        log.error(">>cardSet-消费者- 批量设置消费者:{}<<", e.getMessage());
                    }

            } else {
                log.info("上传ICCID卡号不在数据库中！请核对后重试！");
                writeCSV.OutCSVObj(list, newName, "设置iccid卡号平台中未找到！设置取消！", create_by, "设置失败", Outcolumns, keys,OutSize);
            }
        }else{
            log.error( "admin-消费者 上传表格无数据！无需执行设置");
        }
    }










}
