package com.yunze.config;

import com.alibaba.fastjson.JSON;
import com.huawei.CmppOperate;
import com.huawei.insa2.util.Args;
import com.yunze.apiCommon.mapper.mysql.YzCardRouteMapper;
import com.yunze.common.mapper.mysql.bulk.YzBusiCardSmsMapper;
import com.yunze.common.mapper.mysql.bulk.YzSmSBulkBusinessDtailsMapper;
import com.yunze.common.utils.yunze.BulkUtil;
import com.yunze.utils.SMSSendPlugin;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/08/23/10:24
 * @Description: 服务 启动停止类
 */
@Component
public class DisposableBean {

    public Map<String, SMSSendPlugin> sMProxyMap = new HashMap<>();//全局变量记录现已实例化的短信通道
    public Map<String, CmppOperate> copMap = new HashMap<>();//全局变量记录现 已实例化的短信通道

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private BulkUtil bulkUtil;
    @Resource
    private YzBusiCardSmsMapper yzBusiCardSmsMapper;
    @Resource
    private YzSmSBulkBusinessDtailsMapper yzSmSBulkBusinessDtailsMapper;

    /**
     * 获取 SMSSendPlugin 操作对象
     * @param find_card_route_map
     * @return
     */
    public SMSSendPlugin getSMSSendPlugin(Map<String, Object>  find_card_route_map){
        SMSSendPlugin rsMSSendPlugin = null;
        String sms_src_TerminalId = find_card_route_map.get("sms_src_TerminalId").toString();

        boolean isEx = false;
        for(String key:sMProxyMap.keySet()){
            if(key.equals(sms_src_TerminalId)){
                rsMSSendPlugin = sMProxyMap.get(key);
                isEx = true;
                break;
            }
        }
        if(!isEx){
            try {
                CmppOperate cop = new CmppOperate();
                System.out.println("=== find_card_route_map =  "+ JSON.toJSONString(find_card_route_map));
                Args args = cop.CmppOperate(find_card_route_map);
                System.out.println("=== Args =  "+ JSON.toJSONString(args));
                SMSSendPlugin smsSpn = new SMSSendPlugin(args);
                smsSpn.start();
                sMProxyMap.put(sms_src_TerminalId,smsSpn);//存入 对应服务号信息
                rsMSSendPlugin = smsSpn;
            }catch (Exception e){
                System.out.println(" 运行中增加通道 SMSSendPlugin smsSpn = new SMSSendPlugin(args); 异常 "+e.getMessage());
            }
        }

        return rsMSSendPlugin;
    }



    /**
     * 获取 CmppOperate 操作对象
     * @param find_card_route_map
     * @return
     */
    public CmppOperate getCmppOperate(Map<String, Object>  find_card_route_map){
        CmppOperate cmppOperate = null;
        String sms_src_TerminalId = find_card_route_map.get("sms_src_TerminalId").toString();

        boolean isEx = false;
        for(String key:copMap.keySet()){
            if(key.equals(sms_src_TerminalId)){
                cmppOperate = copMap.get(key);
                isEx = true;
                break;
            }
        }
        if(!isEx){
            try {
                CmppOperate cop = new CmppOperate();
                Args args = cop.CmppOperate(find_card_route_map);
                SMSSendPlugin smsSpn = new SMSSendPlugin(args);
                smsSpn.start();
                //注入对象 [初始化时 SMSSendPlugin 是通过new 出来的 没发直接注解注入]
                smsSpn.bulkUtil = bulkUtil;
                smsSpn.yzBusiCardSmsMapper = yzBusiCardSmsMapper;
                smsSpn.yzSmSBulkBusinessDtailsMapper = yzSmSBulkBusinessDtailsMapper;

                //用作短信发送 【2022-08-24】
                cop.bulkUtil = bulkUtil;
                cop.yzBusiCardSmsMapper = yzBusiCardSmsMapper;
                cop.yzSmSBulkBusinessDtailsMapper = yzSmSBulkBusinessDtailsMapper;
                cop.smProxy = smsSpn.smProxy;
                copMap.put(sms_src_TerminalId,cop);//存入 对应服务号信息

                copMap.put(sms_src_TerminalId,cop);//存入 对应服务号信息
                cmppOperate = cop;
            }catch (Exception e){
                System.out.println(" 运行中增加通道 SMSSendPlugin smsSpn = new SMSSendPlugin(args); 异常 "+e.getMessage());
            }
        }

        return cmppOperate;
    }







