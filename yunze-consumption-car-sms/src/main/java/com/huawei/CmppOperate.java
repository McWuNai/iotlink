package com.huawei;

import com.alibaba.fastjson.JSON;
import com.huawei.insa2.comm.cmpp30.message.CMPP30SubmitMessage;
import com.huawei.insa2.comm.cmpp30.message.CMPP30SubmitRepMessage;
import com.huawei.insa2.util.Args;
import com.huawei.insa2.util.TypeConvert;
import com.huawei.smproxy.SMProxy30;
import com.yunze.apiCommon.utils.SMSUtil;
import com.yunze.common.mapper.yunze.bulk.YzBusiCardSmsMapper;
import com.yunze.common.mapper.yunze.bulk.YzSmSBulkBusinessDtailsMapper;
import com.yunze.common.utils.yunze.BulkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/08/18/9:22
 * @Description: 华为 CMPP 短信 操作帮助类
 */
@Component
@Slf4j
public class CmppOperate {

    @Resource
    public BulkUtil bulkUtil;
    @Resource
    public YzBusiCardSmsMapper yzBusiCardSmsMapper;
    @Resource
    public YzSmSBulkBusinessDtailsMapper yzSmSBulkBusinessDtailsMapper;


    public void setBulkUtil(BulkUtil bulkUtil){
        this.bulkUtil = bulkUtil;
    }

    public void setYzBusiCardSmsMapper(YzBusiCardSmsMapper yzBusiCardSmsMapper){
        this.yzBusiCardSmsMapper = yzBusiCardSmsMapper;
    }

    public void setYzSmSBulkBusinessDtailsMapper(YzSmSBulkBusinessDtailsMapper yzSmSBulkBusinessDtailsMapper){
        this.yzSmSBulkBusinessDtailsMapper = yzSmSBulkBusinessDtailsMapper;
    }


    public String host = "";// InfoX主机地址,与移动签合同时移动所提供的地址 需修改
    public String port = "";// InfoX主机端口号
    public String addr = ""; // 企业代码 6位
    public String secret = "";// 企业帐户密码
    public String interval = "10";// 心跳信息发送间隔时间(单位：秒)
    public String reconnect = "10";// 连接中断时重连间隔时间(单位：秒)
    public String Pnoresponseout = "5";// 需要重连时，连续发出心跳而没有接收到响应的个数（单位：个)
    public String timeout = "10";    // 操作超时时间(单位：秒)
    public String version = "1";    // 双方协商的版本号(大于0，小于256)
    public String debug = "false";    // 是否属于调试状态,true表示属于调试状态
    public SMProxy30 smProxy = null;
    public Args args = null;

    /**
     * @pk_Total 相同msg_Id消息总条数,短短信这里是1
     */
    public   int pk_Total = 1;
    /**
     * @pk_Number 相同msg_Id的消息序号
     */
    public   int pk_Number = 1;
    /**
     * @registered_Delivery 是否要求返回状态报告
     */
    public   int registered_Delivery = 0;
    /**
     * @msg_Level 信息级别
     */
    public   int msg_Level = 1;
    /**
     * @fee_UserType 0对目的终端计费；1对源终端计费；2对SP计费;
     */
    public   int fee_UserType = 0;
    /**
     * @tp_Pid GSM协议类型 一般文本的时候设0,铃声图片设1
     */
    public   int tp_Pid = 0;
    /**
     * @tp_Udhi GSM协议类型 0不包含头部信息 1包含头部信息
     */
    public   int tp_Udhi = 0;
    /**
     * @ee_Type 资费类别 一般为02：按条计信息费
     */
    public   String fee_Type = "02";
    /**
     * @@fee_Code 资费代码(以分为单位)
     */
    public   String fee_Code = "0";
    /**
     * @service_Id 业务类型 用户自定义 用来分类查询
     */
    public   String service_Id = "SDYZBDDH";
    /**
     * @fee_Terminal_Id 被计费用户的号码
     */
    public   String fee_TerminalId = "";
    /**
     * @fee_Terminal_Type 被计费用户的类型
     */
    public   int fee_Terminal_Type = 0;
    /**
     * @msg_Src 消息内容来源 6位的企业代码
     */
    public   String msgSrc = "";
    /**
     * @valid_Time 存活有效期
     */
    public   Date valid_Time = null;
    /**
     * @at_Time 定时发送时间
     */
    public  Date at_Time = null;
    /**
     * @src_Terminal_Id 移动所提供的服务代码 此处需修改
     */
    public   String src_TerminalId = null;

