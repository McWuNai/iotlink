package com.yunze.task.yunze.sys;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 清理文件 释放系统硬盘
 */
@Slf4j
@Component
public class CleanUpExecutionTaskFlieTaskMQ {

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;


    /**
     *  清理执行任务 生成文件 释放系统硬盘
     * */
    @RabbitHandler
    @RabbitListener(queues = "admin_cleanExecutionTaskFlie_queue")
    public void ExecutionTaskFlie(String msg) {

        //获取系统仅保留多少天的执行日志文件数据

        Map<String, Object> fMap = new HashMap<>();
        fMap.put("config_key","ExecutionTask.RetentionDays");
        String  day = yzWxByProductAgentMapper.findConfig(fMap);
        String  staTime = VeDate.getNextDay(VeDate.getStringDateShort(),"-"+day);

        Map<String, Object> findMap = new HashMap<>();
        findMap.put("staTime",staTime);
        List<Map<String, Object>> eTaskArr = yzExecutionTaskMapper.findUrlArr(findMap);
        if(eTaskArr!=null && eTaskArr.size()>0){
            try {
                File file2 = new File("");
                String filePath = file2.getCanonicalPath();
                List<String> idArr = new ArrayList<>();
                for (int i = 0; i < eTaskArr.size(); i++) {
                    Map<String, Object> oMap = eTaskArr.get(i);
                    try {
                        if (oMap.get("url")!=null && oMap.get("url").toString().length()>0){
                            List<String> urlArr = new ArrayList<>();
                            String url =  oMap.get("url").toString();
                            if(url.indexOf(",")!=-1){
                                String sArr[] = url.split(",");
                                for (int j = 0; j < sArr.length; j++) {
                                    urlArr.add(sArr[j]);
                                }
                            }else{
                                urlArr.add(url);
                            }
                            if(urlArr.size()>0){
                                for (int j = 0; j < urlArr.size(); j++) {
                                    String path = urlArr.get(j);
                                    String readUrl = new String(filePath);

                                    //切割出下载的地址请求头
                                    String Prefix = path.split("/")[1];
                                    if(Prefix.equals("getOriginal")){

                                    }else{
                                        readUrl += "/mnt/yunze/download/csv/";
                                    }
                                    path = path.substring(Prefix.length() + 2, path.length());
                                    path = readUrl + path;
                                    try {
                                        deleteFile(new File(path));
                                        idArr.add(oMap.get("id").toString());
                                    }catch (Exception e){
                                        log.error("deleteFile  {} 异常 {}",path,e.getCause().toString());
                                    }
                                    try {
                                        String sArr[] = path.split("\\.");
                                        path =  sArr[0]+".xls";
                                        deleteFile(new File(path));
                                        idArr.add(oMap.get("id").toString());
                                    }catch (Exception e){
                                        log.error("deleteFile  {} 异常 {}",path,e.getCause().toString());
                                    }

                                }
                            }else{
                                idArr.add(oMap.get("id").toString());
                            }
                        }
                    }catch (Exception e){
                        log.error("for  {} 异常 {}",JSON.toJSONString(oMap),e.getCause());
                    }
                }

                //删除数据库数据
                if(idArr.size()>0){
                    Map<String, Object> dMap = new HashMap<>();
                    dMap.put("idArr",idArr);
                    int delCount = yzExecutionTaskMapper.delIdArr(dMap);
                    System.out.println("yzExecutionTaskMapper.delIdArr = "+delCount);
                    log.info("yzExecutionTaskMapper.delIdArr =  {} delCount {}",delCount,JSON.toJSONString(dMap));
                }
            }catch (Exception e){
                log.error("ExecutionTaskFlie 异常 {}",e.getMessage());
            }
        }
    }


    public static void deleteFile(File file){
        if (!file.exists()){  //判断当前文件file对象指向的路径是否存在进入if表示文件或目录不存在
            return;
        }
        if (file.isFile()){  //判断单签File是否是一个文件，如果是文件删除， 否则不进入File表示File是一个文件夹需要递归
            file.delete();
            return;
        }
        File files[] = file.listFiles(); //获取当前文件中所有的子文件或子目录
        for (File f :
                files) {  //迭代当前File目录下所有的子文件或子文件夹
            deleteFile(f);  //根据当前目录中的一个子文件或子目录使用递归删除
        }
        file.delete();  //删除当前的空文件夹
    }

}
