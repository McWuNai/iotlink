package com.yunze.system.card;

import com.yunze.common.mapper.yunze.YzCardBriefMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.VeDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CardBriefMQ {

    @Resource
    private YzCardBriefMapper yzCardBriefMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_BatchByBatchAdd_queue")
    public void CardAdd() {

        System.out.println("================= admin_BatchByBatchAdd_queue【Start"+ VeDate.getNow() +"】 ================= ");
        Map<String, Object> map=new HashMap<>();
        Integer currenPage= 0;
        Integer pageSize= 500;
        Integer rowCount = yzCardBriefMapper.selMapCount();
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu =new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类

        int saveCount = 0;
        int StarRow = pu.getStarRow();//开始行数
        for (int i = 0; i < pu.getPageCount(); i++) { //总页数
            //数据打包
            map.put("StarRow", StarRow);
            map.put("PageSize", pu.getPageSize());
            List<String> list = yzCardBriefMapper.selList(map);
            if(list != null && list.size()>0){
                Map<String,Object> objectMap = new HashMap<>();
                objectMap.put("cardArr",list);
                saveCount += yzCardBriefMapper.batchCard(objectMap);
                StarRow +=1;
            }

        }
        System.out.println();
        System.out.println("================= admin_BatchByBatchAdd_queue【End "+ VeDate.getNow() +" 新增卡号 "+saveCount+" 】 ================= ");

    }
}