    public CmppOperate(){}


    //构造赋值
    public CmppOperate(SMProxy30 psmProxy){
        smProxy = psmProxy;
    }


    //构造赋值
    public CmppOperate(Map<String, Object> init_map){

        host = init_map.get("sms_host").toString();
        port = init_map.get("sms_port").toString();
        addr = init_map.get("sms_source_addr").toString();
        secret = init_map.get("sms_shared_secret").toString();
        src_TerminalId = init_map.get("sms_src_TerminalId").toString();
        msgSrc = addr;
        args = new Args();
        args.set("host", host);
        args.set("port", port);
        args.set("source-addr", addr);
        args.set("shared-secret", secret);
        args.set("heartbeat-interval", interval);
        args.set("reconnect-interval", reconnect);
        args.set("heartbeat-noresponseout", Pnoresponseout);
        args.set("transaction-timeout", timeout);
        args.set("version", version);
        args.set("debug", debug);
        smProxy = new SMProxy30(args);
    }



    //获取对象
    public Args CmppOperate(Map<String, Object> init_map){
        host = init_map.get("sms_host").toString();
        port = init_map.get("sms_port").toString();
        addr = init_map.get("sms_source_addr").toString();
        secret = init_map.get("sms_shared_secret").toString();
        src_TerminalId = init_map.get("sms_src_TerminalId").toString();
        msgSrc = addr;
        args = new Args();
        args.set("host", host);
        args.set("port", port);
        args.set("source-addr", addr);
        args.set("shared-secret", secret);
        args.set("heartbeat-interval", interval);
        args.set("reconnect-interval", reconnect);
        args.set("heartbeat-noresponseout", Pnoresponseout);
        args.set("transaction-timeout", timeout);
        args.set("version", version);
        args.set("debug", debug);
        return  args;
    }

    /**
     * 短信发送队列处理程序
     *
     * @param map
     */
    public  Map<String,Object>  threadSend(Map<String, Object> map) {
        Map<String,Object> updMap = null;
        String b_id = map.get("b_id")!=null?map.get("b_id").toString():"";
        String card_number = map.get("card_number")!=null?map.get("card_number").toString():"";

          // @dest_Terminal_Id 接收业务的MSISDN号码,就是接收短信的手机号,String数组
        String[] simId = { map.get("sms_number").toString() };
        // @msg_Content 消息内容 ,发送的消息内容
        String content = map.get("content").toString();
        try {
            byte[] messageUCS2 = content.getBytes("UnicodeBigUnmarked");// 转换为byte[]
            int messageUCS2Len = messageUCS2.length;// 长短信长度
            int maxMessageLen = 140;
            if (messageUCS2Len > maxMessageLen){
                updMap = sendLongSMS( simId, content);
            }else{
                updMap = sendSMS( simId, content);
            }
        }catch (Exception e){
            System.out.println("CMPP短信下发异常 "+e.getMessage());
        }
        if(updMap!=null){
            updMap.put("b_id",b_id);
            updMap.put("card_number",card_number);
            updMap.put("send_number",simId[0]);
            String state_id = "2";
            boolean bool = (boolean) updMap.get("bool");
            if(bool){
                state_id = "1";
            }
            updMap.put("state_id",state_id);
            if(bulkUtil!=null){
                bulkUtil.smsDupdate(updMap);
            }
            if(yzSmSBulkBusinessDtailsMapper!=null){
                yzSmSBulkBusinessDtailsMapper.update(updMap);
            }






        }else{
            System.out.println("【"+msgSrc+"】updMap == null "+JSON.toJSONString(map));
        }
        return updMap;
    }


