package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 划卡 消费 者
 */
@Slf4j
@Component
public class DistributeCard {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;



    /**
     * 卡划分
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_DistributeCard_queue")
    public void DbCard(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String set_dept_name = map.get("set_dept_name").toString();//上传新文件名
            List<String> dividIdArr = (List<String>) map.get("dividIdArr");//输出文件名

            String prefix = "admin_DistributeCard_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ set_dept_name);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ set_dept_name, msg, 10, TimeUnit.SECONDS);//10 秒缓存 避免 重复消费
                execution(map,dividIdArr);//执行卡划分
            }
        } catch (Exception e) {
            log.error(">>错误 - 卡划分 消费者:{}<<", e.getMessage().toString());
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_DistributeCardOne_queue")
    public void DbCardOne(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String set_dept_name = map.get("set_dept_name").toString();//上传新文件名
            String prefix = "admin_DistributeCardOne_queue";

            yzCardMapper.dividCardOne(map);

        } catch (Exception e) {
            log.error(">>错误 - 卡划分 消费者:{}<<", e.getMessage().toString());
        }
    }

    /**
     * 划卡执行
     * @param map
     */
    public void execution(Map<String, Object> map,List<String> dividIdArr){
        String Message = "";
        if(dividIdArr!=null && dividIdArr.size()>0){

           //1.备份现有卡所属信息
            String set_dept_name = map.get("set_dept_name").toString();
            String set_user_name = map.get("set_user_name").toString();
            String  create_by = " [ "+set_dept_name+" ] - "+" [ "+set_user_name+" ] ";
            String newName = UUID.randomUUID().toString().replace("-","")+"_CardDistribute";
            String DistributeBackupName = UUID.randomUUID().toString().replace("-","")+"_CardDistributeBackup";
            String  agent_id = map.get("TaskAgent_id").toString();

            String task_name ="物联卡管理 [划卡] ";
            String SaveUrl = "/getcsv/"+newName+".csv";
            SaveUrl += ",/getcsv/"+DistributeBackupName+".csv";
            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "2");

            yzExecutionTaskMapper.add(task_map);//添加执行 任务表
            //备份 :太卡先注释
            //List<Map<String,Object>> list =  yzCardMapper.BackupAssociate();
            //OutCSV(list,DistributeBackupName,create_by);

            //2.开始划卡
            Map<String, Object> Pmap = new HashMap<>();
            Pmap.put("set_dept_id",map.get("set_dept_id"));
            Pmap.put("set_user_id",map.get("set_user_id"));
            Pmap.put("idArr",dividIdArr);

            int updCount = yzCardMapper.dividCard(Pmap);
            if(updCount>0){
                Message = "当前筛选条件下需要划分的数据 [ "+dividIdArr.size()+" ] 条，成功划分 [ "+updCount+" ] 条，至 [ "+set_dept_name+" ] [ "+set_user_name+" ] ！";
                log.info(Message);
            }else{
                Message = "当前筛选条件下需要划分的数据 [ "+dividIdArr.size()+" ] 条 至 [ "+set_dept_name+" ] [ "+set_user_name+" ] 划分失败 ！";
                log.error(Message);
            }
            OutCSVUpd(dividIdArr,set_dept_name,set_user_name,newName,create_by,Message);
            yzExecutionTaskMapper.set_end_time(task_map);
        }else{
            log.info("当前筛选条件下未找到需要划分的数据！请核对后重试！");
        }
    }



    /**
     * 输出
     * @param list 输出数据
     * @param fileName_flow 文件名
     * @param agentName 执行人
     */
    public void OutCSV(List<Map<String, Object>> list , String fileName_flow, String agentName){

        List<Map<String, Object>> out_list = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> out_map= new HashMap<String, Object>();

            Map<String,Object> cardIndex=list.get(i);
            String iccid =cardIndex.get("iccid").toString();
            String id =cardIndex.get("id").toString();
            String dept_id =cardIndex.get("dept_id").toString();
            String user_id =cardIndex.get("user_id").toString();
            //String tableName ="zy_"+cardType;

            String Outcolumns[] = {"id","iccid","dept_id","user_id","执行时间","执行人"};
            String keys[] = {"id","iccid","dept_id","user_id","time","agentName"};
            String Time= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out_map.put("id",id);
            out_map.put("iccid",iccid);
            out_map.put("dept_id",dept_id);
            out_map.put("user_id",user_id);
            out_map.put("time",Time);
            out_map.put("agentName",agentName);

            out_list.add(out_map);
            if ((i+1)%50==0 || (i+1)==list.size()){
                //执行导出
                if(out_list.size()>0){
                    writeCSV.Write(fileName_flow,out_list,Outcolumns,null,keys);
                    out_list = new ArrayList<>();
                }
            }
        }

    }





    /**
     * 输出 划卡数据
     * @param list 输出数据
     * @param fileName_flow 文件名
     * @param agentName 执行人
     */
    public void OutCSVUpd(List<String> list , String set_dept_id, String set_user_id, String fileName_flow, String agentName,String Msg){

        List<Map<String, Object>> out_list = new ArrayList<>();
        List<Map<String, Object>> outLast_list = new ArrayList<>();

        String Outcolumns[] = {"id","set_dept_id","set_user_id","执行时间","执行人"};
        String keys[] = {"id","set_dept_id","set_user_id","time","agentName"};
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> out_map= new HashMap<String, Object>();
            String id =list.get(i);

            String Time= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out_map.put("id",id);
            out_map.put("set_dept_id",set_dept_id);
            out_map.put("set_user_id",set_user_id);
            out_map.put("time",Time);
            out_map.put("agentName",agentName);

            out_list.add(out_map);
            if ((i+1)%50==0 || (i+1)==list.size()){
                //执行导出
                if(out_list.size()>0){
                    writeCSV.Write(fileName_flow,out_list,Outcolumns,null,keys);
                    out_list = new ArrayList<>();
                }
            }
        }

        Map<String, Object> out_map= new HashMap<String, Object>();
        out_map.put("id",Msg);
        out_map.put("set_dept_id",set_dept_id);
        out_map.put("set_user_id",set_user_id);
        out_map.put("time",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        out_map.put("agentName",agentName);

        outLast_list.add(out_map);
        writeCSV.Write(fileName_flow,outLast_list,Outcolumns,null,keys);

    }






}
