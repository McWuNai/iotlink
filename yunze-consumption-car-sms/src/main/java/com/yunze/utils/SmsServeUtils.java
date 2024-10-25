package com.yunze.utils;

import com.alibaba.fastjson.JSON;
import com.huawei.CmppOperate;
import com.yunze.apiCommon.mapper.mysql.YzCardRouteMapper;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.change.ServiceAccept_DX5G;
import com.yunze.common.mapper.mysql.bulk.YzBusiCardSmsMapper;
import com.yunze.common.mapper.mysql.bulk.YzSmSBulkBusinessDtailsMapper;
import com.yunze.common.utils.yunze.BulkUtil;
import com.yunze.config.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/08/18/9:02
 * @Description:
 */
@Component
public class SmsServeUtils {

    @Resource
    private BulkUtil bulkUtil;
    @Resource
    private YzBusiCardSmsMapper yzBusiCardSmsMapper;
    @Resource
    private YzSmSBulkBusinessDtailsMapper yzSmSBulkBusinessDtailsMapper;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private DisposableBean disposableBean;

    public Map<String, Object> sendSms(Map<String, Object> map) {
        return CPU(map, "sendSms",null);
    }


    public Map<String, Object> sendSms(Map<String, Object> map,Map<String, Object> find_card_route_map) {
        return CPU(map, "sendSms",find_card_route_map);
    }



    /**
     * 短信业务处理器
     * @param map
     * @param function_name
     * @param find_card_route_map
     * @return
     */
    private Map<String, Object> CPU(Map<String, Object> map, String function_name, Map<String, Object> find_card_route_map) {

        Map<String, Object> rmap = new HashMap<String, Object>();
        //判断卡 通道 类型 类型
        //System.out.println(map);
        find_card_route_map = find_card_route_map==null?yzCardRouteMapper.find_route(map):find_card_route_map;
        //System.out.println(find_card_route_map);
        String sms_code = find_card_route_map.get("sms_code").toString();//短信-执行模板
        String cd_status = find_card_route_map.get("cd_status").toString();//通道状态
        String sms_number = map.get("sms_number").toString();//短信服务号码
        String iccid = map.get("iccid").toString();//iccid
        String content = map.get("content").toString();//发送内容

        rmap.put("iccid",iccid);
        rmap.put("sms_number",sms_number);

        if ( sms_code != null && sms_code.equals("") == false && cd_status.equals("1")) {
            if (sms_code.equals("YD_CMPP")) {
                CmppOperate smsPn=  disposableBean.getCmppOperate(find_card_route_map);
                Map<String, Object> sendMap = new HashMap<String, Object>();
                sendMap.putAll(map);
                sendMap.put("msisdn", map.get("sms_number"));
                //注入对象 [初始化时 SMSSendPlugin 是通过new 出来的 没发直接注解注入]
                smsPn.bulkUtil = smsPn.bulkUtil!=null?smsPn.bulkUtil:bulkUtil;
                smsPn.yzBusiCardSmsMapper = smsPn.yzBusiCardSmsMapper!=null?smsPn.yzBusiCardSmsMapper:yzBusiCardSmsMapper;
                smsPn.yzSmSBulkBusinessDtailsMapper = smsPn.yzSmSBulkBusinessDtailsMapper!=null?smsPn.yzSmSBulkBusinessDtailsMapper:yzSmSBulkBusinessDtailsMapper;
                if (function_name.equals("sendSms")) {
                    //实例化  查询 类
                    smsPn.threadSend(sendMap);
                }
            }else if (sms_code.equals("DX_CMP5G")) {
                Map<String,Object> updMap = null;
                boolean bool = false;
                String resultMsg = null;
                String group_TRANSACTIONID = "";
                try {
                    updMap = new HashMap<>();
                    ServiceAccept_DX5G Sr = new ServiceAccept_DX5G(map);

                    //电信短信下发回调地址
                    String callbackUrl = "http://api.5iot.cn";
                    String statusCallback = callbackUrl+"/route/call/dianxin5g/status";
                    String returnSMSCallback = callbackUrl+"/route/call/dianxin5g/returnSMS";
                    String returnType = "1";

                    Map<String, Object> sendRMap =  Sr.sendSms(sms_number,content,statusCallback,returnSMSCallback,returnType,null);
                    if(sendRMap!=null ){
                        //{"result":"0","resultMsg":"处理成功！","group_TRANSACTIONID":"100000005391814"}
                        String result =  sendRMap.get("result")!=null?sendRMap.get("result").toString():null;
                        resultMsg =  sendRMap.get("resultMsg")!=null?sendRMap.get("resultMsg").toString():null;
                        if(result.equals("0")){
                            bool = true;
                            group_TRANSACTIONID =   sendRMap.get("group_TRANSACTIONID")!=null?sendRMap.get("group_TRANSACTIONID").toString():"";
                        }
                    }
                }catch (Exception e){
                    System.out.println("发送短信异常 "+e.getMessage());
                    resultMsg = "发送短信异常";
                }
                updMap.put("bool",bool);
                updMap.put("result",resultMsg);
                updMap.put("msgId",group_TRANSACTIONID);
                updMap.put("mydescribe",resultMsg);
                String b_id = map.get("b_id") != null ? map.get("b_id").toString() : "";
                String card_number = map.get("card_number")!=null?map.get("card_number").toString():"";
                updMap.put("b_id", b_id);
                updMap.put("card_number",card_number);
                updMap.put("send_number", sms_number);
                String state_id = "2";
                if (bool) {
                    state_id = "1";
                }
                updMap.put("state_id", state_id);
                bulkUtil.smsDupdate(updMap);
                yzSmSBulkBusinessDtailsMapper.update(updMap);

                Map<String, Object> addMap = new HashMap<>();
                try {
                    addMap.put("msgid",group_TRANSACTIONID);
                    addMap.put("msisdn",sms_number);
                    addMap.put("content",content);
                    addMap.put("state_id",state_id);
                    addMap.put("type","send");
                    addMap.put("sms_src_TerminalId","");
                    yzBusiCardSmsMapper.add(addMap);
                }catch (Exception e){
                    System.out.println("yzBusiCardSmsMapper.add(addMap) 异常 "+ JSON.toJSONString(addMap));
                }

            }else {
                rmap.put("cd_code", 500);
                rmap.put("Message", "划分的通道 编号配置 错误请检查通道编号！");
            }

        }else if (cd_status == "2" || cd_status == "3") {
            rmap.put("cd_code", 500);
            rmap.put("Message", "划分通道 已停用或已删除！");
        } else {
            rmap.put("cd_code", 500);
            rmap.put("Message", "未找到 划分通道！");
        }
        rmap.put("cd_code", 200);
        rmap.put("Message", "操作成功");
        return rmap;

    }
}
