package com.yunze.task.yunze.card;

import com.alibaba.fastjson.JSONObject;

import com.yunze.apiCommon.mapper.YzCardPollingUtilCzcMapper;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.Inquire.Query_YD;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.apiCommon.utils.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class autoPollingCzcTaskMQ {

    @Resource
    private YzCardPollingUtilCzcMapper yzCardPollingUtilCzcMapper;

    @Resource
    private InternalApiRequest internalApiRequest;

    @Transactional
    @RabbitHandler
    @RabbitListener(queues = "admin_autoPollingCzc_queue")
    public void init() {
        // 将轮询全部修改为今日为轮询
        yzCardPollingUtilCzcMapper.updAutoPollingAll();
        // 获取需要自动同步卡 的通道分组
        List<String> routes = yzCardPollingUtilCzcMapper.selRoute();
        for (int i = 0; i < routes.size(); i++) {
            //判断通道是否为 ECV5 todo 后续根据需求自己对接上游接口
            String code = yzCardPollingUtilCzcMapper.selRouteCode(routes.get(i));
            if (code.equals("YiDong_EC")) {
                //是 移动ECV5
                //获取route信息进行 创建 移动官方对象
                Map<String, Object> initRoute = yzCardPollingUtilCzcMapper.selRouteMap(routes.get(i));
                //YD_EC_Api yd_ec_api = new YD_EC_Api(initRoute,initRoute.get("cd_code").toString());//
                Query_YD query_yd = new Query_YD(initRoute, initRoute.get("cd_code").toString());
                // 轮询表中 的卡号
                List<String> iccids = yzCardPollingUtilCzcMapper.selIccids(routes.get(i));
                //拆分List
                List<List<String>> lists = subList(iccids, 100);
                for (int j = 0; j < lists.size(); j++) {
                    // 将拆分出来的List 拼接去做接口请求 YDECV5
                    String res = query_yd.autoPolling(lists.get(j));
                   log.info("自动轮询-上游返回数据:{}",res);
                    Map<String, Object> resMap = JSONObject.parseObject(res);
                    String status = resMap.get("status").toString();
                    if (status.equals("0")){
                        log.info("上游请求成功");
                        List<Map<String,Object>> result=(List<Map<String,Object>>)resMap.get("result");
                        for (int k = 0; k < result.size(); k++) {
                            List<Map<String, Object>> cardMapList = (List<Map<String, Object>>) result.get(k).get("dataAmountList");
                            Map<String, Object> cardUsedMap = cardMapList.get(0);
                            String iccid = cardUsedMap.get("iccid").toString();
                            double dataAmount = Double.parseDouble(cardUsedMap.get("dataAmount").toString());
                            double used = dataAmount / 1024;
                            CalculateUsage(iccid,used);
                            // 修改auto_polling表中字段 全部成功
                            yzCardPollingUtilCzcMapper.updAutoPollingInfo(iccid);
                        }
                    }else{
                        log.error("轮询失败:{}",resMap.get("message").toString());
                        List<String> errorIccids = lists.get(j);
                        for (int k = 0; k < errorIccids.size(); k++) {
                            String iccid = errorIccids.get(k);
                            yzCardPollingUtilCzcMapper.updAutoPollingError(iccid);
                        }
                        // todo 上游接口请求失败,将本次循环的数据 添加至 失败从新执行
                    }


                }


            }

        }

    }

    /**
     * list 拆分
     *
     * @param sourceList 被拆分List
     * @param n          拆分的List大小
     * @param <T>
     * @return 拆分玩之后的List
     */
    private static <T> List<List<T>> subList(List<T> sourceList, int n) {
        List<List<T>> rsList = new ArrayList<>();
        if (n <= 0) {
            rsList.add(sourceList);
            return rsList;
        }
        int listSize = sourceList.size();
        int groupNum = (sourceList.size() / n) + 1;
        for (int i = 0; i < groupNum; i++) {
            List<T> subList = new ArrayList<>();
            for (int j = i * n; j < (i + 1) * n; j++) {
                if (j < listSize) {
                    subList.add(sourceList.get(j));
                }
            }
            rsList.add(subList);
        }
        if (rsList.get(rsList.size() - 1).size() == 0) {
            rsList.remove(rsList.size() - 1);
        }
        return rsList;
    }

    /**
     * 计算用量
     * @param iccid 卡号
     * @param used 用量
     */
    private void CalculateUsage(String iccid,double used){
        Map<String, Object> updMap = new HashMap<>();
        //1.有无资费 或年月资费
        Map<String, Object> packetMap = yzCardPollingUtilCzcMapper.selCardPacket(iccid);
        String now = com.yunze.common.utils.yunze.VeDate.getStringDateShort();//yyyy-mm-dd
        if (packetMap == null){
            //卡未充值资费
            log.info("卡:{},未订购资费,用量:{}",iccid,used);
        }else {
            String packet_valid_name = packetMap.get("packet_valid_name").toString();
            if (packet_valid_name.equals("月")){
                //资费充值为  月套餐
                log.info("卡:{},订购月资费包,用量:{}",iccid,used);
                //3. 校验是否为一个月第一天,如果是 则初始化 月资费的剩余用量
                boolean firstDayOfMonth = com.yunze.common.utils.yunze.VeDate.isFirstDayOfMonth();
                if (firstDayOfMonth){
                    //是当月第一天
                    Map<String, Object> infoUsedMap = yzCardPollingUtilCzcMapper.selCardUseInfo(iccid);
                    double error_flow = Double.parseDouble(infoUsedMap.get("error_flow").toString());
                    double remaining = error_flow - used;
                    updMap.put("remaining",remaining);
                    updMap.put("used",used);
                    updMap.put("iccid",iccid);
                    yzCardPollingUtilCzcMapper.updCardUsedByDayIsFastDayInMonth(updMap);
                    log.info("月套餐二号更新 :卡号{},用量{},剩余{}",iccid,used,remaining);
                }else{
                    //不是最后一天
                    Map<String, Object> infoUsedMap = yzCardPollingUtilCzcMapper.selCardOne(iccid);
                    String oldUsed = infoUsedMap.get("used").toString();
                    double remaining = Double.parseDouble(infoUsedMap.get("remaining").toString());
                    double r = used - Double.parseDouble(oldUsed);
                    updMap.put("remaining",remaining-r);
                    updMap.put("used",used);
                    updMap.put("iccid",iccid);
                    yzCardPollingUtilCzcMapper.updCardUsedByDayIsFastDayInMonth(updMap);
                    log.info("月套餐日常轮询 :卡号{},用量{},剩余{}",iccid,used,remaining);
                }
            }else if (packet_valid_name.equals("年")){
                //资费充值为 年套餐
                log.info("卡:{},订购年资费包,用量:{}",iccid,used);
                //2. 校验是否为月最后一天,如果是则区分卡是否为年包,是年包 则将卡用量详情 插入 yz_card_month表
                boolean isLastDay = com.yunze.common.utils.yunze.VeDate.isFirstDayOfMonthOuth();//判断是否为最后一天
                if (isLastDay){
                    //是最后一天 将用量传入
                    String[] split = now.split("-");
                    String time = split[0]+split[1];
                    Map<String, Object> monthMap = new HashMap<>();
                    monthMap.put("iccid",iccid);
                    monthMap.put("used",used);
                    monthMap.put("month",time);
                    yzCardPollingUtilCzcMapper.insertMonth(monthMap);
                }
                //修改 用量及剩余用量
                // todo 判断是否在年包有效期,表字段未设置 后续补充
                Map<String, Object> infoUsedMap = yzCardPollingUtilCzcMapper.selCardOne(iccid);
                String oldUsed = infoUsedMap.get("used").toString();
                double remaining = Double.parseDouble(infoUsedMap.get("remaining").toString());
                double r = used - Double.parseDouble(oldUsed);
                updMap.put("remaining",remaining-r);
                updMap.put("used",used);
                updMap.put("iccid",iccid);
                yzCardPollingUtilCzcMapper.updCardUsedByDayIsFastDayInMonth(updMap);
                log.info("年套餐日常轮询 :卡号{},用量{},剩余{}",iccid,used,remaining);
            }else{
                //未知类型套餐
                log.info("卡:{},订购未知资费包,用量:{}",iccid,used);
                Map<String, Object> infoUsedMap = yzCardPollingUtilCzcMapper.selCardOne(iccid);
                String oldUsed = infoUsedMap.get("used").toString();
                double remaining = Double.parseDouble(infoUsedMap.get("remaining").toString());
                double r = used - Double.parseDouble(oldUsed);
                updMap.put("remaining",remaining-r);
                updMap.put("used",used);
                updMap.put("iccid",iccid);
                yzCardPollingUtilCzcMapper.updCardUsedByDayIsFastDayInMonth(updMap);
                log.info("年套餐日常轮询 :卡号{},用量{},剩余{}",iccid,used,remaining);
            }
        }


    }




    public static void main(String[] args) {
        String nowDateShortNuber = VeDate.getNowDateShortNuber();
        System.out.println(nowDateShortNuber);
    }


}
