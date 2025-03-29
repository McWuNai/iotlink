package com.yunze.common.utils.yunze;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardFlowHisMapper;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.Arith;
import com.yunze.common.utils.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 卡用量同步
 * @Auther: zhang feng
 * @Date: 2021/07/20/15:19
 * @Description:
 */
@Component
public class CardFlowSyn {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    @Resource
    private YzCardFlowHisMapper yzCardFlowHisMapper;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisCache redisCache;

    @Resource
    private InternalApiRequest internalApiRequest;

    final private static String isChickDateValue = "isChickDateValue";
    final private static String stringDateShortStart = "stringDateShortStart";
    /**
     * 用量计算 [直接同步 yzCardMapper ]
     * @param iccid
     * @param ApiUsed
     * @return
     */
    public Map<String,Object> CalculationFlow(String iccid,Double ApiUsed, Map<String,Object> map){
        Map<String,Object> Rmap = CalculationFlowCommon(iccid, ApiUsed, map, null);
        return CalculationFlowCommon(Rmap, iccid);
    }

    public Map<String,Object> CalculationBatchFlow(String iccid,Double ApiUsed, Map<String,Object> map, String realNameStatus){
        Map<String,Object> Rmap = CalculationFlowCommon(iccid, ApiUsed, map, realNameStatus);
        return CalculationFlowCommon(Rmap, iccid);
    }

    public Map<String,Object> CalculationFlowCommon(Map<String,Object> Rmap, String iccid){
        Double SumFlow = Double.parseDouble(Rmap.get("SumFlow").toString());
        Double total_show_flow = Double.parseDouble(Rmap.get("total_show_flow").toString());
        boolean bool_info;
        Double remaining = Arith.sub(SumFlow,total_show_flow);
        Map<String,Object> updUsedMap = new HashMap<>();
        updUsedMap.put("iccid",iccid);
        updUsedMap.put("used",total_show_flow);
        updUsedMap.put("remaining",remaining);
        bool_info = yzCardMapper.updUsed(updUsedMap)>0;
        //返回数据
        Rmap.put("used",total_show_flow);
        Rmap.put("remaining",remaining);
        Rmap.put("bool_info",bool_info);
        return  Rmap;
    }

