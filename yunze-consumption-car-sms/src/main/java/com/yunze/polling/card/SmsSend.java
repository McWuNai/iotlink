package com.yunze.polling.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.mysql.YzCardMapper;
import com.yunze.common.mapper.mysql.bulk.YzSmSBulkBusinessDtailsMapper;
import com.yunze.common.mapper.mysql.bulk.YzSmSBulkBusinessMapper;
import com.yunze.common.utils.yunze.BulkUtil;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.utils.SmsServeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/08/15/14:21
 * @Description:
 */
@Slf4j
@Component
public class SmsSend {


    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzSmSBulkBusinessMapper yzSmSBulkBusinessMapper;
    @Resource
    private YzSmSBulkBusinessDtailsMapper yzSmSBulkBusinessDtailsMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private BulkUtil bulkUtil;
    @Resource
    private SmsServeUtils smsServeUtils;



    @RabbitHandler
    @RabbitListener(queues = "admin_cardSmsExecution_queue")
    public void cardSmsExecution_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            SmsGetTask(msg,channel_1);
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_cardSmsExecution_queue")
    public void dlxCardSmsExecution_queue(String msg, Channel channel_1) throws IOException {
        if(msg!=null && msg.length()>0){
            SmsGetTask(msg,channel_1);
        }
    }


