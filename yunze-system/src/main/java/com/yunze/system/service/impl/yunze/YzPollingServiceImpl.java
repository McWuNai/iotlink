package com.yunze.system.service.impl.yunze;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.polling.PollingMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYPollingService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@Service
public class YzPollingServiceImpl implements IYPollingService {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private PollingMapper pollingMapper;

    @Override
    public Map<String,Object> getList(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = pollingMapper.MapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", pollingMapper.getList(map));
        return  omp;
    }

    @Override
    public boolean plEx(Map map) {
        boolean bool = true;

        String polling_routingKey = "admin.FlowCompensateSend.queue";
        String polling_exchangeName = "admin_exchange";//路由
        String name = map.get("name").toString();
        String cd_id = map.get("cd_id").toString();
        if(name.equals("flow")){
            polling_routingKey = "admin.FlowCompensateSend.queue";
        }else if(name.equals("stats")){
            polling_routingKey = "admin.StateCompensateSend.queue";
        }else if(name.equals("activation")){
            polling_routingKey = "admin.pollingActivateDate.queue";
        }
        try {
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("day","1");
            start_type.put("time","30");
            start_type.put("FindCd_id",cd_id);
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            bool = false;
            System.out.println("指定通道轮询 指令发送失败 " + e.getMessage());
        }
        return bool;
    }

}
