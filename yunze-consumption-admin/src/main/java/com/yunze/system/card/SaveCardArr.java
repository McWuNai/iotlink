package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.*;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardBriefMapper;
import com.yunze.common.mapper.yunze.YzCardInfoExpandMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
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
 * 批量导卡 消费 者
 */
@Slf4j
@Component
public class SaveCardArr {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardInfoExpandMapper yzCardInfoExpandMapper;
    @Resource
    private YzCardBriefMapper yzCardBriefMapper;


    /**
     * 批量导入卡
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_saveCard_queue")
    public void Cardsave(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String filePath = map.get("filePath").toString();//项目根目录
            String ReadName = map.get("ReadName").toString();//上传新文件名
            String newName = map.get("newName").toString();//输出文件名
            Map<String,Object> User =  ( Map<String,Object>)map.get("User");//登录用户信息

            Map<String, Object> pTaskMap = null;
            try {
                pTaskMap =  map.get("pTaskMap")!=null? ( Map<String,Object>)map.get("pTaskMap"):null;
            }catch (Exception e){
                System.out.println("pTaskMap 获取异常 "+e.getMessage());
            }
            String prefix = "admin_saveCard_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ ReadName);
            if(isExecute==null){
                //System.out.println("SUCCESS");
                redisCache.setCacheObject(prefix+":"+ ReadName, msg, 30, TimeUnit.SECONDS);//30 秒缓存 避免 重复消费
                ImportCar(filePath,ReadName,User,newName,pTaskMap);//执行导入
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量导入卡 消费者:{}<<", e.getMessage().toString());
        }
    }


    /**
     * 导卡执行
     * @param filePath
     * @param ReadName
     * @param User
     * @param newName
     */
    public void ImportCar(String filePath, String ReadName,Map<String,Object> User,String newName,Map<String, Object> pTaskMap){
        String path = filePath +  ReadName;
        ExcelConfig excelConfig = new ExcelConfig();
        String columns[] = {"msisdn","iccid","imsi","open_date","activate_date","agent_id","channel_id","is_pool","batch_date","remarks","status_id","package_id","imei","type","network_type","is_sms","sms_number","gprs","user_id","test_period_last_time","silent_period_last_time"};
        String maxVid  = yzCardMapper.findMaxVid();
        maxVid = maxVid!=null?maxVid:"16800000000";
        Long maxVidInt = Long.parseLong(maxVid);
        List<Map<String, String>> list = excelConfig.getExcelListMap(path,columns,maxVidInt);
        Map<String, String> Dept = (Map<String, String>)User.get("dept");

        String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";

        String Outcolumns[] = {"iccid","操作描述","执行时间","执行结果","执行人"};
        String keys[] = {"iccid","description","time","result","agentName"};

        if(list!=null && list.size()>0){
            //筛选出  vid msisdn iccid imsi 的 卡号 重复项
            HashMap<String, Object> map = new HashMap<>();
            map.put("card_arrs",list);
            map.put("type","3");
            List<String>  iccidarr = yzCardMapper.isExistence(map);

            String SaveUrl = "/getcsv/"+newName+".csv";
            String task_name ="连接管理上传 [导入] ";
            Map<String, Object> task_map = new HashMap<String, Object>();


            if(pTaskMap!=null){
                task_map = pTaskMap;
            }else{
                task_map.put("auth",create_by);
                task_map.put("task_name",task_name);
                task_map.put("url",SaveUrl);
                task_map.put("agent_id", Dept.get("deptId"));
                task_map.put("type","3");
                yzExecutionTaskMapper.add(task_map);//添加执行 任务表
            }

            //1.判断上传数据与数据库现有数据做对比 大于 0 证明有 以存在数据
            if(iccidarr.size()>0){
                //上传数据>数据库查询 赛选出
                List<String> list1 = new ArrayList<>();
                for (int i = 0; i <list.size() ; i++) {
                    list1.add(list.get(i).get("iccid"));
                }
                //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                List<Map<String, String>> Out_list_Different = Different.getIdentical(list1,iccidarr,"iccid");
                if(Out_list_Different.size()>0){
                    writeCSV.OutCSV(Out_list_Different,newName,"ICCID重复导入失败！",create_by,"导入失败",Outcolumns,keys);
                }
                list = Different.delIdentical(list,iccidarr,"iccid");//删除相同的数据 进行批量上传
            }
            map.put("card_arrs",list);//更新 list
            map.put("create_by",create_by);
            try {
                if(list.size()>0){
                    int  sInt = 0;
                    //导入批量处理 2k 条
                    List<Map<String, String>> listnew = new ArrayList<>();
                    Map<Integer, List<Map<String, String>>> lMap=new HashMap<>();
                    int MaxAdd = 2000;
                    for(int i=0;i<list.size();i++) {
                        if(i==0 ||i%MaxAdd==0) {
                            listnew=new ArrayList<>();
                            listnew.add(list.get(i));
                            lMap.put(i+1, listnew);
                        }else {
                            //listnew=new ArrayList<String>();
                            listnew.add(list.get(i));
                        }
                    }
                    for(Integer key : lMap.keySet()){
                        List<Map<String, String>> card_arrs = lMap.get(key);
                        map.put("card_arrs",card_arrs);//更新 list
                        map.put("create_by",create_by);
                        sInt += yzCardMapper.importCard(map);
//                        Map<String,Object> Ramp = new HashMap<>();
//                        Ramp.put("cardArr",card_arrs);
//                        sInt += yzCardBriefMapper.batchCard(Ramp);

                        try {//有 测试期最后时间 或 沉默期最后时间 存入 卡列表拓展表
                            List<Map<String, String>> addExpandArr = new ArrayList<>();

                            for (int i = 0; i < card_arrs.size(); i++) {
                                Map<String, String> card = card_arrs.get(i);
                                boolean addBool = false;
                                if(card.get("test_period_last_time")!=null && card.get("test_period_last_time")!="" && card.get("test_period_last_time").length()>0){
                                    addBool = true;
                                }
                                if(card.get("silent_period_last_time")!=null && card.get("silent_period_last_time")!="" && card.get("silent_period_last_time").length()>0){
                                    addBool = true;
                                }
                                if(addBool){
                                    addExpandArr.add(card);
                                }
                            }
                            if(addExpandArr.size()>0){
                                cardExpandSyn(addExpandArr);//同步卡拓展信息表数据
                            }
                        }catch (Exception e){
                            System.out.println("卡列表拓展表存入异常 "+e.getMessage());
                        }
                    }
                    if(sInt>0){
                        writeCSV.OutCSV(list,newName,"成功导入卡列表数据 ["+sInt+"] 条",create_by,"导入成功",Outcolumns,keys);
                        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                    }
                }else{
                    yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                    log.error(">> admin-消费者- 上传数据已全部在数据库，无需上传卡信息！ :{}<<");
                }
            }catch (DuplicateKeyException e){
                System.out.println("===="+e.getCause().toString());
                String[] solit=e.getCause().toString().split("'");
                writeCSV.OutCSV(list,newName,e.getCause().toString(),create_by,"导入失败",Outcolumns,keys);
                yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                log.error(">> admin-消费者- 上传excel异常 [插入数据 DuplicateKeyException ] :{}<<", e.getMessage().toString());
            }catch (Exception e){
                writeCSV.OutCSV(list,newName,e.getCause().toString(),create_by,"导入失败",Outcolumns,keys);
                yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                log.error(">>admin-消费者- 批量导卡消费者:{}<<", e.getMessage().toString());
            }
        }else{
            log.error( "admin-消费者 上传表格无数据！无需执行导入");
        }
    }