    public void SmsGetTask(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String type = map.get("type").toString();

            String prefix = "exIdArr";
            //获取已经在执行的 id数组
            Object  exIdArrObj = redisCache.getCacheObject(prefix);
            List<String> exIdArr = new ArrayList<>();
            if(exIdArrObj!=null){
               exIdArr = (List<String>) exIdArrObj;
            }
            Map<String,Object> pMap=new HashMap<>();
            pMap.put("exIdArr",exIdArr);
            pMap.put("type",type);//执行类型

            List<Map<String, Object>> smSBulkArr = yzSmSBulkBusinessMapper.findOneExecution(pMap);

            if(smSBulkArr!=null){
                for (int i = 0; i < smSBulkArr.size(); i++) {
                    Map<String, Object> smSBulkMap = smSBulkArr.get(i);
                    String b_id = smSBulkMap.get("id").toString();
                    String message = smSBulkMap.get("message").toString();
                    //获取 所属下发卡号详情
                    Map<String,Object> findSMSMap = new HashMap<>();
                    findSMSMap.put("b_id",b_id);
                    List<Map<String, Object>> sendSmsArr = yzSmSBulkBusinessDtailsMapper.findSMSbulkArr(findSMSMap);
                    if(sendSmsArr!=null){
                        List<String> list = new ArrayList<>();
                        List<Map<String, Object>> listMap = new ArrayList<>();
                        Map<String,Object> findRouteMap=new HashMap<>();
                        for (int j = 0; j < sendSmsArr.size(); j++) {
                            Map<String, Object> sndCardMap = sendSmsArr.get(j);
                            Map<String,Object> iccidMap = new HashMap<>();
                            String iccid = sndCardMap.get("card_number").toString();
                            String childMessage = sndCardMap.get("message")!=null?sndCardMap.get("message").toString():"";
                            iccidMap.put("iccid",iccid);
                            iccidMap.put("childMessage",childMessage);
                            list.add(iccid);
                            listMap.add(iccidMap);
                        }
                        findRouteMap.put("card_arrs", listMap);
                        findRouteMap.put("type", "3");
                        findRouteMap.put("sms_code", "notNull");//通道配置不能为空

                        List<Map<String, Object>> findRouteArr = yzCardMapper.findRouteArr(findRouteMap);//查询 已配置短信通道的卡号
                        if(findRouteArr!=null && findRouteArr.size()>0){

                            List<Map<String, Object>> exList = listMap;
                            if (!(findRouteArr.size() == list.size())) {
                                //执行卡号与 存储不一样 赛选出 改为失败
                                List<String> list1 = new ArrayList<>();
                                for (int k = 0; k < list.size(); k++) {
                                    list1.add(list.get(k));
                                }
                                List<String> list_findRouteArr = new ArrayList<>();
                                for (int k = 0; k < findRouteArr.size(); k++) {
                                    list_findRouteArr.add(findRouteArr.get(k).get("iccid").toString());
                                }
                                // 获取 数组去重数据 和 重复值
                                Map<String, Object> getNotRepeatingMap_DB = Different.getNotRepeating(listMap, list_findRouteArr, "iccid");//获取 筛选不重复的某列值 和 重复的
                                exList = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Rlist");//更新 查询数据
                                List<Map<String, Object>> Out_list_Different = (List<Map<String, Object>>) getNotRepeatingMap_DB.get("Repeatlist");//更新 查询数据
                                //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                                if (Out_list_Different.size() > 0) {
                                    bulkUtil.smsDupdateArr(b_id,Out_list_Different,VeDate.getStringDate(),"2","通道状态 为停用或已删除！操作取消！");
                                }
                            }


                            if(exList!=null && exList.size()>0){
                                //开始 赋值 下发 短信号
                                for (int k = 0; k < exList.size(); k++) {
                                    Map<String, Object> card = exList.get(k);
                                    Map<String, Object> updMap = new HashMap<>();
                                    String iccid = card.get("iccid").toString();
                                    String childMessage = card.get("childMessage").toString();

                                    updMap.put("b_id",b_id);
                                    updMap.put("card_number",iccid);
                                    String send_number = "";
                                    String content = "";

                                    Map<String, Object> sendMap = new HashMap<>();
                                    Map<String, Object> find_card_route_map = null;
                                    for (int j = 0; j < findRouteArr.size(); j++) {
                                        Map<String, Object> cardRoute = findRouteArr.get(j);
                                        String rIccid = cardRoute.get("iccid").toString();
                                        String sms_number = cardRoute.get("sms_number").toString();
                                        if(iccid.equals(rIccid)){
                                            find_card_route_map = cardRoute;
                                            send_number = sms_number;//赋值短信号
                                            break;
                                        }
                                    }
                                    content = childMessage.length()>0?childMessage:message;//子类数据有 消息主体是抄送子类短信 没有时获取 父类模板
                                    updMap.put("send_number",send_number);
                                    sendMap.put("sms_number",send_number);
                                    sendMap.put("iccid",iccid);
                                    sendMap.put("content",content);
                                    sendMap.put("b_id", b_id);
                                    sendMap.put("card_number",iccid );
                                    String mydescribe = "";
                                    String result = "";
                                    String msgId = "";
                                    String state_id = "2";
                                    Map<String, Object> rMap =  smsServeUtils.sendSms(sendMap,find_card_route_map);
                                    System.out.println("=======================================");
                                    System.out.println(JSON.toJSONString(rMap));
                                    /*if(rMap!=null){
                                        System.out.println("=======================================");
                                        System.out.println(JSON.toJSONString(rMap));
                                        String cd_code = rMap.get("cd_code").toString();
                                        if(cd_code.equals("200")){
                                            Map<String, Object> dataMap = (Map<String, Object>) rMap.get("data");
                                            if(dataMap!=null){
                                                boolean bool = (boolean) dataMap.get("bool");
                                                if(bool){
                                                    state_id = "1";
                                                }
                                                result = dataMap.get("result").toString();
                                                msgId = dataMap.get("msgId").toString();
                                                mydescribe = dataMap.get("mydescribe").toString();
                                            }
                                        }

                                    }
                                    updMap.put("result",result);
                                    updMap.put("msgId",msgId);
                                    updMap.put("mydescribe",mydescribe);
                                    updMap.put("state_id",state_id);
                                    bulkUtil.smsDupdate(updMap);*/
                                }
                            }

                        }else{
                            bulkUtil.smsDupdateArr(b_id,list,VeDate.getStringDate(),"2","通道未配置短信接口信息 取消操作");
                        }
                    }
                    //修改执行状态
                    Map<String,Object> bulkMap = new HashMap<>();
                    bulkMap.put("id",b_id);
                    bulkMap.put("state_id","1");//状态  完成 1
                    bulkMap.put("end_time", VeDate.getStringDate());//赋值 结束时间
                    bulkUtil.smsUpdate(bulkMap);//消费者进入变更执行状态
                }
            }

        } catch (Exception e) {
            log.error(">>错误 - SmsGetTask 消费者:{}<<", e.getMessage().toString());
        }
    }

}
