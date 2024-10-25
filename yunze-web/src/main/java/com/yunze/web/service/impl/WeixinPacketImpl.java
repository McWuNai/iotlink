package com.yunze.web.service.impl;


import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.alibaba.fastjson.JSON;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.util.SignUtils;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.business.token.TokenProcess;
import com.yunze.cn.mapper.*;
import com.yunze.cn.util.CardFlowSyn;
import com.yunze.cn.util.CheckIsNull;
import com.yunze.cn.util.RedisUtil_Yz;
import com.yunze.web.config.WxPayServiceInterface;
import com.yunze.web.service.IWeixinPacket;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.cn.config.RabbitMQConfig;
import com.yunze.cn.config.YzWxConfigInit;
import com.yunze.cn.service.impl.YzWeChatUserImpl;
import com.yunze.cn.service.impl.YzCardServiceImpl;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.*;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/26/8:35
 * @Description:
 */
@Component
public class WeixinPacketImpl implements IWeixinPacket {

    @Resource
    private CardFlowSyn cardFlowSyn;
    @Resource
    private YzCardServiceImpl yzCardServiceImpl;
    @Resource
    private RedisUtil_Yz redisUtilYz;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzAgentPacketMapper yzAgentPacketMapper;
    @Resource
    private YzCardPacketMapper yzCardPacketMapper;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    @Resource
    private YzWxConfigMapper yzWxConfigMapper;
    @Resource
    private WxPayServiceInterface wxPayServiceInterface;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzWeChatUserImpl yzWeChatUserImpl;
    @Resource
    private YzWxConfigInit yzWxConfigInit;
    @Resource
    private TokenProcess tokenProcess;
    @Resource
    private YzWeChatUserMapper yzWeChatUserMapper;



    //执行 加包 队列信息
    String addPackage_exchangeName = "polling_addPackage_card", addPackage_queueName = "p_addPackage_card", addPackage_routingKey = "p.addPackage.card",
            addPackage_del_exchangeName = "dlx_"+addPackage_exchangeName,addPackage_del_queueName = "dlx_"+addPackage_queueName, addPackage_del_routingKey = "dlx_"+addPackage_routingKey;



