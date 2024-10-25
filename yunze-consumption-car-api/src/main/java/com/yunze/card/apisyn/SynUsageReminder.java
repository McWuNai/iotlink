package com.yunze.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.Arith;
import com.yunze.common.mapper.yunze.card.YzCardUsageReminderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/04/08/15:41
 * @Description: 同步上游返回套餐记录信息
 */
@Slf4j
@Component
public class SynUsageReminder {

    @Resource
    private YzCardUsageReminderMapper yzCardUsageReminderMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_sendSynUsageReminder_queue",containerFactory = "customContainerFactory")
    public void SynUsageReminder(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }

            Map<String,Object> Pmap = JSON.parseObject(msg);
            //System.out.println(Pmap);
            String iccid = Pmap.get("iccid").toString();//iccid
            String cd_code = Pmap.get("cd_code").toString();//iccid
            List<Map<String, Object>> setResult = (List<Map<String, Object>>) Pmap.get("setResult");
            CardSynEnforce(iccid,cd_code,setResult);
        }catch (Exception e){
            log.error(">>错误 - 同步上游返回套餐记录信息 消费者:{}<<", e.getMessage().toString());
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_sendSynUsageReminder_queue",containerFactory = "customContainerFactory")
    public void dlxSynUsageReminder(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }

            Map<String,Object> Pmap = JSON.parseObject(msg);
            //System.out.println(Pmap);
            String iccid = Pmap.get("iccid").toString();//iccid
            String cd_code = Pmap.get("cd_code").toString();//iccid
            List<Map<String, Object>> setResult = (List<Map<String, Object>>) Pmap.get("setResult");
            CardSynEnforce(iccid,cd_code,setResult);
        }catch (Exception e){
            log.error(">>错误 - 同步上游返回套餐记录信息 消费者:{}<<", e.getMessage().toString());
        }
    }



    public  void CardSynEnforce(String iccid,String cd_code,List<Map<String, Object>> setResult){
        List<Map<String, Object>> card_arrs = new ArrayList<>();
        if(setResult!=null && setResult.size()>0){
            for (int i = 0; i < setResult.size(); i++) {
                Map<String, Object> card = setResult.get(i);
                Map<String, Object> addMap = new HashMap<>();
                String offeringId = "";
                String offeringName = "";
                String apnName = "";
                double totalAmount = 0.0;
                double useAmount = 0.0;
                double remainAmount = 0.0;
                double percentage = 0.0;

                //格式化数据
                switch (cd_code){
                    case "YiDong_EC_Combo":

                        offeringId = card.get("offeringId")!=null?card.get("offeringId").toString():offeringId;
                        offeringName = card.get("offeringName")!=null?card.get("offeringName").toString():offeringName;
                        apnName = card.get("apnName")!=null?card.get("apnName").toString():apnName;

                        totalAmount = card.get("totalAmount")!=null?Double.parseDouble(card.get("totalAmount").toString()):totalAmount;
                        useAmount = card.get("useAmount")!=null?Double.parseDouble(card.get("useAmount").toString()):useAmount;
                        remainAmount = card.get("remainAmount")!=null?Double.parseDouble(card.get("remainAmount").toString()):remainAmount;
                        //kb 转 MB
                        totalAmount = Arith.formatToTwo(Arith.div(totalAmount,1024));
                        useAmount = Arith.formatToTwo(Arith.div(useAmount,1024));
                        remainAmount = Arith.formatToTwo(Arith.div(remainAmount,1024));
                        break;
                    case "YiDong_ECv2_Combo":
                        offeringId = card.get("prodid")!=null?card.get("prodid").toString():offeringId;
                        offeringName = card.get("prodname")!=null?card.get("prodname").toString():offeringName;
                        apnName = card.get("apnname")!=null?card.get("apnname").toString():apnName;

                        totalAmount = card.get("total")!=null?Double.parseDouble(card.get("total").toString()):totalAmount;//MB
                        useAmount = card.get("used")!=null?Double.parseDouble(card.get("used").toString()):useAmount;//KB
                        remainAmount = card.get("left")!=null?Double.parseDouble(card.get("left").toString()):remainAmount;//KB

                        useAmount = Arith.formatToTwo(Arith.div(useAmount,1024));
                        remainAmount = Arith.formatToTwo(Arith.div(remainAmount,1024));
                        break;
                }
                if(useAmount>0.0){
                    percentage = Arith.formatToTwo(Arith.mul(Arith.div(useAmount,totalAmount),100));
                }else {
                    percentage = 0;
                }
                addMap.put("offeringId",offeringId);
                addMap.put("offeringName",offeringName);
                addMap.put("apnName",apnName);
                addMap.put("totalAmount",totalAmount);
                addMap.put("useAmount",useAmount);
                addMap.put("remainAmount",remainAmount);
                addMap.put("percentage",percentage);
                card_arrs.add(addMap);
            }

            List<Map<String, Object>> ex_arrs = new ArrayList<>();
            for (int i = 0; i < card_arrs.size(); i++) {
                Map<String, Object> findMap = card_arrs.get(i);
                findMap.put("iccid",iccid);
                Map <String,Object> isMap =   yzCardUsageReminderMapper.is_ex(findMap);
                if(isMap!=null && isMap.get("iccid")!=null){
                    ex_arrs.add(isMap);
                }
            }


            List<Map<String, Object>> updarrs = new ArrayList<>();
            List<Map<String, Object>> addarrs = new ArrayList<>();


            if(ex_arrs!=null && ex_arrs.size()>0){


                for (int i = 0; i < card_arrs.size(); i++) {
                    Map<String, Object> c_obj = card_arrs.get(i);
                    String c_offeringId = c_obj.get("offeringId").toString();
                    boolean bool = false;
                    for (int j = 0; j < ex_arrs.size(); j++) {
                        Map<String, Object> ex_obj = ex_arrs.get(j);
                        String ex_iccid = ex_obj.get("iccid").toString();
                        String ex_offeringId = ex_obj.get("offeringId").toString();
                        if(iccid.equals(ex_iccid) && c_offeringId.equals(ex_offeringId)){
                            updarrs.add(c_obj);
                            bool = true;
                            break;
                        }
                    }
                    if(!bool){
                        addarrs.add(c_obj);
                    }
                }
            }else {
                addarrs.addAll(card_arrs);
            }
            int saveCount = 0;
            int updCount = 0;
            if(addarrs!=null && addarrs.size()>0){
                Map<String, Object> addMap = new HashMap<>();
                addMap.put("card_arrs",addarrs);
                addMap.put("iccid",iccid);
                saveCount =  yzCardUsageReminderMapper.save(addMap);
            }
            if(updarrs!=null && updarrs.size()>0){
                for (int i = 0; i < updarrs.size(); i++) {
                    Map<String, Object> updMap = updarrs.get(i);
                    updMap.put("iccid",iccid);
                    updCount +=  yzCardUsageReminderMapper.upd(updMap);
                }
            }
            log.info("iccid {} : 新增套餐 {} 修改套餐 {} ",iccid,saveCount,updCount);
        }

    }


    public  double double_format(double value, int retain) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(retain, RoundingMode.HALF_UP);
        return Double.parseDouble(bd.toString());
    }


}
