package com.yunze.system.index;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.yunze.common.annotation.DataScope;
import com.yunze.common.config.MyDictionary;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.*;
import com.yunze.common.mapper.yunze.YrootlowHisMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.mapper.yunze.YzUserMapper;
import com.yunze.common.mapper.yunze.sysgl.YzIndexpageMapper;
import com.yunze.common.mapper.yunze.xsgl.Contract.YzXsglContractMapper;
import com.yunze.common.mapper.yunze.xsgl.customer.YzXsglCustomerMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;

import com.yunze.common.utils.Arith;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.ListMapUtils;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 首页数据生成  消费者
 * */
@Slf4j
@Component
public class IndexDataGenerate {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzIndexpageMapper yzIndexpageMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzUserMapper yzUserMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private MyDictionary myDictionary;
    @Resource
    private YrootlowHisMapper yrootlowHisMapper;
    @Resource
    private YzXsglCustomerMapper yzXsglCustomerMapper;
    @Resource
    private YzXsglContractMapper yzXsglContractMapper;
    @Resource
    private YzOrderMapper yzOrderMapper;
//    @Resource
//    private TdbCardFlowHisMapper tdbCardFlowHisMapper;


    @RabbitHandler
    @RabbitListener(queues = "admin_IndexDataGenerate_queue")
    private void Listener(String msg, Channel channel){
        ListeneCommonr(msg,channel);
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_IndexDataGenerate_queue")
    private void Listener1(String msg, Channel channel){
        ListeneCommonr(msg,channel);
    }

    @RabbitHandler
    @RabbitListener(queues = "admin_IndexDataGenerate_queue")
    private void Listener2(String msg, Channel channel){
        ListeneCommonr(msg,channel);
    }

    /**
     * 监听公用
     * @param msg
     * @param channel
     */
    private void ListeneCommonr(String msg, Channel channel){
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            String dept_id = Pmap.get("dept_id").toString();
            String prefix = "admin_IndexDataGenerate_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            String recordKey = prefix+":"+ dept_id;
            Object  isExecute = redisCache.getCacheObject(recordKey);
            if(isExecute==null){
                redisCache.setCacheObject(recordKey, msg, 1*60, TimeUnit.SECONDS);// 1分钟 缓存 避免 重复消费
                try {
                    DataGenerate(Pmap,recordKey);
                }catch (Exception e){
                    log.error(">>admin_IndexDataGenerate_queue  DataGenerate 消费者:{}<<", e.getMessage());
                    redisCache.deleteObject(recordKey);//删除正在执行key
                }
            }
        } catch (Exception e) {
            log.error(">>错误 - 首页数据生成 消费者:{}<<", e.getMessage());
        }
    }