    /**
     * 同步卡拓展信息表数据
     * @param addExpandArr
     */
    public void cardExpandSyn( List<Map<String, String>> addExpandArr){
        Map<String, Object> findExpandMap = new HashMap<>();
        findExpandMap.put("card_arrs",addExpandArr);
        //查询是否存在 拓展信息
        List<String> exExpandArr =  yzCardInfoExpandMapper.findInfoExpandArr(findExpandMap);
        List<Map<String, String>> addEArr = new ArrayList<>();
        List<Map<String, String>> updEArr = new ArrayList<>();
        Map<String, Object> addExpandMap = new HashMap<>();
        if(exExpandArr!=null && exExpandArr.size()>0){

            //匹对出以存在拓展信息 加入修改数组 否则 加入 新增数组
            for (int i = 0; i < addExpandArr.size(); i++) {
                Map<String, String> cObj = addExpandArr.get(i);
                String iccid = cObj.get("iccid");
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
        if(addEArr!=null && addEArr.size()>0){
            addExpandMap.put("card_arrs",addEArr);
            addExpanCount = yzCardInfoExpandMapper.saveInfoExpand(addExpandMap);
        }
        if(updEArr!=null && updEArr.size()>0){
            for (int i = 0; i < updEArr.size(); i++) {
                updExpanCount += yzCardInfoExpandMapper.updInfoExpand(updEArr.get(i));
            }
        }
        System.out.println("卡信息拓展表需 同步 ："+addExpandArr.size()+" 实际新增 ："+addExpanCount+" 实际修改 "+ updExpanCount);
    }





    /**
     * 获取 运营商类型 的通道id
     * @param map
     * @return
     */
    private Map<String,Object> getChannelIdArr (Map<String,Object> map){
        //判断是否选择 运营商类型
        Object cd_operator_type=map.get("cd_operator_type");
        if(cd_operator_type!=null){
            String Str_cd_operator_type = cd_operator_type.toString();
            if(Str_cd_operator_type!="" && Str_cd_operator_type.length()>0){
                List<Map<String, Object>> smap = yzCardRouteMapper.find_simple(map);
                //添加 【符合 运营类型】 的 通道 查询条件
                List<String> channel_id = new ArrayList<String>();
                if(smap!=null && smap.size()>0){
                    for (int i = 0; i < smap.size() ; i++) {
                        channel_id.add(smap.get(i).get("cd_id").toString());
                    }
                }
                //未找到相匹配的通道时 将 通道id传参 -1 使查询不到相对应数据
                if(channel_id.size()==0){
                    channel_id.add("-1");
                }
                map.put("channel_id",channel_id);
            }else{
                Object c_id = map.get("channel_id");
                if(c_id!=null ){
                    String Str_c_id = c_id.toString();
                    if(Str_c_id!="" && Str_c_id.length()>0) {
                        List<String> channel_id = new ArrayList<String>();
                        channel_id.add(c_id.toString());
                        map.put("channel_id",channel_id);
                    }
                }
            }
        }else{
            Object c_id = map.get("channel_id");
            if(c_id!=null ){
                String Str_c_id = c_id.toString();
                if(Str_c_id!="" && Str_c_id.length()>0) {
                    List<String> channel_id = new ArrayList<String>();
                    channel_id.add(c_id.toString());
                    map.put("channel_id",channel_id);
                }
            }
        }
        return  map;
    }





}
