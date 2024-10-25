package com.yunze.task.yunze.card;

import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.card.YzCardDueSoonMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.Email.EmailCc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2022年8月9日10:55:45
 * 监听 admin_CardDueSoon_queue
 */
@Slf4j
@Component
public class CardDueSoonMQ {

    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private YzCardDueSoonMapper yzCardDueSoonMapper;

    @Resource
    private EmailCc emailCc;


    @RabbitHandler
    @RabbitListener(queues = "admin_CardDueSoon_queue")
    public void cardDueSoon() throws Exception {
        HashMap<String, Object> configMap = new HashMap<>();
        configMap.put("config_key", "card.expiringSoonCount");
        String delay = yzWxByProductAgentMapper.findConfig(configMap);// 快到期 天数
        Map<String, Object> Pmap_expiringSoonCount = new HashMap<>();
        String record_date = VeDate.getStringDate();
        Pmap_expiringSoonCount.put("timetype", "5");
        Pmap_expiringSoonCount.put("staTime", record_date);
        String nextDay = VeDate.getNextDay(record_date, delay);
        nextDay = nextDay + " 23:59:59";
        Pmap_expiringSoonCount.put("endTime", nextDay);
        List<Map> maps = yzCardDueSoonMapper.SelectCardDueSoon(Pmap_expiringSoonCount);
        if (maps != null && maps.size()>0) {
            //判断条件,根据符合条件的进行发送邮件
            for (Map s : maps) {
                if (s.get("email") != null) {
                    Map<String, Object> sendEmailRes = emailCc.sendEmailForDueSoon(s, Pmap_expiringSoonCount);
                    if (sendEmailRes.get("bool").toString() == "true") {
                        //发送成功
                        // System.out.println(">>根据服务快到期进行发送邮件===>>>成功");
                        log.info(">>根据服务快到期进行发送邮件===>>>成功");
                    } else {
                        //发送失败
                        //System.out.println(">>根据服务快到期进行发送邮件===>>>失败");
                        log.error(">>根据服务快到期进行发送邮件===>>>失败");
                    }
                }
            }
        }else{
            log.info(">>根据服务快到期进行发送邮件==>>>查询到服务快到期的个数为: 0");
        }


    }


}
