package com.yunze.task.yunze.sys;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryIccidMapper;
import com.yunze.common.mapper.yunze.card.YzApplicationforRenewalPrimaryMapper;
import com.yunze.common.utils.yunze.VeDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业续费 审核成功之后触发 订单生成
 */
@Component
public class OrderRenewalGenerationMQ {

    @Resource
    private YzOrderMapper yzOrderMapper;

    @Resource
    private YzApplicationforRenewalPrimaryMapper yzApplicationforRenewalPrimaryMapper;
    @Resource
    private YzApplicationforRenewalMapper yzApplicationforRenewalMapper;
    @Resource
    private YzApplicationforRenewalPrimaryIccidMapper yzApplicationforRenewalPrimaryIccidMapper;


    /**
     *  企业续费 订单生成
     * */
    @RabbitHandler
    @RabbitListener(queues = "admin_RenewalToGenerateOrder_queue")
    public void ExecutionTaskFlie( String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        Map<String,Object> Pmap = JSON.parseObject(msg);
        Integer day = Integer.parseInt(Pmap.get("day").toString());

        RenewalToGenerateOrder(day);
    }



    /**
     * 续费生成订单
     */
    public void RenewalToGenerateOrder(Integer day) {
        Map<String,Object> findAppOrderMap = new HashMap<>();
        findAppOrderMap.put("sTime", VeDate.getNextDay(VeDate.getStringDateShort(),"-"+day) );//获取前 day 天的数据 默认建议 填 3 天
        findAppOrderMap.put("eTime", VeDate.getStringDateSSS() );
        //0.找到 续费申请中 已经审核通过的数组
        List<Map<String,Object>> appArr = yzApplicationforRenewalPrimaryMapper.findAppOrderGr(findAppOrderMap);
        if(appArr!=null && appArr.size()>0){
            //1.找到对应申请下资费计划
            for (int i = 0; i < appArr.size(); i++) {
                Map<String,Object> appObj = appArr.get(i);
                String p_id = appObj.get("id").toString();//主表id
                String dept_id = appObj.get("dept_id").toString();
                Map<String,Object> findPacket = new HashMap<>();
                findPacket.put("p_id",p_id);
                findPacket.put("generate_tariff_orders","0");//没有生成过订单的进行生成订单
                List<Map<String,Object>> packetArr =  yzApplicationforRenewalMapper.find(findPacket);
                if(packetArr!=null && packetArr.size()>0){

                    for (int j = 0; j < packetArr.size(); j++) {//循环资费计划
                        Map<String,Object>  packe = packetArr.get(j);
                        String packet_id = packe.get("packet_id").toString();

                        Map<String,Object> findIccid = new HashMap<>();
                        String b_id = packe.get("id").toString();//资费表id
                        findIccid.put("b_id",b_id);
                        List<Map<String,Object>> iccidArr =  yzApplicationforRenewalPrimaryIccidMapper.find(findIccid);
                        if(iccidArr!=null && iccidArr.size()>0) {

                            //2.获取订单 map
                            Map<String,Object> gOrderMap = new HashMap<>();
                            gOrderMap.put("validate_type","3");//生效类型 默认 续费都是 》 周期延顺 3
                            gOrderMap.put("agent_id",dept_id);//申请人企业id

                            Map<String,Object> packet_info = JSON.parseObject(packe.get("packet_info").toString());

                            Map<String, Object> OrderMap = orderGenerate(gOrderMap,packet_info);

                            List<Map<String,Object>> order_arrs = getSaveOrderArr(iccidArr,OrderMap);
                            Map<String,Object> saveMap = new HashMap<>();
                            saveMap.put("order_arrs",order_arrs);
                            int  sInt = yzOrderMapper.importOrder(saveMap);

                            if(sInt>0){//修改 续费申请表-资费计划列表 【是否已生成批量加包订单】 generate_tariff_orders
                                Map<String,Object> updPktMap = new HashMap<>();
                                updPktMap.put("packet_id",packet_id);
                                updPktMap.put("p_id",p_id);
                                updPktMap.put("generate_tariff_orders","1");//已执行加包
                                yzApplicationforRenewalMapper.upd(updPktMap);
                                System.out.println(" 【续费订单已生成】  ===  "+JSON.toJSONString(updPktMap));
                            }
                        }
                    }

                }
            }

        }

    }


    /**
     * 获取订单 Map
     * @param map
     * @param PacketMap
     * @return
     */
    public Map<String, Object> orderGenerate(Map<String,Object> map,Map<String,Object> PacketMap){
        Map<String, Object> OrderMap = new HashMap<>();
        //或充值 资费计划信息
        String agent_id = map.get("agent_id").toString();

        if(PacketMap!=null) {
            String ord_type = "2";
            //添加订单生成参数
            PacketMap.put("validate_type", map.get("validate_type"));//生效类型
            PacketMap.put("ord_type", ord_type);//订单类型
            OrderMap.put("add_parameter", JSON.toJSONString(PacketMap));//加包参数
            OrderMap.put("ord_type", ord_type);//订单类型 用量充值
            OrderMap.put("ord_name", "企业续费-用量充值");//交易名称 用量充值
            OrderMap.put("wx_ord_no", null);//微信订单号
            OrderMap.put("status", "1");//支付状态 支付成功
            OrderMap.put("price", PacketMap.get("packet_price"));//交易金额
            OrderMap.put("account", "0");//交易后金额
            OrderMap.put("pay_type", "s");//支付方式 平台记录
            OrderMap.put("is_profit", "0");//是否需要分润 0否-1是
            OrderMap.put("add_package", "0");//是否已经执行过加包 0否 1 是
            OrderMap.put("show_status", "0");//是否显示交易记录:0否-1显示
            OrderMap.put("packet_id", PacketMap.get("packet_id"));//资费计划 ID
            OrderMap.put("validate_type", map.get("validate_type"));//生效类型
            OrderMap.put("open_id", null);//openid 微信支付记录操作人
            OrderMap.put("agent_id", agent_id);// 所属记录企业id
            OrderMap.put("profit_type", "0");//分润类型
            OrderMap.put("info", null);//备注
            OrderMap.put("cre_type", "sys");//生成方式  系统 sys 人工 ai

        }
        return OrderMap;
    }


    public List<Map<String,Object>> getSaveOrderArr (List<Map<String,Object>> iccidArr,Map<String, Object> OrderMap){
        List<Map<String,Object>> rList = new ArrayList<>();
        for (int i = 0; i < iccidArr.size(); i++) {
            Map<String,Object> card = iccidArr.get(i);
            Map<String,Object> oMap = new HashMap<>();
            oMap.putAll(OrderMap);
            String ord_no = VeDate.getNo(8);
            oMap.put("ord_no",ord_no);
            oMap.put("iccid",card.get("iccid"));
            rList.add(oMap);
        }
        return  rList;
    }




}