    private void DataGenerate(Map<String, Object> map,String recordKey) {

        HashMap<String, Object> requestMap = new HashMap<>();

        Integer findExistCount = null;
        //判断是否已经生成过数据
        if(map.get("id")!=null && map.get("id").toString().length()>0){
            findExistCount = Integer.parseInt(map.get("id").toString());
        }else{
            HashMap<String, Object> findExistMap = new HashMap<>();
            findExistCount = yzIndexpageMapper.findExist(findExistMap);
            System.out.println(findExistCount);
        }
        findExistCount = findExistCount!=null?findExistCount:0;


        //  findExistCount = 0; //测试必须生成数据 【正式打包需注释！！】


        HashMap<String, Object> configMap = new HashMap<>();
        configMap.put("config_key", "card.expiringSoonCount");
        String delay =  yzWxByProductAgentMapper.findConfig(configMap);// 快到期 天数


        List<Integer> agent_id = new ArrayList<>();
        String dept_id = map.get("dept_id").toString();
        String record_date = map.get("record_date").toString();
        agent_id.add(Integer.parseInt(dept_id));
        map.put("agent_id",agent_id);


        boolean internal = Different.Is_existence(agent_id, 100);
        if (!internal) {
            List<String> user_id = getUserID(map);
            map.put("user_id", user_id);
        }
        //总平台时 删除所属ID 限制
        if (internal) {
            agent_id = new ArrayList<>();
            map.put("agent_id",agent_id);
        }
        //非管理员 将 所属卡号查询出打包至
        HashMap<String, Object> iccidAgentMap = new HashMap<>();
        iccidAgentMap.putAll(map);
        if (!internal) {
            List<String> IccidArr = yzCardMapper.findBelongingCard(map);
            iccidAgentMap.put("IccidArr", IccidArr);
        }

        String  day1 = record_date.substring(0,8)+"01";// 1号


        // Sim卡总数
        Map<String,Object> Pmap_simCardCount = new HashMap<>();
        Pmap_simCardCount.putAll(map);

        requestMap.put("Pmap_simCardCount",Pmap_simCardCount);

        Integer  simCardCount = yzCardMapper.selMapCount(Pmap_simCardCount);//卡总数
        simCardCount = simCardCount!=null?simCardCount:0;

        Integer  downCount = 0;
        Integer  overdoseCount = 0;
        Integer  thresholdReachedCount = 0;
        Integer  expiringSoonCount = 0;
        Integer  simCardNewCount = 0;
        String  currentMonth = "";
        Integer  simActivity = 0;
        Integer  shipCard = 0;
        Integer  activationCardCount = 0;
        Integer  customerCount = 0;
        Integer  customerNewCount = 0;
        Integer  salesContractCount = 0;
        String lifeCycleDistribution = "";
        Double D_simActivity = 0.0;
        Double D_currentMonth = 0.0;
        Integer api_pending_order = 0;



        String salesContractDepositCount = "0.0";
        String businessVolumeFlow = "";
        String businessVolumeVoice = "";
        String businessVolumeMessage = "";
        //查询出卡总数的情况下执行
        if(simCardCount>0){
            // 已停机
            Map<String,Object> Pmap_downCount = new HashMap<>();
            Pmap_downCount.putAll(map);
            Pmap_downCount.put("status_ShowId","5");
            downCount = yzCardMapper.selMapCount(Pmap_downCount);
            downCount = downCount!=null?downCount:0;
            requestMap.put("Pmap_downCount",Pmap_downCount);

            // 超用量
            Map<String,Object> Pmap_overdoseCount = new HashMap<>();
            Pmap_overdoseCount.putAll(map);
            Pmap_overdoseCount.put("dimensionField","2");
            Pmap_overdoseCount.put("dimensionType","4");
            Pmap_overdoseCount.put("dimensionValue","0");
            overdoseCount = yzCardMapper.selMapCount(Pmap_overdoseCount);
            overdoseCount = overdoseCount!=null?overdoseCount:0;
            requestMap.put("Pmap_overdoseCount",Pmap_overdoseCount);

            // 快到期
            Map<String,Object> Pmap_expiringSoonCount = new HashMap<>();
            Pmap_expiringSoonCount.putAll(map);
            Pmap_expiringSoonCount.put("timetype","5");
            Pmap_expiringSoonCount.put("staTime",record_date);
            Pmap_expiringSoonCount.put("endTime", VeDate.getNextDay(record_date,delay));
            expiringSoonCount = yzCardMapper.selMapCount(Pmap_expiringSoonCount);
            expiringSoonCount = expiringSoonCount!=null?expiringSoonCount:0;
            requestMap.put("Pmap_expiringSoonCount",Pmap_expiringSoonCount);

            // 已达停机阈值
            Map<String,Object> Pmap_thresholdReachedCount = new HashMap<>();
            Pmap_thresholdReachedCount.putAll(map);
            Pmap_thresholdReachedCount.put("exceedsThreshold","1");
            thresholdReachedCount = yzCardMapper.selMapCount(Pmap_thresholdReachedCount);
            thresholdReachedCount = thresholdReachedCount!=null?thresholdReachedCount:0;
            requestMap.put("Pmap_thresholdReachedCount",Pmap_thresholdReachedCount);

            // Sim卡本月新增数量
            Map<String,Object> Pmap_simCardNewCount = new HashMap<>();
            Pmap_simCardNewCount.putAll(map);
            Pmap_simCardNewCount.put("timetype","6");
            Pmap_simCardNewCount.put("staTime",day1);
            Pmap_simCardNewCount.put("endTime", record_date);
            simCardNewCount = yzCardMapper.selMapCount(Pmap_simCardNewCount);
            simCardNewCount = simCardNewCount!=null?simCardNewCount:0;
            requestMap.put("Pmap_simCardNewCount",Pmap_simCardNewCount);


            if(findExistCount==0){


                // SIm卡活跃度 = (使用用量>0 数量 / 总数)*100
                Map<String,Object> Pmap_simActivity = new HashMap<>();
                Pmap_simActivity.putAll(map);
                Pmap_simActivity.put("dimensionField","1");
                Pmap_simActivity.put("dimensionType","2");
                Pmap_simActivity.put("dimensionValue","0");
                simActivity = yzCardMapper.selMapCount(Pmap_simActivity);
                simActivity = simActivity!=null?simActivity:0;

                requestMap.put("Pmap_simActivity",Pmap_simActivity);

                D_simActivity = simActivity!=0? Arith.div(Double.parseDouble(""+simActivity),simCardCount,2):0.0;
                D_simActivity = Arith.mul(D_simActivity,100);//百分比
            }



            //生命周期分布
            Map<String,Object> Pmap_status_ShowIdGroup = new HashMap<>();
            Pmap_status_ShowIdGroup.putAll(map);
            List<Map<String,Object>>  status_ShowIdGroupArr = yzCardMapper.status_ShowIdGroup(Pmap_status_ShowIdGroup);
            List<Map<String, Object>> P_series_data = new ArrayList<>();
            if(status_ShowIdGroupArr!=null){
                List<Map<String, Object>> status_ShowIdArr =  myDictionary.getDictDataArr("yunze_card_status_ShowId");
                for (int i = 0; i < status_ShowIdArr.size(); i++) {
                    Map<String, Object> obj = status_ShowIdArr.get(i); // dict_value -> 1  dict_label -> 库存
                    Map<String, Object> data = new HashMap<>();
                    String dict_value = obj.get("dict_value").toString();
                    String cardCount = "0";
                    //获取对应数据
                    for (int j = 0; j < status_ShowIdGroupArr.size(); j++) {
                        Map<String, Object> Count_obj = status_ShowIdGroupArr.get(j);
                        Object status_ShowId =  Count_obj.get("status_ShowId");
                        if(status_ShowId!=null && status_ShowId.toString().length()>0){
                            if(status_ShowId.toString().equals(dict_value)){
                                cardCount =  Count_obj.get("cardCount").toString();
                                break;
                            }
                        }
                    }
                    data.put("name",obj.get("dict_label"));
                    data.put("value",cardCount);
                    P_series_data.add(data);
                }
                requestMap.put("Pmap_status_ShowIdGroup",Pmap_status_ShowIdGroup);

                //获取无状态数据 【未更新到数据的卡号】
                Double noStatuscardCount = 0.0;//无状态 数量
                for (int j = 0; j < status_ShowIdGroupArr.size(); j++) {
                    Map<String, Object> Count_obj = status_ShowIdGroupArr.get(j);
                    Object status_ShowId =  Count_obj.get("status_ShowId");
                    if(status_ShowId!=null && status_ShowId.toString().length()>0){

                    }else{
                        Double cardCount =  Double.parseDouble(Count_obj.get("cardCount").toString());
                        noStatuscardCount = Arith.add(noStatuscardCount,cardCount);
                    }
                }

                if(noStatuscardCount>0.0){
                    for (int i = 0; i < P_series_data.size(); i++) {
                        Map<String, Object> obj = P_series_data.get(i);
                        String name = obj.get("name").toString();
                        if(name.equals("未知")){
                            Double cardCount =  Double.parseDouble(obj.get("value").toString());
                            cardCount = Arith.add(cardCount,noStatuscardCount);
                            obj.put("value",cardCount);
                            P_series_data.set(i, obj);//更新未知 数量
                            break;
                        }
                    }
                }
            }

            Map<String,Object> status_ShowId_P_series = new HashMap<>();
            status_ShowId_P_series.put("data",P_series_data);
            lifeCycleDistribution = JSON.toJSONString(status_ShowId_P_series);

            //业务使用情况

            if(findExistCount==0) {
                //-数据 businessVolumeFlow
                // 日用量折线图
                Map<String, Object> Pmap_getDaySum = new HashMap<>();
                Pmap_getDaySum.putAll(iccidAgentMap);
                Pmap_getDaySum.put("timetype", "1"); //创建时间
                Pmap_getDaySum.put("staTime", VeDate.getNextDay(record_date, "-6"));//获取七天前日期
                Pmap_getDaySum.put("endTime", record_date);
                Pmap_getDaySum.put("FindType", "day");
                Pmap_getDaySum.put("GreaterThanZero", "1"); // 当天使用总量 > 0

                List<Map<String, Object>> DaySumArr = yrootlowHisMapper.getDayMonthSum(Pmap_getDaySum);


                Map<String, Object> CardFlow_Info = new HashMap<>();
                if (DaySumArr != null && DaySumArr.size() > 0) {
                    Map<String, Object> Day_CardFlowMap = new HashMap<>();
                    ArrayList Day_CardFlowArr = new ArrayList();//数据列
                    ArrayList Day_CardFlow_xAxis = new ArrayList();//X 轴
                    for (int i = 0; i < DaySumArr.size(); i++) {
                        Map<String, Object> obj = DaySumArr.get(i);
                        String total_show_flow_now = obj.get("total_show_flow_now") != null ? obj.get("total_show_flow_now").toString() : "0";
                        Day_CardFlowArr.add(total_show_flow_now);
                        Day_CardFlow_xAxis.add(obj.get("monthDay"));
                    }
                    Day_CardFlowMap.put("Day_CardFlowArr", Day_CardFlowArr);
                    CardFlow_Info.put("Day_CardFlowMap", Day_CardFlowMap);
                    CardFlow_Info.put("Day_CardFlow_xAxis", Day_CardFlow_xAxis);
                }

                // 月 用量折线图  【这里新版本数据储存近当月和上个月的用量储存在mysql其他数据做迁移储存到TDengine,近两个月月的数据从mysql获取 其他的数据通过 已生成数据中最大创建时间一条记录的折线图里取之前统计的月数据】
                String twoMonthsAgo = "";

                String threeMonthsAgo = "";
                String FourMonthsAgo = "";
                String fiveMonthsAgo = "";
                String sixMonthsAgo = "";
                List<String> threeAndsixArr = new ArrayList<>();
                try {
                    twoMonthsAgo = VeDate.getFirstDayOfGivenMonth(VeDate.getMonthDate(record_date + " 00:00:00", -1), "yyyy-MM-dd");
                    threeMonthsAgo = VeDate.getFirstDayOfGivenMonth(VeDate.getMonthDate(record_date + " 00:00:00", -2), "yyyy-MM");
                    FourMonthsAgo = VeDate.getFirstDayOfGivenMonth(VeDate.getMonthDate(record_date + " 00:00:00", -3), "yyyy-MM");
                    fiveMonthsAgo = VeDate.getFirstDayOfGivenMonth(VeDate.getMonthDate(record_date + " 00:00:00", -4), "yyyy-MM");
                    sixMonthsAgo = VeDate.getFirstDayOfGivenMonth(VeDate.getMonthDate(record_date + " 00:00:00", -5), "yyyy-MM");
                    // 6,5,4,3 下标add 方式不要动
                    threeAndsixArr.add(sixMonthsAgo);
                    threeAndsixArr.add(fiveMonthsAgo);
                    threeAndsixArr.add(FourMonthsAgo);
                    threeAndsixArr.add(threeMonthsAgo);
                } catch (Exception e) {
                    System.out.println("获取 twoMonthsAgo 异常" + e.getMessage());
                }
                Map<String, Object> Pmap_getMonthSum = new HashMap<>();
                Pmap_getMonthSum.putAll(iccidAgentMap);
                Pmap_getMonthSum.put("timetype", "1");

                Pmap_getMonthSum.put("staTime", twoMonthsAgo);//获取 二个月前
                Pmap_getMonthSum.put("endTime", record_date);

                Pmap_getMonthSum.put("FindType", "month");
                Pmap_getMonthSum.put("GreaterThanZero", "1");
                String yyyyM = VeDate.getyyyyMM();
                List<Map<String, Object>> MonthSumArr = yrootlowHisMapper.getDayMonthSum(Pmap_getMonthSum);
                // List<Map<String, Object>> MonthSumArr = getDayMonthSum(Pmap_getMonthSum,6,record_date);

                try {
                    //0.查询当前企业最大创建时间数据 【没有时不操作，改数据会通过定时任务补偿每个企业会保证每月有一条数据生成】
                    Map<String, Object> fMap = new HashMap<>();
                    fMap.put("dept_id",dept_id);
                    String bFStr = yzIndexpageMapper.findBusinessVolumeFlow(fMap);
                    if(bFStr!=null && bFStr.length()>1){
                        Map<String, Object> bFMap = JSONObject.parseObject(bFStr);
                        if(bFMap.get("Month_CardFlowMap")!=null && bFMap.get("Month_CardFlow_xAxis")!=null){
                            Map<String, Object> Month_CardFlowMap = (Map<String, Object>) bFMap.get("Month_CardFlowMap");
                            JSONArray Month_CardFlow_xAxis = bFMap.get("Month_CardFlow_xAxis")!=null? (JSONArray) bFMap.get("Month_CardFlow_xAxis") :null;//X 轴
                            JSONArray Month_CardFlowArr = Month_CardFlowMap.get("Month_CardFlowArr")!=null?(JSONArray) Month_CardFlowMap.get("Month_CardFlowArr"):null;//X 轴
                            if(Month_CardFlow_xAxis!=null && Month_CardFlowArr!=null && Month_CardFlow_xAxis.size()>0 && Month_CardFlowArr.size()>0){
                                //对比年月 去除 之前记录的用量信息
                                List<Map<String, Object>> new_MonthSumArr = new ArrayList<>();
                                for (int i = 0; i < threeAndsixArr.size(); i++) {
                                    String yyyyMM = threeAndsixArr.get(i);
                                    for (int j = 0; j <Month_CardFlow_xAxis.size() ; j++) {
                                        String ym = Month_CardFlow_xAxis.get(j).toString();
                                        if(yyyyMM.equals(ym)){
                                            Double total_show_flow_now = Double.parseDouble(Month_CardFlowArr.get(j).toString());
                                            Map<String, Object> addMap = new HashMap<>();
                                            addMap.put("yearMonth",yyyyMM);
                                            addMap.put("total_show_flow_now",total_show_flow_now);
                                            new_MonthSumArr.add(addMap);
                                            break;
                                        }
                                    }
                                }
                                //追加前4个月的数据，拼接后重新赋值
                                new_MonthSumArr.addAll(MonthSumArr);
                                MonthSumArr = new_MonthSumArr;
                            }
                        }
                    }
                }catch (Exception e){
                    System.out.println(" 拼接月业务使用量异常 "+e.getMessage());
                }

                if (MonthSumArr != null && MonthSumArr.size() > 0) {
                    Map<String, Object> Month_CardFlowMap = new HashMap<>();
                    ArrayList Month_CardFlowArr = new ArrayList();//数据列
                    ArrayList Month_CardFlow_xAxis = new ArrayList();//X 轴
                    for (int i = 0; i < MonthSumArr.size(); i++) {
                        Map<String, Object> obj = MonthSumArr.get(i);
                        String total_show_flow_now = obj.get("total_show_flow_now") != null ? obj.get("total_show_flow_now").toString() : "0";
                        String yearMonth =  obj.get("yearMonth").toString();
                        Month_CardFlowArr.add(total_show_flow_now);
                        Month_CardFlow_xAxis.add(yearMonth);
                        try {
                            if(yyyyM.equals(yearMonth)){
                                // 本月数据用量GB
                                D_currentMonth = total_show_flow_now!=null?Double.parseDouble(total_show_flow_now):0;// GB
                            }
                        }catch (Exception e){

                        }
                    }
                    Month_CardFlowMap.put("Month_CardFlowArr", Month_CardFlowArr);
                    CardFlow_Info.put("Month_CardFlowMap", Month_CardFlowMap);
                    CardFlow_Info.put("Month_CardFlow_xAxis", Month_CardFlow_xAxis);
                }







                //Top 5 排行
                Map<String,String> cMap = new HashMap<>();
                cMap.put("total_show_flow","used");

                Map<String, Object> Pmap_Day_cardFlowList = new HashMap<>();
                Pmap_Day_cardFlowList.putAll(iccidAgentMap);
                Pmap_Day_cardFlowList.put("timetype", "1");
                Pmap_Day_cardFlowList.put("staTime", record_date);//指定当天
                Pmap_Day_cardFlowList.put("endTime", record_date);
                Pmap_Day_cardFlowList.put("GreaterThanZero", "1");
                //log.info(JSON.toJSONString(Pmap_Day_cardFlowList));
                List<Map<String, Object>> Day_cardFlowList = yrootlowHisMapper.getTop5(Pmap_Day_cardFlowList);
                //List<Map<String, Object>> Day_cardFlowList = ListMapUtils.DbAs(tdbCardFlowHisMapper.getTop5(Pmap_Day_cardFlowList),cMap);// as 别名
                //log.info(JSON.toJSONString(Day_cardFlowList));

                Map<String, Object> Pmap_Month_cardFlowList = new HashMap<>();
                Pmap_Month_cardFlowList.putAll(iccidAgentMap);
                Pmap_Month_cardFlowList.put("timetype", "1");
                Pmap_Month_cardFlowList.put("staTime", day1);//一号开始
                Pmap_Month_cardFlowList.put("endTime", record_date);
                Pmap_Month_cardFlowList.put("GreaterThanZero", "1");
                List<Map<String, Object>> Month_cardFlowList = yrootlowHisMapper.monthGetTop5(Pmap_Month_cardFlowList);
                //List<Map<String, Object>> Month_cardFlowList = monthGetTop5(tdbCardFlowHisMapper.monthGetTop5(Pmap_Month_cardFlowList),cMap);

                //获取 vid
                if (Day_cardFlowList != null && Day_cardFlowList.size() > 0) {
                    Map<String, Object> Pmap_getVid = new HashMap<>();

                    List<Map<String, Object>> UpArr = new ArrayList<>();
                    for (int i = 0; i < Day_cardFlowList.size(); i++) {
                        Map<String, Object> obj = Day_cardFlowList.get(i);
                        obj.put("cardNumber", obj.get("iccid"));
                        UpArr.add(obj);
                    }
                    Pmap_getVid.putAll(map);
                    Pmap_getVid.put("UpType", "2");
                    Pmap_getVid.put("UpArr", UpArr);//iccid arr

                    List<Map<String, Object>> VidArr = yzCardMapper.selVid(Pmap_getVid);

                    for (int i = 0; i < Day_cardFlowList.size(); i++) {
                        Map<String, Object> obj = Day_cardFlowList.get(i);
                        String iccid = obj.get("iccid").toString();
                        for (int j = 0; j < VidArr.size(); j++) {
                            Map<String, Object> vidObj = VidArr.get(j);
                            String v_iccid = vidObj.get("iccid").toString();
                            if (iccid.equals(v_iccid)) {
                                obj.put("vid", vidObj.get("vid"));
                                Day_cardFlowList.set(i, obj);//更新
                                break;
                            }
                        }
                    }
                }


                //获取 vid
                if (Month_cardFlowList != null && Month_cardFlowList.size() > 0) {
                    Map<String, Object> Pmap_getVid = new HashMap<>();

                    List<Map<String, Object>> UpArr = new ArrayList<>();
                    for (int i = 0; i < Month_cardFlowList.size(); i++) {
                        Map<String, Object> obj = Month_cardFlowList.get(i);
                        obj.put("cardNumber", obj.get("iccid"));
                        UpArr.add(obj);
                    }
                    Pmap_getVid.putAll(map);
                    Pmap_getVid.put("UpType", "2");
                    Pmap_getVid.put("UpArr", UpArr);//iccid arr

                    List<Map<String, Object>> VidArr = yzCardMapper.selVid(Pmap_getVid);

                    for (int i = 0; i < Month_cardFlowList.size(); i++) {
                        Map<String, Object> obj = Month_cardFlowList.get(i);
                        String iccid = obj.get("iccid").toString();
                        for (int j = 0; j < VidArr.size(); j++) {
                            Map<String, Object> vidObj = VidArr.get(j);
                            String v_iccid = vidObj.get("iccid").toString();
                            if (iccid.equals(v_iccid)) {
                                obj.put("vid", vidObj.get("vid"));
                                Month_cardFlowList.set(i, obj);//更新
                                break;
                            }
                        }
                    }
                }

                CardFlow_Info.put("Day_cardFlowList", Day_cardFlowList);
                CardFlow_Info.put("Month_cardFlowList", Month_cardFlowList);

                businessVolumeFlow = JSON.toJSONString(CardFlow_Info);

                //-语音 businessVolumeVoice
                Map<String, Object> CardVoice_Info = new HashMap<>();
                businessVolumeVoice = JSON.toJSONString(CardVoice_Info);

                //-短信 businessVolumeMessage
                Map<String, Object> CardMessage_Info = new HashMap<>();
                businessVolumeMessage = JSON.toJSONString(CardMessage_Info);
            }

            // 本月激活卡数
            Map<String,Object> Pmap_activationCardCount = new HashMap<>();
            Pmap_activationCardCount.putAll(map);
            Pmap_activationCardCount.put("timetype","2");
            Pmap_activationCardCount.put("staTime",day1);
            Pmap_activationCardCount.put("endTime", record_date);
            activationCardCount = yzCardMapper.selMapCount(Pmap_activationCardCount);
            activationCardCount = activationCardCount!=null?activationCardCount:0;

            requestMap.put("Pmap_activationCardCount",Pmap_activationCardCount);
            // 本月发货卡数
            Map<String,Object> Pmap_shipCard = new HashMap<>();
            Pmap_shipCard.putAll(map);
            Pmap_shipCard.put("timetype","4");
            Pmap_shipCard.put("staTime",day1);
            Pmap_shipCard.put("endTime", record_date);
            shipCard = yzCardMapper.selMapCount(Pmap_shipCard);
            shipCard = shipCard!=null?shipCard:0;

            requestMap.put("Pmap_shipCard",Pmap_shipCard);
        }

        //内部人员生成 数据
//        if (internal) {
//
//            //客户总数 系统默认获取所有
//            Map<String,Object> Pmap_selMapCount = new HashMap<>();
//            customerCount =  yzXsglCustomerMapper.selMapCount(Pmap_selMapCount);
//            customerCount = customerCount!=null?customerCount:0;
//
//
//            //本月新增客户 系统默认获取所有
//            Map<String,Object> Pmap_customerNewCount = new HashMap<>();
//            Pmap_customerNewCount.put("timetype","0");
//            Pmap_customerNewCount.put("staTime",day1);
//            Pmap_customerNewCount.put("endTime",record_date);
//            customerNewCount =  yzXsglCustomerMapper.selMapCount(Pmap_customerNewCount);
//            customerNewCount = customerNewCount!=null?customerNewCount:0;
//
//            requestMap.put("Pmap_customerNewCount",Pmap_customerNewCount);
//
//            //本月销售合同总数
//            Map<String,Object> Pmap_salesContractCount = new HashMap<>();
//            Pmap_salesContractCount.put("timetype","1");
//            Pmap_salesContractCount.put("staTime",day1);
//            Pmap_salesContractCount.put("endTime",record_date);
//            salesContractCount =  yzXsglContractMapper.selMapCount(Pmap_salesContractCount);
//            salesContractCount = salesContractCount!=null?salesContractCount:0;
//
//            requestMap.put("Pmap_salesContractCount",Pmap_salesContractCount);
//
//            //todo 本月销售合同入款金额  salesContractDepositCount 【代码同步后操作】
//            //创建查询条件时间集合
//            String endTime = VeDate.getStringDate();
//            HashMap<String, Object> pmap = new HashMap<>();
//            pmap.put("startTime",day1);
//            pmap.put("endTime",endTime);
//            Map<String, Object> contractPaymentAmountOfThisMonth = yzXsglContractMapper.getContractPaymentAmountOfThisMonth(pmap);
//            /*这个是合同金额总额?*/
//            if(contractPaymentAmountOfThisMonth!=null){
//                salesContractDepositCount = contractPaymentAmountOfThisMonth.get("sumAmount").toString();
//            }
//
//        }



        //微信订单收款 weChatOrderCollection
        Map<String,Object> Pmap_getOrderSum = new HashMap<>();
        Pmap_getOrderSum.putAll(map);
        Pmap_getOrderSum.put("timetype","1");
        Pmap_getOrderSum.put("staTime",day1);
        Pmap_getOrderSum.put("endTime",record_date);
        Pmap_getOrderSum.put("pay_type","wx");
        Pmap_getOrderSum.put("status","1");
        String weChatOrderCollection =  yzOrderMapper.getOrderSum(Pmap_getOrderSum);
        weChatOrderCollection = weChatOrderCollection!=null?weChatOrderCollection:"0";
        requestMap.put("Pmap_getOrderSum",Pmap_getOrderSum);

        //本月订单总金额
        Map<String,Object> Pmap_getOrderAmount = new HashMap<>();
        Pmap_getOrderAmount.putAll(map);
        Pmap_getOrderAmount.put("timetype","1");
        Pmap_getOrderAmount.put("staTime",day1);
        Pmap_getOrderAmount.put("endTime",record_date);
        Pmap_getOrderAmount.put("status","1");
        String orderAmount =  yzOrderMapper.getOrderSum(Pmap_getOrderAmount);
        orderAmount = orderAmount!=null?orderAmount:"0";

        requestMap.put("Pmap_getOrderAmount",Pmap_getOrderAmount);

        //待审核订单数量-平台使用API申请
        Map<String,Object> Pmap_pOrder = new HashMap<>();
        Pmap_pOrder.putAll(map);
        Pmap_pOrder.put("is_audit","0");
        Pmap_pOrder.put("staTime",day1);
        Pmap_pOrder.put("endTime",record_date);
        Pmap_getOrderAmount.put("status","1");
        api_pending_order =  yzOrderMapper.selMapCount(Pmap_pOrder);
        api_pending_order = api_pending_order!=null?api_pending_order:0;

        //系统企业总数
        Map<String,Object> Pmap_selectDeptList = new HashMap<>();
        Pmap_selectDeptList.putAll(map);
        if(!internal){
            Pmap_selectDeptList.put("dept_id",dept_id);
        }
        Integer systemCliqueCount =  selectDeptListCount(Pmap_selectDeptList);
        systemCliqueCount = systemCliqueCount!=null?systemCliqueCount:0;

        //系统用户 总数
        Map<String,Object> Pmap_systemUserCount = new HashMap<>();
        Pmap_systemUserCount.put("deptId",dept_id);
        Integer systemUserCount =  yzUserMapper.selMapCount(Pmap_systemUserCount);
        systemUserCount = systemUserCount!=null?systemUserCount:0;

        //本月登录ip 总数
        Integer logInIp = null;
        if(internal){
            Map<String,Object> Pmap_logInIp = new HashMap<>();
            Pmap_logInIp.putAll(map);
            List<Map<String, String>>  LoginCount =  yzUserMapper.selectLoginCount(Pmap_logInIp);
            if(LoginCount!=null ){
                logInIp = LoginCount.size();
            }
            requestMap.put("Pmap_logInIp",Pmap_logInIp);
        }
        logInIp = logInIp!=null?logInIp:0;



        Map<String,Object> IndexpageMap = new HashMap<>();
        IndexpageMap.put("downCount",downCount);//upd
        IndexpageMap.put("overdoseCount",overdoseCount);//upd
        IndexpageMap.put("expiringSoonCount",expiringSoonCount);//upd
        IndexpageMap.put("thresholdReachedCount",thresholdReachedCount);//upd
        IndexpageMap.put("simCardCount",simCardCount);//upd
        IndexpageMap.put("simCardNewCount",simCardNewCount);//upd
        IndexpageMap.put("currentMonth",D_currentMonth);
        IndexpageMap.put("simActivity",D_simActivity);
        IndexpageMap.put("lifeCycleDistribution",lifeCycleDistribution);//upd
        IndexpageMap.put("businessVolumeFlow",businessVolumeFlow);
        IndexpageMap.put("businessVolumeVoice",businessVolumeVoice);
        IndexpageMap.put("businessVolumeMessage",businessVolumeMessage);
        IndexpageMap.put("customerCount",customerCount);//upd
        IndexpageMap.put("customerNewCount",customerNewCount);//upd
        IndexpageMap.put("salesContractCount",salesContractCount);//upd
        IndexpageMap.put("salesContractDepositCount",salesContractDepositCount);//upd
        IndexpageMap.put("weChatOrderCollection",weChatOrderCollection);//upd
        IndexpageMap.put("orderAmount",orderAmount);//upd
        IndexpageMap.put("systemCliqueCount",systemCliqueCount);//upd
        IndexpageMap.put("systemUserCount",systemUserCount);//upd
        IndexpageMap.put("activationCardCount",activationCardCount);//upd
        IndexpageMap.put("shipCard",shipCard);//upd
        IndexpageMap.put("logInIp",logInIp);//upd
        IndexpageMap.put("record_date",record_date);
        IndexpageMap.put("dept_id",dept_id);
        IndexpageMap.put("requestMap",JSON.toJSONString(requestMap));
        IndexpageMap.put("api_pending_order",api_pending_order);
        try {
            if(findExistCount==0) {
                int add = yzIndexpageMapper.addIndexpage(IndexpageMap);
                log.info("新增首页数据成功 add:{} dept_id:{} record_date:{}",add,dept_id,record_date);
            }else{
                IndexpageMap.put("id",findExistCount);
                int upd = yzIndexpageMapper.updInfo(IndexpageMap);
                log.info("更新首页数据成功 id:{} upd:{} dept_id:{} record_date:{} ",findExistCount,upd,dept_id,record_date);
            }

        }catch (Exception e){
            log.info(JSON.toJSONString(IndexpageMap));
            log.error("首页数据DB操作异常  {}",e.getMessage());
        }
        redisCache.deleteObject(recordKey);//删除正在执行key
    }




    public List<Map<String,Object>> monthGetTop5 (List<Map<String,Object>>  list,Map<String,String> cMap){
        List<Map<String,Object>>  rList = ListMapUtils.GruoupByKey(list,"iccid");//分组过滤

        rList  = ListMapUtils.DbAs(rList,cMap);// as 别名
        return  rList;
    }













    @DataScope(deptAlias = "d" , userAlias = "u" , isMap=true)
    public List<String> getUserID(Map<String, Object> map) {
        return yzUserMapper.getUserID(map);
    }



    /**
     * 查询部门管理数据
     */
    @DataScope(deptAlias = "d", isMap=true)
    public Integer selectDeptListCount(Map<String, Object> map)
    {
        return yzUserMapper.selectDeptListCount(map);
    }






}






