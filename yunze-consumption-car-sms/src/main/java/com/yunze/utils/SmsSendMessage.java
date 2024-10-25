package com.yunze.utils;

import com.huawei.insa2.util.Args;

/**
 * @Auther: zhang feng
 * @Date: 2022/08/11/17:06
 * @Description:
 */
public class SmsSendMessage {




    public static void main(String[] a) {

        String host = "183.230.96.94";
        String port = "17890";
        String addr = "162623";
        String secret = "162623";
        String interval = "10";
        String reconnect = "10";
        String Pnoresponseout = "5";
        String timeout = "10";
        String version = "1";
        String debug = "true";



        Args args = new Args();
        // InfoX主机地址,与移动签合同时移动所提供的地址 需修改
        args.set("host", host);
        // InfoX主机端口号
        args.set("port", port);
        // 企业代码 6位
        args.set("source-addr", addr);
        // 企业帐户密码
        args.set("shared-secret", secret);
        // 心跳信息发送间隔时间(单位：秒)
        args.set("heartbeat-interval", interval);
        // 连接中断时重连间隔时间(单位：秒)
        args.set("reconnect-interval", reconnect);
        // 需要重连时，连续发出心跳而没有接收到响应的个数（单位：个)
        args.set("heartbeat-noresponseout", Pnoresponseout);
        // 操作超时时间(单位：秒)
        args.set("transaction-timeout", timeout);
        // 双方协商的版本号(大于0，小于256)
        args.set("version", version);
        // 是否属于调试状态,true表示属于调试状态
        args.set("debug", debug);

        SMSSendPlugin smsp = new SMSSendPlugin(args);
        smsp.start();

       /* SMProxy30 smProxy = new SMProxy30(args);
        smProxy.threadSend(smProxy, new String[] { "1440480745501" },
                "<EXTBE14061675,410926196404061675,刘思亮    ,LZZ1CLVB0KA353388,豫JA8515    ,危险品运输车,190417202357            ,410926196404061675,00>");*/

    }



}
