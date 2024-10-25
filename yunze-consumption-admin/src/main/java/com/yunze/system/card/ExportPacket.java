package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.WriteCSV;
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
 * 资费计划 导出
 * */
@Slf4j
@Component
public class ExportPacket {

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;


    @RabbitHandler
    @RabbitListener(queues = "admin_ExportPacket_queue")
    public void ExportPacket(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
    /*        Map<String, Object> map = (Map<String, Object>) Pmap.get("map");
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");*/
            List<Map<String, Object>> outDataArr = (List<Map<String, Object>>) Pmap.get("outDataArr");

            List<Map<String, Object>> customize_whether = (List<Map<String, Object>>) Pmap.get("customize_whether");

            String newName = (String) Pmap.get("newName");
            Map<String,Object> task_map = (Map<String, Object>) Pmap.get("task_map");

            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            OutDataCard(outDataArr,newName,task_map,customize_whether);

        }catch (Exception e){
            log.error(">>错误 - 资费计划 消费者:{}<<", e.getMessage().toString());
        }

    }

    private void OutDataCard(List<Map<String, Object>> outDataArr, String newName, Map<String, Object> task_map,List<Map<String, Object>> customize_whether) {


        List<Map<String,Object>> out_list = new ArrayList<Map<String,Object>>();
        //循环添加单卡数据信息
        for (int i = 0; i < outDataArr.size(); i++) {
            Map<String,Object> CarMap =  outDataArr.get(i);

            //写入 在售
            CarMap  =  WriteDic(CarMap,customize_whether,"in_stock","in_stock_value");
            //写入 拆分到账
            CarMap  =  WriteDic(CarMap,customize_whether,"is_month","is_month_value");
            //写入 可微信支付
            CarMap  =  WriteDic(CarMap,customize_whether,"wechat_pay","wechat_pay_value");
            //删除导出不需要的字段
            CarMap.remove("packet_valid_type");
            CarMap.remove("wechat_pay");
            CarMap.remove("in_stock");
            CarMap.remove("is_month");
            out_list.add(CarMap);
        }


        try {
            WriteOutCard(out_list,newName,task_map);
        }catch (Exception e){
            log.error("导出资费计划异常 [导出数据 Exception] "+e.getCause().toString() );
        }
    }

    private void WriteOutCard(List<Map<String, Object>> out_list, String newName, Map<String, Object> task_map) {

        String Outcolumns[] = {"资费组编号","资费组名称","资费组别称",     "资费计划编号","资费计划名称","资费计划别称","售价","成本","可微信支付","规格","规格类型","在售","拆分到账", "卡总数",         "所属公司","所属人","创建时间"};
        String keys[] = {"package_id","package_name","package_agentname",   "packet_id","packet_name","packet_wx_name","packet_price","packet_cost","wechat_pay_value","packet_valid_time","packet_valid_name","in_stock_value","is_month_value","card_count",      "dept_name","nick_name","create_time"};


        writeCSV.Write(newName,out_list,Outcolumns,null,keys);
        yzExecutionTaskMapper.set_end_time(task_map);//任务结束

    }



    /**
     * 字典获取
     * @param CarMap 写入Map
     * @param DictDataArr 字典 list
     * @param basis 获取依据 字段 与 dictValue 判断
     * @param fieldName 返回字段名称
     * @return
     */
    public Map<String,Object> WriteDic (Map<String,Object> CarMap,List<Map<String, Object>> DictDataArr,String basis,String fieldName){

        Map<String,Object> Rmap = new HashMap<String,Object>();
        String status_id = CarMap.get(basis)!=null?CarMap.get(basis).toString():null;
        boolean bool = false;
        if(status_id!=null){
            for (int i = 0; i < DictDataArr.size(); i++) {
                Map<String, Object>  dictData =  DictDataArr.get(i);
                String dictValue = dictData.get("dictValue").toString();
                if(dictValue.equals(status_id)){
                    CarMap.put(fieldName,dictData.get("dictLabel"));
                    bool = true;
                    break;
                }
            }
        }
        //字段 默认值
        if(!bool){
            CarMap.put(fieldName,"");
        }
        return CarMap;
    }


}
