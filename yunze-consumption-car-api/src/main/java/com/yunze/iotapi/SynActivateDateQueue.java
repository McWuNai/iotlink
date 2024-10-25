package com.yunze.iotapi;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.utils.InternalApiRequest;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 激活时间补偿获取
 * @Auther: zhang feng
 * @Date: 2022/01/27/22:37
 * @Description:
 */
@Slf4j
@Component
public class SynActivateDateQueue {


    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private RedisCache redisCache;

    @RabbitHandler
    @RabbitListener(queues = "admin_getSynActivateDate_queue")
    public void SynActivateDate(String msg, Channel channel) throws IOException {
        ListeneCommonr(msg,channel,"admin_getSynActivateDate_queue");
    }

    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_getSynActivateDate_queue")
    public void DelSynActivateDate(String msg, Channel channel) throws IOException {
        ListeneCommonr(msg,channel,"dlx_admin_getSynActivateDate_queue");
    }


    /**
     * 监听公用
     * @param msg
     * @param channel
     */
    private void ListeneCommonr(String msg, Channel channel,String ListenePrefix){
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            String iccid = Pmap.get("iccid")!=null?Pmap.get("iccid").toString():"";
            String prefix = ListenePrefix+iccid;
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix);
            if(isExecute==null){
                redisCache.setCacheObject(prefix, msg,10 , TimeUnit.SECONDS);//10 秒缓存 避免 重复消费
                SynExAcd(Pmap);
            }
        }catch (Exception e){
            log.error(">> iotApi 激活时间补偿获取 消费者:{}<<", e.getMessage().toString());
        }
    }




    private void SynExAcd(Map<String, Object> Parammap) {
        Map<String, Object> Route  = yzCardMapper.findRoute(Parammap);
        if(Route!=null ){
            String cd_status = Route.get("cd_status").toString();
            if(cd_status!=null && cd_status!="" && cd_status.equals("1")){
                String activate_date = Route.get("activate_date")!=null?Route.get("activate_date").toString():"";
                if(activate_date.length()>0){
                    log.info("  activate_date.length()>0 {} | Route {}  ",JSON.toJSONString(Parammap),JSON.toJSONString(Route));
                }else{
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
                            if(activateDate!=null && activateDate.length()>0 ){
                                Upd_Map.put("activate_date",activateDate);
                            }
                            if(openDate!=null){
                                Upd_Map.put("open_date",openDate);
                            }
                            Upd_Map.put("iccid",Parammap.get("iccid").toString());
                            try {
                                boolean bool = yzCardMapper.updActivate(Upd_Map)>0;//变更卡状态
                                if(bool){
                                    log.info("已成功同步激活时间！");
                                }else{
                                    log.error("保存激活时间异常:False！");
                                }
                            }catch (Exception e){
                                log.error("DB保存状态异常！"+e.getMessage().toString());
                            }
                        }else{
                            //System.out.println(Rmap);
                            log.error("上游接口返回数据异常,稍后重试！");
                        }
                    }else{
                        log.error("网络繁忙稍后重试！"+Rmap.get("Message").toString());
                    }
                }
            }else{
                String statusVal = cd_status.equals("2")?"已停用":cd_status.equals("3")?"已删除":"状态未知";
                log.error("同步激活时间 异常！"+" 通道 ["+statusVal+"]");
            }
        }else{
            log.error(" iccid ["+Parammap.get("iccid")+"] 未划分 API通道 ！请划分通道后重试！");
        }

    }




}
