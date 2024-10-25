package com.yunze.card.apisyn;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.mapper.yunze.YzCardMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取 开卡日期
 */
@Slf4j
@Component
public class CardGetOpenDate {

    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardMapper yzCardMapper;

    /**
     * @param msg
     * @param channel_1
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_CardGetOpenDate_queue",containerFactory = "customContainerFactory")
    public void SynOffering(String msg, Channel channel_1) {
        if(msg!=null && msg.length()>0){
            Offering(msg);
        }
    }


    /**
     * 死信队列
     * @param msg
     * @param channel_1
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_CardGetOpenDate_queue",containerFactory = "customContainerFactory")
    public void dlxSynOffering(String msg, Channel channel_1)  {
        if(msg!=null && msg.length()>0){
            Offering(msg);
        }
    }


    /**
     * 同步 获取 开卡日期
     * @param msg
     * @throws IOException
     */
    public void Offering(String msg) {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Parammap = JSON.parseObject(msg);
            //System.out.println(Parammap);
            Map<String, Object> Route  = yzCardMapper.findRoute(Parammap);
            String opType = Parammap.get("opType").toString();// 更新激活时间 还是 开卡时间
            if(Route!=null ){
                String cd_status = Route.get("cd_status").toString();
                if(cd_status!=null && cd_status!="" && cd_status.equals("1")){
                    Map<String,Object> Rmap =  internalApiRequest.queryCardActiveTime(Parammap,Route);
                    String code =  Rmap.get("code")!=null?Rmap.get("code").toString():"500";
                    if(code.equals("200")){
                        //获取 激活时间
                        if(Rmap.get("activateDate")!=null &&  Rmap.get("activateDate")!="" &&  Rmap.get("activateDate").toString().trim().length()>0 ||
                                Rmap.get("openDate")!=null &&  Rmap.get("openDate")!="" &&  Rmap.get("openDate").toString().trim().length()>0){
                            Object activateDate_obj = Rmap.get("activateDate")!=null &&  Rmap.get("activateDate")!="" &&  Rmap.get("activateDate").toString().trim().length()>0?
                                    Rmap.get("activateDate").toString().trim():"";
                            String activateDate = activateDate_obj.toString().trim();
                            activateDate = activateDate.length()>10?activateDate.substring(0,10):activateDate;
                            Object openDate = Rmap.get("openDate")!=null &&  Rmap.get("openDate")!="" &&  Rmap.get("openDate").toString().trim().length()>0?
                                    Rmap.get("openDate").toString().trim():null;
                            Map<String,Object> Upd_Map = new HashMap<>();
                            if(activateDate!=null && activateDate.length()>0 && opType.equals("activate")){
                                Upd_Map.put("activate_date",activateDate);
                            }
                            if(openDate!=null){
                                Upd_Map.put("open_date",openDate);
                            }
                            Upd_Map.put("iccid",Parammap.get("iccid").toString());
                            try {
                                boolean bool = yzCardMapper.updActivate(Upd_Map)>0;//变更激活时间 开卡日期
                                if(bool){
                                   log.info(" 成功 同步激活时间 {}   ",JSON.toJSONString(Upd_Map));
                                }else{
                                   log.error(" 失败 同步激活时间 {}  ",JSON.toJSONString(Upd_Map));
                                }
                            }catch (Exception e){
                                log.error("DB保存状态操作失败！"+e.getMessage());
                            }
                        }else{
                            log.error("上游接口返回数据操作失败，{}",JSON.toJSONString(Rmap));
                        }
                    }else{
                        log.error("网络繁忙稍后重试！{}",JSON.toJSONString(Rmap));
                    }
                }else{
                    String statusVal = cd_status.equals("2")?"已停用":cd_status.equals("3")?"已删除":"状态未知";
                    log.error("同步激活时间 操作失败！ "+" 通道 ["+statusVal+"]");
                }
            }else{
                log.error(" iccid [{}] 未划分 API通道 ！请划分通道后重试！",Parammap.get("iccid"));
            }
        } catch (Exception e) {
            log.error(">>错误 - 同步 获取 开卡日期 消费者:{}<<", e.getMessage());
        }
    }


}
