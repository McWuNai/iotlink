package com.yunze.task.yunze.card;

import com.yunze.common.mapper.yunze.YzCardInfoChangeMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务 yz_card_info_change 删除 系统参数配置下 数据
 */
@Slf4j
@Component
public class CardInfoChangeTaskMQ {

    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private YzCardInfoChangeMapper yzCardInfoChangeMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_cardInfoChange_queue")
    public void ClearUp() {
        Map<String,Object> passwordMap = new HashMap<>();

        passwordMap.put("config_key","yunze.cardInfoChange.LongDay");
        String longDay = yzWxByProductAgentMapper.findConfig(passwordMap);
        longDay = longDay!=null?longDay:"90";//默认仅保存90天数据
        Map<String,Object> delMap = new HashMap<>();
        String delTime = VeDate.getNextDay( VeDate.getStringDate(),"-"+longDay);
        delMap.put("delTime",delTime);
        int delCount = yzCardInfoChangeMapper.del(delMap);
        log.info("已删除 yz_card_info_change 数据 {} 条",delCount);
    }

}