    @PostConstruct
    public void init(){
        //初始化
        System.out.println("============ cardSMS 启动 开始初始化需要启动的短信通道 ============ ");
        //获取现在数据库中 开启轮询 状态正常的通道进行监听
        Map<String,Object> findRouteMap=new HashMap<>();
        findRouteMap.put("type", "3");
        findRouteMap.put("sms_code", "notNull");//通道配置不能为空
        List<String> cd_codeArr = new ArrayList<>();
        cd_codeArr.add("YiDong_EC");
        findRouteMap.put("cd_codeArr", cd_codeArr);//只需要初始化指定的 运营

        List<Map<String, Object>> findRouteArr = yzCardRouteMapper.findRouteID(findRouteMap);//查询 已配置短信通道的卡号
        if(findRouteArr!=null && findRouteArr.size()>0) {
            for (int i = 0; i < findRouteArr.size(); i++) {
                Map<String, Object>  find_card_route_map = findRouteArr.get(i);
                String sms_src_TerminalId = find_card_route_map.get("sms_src_TerminalId").toString();
                CmppOperate cop = new CmppOperate();
                System.out.println("=== find_card_route_map =  "+ JSON.toJSONString(find_card_route_map));
                Args args = cop.CmppOperate(find_card_route_map);
                System.out.println("=== Args =  "+ args.toString());
                System.out.println("=== cop.args =  "+ args.toString());
                args = args!=null?args:cop.args;
                try {
                    SMSSendPlugin smsSpn = new SMSSendPlugin(args);
                    //注入对象 [初始化时 SMSSendPlugin 是通过new 出来的 没发直接注解注入]
                    smsSpn.bulkUtil = bulkUtil;
                    smsSpn.yzBusiCardSmsMapper = yzBusiCardSmsMapper;
                    smsSpn.yzSmSBulkBusinessDtailsMapper = yzSmSBulkBusinessDtailsMapper;


                    //用作短信发送 【2022-08-24】
                    cop.bulkUtil = bulkUtil;
                    cop.yzBusiCardSmsMapper = yzBusiCardSmsMapper;
                    cop.yzSmSBulkBusinessDtailsMapper = yzSmSBulkBusinessDtailsMapper;
                    cop.smProxy = smsSpn.smProxy;
                    copMap.put(sms_src_TerminalId,cop);//存入 对应服务号信息

                    sMProxyMap.put(sms_src_TerminalId,smsSpn);//存入 对应服务号信息
                    sMProxyMap.get(sms_src_TerminalId).start();

                }catch (Exception e){
                    System.out.println(" 初始化 SMSSendPlugin smsSpn = new SMSSendPlugin(args); "+sms_src_TerminalId+" 异常 "+e.getMessage());
                }
            }
        }

    }



    @PreDestroy
    public void exit(){
        //销毁断开连接
        System.out.println("============ cardSMS 程序结束 执行断开连接 ============ ");
        //获取现在数据库中 开启轮询 状态正常的通道进行监听
        if(sMProxyMap!=null ) {
            for(String key:sMProxyMap.keySet()){
                String message = " 关闭异常";
                try {
                    SMSSendPlugin smsSpn = sMProxyMap.get(key);
                    smsSpn.stop();
                    message = " 已关闭";
                }catch (Exception e){
                    message = " 关闭异常 "+e.getMessage();
                }
                System.out.println("短信通道 【"+key+"】 "+message);
            }
        }

    }
}
