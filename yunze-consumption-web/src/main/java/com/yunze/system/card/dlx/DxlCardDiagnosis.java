package com.yunze.system.card.dlx;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.config.MyDictionary;
import com.yunze.common.core.card.CardChange;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 连接诊断  死信 消费者
 */
@Slf4j
@Component
public class DxlCardDiagnosis {


    @Resource
    private RedisCache redisCache;
    @Resource
    private CardChange cardChange;
    @Resource
    private MyDictionary myDictionary;
    @Resource
    private YzCardMapper yzCardMapper;


    /**
     * 连接诊断  死信 消费者
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_CardDiagnosis_queue")
    public void CardDiagnosis(String msg, Channel channel) throws IOException {
        Main(msg);
    }

    /**
     * 连接诊断  死信 消费者
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_CardDiagnosis_queue")
    public void CardDiagnosis_1(String msg, Channel channel) throws IOException {
        Main(msg);
    }

    /**
     * 连接诊断  死信 消费者
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_CardDiagnosis_queue")
    public void CardDiagnosis_2(String msg, Channel channel) throws IOException {
        Main(msg);
    }


    public void Main(String msg)  {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> map = JSON.parseObject(msg);
            String iccid = map.get("iccid").toString();//iccid
            List<String> operationArr = (List<String>) map.get("optionType");//诊断 操作类型

            String prefix = "dlx_admin_CardDiagnosis_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ iccid);
            if(isExecute==null){
                //System.out.println("SUCCESS");
                redisCache.setCacheObject(prefix+":"+ iccid, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                Diagnosis(iccid,operationArr);//执行导入
            }
        } catch (Exception e) {
            log.error(">>错误 - 批量导入卡 消费者:{}<<", e.getMessage().toString());
        }
    }


















    /**
     * 诊断 消费
     * @param iccid
     * @param operationArr
     */
    public void Diagnosis(String iccid, List<String> operationArr){
        Map<String,Object> Parammap = new HashMap<>();
        Parammap.put("iccid",iccid);

        if(operationArr!=null && operationArr.size()>0){
            Map<String,Object> map = yzCardMapper.findRoute(Parammap);
            for (int i = 0; i < operationArr.size(); i++) {
                String OPtion = operationArr.get(i);
                switch (OPtion){
                    case "PowerOn"://开机
                        Parammap.put("Is_Stop","on");
                        Open(iccid,Parammap,map);
                        break;
                    case "OpenNetwork"://开网
                        Parammap.put("Is_Break","0");
                        OpenConnection(iccid,Parammap,map);
                        break;
                }
            }
        }
    }




    /**
     * 开机
     * @param iccid
     * @param Parammap
     * @param map
     */
    public  void Open (String iccid,Map<String,Object> Parammap,Map<String,Object> map){
        String status_id = map.get("status_id")!=null &&map.get("status_id").toString().length()>0?map.get("status_id").toString():"未知";
        String cbefore = "";
        if(!status_id.equals("未知")){
            cbefore =  myDictionary.getdictLabel("yunze_card_status_ShowId",status_id);
        }else{
            cbefore = status_id;
        }
        cbefore = cbefore !=null && cbefore != "" && cbefore.length()>0?cbefore:"未知";
        Map<String,Object> Add_Map = new HashMap<>();
        Add_Map.put("iccid",iccid);
        Add_Map.put("ctype","2");//生命周期
        Add_Map.put("cbefore",cbefore);
        Add_Map.put("cafterward","开机");
        Add_Map.put("source_type","4");// 来源 类型 ：4 C端充值

        Map<String,Object> Upd_Map = new HashMap<>();
        Upd_Map.put("status_id","1");
        Upd_Map.put("iccid",iccid);
        cardChange.OnOff(Parammap,map,Add_Map,Upd_Map);
    }



    /**
     * 开网
     * @param iccid
     * @param Parammap
     * @param map
     */
    public  void OpenConnection (String iccid,Map<String,Object> Parammap,Map<String,Object> map){
        String connection_status = map.get("connection_status")!=null &&map.get("connection_status").toString().length()>0?map.get("connection_status").toString():"未知";
        String cbefore = "";
        if(!connection_status.equals("未知")){
            cbefore =  myDictionary.getdictLabel("yz_cardConnection_type",connection_status);
        }else{
            cbefore = connection_status;
        }
        cbefore = cbefore !=null && cbefore != "" && cbefore.length()>0?cbefore:"未知";
        Map<String,Object> Add_Map = new HashMap<>();
        Add_Map.put("iccid",iccid);
        Add_Map.put("ctype","1");//断开网状态
        Add_Map.put("cbefore",cbefore);
        Add_Map.put("cafterward","正常");
        Add_Map.put("source_type","4");// 来源 类型 ：4 C端充值

        Map<String,Object> Upd_Map = new HashMap<>();
        Upd_Map.put("connection_status","1");//正常
        Upd_Map.put("iccid",iccid);
        cardChange.ChangeConnection(Parammap,map,Add_Map,Upd_Map);
    }


}
