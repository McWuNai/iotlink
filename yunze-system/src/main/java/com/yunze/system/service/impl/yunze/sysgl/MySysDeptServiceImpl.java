package com.yunze.system.service.impl.yunze.sysgl;

import com.alibaba.fastjson.JSON;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.mapper.yunze.wechat.YzWxConfigMapper;
import com.yunze.common.utils.yunze.XmlUtil;
import com.yunze.system.config.SysWxPayServiceInterface;
import com.yunze.system.service.yunze.sysgl.IMySysDeptService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Service
public class MySysDeptServiceImpl implements IMySysDeptService {


    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private YzWxConfigMapper yzWxConfigMapper;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private SysWxPayServiceInterface sysWxPayServiceInterface;


    @Override
    public String updCredit(Map<String, Object> map, SysUser operateMap) {
        String Message = "";
            //修改企业金额信息 路由队列
            String polling_queueName = "admin_updDeptAmount_queue";
            String polling_routingKey = "admin.updDeptAmount.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "updCredit");//启动类型
                start_type.put("Pmap", map);//请求参数
                start_type.put("operateMap", operateMap);//操作人信息
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
                Message = "企业授信 已授信额度操作 指令发送成功！请稍后刷新查看";
            } catch (Exception e) {
                System.out.println("导入 卡号 失败 " + e.getMessage());
                Message =  "企业授信 已授信额度操作 操作失败！";
            }
        return Message;
    }

    @Override
    public Map<String, Object> initiatePreSave(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<>();
        String Message = "";boolean bool = false;

        WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest=null;
        WxPayUnifiedOrderResult wxPayUnifiedOrderResult= null;
        if( map.get("deptId")!=null && map.get("deptId").toString().length()>0 && map.get("deposit")!=null && map.get("deposit").toString().length()>0){
            String deptName = map.get("deptName").toString();
            Double paySum = Double.parseDouble(map.get("deposit").toString());
            String deptId = map.get("deptId").toString();
            String appId= null;
            String nativeUrl = null;
            String productId = String.valueOf(System.currentTimeMillis());
            String ordNo = VeDate.getNo(8);
            try {
                String body = "企业预存编号-"+deptId;
                //获取收款的 appid 统一默认为 平台代理 公众号 100

                Map<String, Object> findMap = new HashMap<>();
                findMap.put("agent_id","100");


                Map<String, Object> wxConfig = yzWxConfigMapper.findOne(findMap);
                if(null==wxConfig){
                    Message = "未找到收款方，请联系管理员进行配置！";
                }
                appId = wxConfig.get("app_id").toString();
                nativeUrl = wxConfig.get("native_url").toString();

                //添加订单生成参数
                Map<String, Object> OrderMap = new HashMap<>();

                OrderMap.put("ord_type", 4);//订单类型 企业预存 4  预存抵扣 6
                OrderMap.put("ord_name", "企业预存");//交易名称 商品购买
                OrderMap.put("wx_ord_no", null);//微信订单号
                OrderMap.put("status", "0");//支付状态 未支付
                OrderMap.put("price", paySum);//交易金额
                OrderMap.put("account", "0");//交易后金额
                OrderMap.put("pay_type", "wx");//支付方式 微信支付
                OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
                OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
                OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
                OrderMap.put("open_id", null);//openid 微信支付记录操作人
                OrderMap.put("agent_id", deptId);// 所属记录企业id
                OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
                OrderMap.put("iccid", "");
                OrderMap.put("ord_no", ordNo);
                OrderMap.put("info", "");

                //生成交易订单
                if(yzOrderMapper.save(OrderMap)<=0){
                    Message = "发起支付失败 步骤 1";
                    Rmap.put("bool",bool);
                    Rmap.put("Msg",Message);
                    return Rmap;
                }
                wxPayUnifiedOrderRequest = WxPayUnifiedOrderRequest
                        .newBuilder()
                        //订单号
                        .outTradeNo(ordNo)
                        //金额
                        .totalFee(yuanToFee(new BigDecimal(paySum)))
                        //描述
                        .body(body)
                        //ip
                        .spbillCreateIp(InetAddress.getLoopbackAddress().getHostAddress())
                        .notifyUrl(nativeUrl)
                        .productId(productId)
                        .build();
                //微信支付参数初始化
                sysWxPayServiceInterface.afterPropertiesSet();

            } catch (NumberFormatException e) {
                System.out.println("微信支付开始调起异常 NumberFormatException----------------");
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Exception "+e.getMessage());
            }
            try {
                wxPayUnifiedOrderResult= sysWxPayServiceInterface
                        .getWxPayService(appId)
                        .unifiedOrder(wxPayUnifiedOrderRequest);
            } catch (WxPayException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("微信支付调起失败");
            } catch (Exception e) {
                e.printStackTrace();
            }
            String codeUrl = wxPayUnifiedOrderResult.getCodeURL();
            Rmap.put("Data",codeUrl);
            Rmap.put("ordNo",ordNo);
            bool = true;
            Message = "操作成功！";
        }else{
            Message = "操作参数不全，请刷新后重试！";
        }

        Rmap.put("bool",bool);
        Rmap.put("Msg",Message);
        return Rmap;
    }

    /**
     * 预存回调
     * @param xmlData
     * @return
     */
    public String weChatNotify(String xmlData,String ip){
        String outOrdNo = "";
        try {
            System.out.println("=================开始回调："+xmlData);

            Map<String, Object> findMap = new HashMap<>();
            findMap.put("agent_id","100");

            Map<String, Object> wxConfig = yzWxConfigMapper.findOne(findMap);

            final WxPayOrderNotifyResult notifyResult = SysWxPayServiceInterface
                    .wxPayServiceMap
                    .get(wxConfig.get("app_id").toString())
                    .parseOrderNotifyResult(xmlData);
            //这里是存储我们发起支付时订单号的信息，所以取出来
            String wxOrdNo = notifyResult.getTransactionId();

            Map<String,Object> map = XmlUtil.xmlToMap(xmlData);
            System.out.println("解析后的返回MAP："+map);
            outOrdNo = map.get("out_trade_no").toString();
            Map<String, Object> getOrderMap = new HashMap<>();
            getOrderMap.put("ord_no",outOrdNo);
            Map<String,Object> order =  yzOrderMapper.getOrderBriefly(getOrderMap);
            if(null==order){
                System.out.println("未找到订单号对应订单 "+outOrdNo);
                return WxPayNotifyResponse.fail("回调失败！");
            }
            int status = Integer.parseInt(order.get("status").toString());
            String agentId = order.get("agent_id").toString();
            if(status==0 && order!=null){
                //修改状态
                Map<String, Object> updMap = new HashMap<>();
                updMap.put("status","1");
                updMap.put("wx_ord_no",wxOrdNo);
                updMap.put("id",order.get("id"));
                boolean updBool = yzOrderMapper.updStatus(updMap)>0;
                //企业预存增加余额
                try {
                    Map<String, Object> sendMap = new HashMap<>();
                    Map<String, Object> Pmap = new HashMap<>();
                    Pmap.put("operateType","add");
                    Pmap.put("deposit_amount",order.get("price"));
                    Pmap.put("dept_id",agentId);
                    Pmap.put("agent_id",agentId);
                    Map<String, Object> operateMap = new HashMap<>();
                    operateMap.put("ip",ip);

                    sendMap.put("Pmap",Pmap);
                    sendMap.put("operateMap",operateMap);
                    //修改企业金额信息 路由队列
                    String polling_routingKey = "admin.UpdDepositAmount.queue";
                    String polling_exchangeName = "admin_exchange";//路由

                    rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(sendMap), message -> {
                        // 设置消息过期时间 60 分钟 过期
                        message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                        return message;
                    });
                } catch (Exception e) {
                    System.out.println("admin_UpdDepositAmount_queue 发送 失败 "+e.getMessage() );
                }
                //成功后回调微信信息
                if(updBool){
                    return WxPayNotifyResponse.success("回调成功！");
                }else{
                    return WxPayNotifyResponse.fail("回调失败！");
                }
            }
            return WxPayNotifyResponse.fail("回调有误!");
        } catch (Exception e) {
            System.out.println("微信回调异常："+e.getMessage());
            return WxPayNotifyResponse.fail("回调有误!");
        }
    }

    @Override
    public Map<String, Object> queryWxOrder(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<>();
        String Message = "";boolean bool = false;
        String ordNo = map.get("ord_no").toString();
        Map<String,Object> order =  yzOrderMapper.getOrderBriefly(map);
        if(order!=null){
            Map<String, Object> findMap = new HashMap<>();
            findMap.put("agent_id","100");
            Map<String, Object> wxConfig = yzWxConfigMapper.findOne(findMap);
            if(null!=wxConfig){
                WxPayOrderQueryResult wxPayOrderQueryResult = null;
                try {
                    wxPayOrderQueryResult =
                            sysWxPayServiceInterface.getWxPayService(wxConfig.get("app_id").toString()).queryOrder("",ordNo);
                } catch (WxPayException e) {
                    System.out.println(e.getMessage());
                }
                String trade_state=null;
                if(null!=wxPayOrderQueryResult) {
                    trade_state = wxPayOrderQueryResult.getTradeState();

                    if ("SUCCESS".equals(trade_state)) {
                        bool = true;
                        Message = "支付成功，等待页面跳转";
                        String outOrdNo = wxPayOrderQueryResult.getOutTradeNo();
                        String wxOrdNo = wxPayOrderQueryResult.getTransactionId();
                        try {
                            updPrestore(outOrdNo, wxOrdNo, "");
                        }catch (Exception e){
                            System.out.println(" SUCCESS = Exception "+e.getMessage());
                        }
                    }else {
                        bool = false;
                        if ("REFUND".equals(trade_state)) {
                            Message = "转入退款";
                        }else if ("NOTPAY".equals(trade_state)) {
                            Message = "未支付";
                        }else if ("CLOSED".equals(trade_state)) {
                            Message = "已关闭";
                        }else if ("REVOKED".equals(trade_state)) {
                            Message = "已撤销（付款码支付）";
                        }else if ("USERPAYING".equals(trade_state)) {
                            Message = "用户支付中（付款码支付）";
                        }else if ("PAYERROR".equals(trade_state)) {
                            Message = "支付失败";
                        }
                    }
                }
            }else{
                Message = "未找到收款方，请联系管理员进行配置！";
            }
        }else{
            Message = "未找到订单信息";
        }
        Rmap.put("bool",bool);
        Rmap.put("Msg",Message);
        return Rmap;
    }

    @Override
    public Map<String, Object> sysPreSaved(Map<String, Object> map) {
        return saveOperation(map,"add","4","企业预存-人工操作");
    }

    @Override
    public Map<String, Object> sysDeduct(Map<String, Object> map) {
        return saveOperation(map,"sub","6","预存抵扣-人工操作");
    }


    /**
     * 预存操作
     * @param map
     * @return
     */
    private Map<String, Object> saveOperation(Map<String, Object> map,String operateType,String ord_type,String ord_name) {
        Map<String, Object> Rmap = new HashMap<>();
        String Message = "";boolean bool = false;

        String ordNo = VeDate.getNo(8);
        //添加订单生成参数
        Map<String, Object> OrderMap = new HashMap<>();

        String paySum = map.get("paySum").toString();
        String deptId = map.get("deptId").toString();
        String ip = map.get("ip").toString();
        String info = map.get("info").toString();

        OrderMap.put("ord_type", ord_type);//订单类型 企业预存 4  预存抵扣 6
        OrderMap.put("ord_name", ord_name);//交易名称 商品购买
        OrderMap.put("wx_ord_no", null);//微信订单号
        OrderMap.put("status", "1");//支付状态 未支付
        OrderMap.put("price", paySum);//交易金额
        OrderMap.put("account", "0");//交易后金额
        OrderMap.put("pay_type", "al");//支付方式 微信支付
        OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
        OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
        OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
        OrderMap.put("open_id", null);//openid 微信支付记录操作人
        OrderMap.put("agent_id", deptId);// 所属记录企业id
        OrderMap.put("cre_type", "ai");//生成方式  系统 sys 人工 ai
        OrderMap.put("iccid", "");
        OrderMap.put("ord_no", ordNo);
        OrderMap.put("info", info);
        if(yzOrderMapper.save(OrderMap)>0){
            //企业预存增加余额
            try {
                Map<String, Object> sendMap = new HashMap<>();
                Map<String, Object> Pmap = new HashMap<>();
                Pmap.put("operateType",operateType);
                Pmap.put("deposit_amount",paySum);
                Pmap.put("dept_id",deptId);
                Pmap.put("agent_id",deptId);
                Map<String, Object> operateMap = new HashMap<>();
                operateMap.put("ip",ip);

                sendMap.put("Pmap",Pmap);
                sendMap.put("operateMap",operateMap);
                //修改企业金额信息 路由队列
                String polling_routingKey = "admin.UpdDepositAmount.queue";
                String polling_exchangeName = "admin_exchange";//路由

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(sendMap), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
                Message = "订单已生成，修改预存指令已发送！";
                bool = true;
            } catch (Exception e) {
                Message = "修改预存金额指令发送失败……";
                System.out.println("admin_UpdDepositAmount_queue 发送 失败 "+e.getMessage() );
            }
        }else{
            Message = "订单新增失败……";
        }

        Rmap.put("bool",bool);
        Rmap.put("Msg",Message);
        return Rmap;
    }














    /**
     * 修改预存发送
     * @param outOrdNo
     * @param wxOrdNo
     */
    public void updPrestore(String outOrdNo,String wxOrdNo,String ip) {
        Map<String, Object> getOrderMap = new HashMap<>();
        getOrderMap.put("ord_no", outOrdNo);
        Map<String, Object> order = yzOrderMapper.getOrderBriefly(getOrderMap);
        if (null == order) {
            System.out.println("未找到订单号对应订单 " + outOrdNo);
        }
        int status = Integer.parseInt(order.get("status").toString());
        String agentId = order.get("agent_id").toString();
        if (status == 0 && order != null) {
            //修改状态
            Map<String, Object> updMap = new HashMap<>();
            updMap.put("status", "1");
            updMap.put("wx_ord_no", wxOrdNo);
            updMap.put("id", order.get("id"));
            boolean updBool = yzOrderMapper.updStatus(updMap) > 0;
            //企业预存增加余额
            try {
                Map<String, Object> sendMap = new HashMap<>();
                Map<String, Object> Pmap = new HashMap<>();
                Pmap.put("operateType", "add");
                Pmap.put("deposit_amount", order.get("price"));
                Pmap.put("dept_id", agentId);
                Pmap.put("agent_id", agentId);
                Map<String, Object> operateMap = new HashMap<>();
                operateMap.put("ip", ip);

                sendMap.put("Pmap", Pmap);
                sendMap.put("operateMap", operateMap);
                //修改企业金额信息 路由队列
                String polling_routingKey = "admin.UpdDepositAmount.queue";
                String polling_exchangeName = "admin_exchange";//路由

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(sendMap), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("admin_UpdDepositAmount_queue 发送 失败 " + e.getMessage());
            }
        }
    }

    /**
     * 描述： 1元钱转为 100分
     */
    private int yuanToFee(BigDecimal bigDecimal) {
        return bigDecimal.multiply(new BigDecimal(100)).intValue();
    }
}
