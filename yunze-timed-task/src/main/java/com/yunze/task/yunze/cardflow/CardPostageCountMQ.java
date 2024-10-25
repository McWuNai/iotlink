package com.yunze.task.yunze.cardflow;

import com.yunze.common.mapper.yunze.YzAgentPackageMapper;
import com.yunze.common.mapper.yunze.YzCardPackageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CardPostageCountMQ {


    @Resource
    private YzCardPackageMapper yzCardPackageMapper;
    @Resource
    private YzAgentPackageMapper yzAgentPackageMapper;

    @Transactional
    @RabbitHandler
    @RabbitListener(queues = "admin_synchronizationP_queue")
    public void synchronization() {
        Map<String, Object> Rmap = new HashMap<>();

        List<Map<String, Object>> Cardlist = yzCardPackageMapper.cardCount(Rmap);//平台资费组
        List<Map<String, Object>> AgentList = yzAgentPackageMapper.agentCount(Rmap);//代理资费组

        if (Cardlist != null && AgentList != null) {

            for (int i = 0; i < Cardlist.size(); i++) {
                Map<String, Object> obj = Cardlist.get(i);
                if (obj.get("package_id") != null) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.putAll(obj);
                    objectMap.put("COUNT",null);
                    Integer integer = yzCardPackageMapper.upCountC(objectMap); //进行修改 平台资费组 卡总数
                    log.info(integer + "平台资费组--->清空总数");
                }
                if(obj.get("package_id") != null){
                    Integer integer = yzCardPackageMapper.upCountC(obj); //进行修改 平台资费组 卡总数
                    log.info(integer + "平台资费组--->数据操作成功");
                }
            }

            for (int i = 0; i < AgentList.size(); i++) {
                Map<String, Object> obj = AgentList.get(i);
                if (obj.get("package_id") != null) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.putAll(obj);
                    objectMap.put("COUNT",null);
                    Integer integer = yzAgentPackageMapper.upCountA(objectMap); //进行修改 代理资费组 卡总数
                    log.info(integer + "代理资费组--->清空总数");
                }
                if(obj.get("package_id") != null){
                    Integer integer = yzAgentPackageMapper.upCountA(obj); //进行修改 代理资费组 卡总数
                    log.info(integer + "代理资费组--->数据操作成功");
                }
            }

        }
    }

}
