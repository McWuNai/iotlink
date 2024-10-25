package com.yunze.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.util.SignUtils;
import com.yunze.web.config.WxPayServiceInterface;
import com.yunze.web.service.ICommodity;
import com.yunze.apiCommon.utils.Arith;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.cn.config.RabbitMQConfig;
import com.yunze.cn.mapper.YzWxConfigMapper;
import com.yunze.cn.mapper.commodity.YzWxByProductAgentMapper;
import com.yunze.cn.mapper.commodity.YzWxByProductCategoryMapper;
import com.yunze.cn.mapper.commodity.YzWxByProductInfoMapper;
import com.yunze.cn.mapper.commodity.YzWxByProductPicInfoMapper;
import com.yunze.cn.util.CheckIsNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2021/09/22/10:42
 * @Description:
 */
@Component
public class CommodityImpl implements ICommodity {

    @Resource
    private YzWxByProductCategoryMapper yzWxByProductCategoryMapper;
    @Resource
    private YzWxByProductInfoMapper yzWxByProductInfoMapper;
    @Resource
    private YzWxByProductPicInfoMapper yzWxByProductPicInfoMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private YzWxConfigMapper yzWxConfigMapper;
    @Resource
    private WxPayServiceInterface wxPayServiceInterface;

    @Override
    public Map<String, Object> QuerCategory(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        List<Map<String,Object>> Arr = new ArrayList<>();
        List<Map<String,Object>> ImgArr = new ArrayList<>();
        Rmap.put("code", "500");
        try {
            List<Map<String,Object>> CategoryArr = yzWxByProductCategoryMapper.findConcise(Pmap);
            String fieldValue = "",price = "",product_id = "";
            if(CategoryArr!=null && CategoryArr.size()>0){
                for (int i = 0; i < CategoryArr.size(); i++) {
                    List<Map<String,Object>> childrenArr = null;
                    Map<String, Object> Obj = CategoryArr.get(i);
                    if(Obj.get("category_id")!=null){
                        String category_id = Obj.get("category_id").toString();
                        Pmap.put("one_category_id",category_id);
                        childrenArr = yzWxByProductInfoMapper.findConcise(Pmap);
                        childrenArr = childrenArr!=null && childrenArr.size()>0?childrenArr:new ArrayList<>();
                        Obj.put("children",childrenArr);
                    }
                    Arr.add(Obj);
                    //查询第一条数据的商品图片
                    if(i == 0){
                        Map<String, Object> pObj = childrenArr.get(0);
                        if(pObj.get("product_id")!=null){
                            Pmap.put("product_id",pObj.get("product_id"));
                            ImgArr = yzWxByProductPicInfoMapper.findConcise(Pmap);
                        }
                        fieldValue =  Obj.get("text")+"/"+pObj.get("text");
                        price = pObj.get("price").toString();
                        product_id = pObj.get("product_id").toString();
                    }
                }
            }
            Rmap.put("Data", Arr);
            Rmap.put("imgData", ImgArr);
            Rmap.put("agentArr", yzWxByProductAgentMapper.findConcise(Pmap));
            Rmap.put("ByCardFare", yzWxByProductAgentMapper.findByCardFare());
            Rmap.put("fieldValue", fieldValue);
            Rmap.put("price", price);
            Rmap.put("product_id", product_id);

            Rmap.put("code", "200");
        }catch (Exception e){
            Rmap.put("Message", "获取数据异常！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> QuerImg(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        List<Map<String,Object>> ImgArr = new ArrayList<>();
        Rmap.put("code", "500");
        try {
            ImgArr = yzWxByProductPicInfoMapper.findConcise(Pmap);
            Rmap.put("Data", ImgArr);
            Rmap.put("code", "200");
        }catch (Exception e){
            Rmap.put("Message", "获取数据异常！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> PayCheck(Map<String, Object> Pmap) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "500");
        try {
            Map<String,Object> Product = yzWxByProductInfoMapper.findId(Pmap);
            Double paySum = 0.0;
            if(Product!=null){
                Double price = Double.parseDouble(Product.get("price").toString());
                int count = Integer.parseInt(Pmap.get("count").toString());
                String findByCardFare = yzWxByProductAgentMapper.findByCardFare();//运费
                Double ByCardFare = findByCardFare!=null && findByCardFare.length()>0?Double.parseDouble(findByCardFare):0.0;
                paySum = Arith.add(Arith.mul(price,count),ByCardFare);
            }else{
                Rmap.put("Message", "获取商品数据异常请刷新后重试！");
                return Rmap;
            }
            Rmap.put("paySum", paySum);
            Rmap.put("code", "200");
        }catch (Exception e){
            Rmap.put("Message", "获取数据异常！");
        }
        return Rmap;
    }

    @Override
    public Map<String, Object> ShoppingPay(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("code", "500");
            System.out.println("微信 ShoppingPay 发起支付获取参数：" + map);
            String appId = null;
            WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest = null;
            WxPayUnifiedOrderResult wxPayUnifiedOrderResult = null;
            String mchKey = null;

            try {
                String body = map.get("body").toString();
                String product_id = map.get("product_id").toString();
                String iccid = product_id;
                String shipping_user = map.get("userName").toString();
                String shipping_Phone = map.get("userPhone").toString();
                String province = map.get("province").toString();
                String city = map.get("city").toString();
                String district = map.get("district").toString();
                String address = map.get("DetailedAddress").toString();
                Map<String,Object> Product = yzWxByProductInfoMapper.findId(map);
                if(Product==null){
                    Rmap.put("Message", "支付商品信息未找到！支付取消！");
                    return Rmap;
                }
                Double price = Double.parseDouble(Product.get("price").toString());
                int count = Integer.parseInt(map.get("count").toString());
                String findByCardFare = yzWxByProductAgentMapper.findByCardFare();//运费
                Double ByCardFare = findByCardFare!=null && findByCardFare.length()>0?Double.parseDouble(findByCardFare):0.0;
                Double paySum = Arith.add(Arith.mul(price,count),ByCardFare);

                //页面获取appid，为了区分不同公众号支付
                appId = map.get("appId").toString();
                //根据前端传过来的openid
                String openid = map.get("openid").toString();

                String ordNo = VeDate.getNo(8);
                String ordName = null;
                Integer recordType = null;
                Integer ordType = null;
                String packet_type = null;

                //获取代理商直属id
                String agent_id = map.get("depId").toString();
                String info = map.get("info")!=null&&map.get("info").toString().length()>0?map.get("info").toString():"";
                String ord_type = "3";
                //添加订单生成参数
                Map<String, Object> OrderMap = new HashMap<>();

                OrderMap.put("ord_type", ord_type);//订单类型 商品购买
                OrderMap.put("ord_name", "微信商品购买");//交易名称 商品购买
                OrderMap.put("wx_ord_no", null);//微信订单号
                OrderMap.put("status", "0");//支付状态 未支付
                OrderMap.put("price", paySum);//交易金额
                OrderMap.put("account", "0");//交易后金额
                OrderMap.put("pay_type", "wx");//支付方式 微信支付
                OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
                OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
                OrderMap.put("show_status", "1");//是否显示交易记录:0否-1显示
                OrderMap.put("open_id", openid);//openid 微信支付记录操作人
                OrderMap.put("agent_id", agent_id);// 所属记录企业id
                OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai
                OrderMap.put("iccid", iccid);
                OrderMap.put("ord_no", ordNo);
                OrderMap.put("info", info);

                //订单发货表
                Map<String, Object> byOrderMap = new HashMap<>();
                byOrderMap.put("ord_no", ordNo);
                byOrderMap.put("openid", openid);//openid 微信支付记录操作人
                byOrderMap.put("shipping_user", shipping_user);
                byOrderMap.put("shipping_Phone", shipping_Phone);
                byOrderMap.put("province", province);
                byOrderMap.put("city", city);
                byOrderMap.put("district", district);
                byOrderMap.put("address", address);
                byOrderMap.put("shipping_money", ByCardFare);
                byOrderMap.put("by_count", count);
                byOrderMap.put("by_pid", product_id);

                //生成交易订单
                //1.创建路由 绑定 生产队列 发送消息ord_type
                String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_WxShopping_queue", addOrder_routingKey = "admin.WxShopping.queue",
                        addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
                try {
                    rabbitMQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                    Map<String, Object> MessageMap = new HashMap<>();
                    MessageMap.put("OrderMap", OrderMap);//订单
                    MessageMap.put("byOrderMap", byOrderMap);//订单发货表
                    rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(MessageMap), message -> {
                        // 设置消息过期时间 60 分钟 过期
                        message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                        return message;
                    });
                } catch (Exception e) {
                    System.out.println(" admin_WxShopping_queue 订单生产指令发送失败 " + e.getMessage());
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
                        .totalFee(yuanToFee(new BigDecimal(paySum)))
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
            Rmap.put("code", "200");
        return Rmap;
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


    /**
     * 描述： 1元钱转为 100分
     */
    private int yuanToFee(BigDecimal bigDecimal) {
        return bigDecimal.multiply(new BigDecimal(100)).intValue();
    }


}
