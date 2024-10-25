package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * 修改 卡 消费 者
 */
@Slf4j
@Component
public class CardUpd {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;


    /**
     * 修改 Card
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_CardUpd_queue", containerFactory = "customContainerFactory")
    public void CardUpd(String msg, Channel channel) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> Pmap = JSON.parseObject(msg);
            execute(Pmap);
        } catch (Exception e) {
            log.error(">>错误 - 修改 admin_CardUpd_queue 消费者:{}<<", e.getMessage().toString());
        }
    }


    /**
     * 修改 Card
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_CardUpd_queue", containerFactory = "customContainerFactory")
    public void dlx_CardUpd(String msg, Channel channel) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String, Object> Pmap = JSON.parseObject(msg);
            execute(Pmap);
        } catch (Exception e) {
            log.error(">>错误 - 修改 dlx_admin_CardUpd_queue 消费者:{}<<", e.getMessage().toString());
        }
    }


    public void execute( Map<String, Object> Pmap) {

        String queueTypeName = Pmap.get("queueTypeName").toString();
        String iccid = Pmap.get("iccid").toString();
        int upd = 0;
        if(queueTypeName.equals("admin_CardUpdUsed_queue")){
            upd = yzCardMapper.updUsed(Pmap);
        }else if(queueTypeName.equals("admin_CardUpdActivate_queue")){
            upd = yzCardMapper.updActivate(Pmap);
        }else if(queueTypeName.equals("admin_CardUpdStatusId_queue")) {
            Object status_id = Pmap.get("status_id");
            String status_ShowId = getShowStatIdArr.GetShowStatId(status_id.toString());
            Pmap.put("status_ShowId",status_ShowId);
            upd = yzCardMapper.updStatusId(Pmap);
        }else if(queueTypeName.equals("admin_CardAddInfoRemaining_queue")){
            upd = yzCardMapper.addInfoRemaining(Pmap);
        }else if(queueTypeName.equals("admin_CardUpdimei_queue")){
            upd = yzCardMapper.updimei(Pmap);
        }
        log.info(">> {} 执行结果 - 修改 {} 消费者:{}<<",iccid,queueTypeName,upd);
    }
}