    @Override
    public Map<String, Object> queryFlowInfo(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "200");
        String openid = Pmap.get("openid").toString();
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);

        if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            String iccid = IccidMap.get("iccid").toString();
            if (IccidMap.get("status_id") != null) {
                //获取字典卡状态
                String cardStatus = getdictLabel("yunze_card_status_ShowId", IccidMap.get("status_id").toString());
                cardStatus = cardStatus != null ? cardStatus : "未知";
                Rmap.put("cardStatus", cardStatus);
            }
            Rmap.put("iccid", iccid);
            String EndTime = "-";
            Object obj = redisUtilYz.redisTemplate.opsForValue().get(iccid + openid);
            if (obj!=null) {
                Double used = IccidMap.get("used") == null ? 0.00 : Double.parseDouble(IccidMap.get("used").toString());
                Double remain = IccidMap.get("remaining") == null ? 0.00 : Double.parseDouble(IccidMap.get("remaining").toString());
                Rmap.put("used", used);
                Rmap.put("remain", remain);
                Rmap.put("EndTime", obj);
                if (used == -1.00) {
                    Rmap.put("total", 0.00);
                } else {
                    Rmap.put("total", used + remain);
                }
                return Rmap;
            } else {
                EndTime = yzCardFlowMapper.findEndTime(IccidMap);
                redisUtilYz.setExpire(iccid + openid, EndTime, 60);
            }
            Double R_used = 0.0;
            Double R_remain = 0.0;
            Double total = 0.0;
            Object channel_id = IccidMap.get("channel_id");
            if (channel_id != null && channel_id.toString().length() > 0) {
                Map<String, Object> Route = yzCardRouteMapper.find_route(Rmap);
                if (Route != null) {
                    String cd_status = Route.get("cd_status").toString();
                    if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                        Map<String, Object> synMap = synCardFlow(iccid, Route);
                        if (synMap != null) {
                            if (synMap.get("used") != null) {
                                R_used = Double.parseDouble(synMap.get("used").toString());
                            }
                            if (synMap.get("remaining") != null) {
                                R_remain = Double.parseDouble(synMap.get("remaining").toString());
                            }
                            if (synMap.get("SumFlow") != null) {
                                total = Double.parseDouble(synMap.get("SumFlow").toString());
                            }

                        } else {
                            Rmap.put("Message", "同步数据 上游返回异常，稍后重试 ");
                        }
                    } else {
                        String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                        Rmap.put("Message", "同步数据 取消 通道 " + statusVal);
                        return Rmap;
                    }
                }
            }



            Rmap.put("EndTime", EndTime);
            Rmap.put("used", R_used);
            Rmap.put("remain", R_remain);
            Rmap.put("total", total);
            Rmap.put("code", "200");
        } else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！信息同步取消！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> getWxPackets(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "200");
        String openid = Pmap.get("openid").toString();
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        List<Map<String, Object>> cardPackets = null;//
        List<Map<String, Object>> new_cardPackets = new ArrayList<Map<String, Object>>();

        if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            if (IccidMap.get("package_id") != null && IccidMap.get("package_id").toString().length() > 0) {
                String package_id = IccidMap.get("package_id").toString();
                String agent_id = IccidMap.get("agent_id").toString();
                Map<String, Object> findMap = new HashMap<>();
                findMap.putAll(Pmap);
                findMap.put("package_id", package_id);
                findMap.put("dept_id", agent_id);

                //仅充值一次 包 过滤 是否 已经 充值过
                if (agent_id.equals("100")) {
                    List<String> notInArr =  yzCardPacketMapper.findWebOnlyOnce(findMap);
                    if(notInArr!=null && notInArr.size()>0){
                        findMap.put("notInArr", notInArr);
                    }
                    cardPackets = yzCardPacketMapper.findWebRecharge(findMap);
                } else {
                    List<String> notInArr =  yzAgentPacketMapper.findWebOnlyOnce(findMap);
                    if(notInArr!=null && notInArr.size()>0){
                        findMap.put("notInArr", notInArr);
                    }
                    cardPackets = yzAgentPacketMapper.findWebRecharge(findMap);
                }

                String day = VeDate.getStringDateShort().split("-")[2];
                day = day.substring(0, 1).equals("0") ? day.substring(1, 2) : day;
                //不可充值日期判断 删除
                if (cardPackets != null && cardPackets.size() > 0) {
                    for (int i = 0; i < cardPackets.size(); i++) {
                        Map<String, Object> cp = cardPackets.get(i);
                        Object DateLimit = cp.get("date_limit");
                        if (DateLimit != null && DateLimit.toString() != "" && DateLimit.toString().length() > 0) {
                            if (DateLimit.toString().indexOf(",") != -1) {
                                String[] prohibit = DateLimit.toString().split(",");
                                boolean add = true;
                                for (int j = 0; j < prohibit.length; j++) {
                                    if (prohibit[j].equals(day)) {
                                        add = false;
                                    }
                                    if (!add) {
                                        break;
                                    }
                                }
                                if (add) {
                                    new_cardPackets.add(cp);
                                }
                            } else {
                                if(!day.equals(DateLimit)){
                                    new_cardPackets.add(cp);
                                }
                            }
                        } else {
                            new_cardPackets.add(cp);
                        }
                    }
                }
                Rmap.put("cardPackets", new_cardPackets);
            } else {
                Rmap.put("code", "500");
                Rmap.put("Message", "未划分资费组，请联系客服进行反馈！");
            }
        } else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！信息同步取消！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> querPayType(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "200");
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        Map<String, Object> cardPacket = null;//
        if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            if (IccidMap.get("package_id") != null && IccidMap.get("package_id").toString().length() > 0) {
                String package_id = IccidMap.get("package_id").toString();
                String agent_id = IccidMap.get("agent_id").toString();
                Map<String, Object> findMap = new HashMap<>();
                findMap.putAll(Pmap);
                findMap.put("package_id", package_id);
                findMap.put("dept_id", agent_id);
                if (agent_id.equals("100")) {
                    cardPacket = yzCardPacketMapper.findPayType(findMap);
                } else {
                    cardPacket = yzAgentPacketMapper.findPayType(findMap);
                }
                Rmap.put("cardPacket", cardPacket);
            } else {
                Rmap.put("code", "500");
                Rmap.put("Message", "未划分资费组，请联系客服进行反馈！");
            }
        } else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！操作取消！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> weixinPay(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "500");
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(map);
        Map<String, Object> cardPacket = null;//
        if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            String iccid = IccidMap.get("iccid").toString();
            System.out.println("微信发起支付获取参数：" + map);
            String appId = null;
            WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest = null;
            WxPayUnifiedOrderResult wxPayUnifiedOrderResult = null;
            String mchKey = null;

            String days = VeDate.getYyyyAndMm()[0];
            try {
                String body = map.get("body").toString();
                String monthType = map.get("monthType").toString();
                String priceStr = map.get("price").toString();
                Double price = Double.parseDouble(priceStr);
                String base_packet_type = "";
                //页面获取appid，为了区分不同公众号支付
                appId = map.get("appId").toString();
                //根据前端传过来的openid
                String openid = map.get("openid").toString();
                if (CheckIsNull.isNull(iccid) || CheckIsNull.isNull(body) || CheckIsNull.isNull(price)) {
                    Rmap.put("Message", "获取资费计划参数失败！操作取消！");
                    return Rmap;
                }
                String ordNo = VeDate.getNo(8);
                String ordName = null;
                Integer recordType = null;
                Integer ordType = null;
                String packet_type = null;
                switch (body) {
                    case "Renewal_recharge":
                        //TODO:基础套餐一月允许加一次
                        ordName = "微信资费订购";
                        recordType = 1;
                        ordType = 2;
                        //查询当前  充值资费计划信息
                        String agent_id = IccidMap.get("agent_id").toString();
                        Map<String, Object> findMap = new HashMap<>();
                        map.put("package_id", IccidMap.get("package_id"));
                        findMap.putAll(map);
                        findMap.put("dept_id", agent_id);
                        findMap.put("agent_id", agent_id);
                        if (agent_id.equals("100")) {
                            cardPacket = yzCardFlowMapper.FindPacket(findMap);
                        } else {
                            cardPacket = yzCardFlowMapper.FindPacketAgent(findMap);
                        }
                        if (cardPacket == null) {
                            Rmap.put("Message", "获取 充值资费计划详情数据失败！操作取消！请联系客户反馈！");
                            return Rmap;
                        }
                        packet_type = cardPacket.get("packet_type").toString();//资费计划类型：0 基础资费 -1 叠加资费
                         base_packet_type = cardPacket.get("base_packet_type").toString();//资费计划 类型 0:仅一次，1可叠加

                        Map<String, Object> findFMap = new HashMap<>();
                        String Time = VeDate.getStringDate();
                        findFMap.put("iccid", iccid);
                        findFMap.put("Time", Time);

                        //仅一次【赠送】 充值 判断之前是否充值过
                        if(packet_type.equals("2")){
                            List<String> notInArr =  null;
                            if (agent_id.equals("100")) {
                                 notInArr =  yzCardPacketMapper.findWebOnlyOnce(findMap);
                            } else {
                                notInArr =  yzAgentPacketMapper.findWebOnlyOnce(findMap);
                            }
                            if(notInArr!=null && notInArr.size()>0){
                                Rmap.put("Message", "该资费类型仅为订购一次");
                                return Rmap;
                            }
                        }
                        if(monthType.equals("month")) {
                            Integer flag = yzCardFlowMapper.findBaseFlow(findFMap);//查询本月是否有未到期的基础包
                            //查询是否已经添加过本账单的基础包，无则提示先增加基础包
                            if (flag != null) {
                                if (packet_type.equals("1")) {
                                    if (flag <= 0) {
                                        Rmap.put("Message", "请先订购基础资费 ！");
                                        return Rmap;
                                    }
                                } else if (packet_type.equals("0") && base_packet_type.equals("0")) {//基础包 仅一次
                                    if (flag > 0) {
                                        Rmap.put("Message", "基础资费，同一账期只能订购一个！");
                                        return Rmap;
                                    }
                                } else {
                                    if (base_packet_type.equals("4")) {
                                        Rmap.put("Message", "激活生效 资费不可订购！");
                                        return Rmap;
                                    }
                                }
                            } else {
                                if (packet_type.equals("1")) {
                                    Rmap.put("Message", "请先订购基础资费 ！");
                                    return Rmap;
                                }
                            }
                        }else if(monthType.equals("next")){
                            //资费预购 判断 ( month 当月续费  next 资费预购)
                            if(packet_type.equals("0")){
                                if(packet_type.equals("0") && base_packet_type.equals("0")){//基础包 仅一次 判断 下月 里是否已经订购过
                                    String now = Time.substring(0,10);;//当前时间
                                    String NextMonth = VeDate.getFirstDayOfNextMonth(now,"yyyy-MM-dd")+" 00:00:01";
                                    findFMap.put("Time", NextMonth);
                                    Integer NextFlag = yzCardFlowMapper.findBaseFlow(findFMap);//查询 下月 是否有未到期的基础包
                                    if(NextFlag!=null && NextFlag>0){
                                        Rmap.put("Message", "重复预购！");
                                        return Rmap;
                                    }
                                }
                            }else if(packet_type.equals("0")){//叠加包 不允许 预购
                                Rmap.put("Message", "叠加资费，不能预购 ！");
                                return Rmap;
                            }

                        }





                        //判断是否限制加包
                        if (!CheckIsNull.isNull(cardPacket.get("date_limit"))) {
                            String dateLimit = cardPacket.get("date_limit").toString();
                            if (dateLimit.contains(",")) {
                                String[] dates = dateLimit.split(",");
                                for (int i = 0; i < dates.length; i++) {
                                    String da = dates[i];
                                    if (days.equals(da)) {
                                        Rmap.put("Message", "订购资费 今日限购！");
                                        return Rmap;
                                    }
                                }
                            } else {
                                if (days.equals(dateLimit)) {
                                    Rmap.put("Message", "订购资费 今日限购！详情请咨询客服。");
                                    return Rmap;
                                }
                            }
                        }
                        break;
                }
                //获取代理商直属id
                String agent_id = IccidMap.get("agent_id").toString();

                String ord_type = "2";
                //添加订单生成参数
                Map<String, Object> OrderMap = new HashMap<>();

                String validate_type = cardPacket.get("wxValidType").toString();//生效类型 默认=设置时生效类型
                if(monthType.equals("next")){//资费预购 点击进来的
                    if (packet_type.equals("0")) {//基础仅一次 改为 下月生效
                        if (base_packet_type != null && base_packet_type.equals("0")) {
                            validate_type = "2";
                        }
                    }
                }

                //资费计划类型：0 基础资费 -1 叠加资费
                if (packet_type.equals("0")) {
                    if (base_packet_type != null && base_packet_type.equals("1")) {
                        validate_type = "3";//基础包 可叠加 时 生效 类型 默认改为 【周期延顺】
                    }
                } else if (packet_type.equals("1") || packet_type.equals("2")) {//赠送的 只能当月生效
                    validate_type = "1";//叠加包 默认为 【当月生效】
                }
                cardPacket.put("validate_type", validate_type);
                cardPacket.put("ord_type", ord_type);//订单类型

                OrderMap.put("add_parameter", JSON.toJSONString(cardPacket));//加包参数
                OrderMap.put("ord_type", ord_type);//订单类型 用量充值
                OrderMap.put("ord_name", "微信订购套餐");//交易名称 用量充值
                OrderMap.put("wx_ord_no", null);//微信订单号
                OrderMap.put("status", "0");//支付状态 未支付
                OrderMap.put("price", price);//交易金额
                OrderMap.put("account", "0");//交易后金额
                OrderMap.put("pay_type", "wx");//支付方式 微信支付
                OrderMap.put("is_profit", "1");//是否需要分润 0否-1是
                OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
                OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
                OrderMap.put("packet_id", cardPacket.get("packet_id"));//资费计划 ID
                OrderMap.put("validate_type", validate_type);//生效类型
                OrderMap.put("open_id", openid);//openid 微信支付记录操作人
                OrderMap.put("agent_id", agent_id);// 所属记录企业id
                OrderMap.put("profit_type", "2");//分润类型 差价
                OrderMap.put("info", null);//备注
                OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
                OrderMap.put("iccid", iccid);
                OrderMap.put("ord_no", ordNo);
                //生成交易订单
                //1.创建路由 绑定 生产队列 发送消息ord_type
                String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_WxWebOrder_queue", addOrder_routingKey = "admin.WxWebOrder.queue",
                        addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
                try {
                    //rabbitMQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                    Map<String, Object> MessageMap = new HashMap<>();
                    MessageMap.put("OrderMap", OrderMap);//参数
                    rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(MessageMap), message -> {
                        // 设置消息过期时间 60 分钟 过期
                        message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                        return message;
                    });
                } catch (Exception e) {
                    System.out.println("订单生产指令发送失败 " + e.getMessage());
                    Rmap.put("Message", "订单生产指令发送失败，支付取消！【请及时反馈问题给客服】。");
                    return Rmap;
                }
                Map<String, Object> BackUrlMap = new HashMap<>();
                BackUrlMap.put("app_id", appId);
                Map<String, Object> Wxconfig = getBackUrl(BackUrlMap);
                String back_url = Wxconfig.get("back_url").toString();
                wxPayUnifiedOrderRequest = WxPayUnifiedOrderRequest
                        .newBuilder()
                        //openid
                        .openid(openid)
                        //订单号
                        .outTradeNo(ordNo)
                        //金额
                        .totalFee(yuanToFee(new BigDecimal(price)))
                        //描述
                        .body(body)
                        //ip
                        .spbillCreateIp(InetAddress.getLoopbackAddress().getHostAddress())
                        .notifyUrl(back_url)
                        .build();
                System.out.println("回调路径：" + back_url);
                //微信支付参数初始化
                wxPayServiceInterface.afterPropertiesSet();
                mchKey = wxPayServiceInterface
                        .getWxPayService(appId)
                        .getConfig()
                        .getMchKey();
            } catch (NumberFormatException e) {
                System.out.println("微信支付开始调起异常----------------");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                wxPayUnifiedOrderResult = wxPayServiceInterface
                        .getWxPayService(appId)
                        .unifiedOrder(wxPayUnifiedOrderRequest);
            } catch (WxPayException e) {
                e.printStackTrace();
                throw new RuntimeException("微信支付调起失败");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //成功后组合参数构建支付
            Map<String, String> paySignInfo = new HashMap<>(5);
            String timeStamp = createTimestamp();
            String nonceStr = String.valueOf(System.currentTimeMillis());
            paySignInfo.put("appId", appId);
            paySignInfo.put("nonceStr", nonceStr);
            paySignInfo.put("timeStamp", timeStamp);
            paySignInfo.put("signType", "MD5");
            paySignInfo.put("package", "prepay_id=" + wxPayUnifiedOrderResult.getPrepayId());
            String[] ignore = null;
            String paySign = SignUtils.createSign(paySignInfo, "MD5",
                    mchKey, ignore);
            //组合支付参数
            Map<String, Object> DataMap = new HashMap<>();
            DataMap.put("appId", appId);
            DataMap.put("timeStamp", timeStamp);
            DataMap.put("nonceStr", nonceStr);
            DataMap.put("prepayId", paySignInfo.get("package"));
            DataMap.put("paySign", paySign);
            DataMap.put("signType", "MD5");

            Rmap.put("Data", DataMap);
        } else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！操作取消！");
        }
        Rmap.put("code", "200");

        return Rmap;
    }

    @Override
    public Map<String, Object> weChatNotify(String xmlData, String appId) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "500");
        try {
            System.out.println("===开始回调：" + xmlData);
            final WxPayOrderNotifyResult notifyResult = WxPayServiceInterface
                    .wxPayServiceMap
                    .get(appId)
                    .parseOrderNotifyResult(xmlData);
            //这里是存储我们发起支付时订单号的信息，所以取出来
            String ordNo = notifyResult.getOutTradeNo();
            String wxOrdNo = notifyResult.getTransactionId();
            /**
             * 系统内部业务，修改订单状态之类的
             */
            Map<String, Object> FindOrderMap = new HashMap<>();
            FindOrderMap.put("ord_no",ordNo);
            Map<String, Object>  order = yzOrderMapper.getOrderBriefly(FindOrderMap);
            int ordType = Integer.parseInt(order.get("ord_type").toString());
            String iccid = order.get("iccid").toString();
            String id = order.get("id").toString();
            int status = Integer.parseInt(order.get("status").toString());
            if (status == 0 && !CheckIsNull.isNull(ordNo)) {

                switch (ordType) {
                    case 1://充值
                        try {
                            /*int con = orderService.updateStatusByBal(ordNo, wxOrdNo);
                            if (con != 0) {
                                //查询支付之前的余额(
                                Gdlt gdlt = cardTypeService.queryZiBiaoByIccid(iccid, tableName);
                                Double qBalance = gdlt.getBalance() == null ? 0.00 : gdlt.getBalance();
                                int sum = cardTypeService.wxAddBalance(iccid, tableName, price);
                                int on = orderService.updateAccountByordNo(qBalance, price, ordNo);
                            }*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2://加包
                        //修改支付状态为 1.支付成功 赋值 微信订单号  2.生产加包任务指令发送
                        Map<String, Object> UpdMap = new HashMap<>();
                        UpdMap.put("id",id);
                        UpdMap.put("wx_ord_no",wxOrdNo);
                        UpdMap.put("status","1");// 支付成功
                        int count = yzOrderMapper.updStatus(UpdMap);
                        if (count > 0) {
                            Map<String,Object> findAddPackage_Map = new HashMap<>();
                            findAddPackage_Map.put("ordFId",id);
                            List<Map<String,Object>> AddPackageArr = yzOrderMapper.findAddPackage(findAddPackage_Map);
                            if(AddPackageArr!=null && AddPackageArr.size()>0){
                                if(AddPackageArr.size()!=1){
                                    System.out.println("----WXweb回调 查询到多条订单--【"+findAddPackage_Map.toString()+"】--"+AddPackageArr.size());
                                }
                                Map<String,Object> AddPackage = AddPackageArr.get(0);

                                //生产任务
                                try {
                                    //设置任务 路由器 名称 与队列 名称
                                    rabbitMQConfig.creatExchangeQueue(addPackage_exchangeName, addPackage_queueName, addPackage_routingKey, addPackage_del_exchangeName, addPackage_del_queueName, addPackage_del_routingKey,null);
                                    rabbitTemplate.convertAndSend(addPackage_exchangeName, addPackage_routingKey, JSON.toJSONString(AddPackage), message -> {
                                        // 设置消息过期时间 60 分钟 过期
                                        message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                                        return message;
                                    });
                                } catch (Exception e) {
                                    System.out.println("----WXweb回调 发送加包指令异常--"+e.getMessage());
                                }
                            }

                            //3.生产 订单生产后开机开网 诊断 功能 等 到 卡诊断修复 队列 （）
                            String card_exchangeName = "admin_card_exchange", card_queueName = "admin_CardDiagnosis_queue", card_routingKey = "admin.CardDiagnosis.queue",
                                    card_del_exchangeName = "dlx_"+card_exchangeName,card_del_queueName = "dlx_"+card_queueName, card_del_routingKey = "dlx_"+card_routingKey;
                            try {
                                rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                                Map<String,Object> CardDiagnosisMap = new HashMap<>();
                                List<String> operationArr = new  ArrayList<String>();
                                operationArr.add("PowerOn");//开机
                                operationArr.add("OpenNetwork");//开网
                                CardDiagnosisMap.put("optionType",operationArr);
                                CardDiagnosisMap.put("iccid",iccid);
                                CardDiagnosisMap.put("status_id",iccid);


                                rabbitTemplate.convertAndSend(card_exchangeName, card_routingKey, JSON.toJSONString(CardDiagnosisMap), message -> {
                                    // 设置消息过期时间 30 分钟 过期
                                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                                    return message;
                                });
                                System.out.println("微信回调 发送 卡诊断修复  指令成功 ！");
                            } catch (Exception e) {
                                System.out.println("微信回调 发送 卡诊断修复 指令失败 " + e.getMessage());
                            }
                        }
                        break;
                    case 3://商品购买
                        Map<String, Object> UpdMap_1 = new HashMap<>();
                        UpdMap_1.put("id",id);
                        UpdMap_1.put("wx_ord_no",wxOrdNo);
                        UpdMap_1.put("status","1");// 支付成功
                        int count_1 = yzOrderMapper.updStatus(UpdMap_1);
                        if (count_1 > 0) {
                            //1.生产 购买后邮件抄送 队列 （）
                            String card_exchangeName = "admin_CC_exchange", card_queueName = "admin_ShoppingEmail_queue", card_routingKey = "admin.ShoppingEmail.queue",
                                    card_del_exchangeName = "dlx_"+card_exchangeName,card_del_queueName = "dlx_"+card_queueName, card_del_routingKey = "dlx_"+card_routingKey;
                            try {
                                rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                                Map<String,Object> CCShoppingEmailMap = new HashMap<>();
                                CCShoppingEmailMap.put("id",id);
                                CCShoppingEmailMap.put("ord_no",ordNo);
                                rabbitTemplate.convertAndSend(card_exchangeName, card_routingKey, JSON.toJSONString(CCShoppingEmailMap), message -> {
                                    // 设置消息过期时间 30 分钟 过期
                                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                                    return message;
                                });
                                System.out.println("微信回调 发送 卡诊断修复  指令成功 ！");
                            } catch (Exception e) {
                                System.out.println("微信回调 发送 卡诊断修复 指令失败 " + e.getMessage());
                            }
                        }
                    case 4://运费支付
                        /*// 修改 换卡信息表 > 快递状态 审核中 3 》 状态 已换卡 1
                        int count_1 = orderService.updateStatusByPac(ordNo, wxOrdNo);
                        if (count_1 != 0) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("ord_no", ordNo);
                            cardReplaceService.update_status(map);
                        }*/
                        break;
                }
                //成功后回调微信信息
                Rmap.put("Message", "回调成功！");
            }
            Rmap.put("Message", "多次回调!");
        } catch (Exception e) {
            Rmap.put("Message", "回调有误!");
            System.out.println("微信回调异常：" + e.getMessage());
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> bindCard(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "500");
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        if (IccidMap != null && IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            if(IccidMap.get("del_flag")!=null){
                String xk_status = IccidMap.get("del_flag").toString();
                if(xk_status.equals("1")){
                    Rmap.put("Message", "绑定卡号系统未找到！");
                    return Rmap;
                }
            }
            String appId = Pmap.get("appid").toString();
            String openid = Pmap.get("openid").toString();
            String iccid = IccidMap.get("iccid").toString();
            //查询当前卡号是否已经绑定，若绑定了同一个公众号，则可以无限次绑定
            //若在已经绑定公众号的同时，想绑定另一个公众号，不允许
            Map<String,Object> findMap = new HashMap<>();
            findMap.put("iccid",iccid);
            findMap.put("openid",openid);
            Map<String,Object> wxUser =  yzWeChatUserImpl.find(findMap);
            Object bindAppid=null;
            if(wxUser!=null){
                bindAppid=wxUser.get("app_id");
                if(bindAppid!=null){
                    if(!appId.equals(bindAppid)){
                        Rmap.put("Message", "当前卡号已经绑定过公众号！");
                        return Rmap;
                    }
                }
            }else{
                // 获取微信用户基本信息
                Map<String,Object> ins = new HashMap<>();
                try {
                    WxMpService wxMpService = yzWxConfigInit.wxMpService().switchoverTo(appId);
                    WxMpUser userWxInfo = wxMpService.getUserService().userInfo(openid, null);
                    if (userWxInfo != null) {
                        ins.put("country",userWxInfo.getCountry());
                        ins.put("province",userWxInfo.getProvince());
                        ins.put("city",userWxInfo.getCity());
                        ins.put("head_image",userWxInfo.getHeadImgUrl());
                        ins.put("city",userWxInfo.getSex());
                        ins.put("user_name",userWxInfo.getNickname());
                    }
                } catch (Exception e) {
                    System.out.println("获取iccid绑定的微信信息异常："+e.getMessage());
                }
                ins.put("status",1);
                ins.put("iccid",iccid);
                ins.put("app_id",appId);
                ins.put("openid",openid);
                ins.put("bind_status",1);
                yzWeChatUserImpl.save(ins);
            }
            Map<String,Object> rmap=new HashMap<>();
            rmap.put("iccid",iccid);
            rmap.put("appId",appId);
            //rmap.put("paySign",paySign);
            //微信公众号缓存token
            String webToken =openid+"-_yunzeweb_-"+tokenProcess.makeToken();
            redisUtilYz.setKey(openid+"-yzAuth-web-token",webToken);
            rmap.put("webToken",webToken);
            //查找对应的openid，添加iccid，更改状态为1
            Map<String,Object> Updmap = new HashMap<>();
            Updmap.put("iccid",iccid);
            Updmap.put("bind_status",1);
            Updmap.put("openid",openid);
            if(yzWeChatUserImpl.upd(Updmap)>0){
                try {
                    Object obj = redisUtilYz.redisTemplate.opsForValue().get(iccid+"_cardType");
                    if(obj!=null){
                        rmap.put("cardType",obj);
                    }else{
                        String cd_operator_type = yzCardServiceImpl.findOperatorType(Pmap);
                        String cardType = "默认";
                        if(cd_operator_type!=null && cd_operator_type.length()>0){
                            cardType = cd_operator_type;
                        }
                        rmap.put("cardType",cardType);
                        try {
                            redisUtilYz.setExpire(iccid+"_cardType",cardType,60*5);
                        }catch (Exception e){
                            System.out.println(iccid+"redis存储_cardType 异常"+e.getMessage());
                        }
                    }
                }catch (Exception e){
                    System.out.println("获取 cardType  异常");
                }
                Rmap.put("Data", rmap);
                Rmap.put("code", "200");
                Rmap.put("Message", "操作成功！");
            }else{
                Rmap.put("Message", "绑定失败，请联系反馈客服！");
            }
        } else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！操作取消！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> unbind(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "500");
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        Map<String, Object> cardPacket = null;//
        if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            Map<String,Object> Updmap = new HashMap<>();
            Updmap.put("iccid","");
            Updmap.put("bind_status",0);
            Updmap.put("openid",Pmap.get("openid"));
            try {
                if(yzWeChatUserImpl.upd(Updmap)>0){
                    Rmap.put("code", "200");
                    Rmap.put("Message", "解绑成功！");
                }
            } catch (Exception e) {
                Rmap.put("Message", "解绑异常！"+ e.getMessage());
            }
        } else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！操作取消！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> goWebIndex(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Map<String,Object> map=new HashMap<>();
        Rmap.put("code", "500");
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        Map<String, Object> cardPacket = null;//
        if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            if(IccidMap.get("del_flag")!=null){
                String xk_status = IccidMap.get("del_flag").toString();
                if(xk_status.equals("1")){
                    Rmap.put("Message", "绑定卡号系统未找到！");
                    return Rmap;
                }
            }
            String iccid = IccidMap.get("iccid").toString();
            map.put("iccid",iccid);
            String cd_operator_type = yzCardServiceImpl.findOperatorType(map);
            String cardType = "默认";
            if(cd_operator_type!=null && cd_operator_type.length()>0){
                cardType = cd_operator_type;
            }
            map.put("cardType",cardType);
            try {
                redisUtilYz.setExpire(iccid+"_cardType",cardType,60*5);
            }catch (Exception e){
                System.out.println(iccid+"redis存储_cardType 异常"+e.getMessage());
            }
            Rmap.put("code", "200");
            Rmap.put("Data", map);
        } else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！操作取消！");
        }

        return Rmap;
    }

    @Override
    public Map<String, Object> checkBind(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Map<String,Object> map=new HashMap<>();
        Rmap.put("code", "200");
        Rmap.put("Data", null);
        Rmap.put("Message", "操作成功！");
        try {
            Map<String, Object> wxUser = yzWeChatUserMapper.findBindtatus(Pmap);
            if(null==wxUser){
                return Rmap;
            }
            String iccid = wxUser.get("iccid").toString();
            int bindStatus = Integer.parseInt(wxUser.get("bind_status").toString());
            if(com.yunze.business.util.CheckIsNull.isNull(iccid) ||bindStatus!=1){
                return Rmap;
            }else{
                map.put("iccid",iccid);
                map.put("bindStatus",bindStatus);
                Rmap.put("Data", map);
            }
        }catch (Exception e){
            Rmap.put("Message", "操作异常 "+e.getMessage());
        }
        return Rmap;
    }



    @Override
    public Map<String, Object> getDictionary(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Map<String,Object> map=new HashMap<>();
        Rmap.put("code", "200");
        Rmap.put("Message", "操作成功！");
        String dict_type = Pmap.get("dict_type").toString();
        List<Map<String, Object>> dict_dataArr = null;
        try {
            Object obj = redisUtilYz.redisTemplate.opsForValue().get(dict_type);
            if (obj != null) {
                dict_dataArr = (List<Map<String, Object>>) obj;
                if (dict_dataArr != null && dict_dataArr.size() > 0) {
                } else {
                    dict_dataArr = yzCardMapper.findDict(Pmap);
                }
            } else {
                dict_dataArr = yzCardMapper.findDict(Pmap);
            }
            Rmap.put("Data",dict_dataArr);
        } catch (Exception e) {
            System.out.println("{getDictionary 异常}" + e.getMessage());
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> getOrderFlow(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Map<String,Object> map=new HashMap<>();
        Rmap.put("code", "500");
        Map<String, Object> IccidMap = yzCardServiceImpl.findIccid(Pmap);
        Map<String, Object> cardPacket = null;//
        if (IccidMap.get("iccid") != null && IccidMap.get("iccid").toString().length() > 0) {
            String iccid = IccidMap.get("iccid").toString();
            try {
                Pmap.put("iccid",iccid);
                Rmap.put("Data", yzCardFlowMapper.webFindFlow(Pmap));
                Rmap.put("code", "200");
            }catch (Exception e){
                Rmap.put("Message", "操作异常 "+e.getMessage());
            }
        }else {
            Rmap.put("code", "500");
            Rmap.put("Message", "未找到号码！操作取消！");
        }
        return Rmap;
    }


    /**
     * 同步用量
     *
     * @param iccid
     * @param find_card_route_map
     * @throws IOException
     */
    public Map<String, Object> synCardFlow(String iccid, Map<String, Object> find_card_route_map) {
        try {
            if (StringUtils.isEmpty(iccid)) {
                return null;
            }
            Map<String, Object> Parammap = new HashMap<>();
            Parammap.put("iccid", iccid);
            Map<String, Object> Rmap = internalApiRequest.queryFlow(Parammap, find_card_route_map);
            String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
            if (code.equals("200")) {
                //获取 卡用量 开卡日期 更新 card info
                if (Rmap.get("Use") != null && Rmap.get("Use") != "" && Rmap.get("Use").toString().trim().length() > 0) {
                    Double Use = Double.parseDouble(Rmap.get("Use").toString());
                    if (Use >= 0) {
                        try {
                            Map<String, Object> RMap = cardFlowSyn.CalculationFlow(iccid, Use);
                            //Map<String,Object> RMap = cardFlowSyn.CalculationFlowQueue(iccid,Use);
                            return RMap;
                        } catch (Exception e) {
                            System.out.println(">>cardFlowSyn -  同步卡用量失败:{" + iccid + "} | {" + e.getMessage() + "}<<");
                        }
                    } else {
                        System.out.println(">>API -  未获取到卡用量 statusCode = 0 :{" + iccid + "} | {" + Rmap + "}<<");
                    }
                }
            } else {
                System.out.println(iccid + " 未获取到卡用量 ！" + Rmap);
            }
        } catch (Exception e) {
            System.out.println(">>错误 - web 卡用量:{synCardFlow}<<" + e.getMessage());
        }
        return null;
    }


    /**
     * 获取字典数据
     *
     * @param dict_type
     * @param dict_value
     * @return
     */
    public String getdictLabel(String dict_type, String dict_value) {
        Map<String, Object> findDict = new HashMap<>();
        findDict.put("dict_type", dict_type);
        String dict_label = null;
        List<Map<String, Object>> dict_dataArr = null;
        try {
            Object obj = redisUtilYz.redisTemplate.opsForValue().get(dict_type);
            if (obj != null) {
                dict_dataArr = (List<Map<String, Object>>) obj;
                if (dict_dataArr != null && dict_dataArr.size() > 0) {
                    Map<String, Object> KMap = getKey(dict_dataArr, dict_value);
                    dict_label = KMap.get("dict_label").toString();
                } else {
                    dict_dataArr = yzCardMapper.findDict(findDict);
                    if (dict_dataArr != null && dict_dataArr.size() > 0) {
                        Map<String, Object> KMap = getKey(dict_dataArr, dict_value);
                        dict_label = KMap.get("dict_label").toString();
                    }
                }
            } else {
                dict_dataArr = yzCardMapper.findDict(findDict);
                if (dict_dataArr != null && dict_dataArr.size() > 0) {
                    Map<String, Object> KMap = getKey(dict_dataArr, dict_value);
                    dict_label = KMap.get("dict_label").toString();
                    redisUtilYz.setExpire(dict_type, dict_dataArr, 60 * 60 * 6);
                }
            }
        } catch (Exception e) {
            System.out.println("{getdictLabel 异常}" + e.getMessage());
        }
        return dict_label;
    }








    /**
     * 获取list map 对应数据
     *
     * @param dict_dataArr
     * @param dict_value
     * @return
     */
    public Map<String, Object> getKey(List<Map<String, Object>> dict_dataArr, String dict_value) {
        if (dict_dataArr != null && dict_dataArr.size() > 0) {
            for (int i = 0; i < dict_dataArr.size(); i++) {
                Map<String, Object> obj = dict_dataArr.get(i);
                if (obj != null && obj.get("dict_value") != null) {
                    if (obj.get("dict_value").toString().equals(dict_value)) {
                        return obj;
                    }
                }
            }
        } else {
            return null;
        }
        return null;
    }


    /**
     * 描述： 1元钱转为 100分
     */
    private int yuanToFee(BigDecimal bigDecimal) {
        return bigDecimal.multiply(new BigDecimal(100)).intValue();
    }


    /**
     * 描述： 获取当前appid的微信回调URL
     */
    public Map<String, Object> getBackUrl(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<>();
        if (CheckIsNull.isNull(map)) {
            Rmap.put("back_url", "http://gtw.5iot.cn/web/weixin/pay/weixinPayNotify/wxb9c885c722d8cd36");
            return Rmap;
        }
        return yzWxConfigMapper.find(map);
    }


    /**
     * 描述： 时间戳
     */
    private String createTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }







}
