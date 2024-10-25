package com.yunze.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.card.YzCardApiOfferinginfolistMapper;
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
 * @Date: 2022/04/08/15:41
 * @Description: 同步上游返回套餐记录信息
 */
@Slf4j
@Component
public class SynApiOfferinginfolist {

    @Resource
    private YzCardApiOfferinginfolistMapper yzCardApiOfferinginfolistMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_sendSynApiOfferinginfolist_queue",containerFactory = "customContainerFactory")
    public void SynApiOfferinginfolist(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }

            Map<String,Object> Pmap = JSON.parseObject(msg);
            String iccid = Pmap.get("iccid").toString();//
            String cd_code = Pmap.get("cd_code").toString();//
            List<Map<String, Object>> setResult = (List<Map<String, Object>>) Pmap.get("setResult");
            CardSynEnforce(iccid,cd_code,setResult);
        }catch (Exception e){
            log.error(">>错误 - SynApiOfferinginfolist 消费者:{}<<", e.getMessage().toString());
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_sendSynApiOfferinginfolist_queue",containerFactory = "customContainerFactory")
    public void dlxSynApiOfferinginfolist(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            String iccid = Pmap.get("iccid").toString();//
            String cd_code = Pmap.get("cd_code").toString();//
            List<Map<String, Object>> setResult = (List<Map<String, Object>>) Pmap.get("setResult");
            CardSynEnforce(iccid,cd_code,setResult);
        }catch (Exception e){
            log.error(">>错误 - dlxSynApiOfferinginfolist 消费者:{}<<", e.getMessage().toString());
        }
    }

    @RabbitListener(queues = "dlx_admin_sendSynApiOfferinginfolist_queue")
    public void dlxSynApiOfferinginfolist_1(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            String iccid = Pmap.get("iccid").toString();//
            String cd_code = Pmap.get("cd_code").toString();//
            List<Map<String, Object>> setResult = (List<Map<String, Object>>) Pmap.get("setResult");
            CardSynEnforce(iccid,cd_code,setResult);
        }catch (Exception e){
            log.error(">>错误 - dlxSynApiOfferinginfolist 消费者:{}<<", e.getMessage().toString());
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
                String effectiveDate = "";
                String expiriedDate = "";

                //格式化数据
                if (cd_code.equals("YiDong_EC")   || cd_code.equals("YiDong_EC_Combo") ) {
                    offeringId = card.get("offeringId")!=null?card.get("offeringId").toString():offeringId;
                    offeringName = card.get("offeringName")!=null?card.get("offeringName").toString():offeringName;
                    apnName = card.get("apnName")!=null?card.get("apnName").toString():apnName;
                    effectiveDate = card.get("effectiveDate")!=null?card.get("effectiveDate").toString():effectiveDate;
                    expiriedDate = card.get("expiriedDate")!=null?card.get("expiriedDate").toString():expiriedDate;
                }else if (cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo")) {
                    offeringId = card.get("prodid")!=null?card.get("prodid").toString():offeringId;
                    offeringName = card.get("prodname")!=null?card.get("prodname").toString():offeringName;
                    apnName = card.get("apnname")!=null?card.get("apnname").toString():apnName;
                    effectiveDate = card.get("prodinstefftime")!=null?card.get("prodinstefftime").toString():effectiveDate;
                    expiriedDate = card.get("prodinstexptime")!=null?card.get("prodinstexptime").toString():expiriedDate;
                }


                addMap.put("offeringId",offeringId);
                addMap.put("offeringName",offeringName);
                addMap.put("apnName",apnName);
                addMap.put("effectiveDate",effectiveDate);
                addMap.put("expiriedDate",expiriedDate);
                card_arrs.add(addMap);
            }
            List<Map<String, Object>> updarrs = new ArrayList<>();
            List<Map<String, Object>> addarrs = new ArrayList<>();
            for (int i = 0; i < card_arrs.size(); i++) {
                Map<String, Object> findMap = card_arrs.get(i);
                findMap.put("iccid",iccid);
                Map <String,Object> isMap =   yzCardApiOfferinginfolistMapper.is_ex(findMap);
                if(isMap!=null && isMap.get("iccid")!=null){
                    updarrs.add(findMap);
                }else{
                    addarrs.add(findMap);
                }
            }

            int saveCount = 0;
            int updCount = 0;
            if(addarrs!=null && addarrs.size()>0){
                Map<String, Object> addMap = new HashMap<>();
                addMap.put("card_arrs",addarrs);
                addMap.put("iccid",iccid);
                addMap.put("cd_code",cd_code);
                saveCount =  yzCardApiOfferinginfolistMapper.save(addMap);
            }
            if(updarrs!=null && updarrs.size()>0){
                for (int i = 0; i < updarrs.size(); i++) {
                    Map<String, Object> updMap = updarrs.get(i);
                    updMap.put("iccid",iccid);
                    updMap.put("cd_code",cd_code);
                    updCount +=  yzCardApiOfferinginfolistMapper.upd(updMap);
                }
            }
            log.info("iccid {} : 新增套餐 {} 修改套餐 {} ",iccid,saveCount,updCount);
        }

    }




}