    /**
     * 短短信消息发送接口
     * @param simId   接收者
     * @param content 短信内容
     * @return
     */
    public Map<String,Object> sendSMS( String[] simId, String content) {
        Map<String,Object> rMap = new HashMap<>();
        boolean bool = false;
        String result = "";
        String msgId = "";
        String mydescribe = "";
        // @msg_Fmt 消息格式 0：ASCII串；8：UCS2编码；其他非法
        int msg_Fmt = 0;
        // @msg_Content 消息内容 byte[],发送的消息内容,需要转化为byte[]
        byte[] msgContent = null;
        // 判断是否中文
        if (SMSUtil.isChinese(content)) {
            msg_Fmt = 8;
            try {
                msgContent = content.getBytes("UTF-16BE");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                msgContent = "".getBytes();
            }
        } else {
            msgContent = content.getBytes();
        }
        CMPP30SubmitMessage message = new CMPP30SubmitMessage(pk_Total, pk_Number, registered_Delivery, msg_Level,
                service_Id, fee_UserType, fee_TerminalId, fee_Terminal_Type, tp_Pid, tp_Udhi, msg_Fmt, msgSrc, fee_Type,
                fee_Code, valid_Time, at_Time, src_TerminalId, simId, 0, msgContent, "");
        CMPP30SubmitRepMessage subRM = null;
        try {
            subRM = (CMPP30SubmitRepMessage) smProxy.send(message);
            //content = content.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
            System.out.println("短信下发结果：" + subRM);

            if (subRM.getResult() == 0) {
                bool = true;
                try {
                    msgId = subRM.getMsgId()!=null?""+TypeConvert.byte2long(subRM.getMsgId()):"";
                    result = ""+subRM.getResult();
                }catch (Exception e) {
                    System.out.println("msgId 转化失败……");
                }
                Map<String, Object> addMap = new HashMap<>();
                try {
                    addMap.put("msgid",msgId);
                    addMap.put("msisdn",simId[0]);
                    addMap.put("content",content);
                    addMap.put("state_id","1");
                    addMap.put("type","send");
                    addMap.put("sms_src_TerminalId",src_TerminalId);
                    yzBusiCardSmsMapper.add(addMap);
                }catch (Exception e){
                    log.error("【"+msgSrc+"】yzBusiCardSmsMapper.add(addMap) 异常 "+JSON.toJSONString(addMap));
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            mydescribe = e.getMessage();
            mydescribe = mydescribe.length()>240?mydescribe.substring(0,240):mydescribe;

        }
        rMap.put("bool",bool);
        rMap.put("result",result);
        rMap.put("msgId",msgId);
        rMap.put("mydescribe",mydescribe);
        return rMap;
    }



    /**
     * 长短信消息发送接口
     * @param simId
     *            接收者
     * @param content
     *            短信内容
     * @return
     */
    public  Map<String,Object> sendLongSMS( String[] simId, String content) {
        Map<String,Object> rMap = new HashMap<>();
        boolean bool = false;
        String result = "";
        String msgId = "";
        String mydescribe = "";
        // @msg_Fmt 消息格式 8：UCS2编码；其他非法
        int msg_Fmt = 8;
        List<byte[]> list = SMSUtil.getLongByte(content);// 截取短信内容并配置参数,这个内容要大于70汉字
        //int ret = 0;
        for (int i = 0; i < list.size(); i++) {
            CMPP30SubmitMessage message = new CMPP30SubmitMessage(list.size(), i + 1, registered_Delivery, msg_Level,
                    service_Id, fee_UserType, fee_TerminalId, fee_Terminal_Type, tp_Pid, 1, msg_Fmt, msgSrc, fee_Type,
                    fee_Code, valid_Time, at_Time, src_TerminalId, simId, 0, list.get(i), "");
            CMPP30SubmitRepMessage subRM = null;
            try {
                subRM = (CMPP30SubmitRepMessage) smProxy.send(message);
                System.out.println("短信下发结果：" + subRM);

                if (subRM.getResult() == 0) {
                    bool = true;
                    try {
                        msgId = subRM.getMsgId()!=null?""+TypeConvert.byte2long(subRM.getMsgId()):"";
                        result = ""+subRM.getResult();
                    }catch (Exception e) {
                        System.out.println("msgId 转化失败……");
                    }
                    Map<String, Object> addMap = new HashMap<>();
                    try {
                        addMap.put("msgid",msgId);
                        addMap.put("msisdn",simId[0]);
                        addMap.put("content",content);
                        addMap.put("state_id","1");
                        addMap.put("type","send");
                        addMap.put("sms_src_TerminalId",src_TerminalId);
                        yzBusiCardSmsMapper.add(addMap);
                    }catch (Exception e){
                        log.error("【"+msgSrc+"】yzBusiCardSmsMapper.add(addMap) 异常 "+JSON.toJSONString(addMap));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        rMap.put("bool",bool);
        rMap.put("result",result);
        rMap.put("msgId",msgId);
        rMap.put("mydescribe",mydescribe);
        return rMap;
    }



}