    /**
     * 用量计算 [queue 队列同步 yzCardMapper ]
     * @param iccid
     * @param ApiUsed
     * @return
     */
    public Map<String,Object> CalculationFlowQueue(String iccid,Double ApiUsed,boolean bool, Map<String,Object> map){
        Map<String,Object> Rmap =  CalculationFlowCommon(iccid, ApiUsed, map, null);
        Double SumFlow = Double.parseDouble(Rmap.get("SumFlow").toString());
        Double total_show_flow = Double.parseDouble(Rmap.get("total_show_flow").toString());
        String bool_info = "";
        Double remaining = Arith.sub(SumFlow,total_show_flow);
        Map<String,Object> updUsedMap = new HashMap<>();
        updUsedMap.put("iccid",iccid);
        updUsedMap.put("used",total_show_flow);
        updUsedMap.put("remaining",remaining);
        //1.创建路由 绑定 生产队列 发送消息
        String card_exchangeName = "admin_card_exchange", card_queueName = "admin_CardUpd_queue", card_routingKey = "admin.CardUpd.queue",
                card_del_exchangeName = "dlx_"+card_exchangeName,card_del_queueName = "dlx_"+card_queueName, card_del_routingKey = "dlx_"+card_routingKey;
        try {
            if(bool){
                try {
                   // rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                }catch (Exception e){
                    System.out.println("CalculationFlowQueue   》creatExchangeQueue "+e.getMessage());
                }
            }
            updUsedMap.put("queueTypeName","admin_CardUpdUsed_queue");
            rabbitTemplate.convertAndSend(card_exchangeName, card_routingKey, JSON.toJSONString(updUsedMap), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
            bool_info = "发送 主表修改 指令成功 ！";
        } catch (Exception e) {
            System.out.println("用量计算 队列同步 创建 失败 " + e.getMessage().toString());
            bool_info = e.getMessage().toString();
            bool_info = bool_info.length()>500?bool_info.substring(0,500):bool_info;
        }



        //返回数据
        Rmap.put("used",total_show_flow);
        Rmap.put("remaining",remaining);
        Rmap.put("bool_info",bool_info);
        return  Rmap;
    }


    /**
     * 用量计算
     * @param iccid
     * @param ApiUsed
     * @return
     */
    public Map<String,Object> CalculationFlowCommon(String iccid,Double ApiUsed, Map<String,Object> map, String realNameStatus){
        Map<String,Object> Rmap = new HashMap<>();
        boolean bool_info=false,bool_flow=false,bool_flowHis=false;
        Map<String,Object> findMap = new HashMap<>();
        findMap.put("iccid",iccid);

        String  []YyyyAndMm = VeDate.getYyyyAndMm();
        String nowMonthSta = YyyyAndMm[0]+"-"+YyyyAndMm[1]+"-01";
        String nowMonthEnd = VeDate.getLastDayOfMonth(Integer.parseInt(YyyyAndMm[0]),Integer.parseInt(YyyyAndMm[1]));
        findMap.put("nowMonthSta",nowMonthSta+" 00:00:00");
        findMap.put("nowMonthEnd",nowMonthEnd+" 23:59:59");
        //0.修改 时间到期资费计划
        yzCardFlowMapper.updStatus(findMap);
        //1.查询过期用量
        String SumErrorFlow = yzCardFlowMapper.findInvalidationSumErrorFlow(findMap);
        Double SumErrorFlow_D = SumErrorFlow!=null && SumErrorFlow.length()>0?Double.parseDouble(SumErrorFlow):0.0;

        Double DseFlow = SumErrorFlow!=null && SumErrorFlow.length()>0?Double.parseDouble(SumErrorFlow):0.0;

        /*SumErrorFlow_D = Arith.add(SumErrorFlow_Year_D,SumErrorFlow_D);
        DseFlow = Arith.add(SumErrorFlow_Year_D,DseFlow);*/

        //2.获取 有效资费计划 计算
        Double xiShu = 1.0;//默认

        double sumTrue_flow = 0.0;
        double sumUse_so_flow = 0.0;

        String[] now = VeDate.getStringDateShort().split("-");
        findMap.put("year",now[0]);
        findMap.put("month",now[1]);
        findMap.put("day",now[2]);

        //同步
        Double total_flow_now = 0.00;
        //获取截至当天的当月总流量

        Double total_show_flow = 0.00;
        Double total_show_flow_now = 0.00;
        int IDay = Integer.parseInt(now[2]);

            if (IDay == 1) {
                //判断 是否为当月1号
                //是 调用 B方法
                //是 截至当天的当月总流量 - 前月末总流量 = 新月当天总流量 = 新月当天流量

                Map<String, Object> flowCounting = getFlowCounting(iccid, ApiUsed);
                total_flow_now = flowCounting != null ? Arith.sub(ApiUsed, Double.parseDouble(flowCounting.get("total_flow").toString())) : 0.00;
                total_show_flow = total_flow_now;
                total_show_flow_now = total_flow_now;
            } else if (IDay == 27) {
                //判断 是否为当月 27号
                //是 调用 A方法
                //是 获取 上月流量周期
                //是 上月流量周期 + 截至当天的当月总流量 = 当月总流量
                //是 截至当天的当月总流量 = 当天总流量

                String cycleTotalFlow = getCycleTotalFlow(findMap, map, realNameStatus);
                total_flow_now = ApiUsed;
                total_show_flow = Arith.add(Double.parseDouble(cycleTotalFlow), ApiUsed);
                total_show_flow_now = ApiUsed;
            } else if (IDay > 1 && IDay < 27) {
                //判断 当天号数 > 1 && 当天号数 < 27
                //是 调用 B方法
                //是 截至当天的当月总流量 - 前月末总流量 = 当月总流量
                //是 截至当天的当月总流量 - 截至昨天的当月总流量 = 当天总流量

                Map<String, Object> flowCounting = getFlowCounting(iccid, ApiUsed);
                total_flow_now = flowCounting != null ? Arith.sub(ApiUsed, Double.parseDouble(flowCounting.get("total_flow_yesterday").toString())) : 0.00;
                total_show_flow = flowCounting != null ? Arith.sub(ApiUsed, Double.parseDouble(flowCounting.get("total_flow").toString())) : 0.00;
                total_show_flow_now = total_flow_now;
            } else if (IDay > 27) {
                //判断 当天号数 > 27
                //是 调用 C方法
                //是 调用 A方法
                //是 获取上月流量周期
                //是 上月流量周期 + 截至当天的当月总流量 = 当月总流量
                //是 截至当天的当月总流量 - 截至昨天的当月总流量 = 当天总流量
                String cycleTotalFlow = getCycleTotalFlow(findMap, map, realNameStatus);
                total_show_flow = Arith.add(ApiUsed, Double.parseDouble(cycleTotalFlow));

                String[] yyyyAndMmShortYesterday = VeDate.getYyyyAndMmShortYesterday();
                Map<String, Object> yes = new HashMap<>();
                yes.put("year", yyyyAndMmShortYesterday[0]);
                yes.put("month", yyyyAndMmShortYesterday[1]);
                yes.put("day", yyyyAndMmShortYesterday[2]);
                yes.put("iccid", iccid);
                Integer exist = yzCardFlowHisMapper.isExist(yes);
                if (exist > 0) {
                    total_flow_now = Arith.sub(ApiUsed, Double.parseDouble(yzCardFlowHisMapper.total_flow(yes)));
                } else {
                    int chickDate = isChickDate();
                    total_flow_now = Arith.div(ApiUsed, chickDate);
                    yes.put("total_flow_now", total_flow_now);
                    yes.put("total_flow", Arith.sub(ApiUsed, total_flow_now));
                    yzCardFlowHisMapper.save(yes);
                }
                total_show_flow_now = total_flow_now;
            }

        //3.同步用量历史表
        total_show_flow = Arith.mul(total_show_flow,xiShu);
        total_show_flow_now = Arith.mul(total_show_flow_now,xiShu);
        Integer isExist = yzCardFlowHisMapper.isExist(findMap);//历史用量 月用量记录
        if(isExist!=null && isExist>0){
            Map<String,Object> editMap = new HashMap<>();
            editMap.putAll(findMap);
            editMap.put("total_flow",ApiUsed);
            editMap.put("total_flow_now",total_flow_now);
            editMap.put("total_show_flow",total_show_flow);
            editMap.put("total_show_flow_now",total_show_flow_now);
            bool_flowHis = yzCardFlowHisMapper.edit(editMap)>0;
        }else{
            Map<String,Object> saveMap = new HashMap<>();
            saveMap.putAll(findMap);
            saveMap.put("total_flow",ApiUsed);
            saveMap.put("total_flow_now",total_flow_now);
            saveMap.put("total_show_flow",total_show_flow);
            saveMap.put("total_show_flow_now",total_show_flow_now);
            bool_flowHis = yzCardFlowHisMapper.save(saveMap)>0;
        }

        //当前计算 = 接口用量 - 已到期 用量
        Double cl_Used = Arith.sub(total_show_flow,DseFlow);
        Double used = DseFlow;//主表 账期 已用

        List<Map<String,Object>> InEffectArr =  yzCardFlowMapper.findInEffect(findMap);
        if(InEffectArr!=null && InEffectArr.size()>0){
            Double UdF = cl_Used+0;
            for (int i = 0; i < InEffectArr.size(); i++) {
                Map<String,Object> Pobj = InEffectArr.get(i);
                Double true_flow = Double.parseDouble(Pobj.get("true_flow").toString());
                Double error_flow = Double.parseDouble(Pobj.get("error_flow").toString());
                Double error_time = Double.parseDouble(Pobj.get("error_time").toString());
                String id = Pobj.get("id").toString();
                xiShu = error_time;//同步包系数

                String ord_type = Pobj.get("ord_type").toString();
                if ("3".equals(ord_type)) {
                    Double use_ture_flow = Double.parseDouble(Pobj.get("use_true_flow").toString());
                    cl_Used = Arith.add(cl_Used, use_ture_flow);
                }
                //   当前计算 用量 - 资费计划 用量 作比较 小等 0 用完了 否则未用完继续 作比较
                UdF = Arith.sub(cl_Used,error_flow);
                if(UdF<0){
                    Double use_so_flow = Arith.mul(cl_Used,error_time);
                    DseFlow = Arith.add(DseFlow,use_so_flow);//累加 主表用量 = 已用 * 系数
                    Map<String,Object> UpdMap = new HashMap<>();
                    UpdMap.put("use_true_flow",cl_Used);
                    UpdMap.put("use_so_flow",use_so_flow);
                    UpdMap.put("status","1");// 生效中
                    UpdMap.put("id",id);
                    bool_flow =  yzCardFlowMapper.updFlow(UpdMap)>0;
                    break;
                }else{
                    cl_Used = Arith.sub(cl_Used,error_flow);
                    DseFlow = Arith.add(Arith.add(DseFlow,error_flow),Math.abs(cl_Used));//累加 主表用量 = 包容量 + 超出部分绝对值
                    Map<String,Object> UpdMap = new HashMap<>();
                    UpdMap.put("use_true_flow",error_flow);
                    UpdMap.put("use_so_flow",true_flow);
                    UpdMap.put("status","0");// 已失效
                    UpdMap.put("id",id);
                    //bool_flow = true;
                    bool_flow =  yzCardFlowMapper.updFlow(UpdMap)>0;
                }
            }
        }else{
            //获取最近过期的资费计划 系数
            String Error_time =  yzCardFlowMapper.FindError_time(findMap);
            xiShu = Error_time!=null && Error_time.length()>0 && Double.parseDouble(Error_time)>=1?Double.parseDouble(Error_time):xiShu;

        }

        //同步主表用量
        Double SumFlow = 0.0;
        SumFlow = Arith.add(SumFlow,SumErrorFlow_D);
        if(InEffectArr!=null && InEffectArr.size()>0){
            for (int i = 0; i < InEffectArr.size(); i++) {
                Map<String,Object> Pobj = InEffectArr.get(i);
                Double true_flow = Double.parseDouble(Pobj.get("true_flow").toString());
                SumFlow = Arith.add(SumFlow,true_flow);
            }

        }else{

            //查询该卡是否已经订购过资费计划 且  时间有效状态失效

            //获取时间有效状态失效的资费计划 总量 和 已使用总量
            Map<String,Object> iMap =  yzCardFlowMapper.findInvalidationSum(findMap);
            if(iMap!=null){
                sumTrue_flow = iMap.get("sumTrue_flow")!=null?Double.parseDouble(iMap.get("sumTrue_flow").toString()):sumTrue_flow;
                sumUse_so_flow = iMap.get("sumUse_so_flow")!=null?Double.parseDouble(iMap.get("sumUse_so_flow").toString()):sumUse_so_flow;
                DseFlow = Arith.add(sumUse_so_flow,Arith.mul(cl_Used,xiShu));//目前用量 = 已记录已用加上最近一个 资费计划 系数 * 接口减去已订购失效 真实用量 剩余用量
                SumFlow = sumTrue_flow;//总用量 =  时间有效状态失效 sum（True_flow）
            }else {
                //查询 累计包类型 年包 有年包类型时不做累加
                String SumErrorFlow_Year = yzCardFlowMapper.findInvalidationSumErrorFlow_Year(findMap);
                Double SumErrorFlow_Year_D = SumErrorFlow_Year!=null && SumErrorFlow_Year.length()>0?Double.parseDouble(SumErrorFlow_Year):0.0;
                if(SumErrorFlow_Year_D == 0.0){
                    //未订购过资费计划  但是接口有用量 返回时   主表 账期 已用 += 接口用量 * 系数
                    if(cl_Used>0.0){
                        DseFlow = Arith.add(DseFlow,Arith.mul(cl_Used,xiShu));
                    }
                }
            }
        }



        //返回数据
        Rmap.put("SumFlow",SumFlow);
        Rmap.put("total_show_flow",DseFlow);
        Rmap.put("used",DseFlow);

        Rmap.put("bool_info",bool_info);
        Rmap.put("bool_flowHis",bool_flowHis);
        Rmap.put("bool_flow",bool_flow);
        return  Rmap;
    }

    //判断 上月流量周期 是否存在(A方法)
    /**
     * 判断 上月流量周期 是否存在
     * 否 通过API接口获取 上月流量周期
     * 否 上月流量周期 存入MySQL (存入地址 yyyy.MM.27)
     * 否 直接返回通过API接口获取的数值
     * 是 从MySQL获取数值
     * 返回总流量数值
     */
    private String getCycleTotalFlow(Map<String, Object> findMap, Map<String, Object> fmap, String realNameStatus) {
        String yes[] = VeDate.getYyyyAndMmCycle();
        Map<String, Object> map = new HashMap<>();
        map.put("year", yes[0]);
        map.put("month", yes[1]);
        map.put("day", yes[2]);
        map.put("iccid", findMap.get("iccid"));
        Object billingCycle = redisCache.getCacheObject("billingCycle");
        if (billingCycle != null) {
            map.put("billingCycle", billingCycle.toString());
        } else {
            LocalDate today = LocalDate.now(); // 获取当前日期
            int dayOfMonth = today.getDayOfMonth();
            LocalDate targetDate;
            if (dayOfMonth < 27) {
                // 如果是27号之前，手动调整到上一个月的第一天
                targetDate = today.minusMonths(1).withDayOfMonth(1);
            } else {
                // 如果是27号或之后，获取当月的第一天
                targetDate = today.withDayOfMonth(1);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
            map.put("billingCycle", targetDate.format(formatter));

            // 获取当前时间与今天的23:59:59
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

            // 如果当前时间已经超过今天的23:59:59，则设置为明天的23:59:59
            if (now.isAfter(endOfDay)) {
                endOfDay = endOfDay.plusDays(1);
            }

            // 计算过期时间并设置
            long ttlInSeconds = Duration.between(now, endOfDay).getSeconds();

            redisCache.setCacheObject("billingCycle", map.get("billingCycle").toString());
            redisCache.expire("billingCycle", ttlInSeconds, TimeUnit.SECONDS);
        }

        try {
            Integer exist = yzCardFlowHisMapper.isExist(map);
            String total_flow = null;
            if (exist > 0) {
                total_flow = yzCardFlowHisMapper.total_flow(map); // 历史用量 月用量记录
            }

            if (total_flow == null || "0.00".equals(total_flow)) {
                String use = null;

                if (StringUtils.isEmpty(realNameStatus) || !"1".equals(realNameStatus)) {
                    Map<String, Object> Rmap = internalApiRequest.queryFlowHis(map, fmap);
                    use = Rmap.get("Use").toString();
                }

                if (use == null || "-1".equals(use)) {
                    total_flow = "0.00";
                } else {
                    total_flow = use;
                }
                map.put("total_flow", total_flow);

                if (exist > 0) {
                    yzCardFlowHisMapper.edit(map);
                } else {
                    yzCardFlowHisMapper.save(map);
                }
            }
            return total_flow;
        } catch (NumberFormatException e) {
            // 处理 Double.parseDouble 的异常
            System.out.println("浮点数处理异常：" + e);
            throw new RuntimeException("getYesTotalFlow方法抛出 Double.parseDouble 异常: ", e);
        } catch (Exception e) {
            // 捕获其他所有异常，例如数据库操作异常、API请求异常等
            System.out.println("其他处理异常：" + e);
            throw new RuntimeException("getYesTotalFlow方法抛出: ", e);
        }
    }


    //判断前月末总流量是否存在(B方法)
    //否 判断 从 Redis 获取上月27 - 当天号数 = 共有几天 是否存在
    //否 否 调用 C方法
    //否 是 截至当天的当月总流量 / 总天数 = 每天平均用量
    //否 是 计算 上月27 ~ 上月月末 天数
    //否 是 平均用量 * 上月27 ~ 上月月末天数 = 前月末总流量
    //否 是 前月末总流量 存入MySQL
    //否 是 前月末总流量 存入Map
    //是 是 通过C方法 获取 总天数
    //是 是 通过MySQL 获取 前月末总流量
    //是 是 判断 获取截至昨天的当月总流量 是否存在
    //是 是 否 总天数 - 1 = 昨天天数
    //是 是 否 平均用量 * 昨天天数 = 截至昨天的当月总流量
    //返回所有数值
    private Map<String, Object> getFlowCounting(String iccid, Double apiUsed) {
        double avgDayFlow = 0.00;
        int countingDate = 0;
        double lastMonthLastDayFlow;
        int stringDateShortStart;
        Map<String, Object> map = new HashMap<>();
        Integer exist = 0;
        String[] yyyyAndMmShortEnd;
        String[] yyyyAndMmShortYesterday;

        try {
            countingDate = isChickDate();
        } catch (Exception e) {
            System.out.println("Error in isChickDate: " + e.getMessage());
        }

        if (countingDate <= 0) countingDate = 0;

        try {
            yyyyAndMmShortEnd = VeDate.getYyyyAndMmShortEnd();
            yyyyAndMmShortYesterday = VeDate.getYyyyAndMmShortYesterday();
        } catch (Exception e) {
            System.out.println("Error in getting date components: " + e.getMessage());
            return map;
        }

        map.put("iccid", iccid);
        map.put("year", yyyyAndMmShortYesterday[0]);
        map.put("month", yyyyAndMmShortYesterday[1]);
        map.put("day", yyyyAndMmShortYesterday[2]);

        try {
            exist = yzCardFlowHisMapper.isExist(map);
        } catch (Exception e) {
            System.out.println("Error checking existence in yzCardFlowHisMapper: " + e.getMessage());
        }

        if (apiUsed != -1 && apiUsed > 0.00) {
            avgDayFlow = apiUsed / countingDate;
        }

        try {
            if (exist > 0) {
                map.put("total_flow_yesterday", yzCardFlowHisMapper.total_flow(map));
            } else {
                double yesterdayFlow = avgDayFlow * (countingDate - 1);
                map.put("total_flow_yesterday", yesterdayFlow);
                map.put("total_flow", yesterdayFlow);
                yzCardFlowHisMapper.save(map);
            }
        } catch (Exception e) {
            System.out.println("Error processing total flow for yesterday: " + e.getMessage());
        }

        map.put("year", yyyyAndMmShortEnd[0]);
        map.put("month", yyyyAndMmShortEnd[1]);
        map.put("day", yyyyAndMmShortEnd[2]);

        try {
            exist = yzCardFlowHisMapper.isExist(map);

            if (exist > 0) {
                lastMonthLastDayFlow = Double.parseDouble(yzCardFlowHisMapper.total_flow(map));
                map.put("total_flow", lastMonthLastDayFlow);
                yzCardFlowHisMapper.edit(map);
            } else {

                Integer cacheObject = redisCache.getCacheObject(CardFlowSyn.stringDateShortStart);
                if (cacheObject == null) {
                    stringDateShortStart = VeDate.getStringDateShortStart();
                    lastMonthLastDayFlow = avgDayFlow * stringDateShortStart;
                    setDailyExpiryKey(CardFlowSyn.stringDateShortStart, stringDateShortStart);
                } else {
                    stringDateShortStart = cacheObject;
                    lastMonthLastDayFlow = avgDayFlow * stringDateShortStart;
                }

                map.put("total_flow", lastMonthLastDayFlow);

                yzCardFlowHisMapper.save(map);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing cache object to integer: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error saving or editing flow data: " + e.getMessage());
        }

        map.put("total_flow_avg", avgDayFlow);
        map.put("countingDate", countingDate);

        return map;
    }

    //计算天数(C方法)

    /**
     * 计算天数
     * 计算 自动根据账期获取27号 - 当天号数 = 共有几天
     * 返回天数
     */
    private int isChickDate() {
        try {
        Object cacheObject = redisCache.getCacheObject(CardFlowSyn.isChickDateValue);
        if (cacheObject != null) return (int) cacheObject;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int between = (int) ChronoUnit.DAYS.between(LocalDate.parse(VeDate.getStringDateShortCycle(), formatter), LocalDate.parse(VeDate.getStringDateShort(), formatter));
        setDailyExpiryKey(CardFlowSyn.isChickDateValue, between);
        return between;
        } catch (DateTimeParseException e) {
            // 处理日期解析异常
            System.out.println("isChickDate方法抛出 日期格式解析异常: " + e);
            return -1;
        } catch (Exception e) {
            // 处理其他未知异常
            System.out.println("isChickDate方法抛出 其他未知异常: " + e);
            return -1;
        }
    }

    /**
     * 设置Redis键值对，并使该键在当天23:59:00失效。
     */
    private void setDailyExpiryKey(String key, Object value) {
        try{
        // 获取当前时间和当天23:59:00的时间点
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(0).withNano(0);

        // 如果当前时间已经过了23:59:00，则设置为次日的23:59:00
        if (now.isAfter(endOfDay)) {
            endOfDay = endOfDay.plusDays(1);
        }

        // 计算当前时间和当天23:59:00之间的时间差，单位为秒
        Duration duration = Duration.between(now, endOfDay);
        int ttlInSeconds = (int) duration.getSeconds();

        // 存储结果到Redis，并设置TTL
        redisCache.setCacheObject(key, value, ttlInSeconds, TimeUnit.SECONDS);
        } catch (DateTimeException e) {
            // 处理日期时间相关的异常
            throw new RuntimeException("setDailyExpiryKey方法抛出 日期时间计算异常", e);
        } catch (Exception e) {
            // 处理其他未知异常
            throw new RuntimeException("setDailyExpiryKey方法抛出 其他未知异常", e);
        }
    }

}
