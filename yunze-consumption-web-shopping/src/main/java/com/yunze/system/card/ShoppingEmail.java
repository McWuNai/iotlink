package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcHisMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcUrlMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByOrderMapper;
import com.yunze.common.utils.Email.EmailCc;
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
 * 微信 商品购买下单 支付后邮件推送 消费者【触发配置自动化规则 进行邮件推送】
 */
@Slf4j
@Component
public class ShoppingEmail {


    @Resource
    private RedisCache redisCache;
    @Resource
    private YzAutomationCcMapper yzAutomationCcMapper;
    @Resource
    private YzAutomationCcUrlMapper yzAutomationCcUrlMapper;
    @Resource
    private YzWxByOrderMapper yzWxByOrderMapper;
    @Resource
    private EmailCc emailCc;
    @Resource
    private YzAutomationCcHisMapper yzAutomationCcHisMapper;


    /**
     * 微信 商品购买下单 支付后邮件推送 消费者
     * @param msg
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_ShoppingEmail_queue")
    public void createListener(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> MsgMap = JSON.parseObject(msg);
            String id = MsgMap.get("id").toString();
            String ord_no = MsgMap.get("ord_no").toString();
            String prefix = "admin_ShoppingEmail_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+id+":"+ord_no);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+id+":"+ord_no, msg, 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                CCEmail(id,ord_no,MsgMap);//生成订单
            }
        } catch (Exception e) {
            log.error(">>错误 - 微信 商品购买下单 支付后邮件推送  消费者:{}<<", e.getMessage());
        }
    }


    /**
     * 抄送邮件
     * @param id
     * @param ord_no
     */
    public void CCEmail(String id,String ord_no,Map<String,Object> MsgMap){
        try {
            Map<String,Object> OrderMap = yzWxByOrderMapper.findConcise(MsgMap);
            if(OrderMap!=null){
                //获取 系统 自动化配置的 【购买下单邮件推送】
                Map<String,Object> findMap = new HashMap<>();
                findMap.put("trigger_type","99");
                List<Map<String, Object>> AutomationCcArr =  yzAutomationCcMapper.findConcise(findMap);//获取自动化 抄送 组
                if(AutomationCcArr!=null && AutomationCcArr.size()>0){
                    for (int i = 0; i < AutomationCcArr.size(); i++) {
                        Map<String, Object> CCObj = AutomationCcArr.get(i);
                        CCObj.put("ord_no",ord_no);
                        List<Map<String, Object>> AutomationCcUrlArr =  yzAutomationCcUrlMapper.findConcise(CCObj);//获取自动化 抄送 邮箱
                        if(AutomationCcUrlArr!=null && AutomationCcUrlArr.size()>0){
                            String execution_template = CCObj.get("execution_template").toString();
                            if(execution_template.equals("1")){
                                for (int j = 0; j < AutomationCcUrlArr.size(); j++) {
                                   Object eM =  AutomationCcUrlArr.get(j).get("email");
                                   if(eM!=null && eM.toString().length()>0){
                                       String SendEmail = eM.toString();
                                       Map<String,Object> saveHisMap = new HashMap<>();
                                       saveHisMap.put("cc_id",CCObj.get("id"));
                                       saveHisMap.put("cc_url","");
                                       saveHisMap.put("cc_email",SendEmail);
                                       saveHisMap.put("cc_state","1");//已抄送

                                        String cc_parameter = JSON.toJSONString(CCObj);
                                       cc_parameter = cc_parameter.length()>500?cc_parameter.substring(0,500):cc_parameter;
                                       saveHisMap.put("cc_parameter",cc_parameter);
                                       Map<String,Object> Rmap = null;
                                       Boolean bool = false;
                                       String remark = "",cc_result = "0";
                                       try {
                                           Rmap = emailCc.ShoppingDelivery_default(OrderMap,SendEmail);
                                           bool = (Boolean) Rmap.get("bool");
                                           if(bool){
                                               cc_result = "1";
                                           }
                                       }catch (Exception e){
                                           remark = e.getMessage();
                                           remark = remark.length()>240?remark.substring(0,240):remark;
                                       }
                                       saveHisMap.put("remark",remark);
                                       saveHisMap.put("cc_result",cc_result);
                                       boolean saveHis =  yzAutomationCcHisMapper.save(saveHisMap)>0;
                                       log.info(">>自动化 已抄送邮箱{}  - 插入抄送记录 bool   {} <<", SendEmail,saveHis);
                                   }else{
                                       log.error(">>自动化 抄送邮箱未获取到  - 抄送取消  {} <<", eM);
                                   }
                                }
                            }else{
                                log.error(">>自动化 抄送模板不批对  - 抄送取消  {} <<", MsgMap);
                            }
                        }else{
                            log.error(">>自动化 获取自动化 抄送 邮箱 未找到数据 - 抄送取消  {} <<", MsgMap);
                        }
                    }
                }else{
                    log.error(">>自动化 购买下单邮件推送 未找到数据 - 抄送取消  {} <<", MsgMap);
                }

            }else{
                log.error(">>未找到数据 - 下单抄送邮件  {} <<", MsgMap);
            }
        }catch (Exception e){
            log.error(">>异常 - 下单抄送邮件 {}<<",MsgMap,e.getMessage());
        }
    }





}
