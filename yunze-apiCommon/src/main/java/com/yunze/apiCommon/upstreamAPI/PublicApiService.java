package com.yunze.apiCommon.upstreamAPI;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP.Inquire.Query_DX;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP.change.serviceAccept_DX;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.Inquire.Query_DX5G;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.change.ServiceAccept_DX5G;
import com.yunze.apiCommon.upstreamAPI.DianXinDCP.DcpDxService;
import com.yunze.apiCommon.upstreamAPI.DianXinDCP.change.serviceAccept_DianXinDCP;
import com.yunze.apiCommon.upstreamAPI.DianXinDCP.qm.DxDcpUtil;
import com.yunze.apiCommon.upstreamAPI.IoTLink.Inquire.Query_IoTLink;
import com.yunze.apiCommon.upstreamAPI.IoTLink.change.serviceAccept_IoTLink;
import com.yunze.apiCommon.upstreamAPI.LianTongCMP.Inquire.Query_LT;
import com.yunze.apiCommon.upstreamAPI.LianTongCMP.change.serviceAccept_LT;
import com.yunze.apiCommon.upstreamAPI.MengWang.Inquire.Query_Mw;
import com.yunze.apiCommon.upstreamAPI.SDYiDong.Inquire.Query_Sdiot;
import com.yunze.apiCommon.upstreamAPI.ShuoLang.Inquire.Query_Shuolang;
import com.yunze.apiCommon.upstreamAPI.TengYu.Inquire.Query_Ty;
import com.yunze.apiCommon.upstreamAPI.TengYu.change.ServiceAccept_Ty;
import com.yunze.apiCommon.upstreamAPI.XuYu.Inquire.Query_XuYu;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.Inquire.Query_YD;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.YD_EC_Api;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.change.serviceAccept_YD;
import com.yunze.apiCommon.upstreamAPI.YiDongEcV2.Inquire.Query_EcV2;
import com.yunze.apiCommon.upstreamAPI.YiKong.Inquire.Query_YK;
import com.yunze.apiCommon.upstreamAPI.YiKong.change.ServiceAccept_YK;
import com.yunze.apiCommon.upstreamAPI.YiYuan.Inquire.Query_YiYuan;
import com.yunze.apiCommon.upstreamAPI.ZCWL.ZhonChuang_Api;
import com.yunze.apiCommon.utils.VeDate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PublicApiService {


    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private DcpDxService dcpDxService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Lazy
    @Resource
    private DxDcpUtil dxDcpUtil;

    /**
     * 内部API 调用
     *
     * @param Param
     * @param function_name
     * @return
     */
    public Map<String, Object> insideApi(Map<String, Object> Param, String function_name, Map<String, Object> find_card_route_map) {
        Map<String, Object> Rmap = new HashMap<>();
        String code = "200";
        String msg = "操作成功！";
        Object data = null;
        try {
            data = CPU(Param, function_name, find_card_route_map);
        } catch (Exception e) {
            System.out.println("= insideApi [" + function_name + "]==");
            System.out.println(e.getMessage());
            System.out.println(JSON.toJSONString(Param));
            System.out.println(JSON.toJSONString(find_card_route_map));
            code = "500";
            msg = "上游接口异常！";
        }
        Rmap.put("code", code);
        Rmap.put("msg", msg);
        Rmap.put("Data", data);
        return Rmap;
    }


    public Map<String, Object> queryFlow(Map<String, Object> map) {
        return CPU(map, "queryFlow", null);
    }

    public Map<String, Object> queryFlowHis(Map<String, Object> map) {
        return CPU(map, "queryFlowHis", null);
    }

    public Map<String, Object> queryCardStatus(Map<String, Object> map) {
        return CPU(map, "queryCardStatus", null);
    }

    public Map<String, Object> queryOnlineStatus(Map<String, Object> map) {
        return CPU(map, "queryOnlineStatus", null);
    }

    public Map<String, Object> queryCardBindStatus(Map<String, Object> map) {
        return CPU(map, "queryCardBIndStatus", null);
    }

    public Map<String, Object> changeCardStatus(Map<String, Object> map) {
        return CPU(map, "changeCardStatus", null);
    }

    public Map<String, Object> queryRealNameStatus(Map<String, Object> map) {
        return CPU(map, "queryRealNameStatus", null);
    }

    public Map<String, Object> queryAPNInfo(Map<String, Object> map) {
        return CPU(map, "queryAPNInfo", null);
    }

    public Map<String, Object> todayUse(Map<String, Object> map) {
        return CPU(map, "todayUse", null);
    }

    public Map<String, Object> FunctionApnStatus(Map<String, Object> map) {
        return CPU(map, "FunctionApnStatus", null);
    }

    public Map<String, Object> SpeedLimit(Map<String, Object> map) {
        return CPU(map, "SpeedLimit", null);
    }

    public Map<String, Object> MachineCardBinding(Map<String, Object> map) {
        return CPU(map, "MachineCardBinding", null);
    }


    public Map<String, Object> EcV2(Map<String, Object> map) {
        return CPU(map, "EcV2", null);
    }

    public Map<String, Object> queryCardActiveTime(Map<String, Object> map) {
        return CPU(map, "queryCardActiveTime", null);
    }


    public Map<String, Object> queryCardImei(Map<String, Object> map) {
        return CPU(map, "queryCardImei", null);
    }


    public Map<String, Object> queryFlow(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryFlow", find_card_route_map);
    }

    public Map<String, Object> queryFlowHis(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryFlowHis", find_card_route_map);
    }

    public Map<String, Object> queryCardStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryCardStatus", find_card_route_map);
    }

    public Map<String, Object> queryOnlineStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryOnlineStatus", find_card_route_map);
    }

    public Map<String, Object> queryCardBindStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryCardBIndStatus", find_card_route_map);
    }

    public Map<String, Object> changeCardStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "changeCardStatus", find_card_route_map);
    }

    public Map<String, Object> queryRealNameStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryRealNameStatus", find_card_route_map);
    }

    public Map<String, Object> queryAPNInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryAPNInfo", find_card_route_map);
    }

    public Map<String, Object> todayUse(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "todayUse", find_card_route_map);
    }

    public Map<String, Object> FunctionApnStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "FunctionApnStatus", find_card_route_map);
    }

    public Map<String, Object> SpeedLimit(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "SpeedLimit", find_card_route_map);
    }

    public Map<String, Object> MachineCardBinding(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "MachineCardBinding", find_card_route_map);
    }


    public Map<String, Object> EcV2(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "EcV2", find_card_route_map);
    }


    public Map<String, Object> simStopReason(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "simStopReason", find_card_route_map);
    }

    public Map<String, Object> onOffStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "onOffStatus", find_card_route_map);
    }

    public Map<String, Object> apnInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "apnInfo", find_card_route_map);
    }

    public Map<String, Object> cardBindStatus(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "cardBindStatus", find_card_route_map);
    }

    public Map<String, Object> simChangeHistory(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "simChangeHistory", find_card_route_map);
    }

    public Map<String, Object> simDataMargin(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "simDataMargin", find_card_route_map);
    }

    public Map<String, Object> queryOffering(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryOffering", find_card_route_map);
    }


    public Map<String, Object> queryGroupInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryGroupInfo", find_card_route_map);
    }

    public Map<String, Object> queryGroupMember(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryGroupMember", find_card_route_map);
    }

    public Map<String, Object> queryCardInfo(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryCardInfo", find_card_route_map);
    }


    public Map<String, Object> cardInfo(Map<String, Object> map) {
        Map<String, Object> Rmap = yzCardRouteMapper.findCard(map);
        Object iccid = Rmap.get("iccid") != null ? Rmap.get("iccid") : "";
        map.put("iccid", iccid);
        Rmap.put("packetArr", yzCardRouteMapper.findFlow(map));
        try {
            if (Rmap != null) {
                Object activateDate = Rmap.get("activateDate");
                String statusId = Rmap.get("statusId") != null ? Rmap.get("statusId").toString() : "0";

                if (activateDate != null && activateDate.toString().length() > 0) {


                } else {
                    //库存 正常 可测试 不需要获取激活时间
                    if (statusId.equals("1") || statusId.equals("2") || statusId.equals("3")) {
                        System.out.println(" iccid = " + iccid + " 不需要获取激活时间 statusId = " + statusId);
                    } else {
                        //1.创建路由 绑定 生产队列 发送消息
                        //导卡 路由队列
                        String polling_queueName = "admin_getSynActivateDate_queue";
                        String polling_routingKey = "admin.getSynActivateDate.queue";
                        String polling_exchangeName = "admin_exchange";//路由
                        try {
                            Map<String, Object> start_type = new HashMap<>();
                            start_type.put("iccid", iccid);
                            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                                // 设置消息过期时间 30 分钟 过期
                                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                                return message;
                            });
                        } catch (Exception e) {
                            System.out.println("发送获取激活时间指令失败 " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("发送获取激活时间指令 异常 " + e.getMessage());
        }
        return Rmap;
    }

    public Map<String, Object> cardPackageInfo(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<>();
        Map<String, Object> Pmap = new HashMap<>();
        Map<String, Object> Cardmap = yzCardRouteMapper.findIccid(map);

        if (Cardmap != null && Cardmap.get("iccid") != null) {
            String iccid = Cardmap.get("iccid").toString();
            Pmap.put("iccid", iccid);
            Pmap.put("all", map.get("all"));
            Rmap.put("packetArr", yzCardRouteMapper.findFlowArr(Pmap));
        } else {
            Rmap.put("packetArr", null);
        }
        return Rmap;
    }

    public Map<String, Object> queryCardActiveTime(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryCardActiveTime", find_card_route_map);
    }

    public Map<String, Object> realNameRemove(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "realNameRemove", find_card_route_map);
    }


    public Map<String, Object> queryCardImei(Map<String, Object> map, Map<String, Object> find_card_route_map) {
        return CPU(map, "queryCardImei", find_card_route_map);
    }

    public Map<String, Object> getToken(Map<String, Object> map) {
        Map<String, Object> Rmap = null;
        String tokenType = map.get("tokenType").toString();
        Object refreshToken = map.get("refreshToken");//刷新token


        String cd_code = "";
        Map<String, Object> find_card_route_map = null;
        if (tokenType.equals("EC01")) {
            map.put("cd_id", "29");
            find_card_route_map = yzCardRouteMapper.getRoute(map);
            //Rmap = sL_Api.getSuolangToken(find_card_route_map);
            cd_code = find_card_route_map.get("cd_code").toString();
            if (refreshToken != null) {
                find_card_route_map.put("refreshToken", refreshToken);
            }
            YD_EC_Api Qy = new YD_EC_Api(find_card_route_map, cd_code);
            Rmap = new HashMap<>();
            Rmap.put("token", Qy.token);
        } else if (tokenType.equals("EC20210825")) {
            Map<String, Object> api = new HashMap<String, Object>();
            map.put("cd_id", "13");
            find_card_route_map = yzCardRouteMapper.getRoute(map);
            cd_code = find_card_route_map.get("cd_code").toString();
            if (refreshToken != null) {
                find_card_route_map.put("refreshToken", refreshToken);
            }
            YD_EC_Api Qy = new YD_EC_Api(find_card_route_map, cd_code);
            Rmap = new HashMap<>();
            Rmap.put("token", Qy.token);
        } else if (tokenType.equals("ECV5@202205191520")) {
            Map<String, Object> api = new HashMap<String, Object>();
            map.put("cd_id", "43");
            find_card_route_map = yzCardRouteMapper.getRoute(map);
            cd_code = find_card_route_map.get("cd_code").toString();
            if (refreshToken != null) {
                find_card_route_map.put("refreshToken", refreshToken);
            }
            YD_EC_Api Qy = new YD_EC_Api(find_card_route_map, cd_code);
            Rmap = new HashMap<>();
            Rmap.put("token", Qy.token);
        }

        return Rmap;
    }


    /**
     * 通道处理器
     *
     * @param map
     * @param function_name
     * @param find_card_route_map
     * @return
     */
    private Map<String, Object> CPU(Map<String, Object> map, String function_name, Map<String, Object> find_card_route_map) {
        //System.out.println("CPU====");
        Map<String, Object> rmap = new HashMap<String, Object>();
        //判断卡 通道 类型 类型
        //System.out.println(map);
        find_card_route_map = find_card_route_map == null ? yzCardRouteMapper.find_route(map) : find_card_route_map;
        //System.out.println(find_card_route_map);
        String cd_code = find_card_route_map.get("cd_code").toString();//通道类型
        try {
            String network_type = find_card_route_map.get("network_type") != null ? find_card_route_map.get("network_type").toString() : "";//通道类型
            if (network_type != null && network_type.length() > 0 && network_type.equals("1")) {
                if (cd_code.equals("YiDong_ECv2")) {
                    cd_code = "YiDong_ECv2_Combo";
                } else if (cd_code.equals("YiDong_EC")) {
                    cd_code = "YiDong_EC_Combo";
                }
            }
        } catch (Exception e) {
            System.out.println("CPU = network_type 异常 " + e.getMessage() + " " + JSON.toJSONString(find_card_route_map));
        }

        String cd_status = find_card_route_map.get("cd_status").toString();//通道状态
        String card_no = find_card_route_map.get("card_no") != null ? find_card_route_map.get("card_no").toString() : "";// 真实号码
        String imei = null;
        String iccid = map.get("iccid") != null ? map.get("iccid").toString() : "";//iccid
        //String card_type = find_card_route_map.get("card_type").toString();// 卡类型


        String dateTime = null;
        String Is_Stop = null;
        String Is_Break = null;
        String speedValue = null;
        String Is_Spee = null;
        String bind_type = null;
        String AutomaticRecovery = null;
        String contactName = null;
        String contactPhone = null;
        String testType = null;
        String operType = null;
        String pageSize = null;
        String startNum = null;
        String groupId = null;
        String quota = null;
        String action = null;
        if (function_name.equals("queryFlowHis")) {
            dateTime = map.get("dateTime") != null ? map.get("dateTime").toString() : null;// 【查询历史流量月】
        } else if (function_name.equals("changeCardStatus")) {
            Is_Stop = map.get("Is_Stop") != null ? map.get("Is_Stop").toString() : null;// 【是否停复机】  on 开机 off 停机
        } else if (function_name.equals("FunctionApnStatus")) {
            Is_Break = map.get("Is_Break") != null ? map.get("Is_Break").toString() : null;//  【断网复机】 是否   0 开机 1 停机
        } else if (function_name.equals("SpeedLimit")) {
            speedValue = map.get("speedValue") != null ? map.get("speedValue").toString() : null;// 【限速】 标准 值
            Is_Spee = map.get("Is_Spee") != null ? map.get("Is_Spee").toString() : null;// 【限速】 是否   0 增加 1 删除
            AutomaticRecovery = map.get("AutomaticRecovery") != null ? map.get("AutomaticRecovery").toString() : null;// 是否 【限速】月初是否恢复  0 自动恢复 1 不自动恢复
        } else if (function_name.equals("MachineCardBinding")) {
            bind_type = map.get("bind_type") != null ? map.get("bind_type").toString() : null;// 【机卡绑定】 【电信CMP】 不填，表示普通机卡重绑 填写2，表示固定机卡重绑
            imei = map.get("imei") != null ? map.get("imei").toString() : null;// 【机卡绑定】 【电信CMP】  只有在bind_typ=2时才需传入，表示固定绑定到该设备号
            contactName = map.get("contactName") != null ? map.get("contactName").toString() : null;// 电信DCP 解绑用
            contactPhone = map.get("contactPhone") != null ? map.get("contactPhone").toString() : null;// 电信DCP 解绑用
        } else if (function_name.equals("cardBindStatus")) {
            testType = map.get("cardBindStatus") != null ? map.get("cardBindStatus").toString() : "1";// 【物联卡机卡分离状态查询 】 ECv5
        } else if (function_name.equals("changeCardStatusFlexible")) {
            /**
             *  0:已激活转已停机
             *  1:已停机转已激活
             *  2:库存转已激活
             *  3:可测试转库存
             *  4:可测试转待激活
             *  5:可测试转已激活
             *  6:待激活转已激活
             */
            operType = map.get("operType") != null ? map.get("operType").toString() : null;// 【状态灵活变更】
        } else if (function_name.equals("queryGroupInfo")) {
            pageSize = map.get("pageSize") != null ? map.get("pageSize").toString() : "1";// 【集团群组信息查询 】 每页查询的数目，不超过50
            startNum = map.get("startNum") != null ? map.get("startNum").toString() : "1";// 【集团群组信息查询 】 开始页，从1开始
        } else if (function_name.equals("queryGroupMember")) {
            pageSize = map.get("pageSize") != null ? map.get("pageSize").toString() : "50";// 【集团群组信息查询 】 每页查询的数目，不超过50
            startNum = map.get("startNum") != null ? map.get("startNum").toString() : "1";// 【集团群组信息查询 】 开始页，从1开始
            groupId = map.get("groupId") != null ? map.get("groupId").toString() : "";// 【集团群组信息查询 】 群组ID
        } else if (function_name.equals("InternetDisconnection")) {
            quota = map.get("quota") != null ? map.get("quota").toString() : null;// 【达量断网订购 】  阈值MB
            action = map.get("action") != null ? map.get("action").toString() : null;// 【达量断网订购 】 新增达量断网功能时action为1，如果已经添加了达量断网功能，需要修改断网阈值，则action设置为2，取消达量断网功能，action设置为3
        }


        rmap.put("iccid", iccid);
        //find_card_route_map != null &&
        if (cd_code != null && cd_code.equals("") == false && cd_status.equals("1")) {

            if (cd_code.equals("IoTLink")) {
                if (function_name.equals("queryFlow")) {
                    Query_IoTLink Qy = new Query_IoTLink(find_card_route_map);
                    rmap.put("Data", Qy.simDataUsage(iccid));
                } else if (function_name.equals("queryCardStatus")) {
                    Query_IoTLink Qy = new Query_IoTLink(find_card_route_map);
                    rmap.put("Data", Qy.simStatus(iccid));
                } else if (function_name.equals("queryOnlineStatus")) {
                    Query_IoTLink Qy = new Query_IoTLink(find_card_route_map);
                    rmap.put("Data", Qy.simSession(iccid));
                } else if (function_name.equals("changeCardStatus")) {
                    serviceAccept_IoTLink Ser = new serviceAccept_IoTLink(find_card_route_map);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "0";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "1";//停机
                    }
                    rmap.put("Data", Ser.changeSimStatus(iccid, stop));
                } else if (function_name.equals("queryCardActiveTime")) {
                    Query_IoTLink Qy = new Query_IoTLink(find_card_route_map);
                    rmap.put("Data", Qy.cardInfo(card_no));
                }
                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("DianXin_CMP")) {

                if (function_name.equals("queryFlow")) {
                    //实例化 电信 CMP 查询 类
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.query(card_no));
                } else if (function_name.equals("queryFlowHis")) {
                    //实例化 电信 CMP 查询 类
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.queryTrafficByDate(card_no, dateTime + "01", VeDate.getEndDateOfMonth(dateTime), "0"));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化 电信 CMP 查询 类
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.queryCardMainStatus(card_no));
                } else if (function_name.equals("queryOnlineStatus")) {
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.queryMainOnlineStatus(card_no));
                } else if (function_name.equals("queryCardBIndStatus")) {
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.queryMainCardBIndStatus(imei));
                } else if (function_name.equals("changeCardStatus")) {
                    //实例化 电信 CMP 业务变更 类
                    serviceAccept_DX Ser = new serviceAccept_DX(find_card_route_map);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "20";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "19";//停机
                    }
                    rmap.put("Data", Ser.disabledNumber(card_no, stop));
                } else if (function_name.equals("queryRealNameStatus")) {
                    //实例化 电信 CMP 查询 类
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.realNameQueryIot(card_no));
                } else if (function_name.equals("queryAPNInfo")) {
                    rmap.put("Data", "未开放接口！");
                } else if (function_name.equals("todayUse")) {
                    //实例化 电信 CMP 查询 类
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    String now = VeDate.getNowDateShortNuber();//获取今天时间 yyyyMMdd
                    //System.out.println("now = "+now);
                    rmap.put("Data", Qy.queryTrafficByDate(card_no, now, now, "0"));
                } else if (function_name.equals("FunctionApnStatus")) {
                    //实例化 电信 CMP 业务变更 类
                    serviceAccept_DX Ser = new serviceAccept_DX(find_card_route_map);
                    String option = Is_Break.equals("0") ? "DEL" : "ADD";
                    rmap.put("Data", Ser.singleCutNet(card_no, option));
                } else if (function_name.equals("SpeedLimit")) {
                    //实例化 电信 CMP 业务变更 类
                    serviceAccept_DX Ser = new serviceAccept_DX(find_card_route_map);
                    String option = Is_Spee.equals("1") ? "DEL" : "ADD";

                    Map<String, Object> Smap = Ser.speedValue_set(speedValue);
                    boolean bool = (boolean) Smap.get("bool");
                    speedValue = Smap.get("speedValue").toString();
                    if (bool) {
                        rmap.put("Data", Ser.speedLimitAction(card_no, speedValue, option));
                    } else {
                        rmap.put("Data", "限速标准不存在，请检查后重试！");
                    }
                } else if (function_name.equals("MachineCardBinding")) {
                    //实例化 电信 CMP 业务变更 类
                    serviceAccept_DX Ser = new serviceAccept_DX(find_card_route_map);
                    rmap.put("Data", Ser.IMEIReRecord(card_no, bind_type, imei));
                } else if (function_name.equals("queryCardActiveTime")) {
                    //实例化 电信 CMP 查询 类
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.queryCardMainStatus(card_no));
                } else if (function_name.equals("realNameRemove")) {
                    //实例化 电信 CMP 业务变更 类
                    serviceAccept_DX Ser = new serviceAccept_DX(find_card_route_map);
                    rmap.put("Data", Ser.ordinaryRealNameClear(card_no));
                } else if (function_name.equals("queryGroupMember")) {
                    Query_DX Qy = new Query_DX(find_card_route_map);
                    rmap.put("Data", Qy.getSIMList(startNum, null, null, null, null, null, null, null));
                } else if (function_name.equals("InternetDisconnection")) {
                    //实例化 电信 CMP 业务变更 类
                    serviceAccept_DX Ser = new serviceAccept_DX(find_card_route_map);

                    action = action.equals("2") ? "1" : action;//兼容 订购
                    rmap.put("Data", Ser.offNetAction(card_no, quota, action));
                }


                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("LianTong_CMP")) {
                if (function_name.equals("queryFlow") || function_name.equals("queryCardActiveTime")) {
                    //实例化 联通 CMP 查询 类
                    Query_LT Qy = new Query_LT(find_card_route_map);
                    rmap.put("Data", Qy.queryFlow(iccid));
                } else if (function_name.equals("queryFlowHis")) {
                    //实例化 联通 CMP 查询 类
                    Query_LT Qy = new Query_LT(find_card_route_map);
                    rmap.put("Data", Qy.queryFlowHis(iccid, dateTime));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化 联通 CMP 查询 类
                    Query_LT Qy = new Query_LT(find_card_route_map);
                    rmap.put("Data", Qy.queryCardStatus(iccid));
                } else if (function_name.equals("changeCardStatus")) {
                    //实例化 联通 CMP 业务变更 类
                    serviceAccept_LT Ser = new serviceAccept_LT(find_card_route_map);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "2";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "3";//停机
                    }
                    rmap.put("Data", Ser.changeCardStatus(iccid, stop));
                } else if (function_name.equals("changeCardStatusFlexible")) {
                    //实例化 联通 CMP 业务变更 类
                    serviceAccept_LT Ser = new serviceAccept_LT(find_card_route_map);
                    if (operType != null || map.get("unbind") != null) {
                        String type = null;
                        Query_LT Qy = new Query_LT(find_card_route_map);
                        Map<String, Object> Obj = ((List<Map<String, Object>>) Qy.queryFlow(iccid).get("terminals")).get(0);
                        int StatusCd = Integer.parseInt(Obj.get("simStatus").toString()); //当前卡状态码

                        if (operType == null) {
                            if (StatusCd == 3) {
                                operType = "1";
                            } else if (StatusCd == 2) {
                                operType = "0";
                            }
                        }

                        switch (operType) { //改变状态的码
                            case "0":
                                if (StatusCd == 2) type = "3";
                                break;
                            case "1":
                                if (StatusCd == 3) type = "2";
                                break;
                            case "2":
                                if (StatusCd == 7) type = "3";
                                break;
                            case "3":
                                if (StatusCd == 0) type = "7";
                                break;
                            case "4":
                                if (StatusCd == 0) type = "1";
                                break;
                            case "5":
                                if (StatusCd == 0) type = "2";
                                break;
                            case "6":
                                if (StatusCd == 1) type = "3";
                                break;
                        }

                        if (type != null) {
                            if (map.get("unbind") == null || StatusCd == 3) {
                                rmap.put("Data", Ser.changeCardStatus(iccid, type));
                            } else if (map.get("unbind") != null && StatusCd == 2) {
                                rmap.put("Data", Ser.changeCardStatus(iccid, type));
                                rmap.put("Data", Ser.changeCardStatus(iccid, "2"));
                            }
                        } else {
                            rmap.put("Message", "操作失败，当前类型有误，请核对后重新选择！");
                            rmap.put("code", "500");
                        }
                    }
                } else if (function_name.equals("queryRealNameStatus")) {
                    //实例化 联通 CMP 查询 类
                    Query_LT Qy = new Query_LT(find_card_route_map);
                    //iccid = iccid.length()>19?iccid.substring(0,19):iccid;
                    rmap.put("Data", Qy.queryRealNameStatus(iccid));
                } else if (function_name.equals("queryAPNInfo")) {
                    //实例化 联通 CMP 查询 类
                    Query_LT Qy = new Query_LT(find_card_route_map);
                    rmap.put("Data", Qy.queryAPNInfo(iccid));
                } else if (function_name.equals("FunctionApnStatus")) {
                    rmap.put("Data", "未开放接口！");
                } else if (function_name.equals("SpeedLimit")) {
                    //实例化 联通 CMP 业务变更 类
                    serviceAccept_LT Ser = new serviceAccept_LT(find_card_route_map);

                    Map<String, Object> Smap = Ser.speedValue_set(speedValue);
                    boolean bool = (boolean) Smap.get("bool");
                    speedValue = Smap.get("speedValue").toString();
                    if (bool) {
                        rmap.put("Data", Ser.upNetWork(iccid, speedValue));
                    } else {
                        rmap.put("Data", "限速标准不存在，请检查后重试！");
                    }
                } else if (function_name.equals("MachineCardBinding")) {
                    rmap.put("Data", "未开放接口！");
                }

                if (rmap.get("Message") == null) rmap.put("Message", "操作成功");
                if (rmap.get("cd_code") == null) rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_TOKE_ShuoLang") || cd_code.equals("YiDong_EC_TengYu") || cd_code.equals("YiDong_EC_Combo") || cd_code.equals("ECV5_token_MW")) {
                if (function_name.equals("queryFlow")) {
                    //实例化 移动 EC    查询 类
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    if (cd_code.equals("YiDong_EC_Combo")) {
                        rmap.put("Data", Qy.queryGprsMarginBySim(card_no));
                    } else {
                        rmap.put("Data", Qy.selectFlow(card_no));
                    }
                } else if (function_name.equals("queryFlowHis")) {
                    //实例化 移动 EC    查询 类
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.designatedMonth(card_no, dateTime));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化 移动 EC    查询 类
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryCardStatus(card_no));
                } else if (function_name.equals("changeCardStatus")) {
                    //实例化 移动 EC  业务变更 类
                    serviceAccept_YD Ser = new serviceAccept_YD(find_card_route_map, cd_code);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "1";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "0";//停机
                    }
                    rmap.put("Data", Ser.changeCardStatus(card_no, stop));
                } else if (function_name.equals("queryRealNameStatus")) {
                    rmap.put("Data", "未开放接口！");
                } else if (function_name.equals("queryAPNInfo")) {
                    //实例化 移动 EC    查询 类
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryApnStatus(card_no));
                } else if (function_name.equals("FunctionApnStatus")) {
                    //实例化 移动 EC  业务变更 类
                    serviceAccept_YD Ser = new serviceAccept_YD(find_card_route_map, cd_code);
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Ser.FunctionApnStatus(card_no, Is_Break, Qy.queryApnStatus(card_no)));
                } else if (function_name.equals("SpeedLimit")) {
                    //实例化 移动 EC  业务变更 类
                    serviceAccept_YD Ser = new serviceAccept_YD(find_card_route_map, cd_code);

                    Map<String, Object> Smap = Ser.speedValue_set(speedValue, AutomaticRecovery);
                    boolean bool = (boolean) Smap.get("bool");
                    speedValue = Smap.get("speedValue").toString();
                    if (bool) {
                        Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                        Map<String, Object> Jobj = Qy.queryApnStatus(card_no);
                        String apnName = null;
                        if (Jobj.get("status").toString().equals("0")) {
                            Map<String, Object> result = ((List<Map<String, Object>>) Jobj.get("result")).get(0);
                            Map<String, Object> data = ((List<Map<String, Object>>) result.get("serviceTypeList")).get(0);
                            apnName = data.get("apnName").toString();
                        }
                        rmap.put("Data", Ser.speed(card_no, apnName, speedValue));
                    } else {
                        rmap.put("Data", "限速标准不存在，请检查后重试！");
                    }
                } else if (function_name.equals("MachineCardBinding")) {
                    rmap.put("Data", "未开放接口！");
                } else if (function_name.equals("queryOnlineStatus")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.sim_session(card_no));
                } else if (function_name.equals("queryCardActiveTime")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryCardActiveTime(card_no));
                } else if (function_name.equals("queryCardImei")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryCardImei(card_no));
                } else if (function_name.equals("simStopReason")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.simStopReason(card_no));
                } else if (function_name.equals("onOffStatus")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.onOffStatus(card_no));
                } else if (function_name.equals("apnInfo")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.apnInfo(card_no));
                } else if (function_name.equals("cardBindStatus")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.cardBindStatus(card_no, testType));
                } else if (function_name.equals("simChangeHistory")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.simChangeHistory(card_no));
                } else if (function_name.equals("changeCardStatusFlexible")) {
                    //实例化 移动 EC  业务变更 类
                    serviceAccept_YD Ser = new serviceAccept_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Ser.changeCardStatus(card_no, operType));
                } else if (function_name.equals("simDataMargin")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.simDataMargin(card_no));
                } else if (function_name.equals("queryOffering")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryOffering(card_no));
                } else if (function_name.equals("queryGroupInfo")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryGroupInfo(pageSize, startNum));
                } else if (function_name.equals("queryGroupMember")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryGroupMember(pageSize, startNum, groupId));
                } else if (function_name.equals("queryCardInfo")) {
                    Query_YD Qy = new Query_YD(find_card_route_map, cd_code);
                    rmap.put("Data", Qy.queryCardActiveTime(card_no));
                }
                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            }
            //dcp 电信
            else if (cd_code.equals("DianXin_DCP")) {
                if (function_name.equals("queryFlow")) {
                    rmap = dcpDxService.queryFlow(iccid, find_card_route_map);
                } else if (function_name.equals("queryFlowHis")) {
                    rmap.put("Data", "未开放接口");
                } else if (function_name.equals("queryCardStatus")) {
                    rmap = dcpDxService.querySimStatus(iccid, find_card_route_map);
                } else if (function_name.equals("changeCardStatus")) {
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "2";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "3";//停机
                    }
                    rmap = dcpDxService.changeCardStatus(iccid, stop, find_card_route_map);
                } else if (function_name.equals("queryRealNameStatus")) {
                    rmap = dcpDxService.queryAuth(iccid, card_no, find_card_route_map);
                } else if (function_name.equals("queryAPNInfo")) {
                    rmap = dcpDxService.queryLimit(iccid, find_card_route_map);
                } else if (function_name.equals("todayUse")) {
                    rmap.put("Data", "未开放接口");
                } else if (function_name.equals("FunctionApnStatus")) {
                    rmap.put("Data", "未开放接口");
                } else if (function_name.equals("SpeedLimit")) {
                    serviceAccept_DX Ser = new serviceAccept_DX(find_card_route_map);
                    Map<String, Object> Smap = Ser.speedValue_set(speedValue);
                    boolean bool = (boolean) Smap.get("bool");
                    speedValue = Smap.get("speedValue").toString();
                    if (bool) {
                        rmap = dcpDxService.changeLimit(iccid,
                                "13", speedValue, find_card_route_map);
                    } else {
                        rmap.put("Data", "限速标准不存在，请检查后重试！");
                    }
                } else if (function_name.equals("MachineCardBinding")) {
                    serviceAccept_DianXinDCP Ser = new serviceAccept_DianXinDCP(find_card_route_map);
                    //bind_type = 联系人姓名  imei = 联系人手机号码
                    rmap.put("Data", Ser.unlockSimbind(iccid, contactName, contactPhone));
                } else if (function_name.equals("realNameRemove")) {
                    serviceAccept_DianXinDCP Ser = new serviceAccept_DianXinDCP(find_card_route_map);
                    //bind_type = 联系人姓名  imei = 联系人手机号码
                    rmap.put("Data", Ser.realNameRemove(iccid, "iccid"));
                } else if (function_name.equals("queryCardActiveTime")) {
                    rmap.put("data", dxDcpUtil.subscriptions(iccid, find_card_route_map));
                }


                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("XuYuWuLian")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_XuYu Qy = new Query_XuYu(find_card_route_map);
                    rmap.put("Data", Qy.queryInfo(card_no));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_XuYu Qy = new Query_XuYu(find_card_route_map);
                    rmap.put("Data", Qy.queryInfo(card_no));
                } else if (function_name.equals("queryCardActiveTime")) {
                    Query_XuYu Qy = new Query_XuYu(find_card_route_map);
                    rmap.put("Data", Qy.queryInfo(card_no));
                }
                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("SDIOT")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_Sdiot Qy = new Query_Sdiot(find_card_route_map);
                    rmap.put("Data", Qy.queryFlow(card_no));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_Sdiot Qy = new Query_Sdiot(find_card_route_map);
                    rmap.put("Data", Qy.queryCardStatus(card_no));
                } else if (function_name.equals("queryOnlineStatus")) {
                    //实例化  查询 类
                    Query_Sdiot Qy = new Query_Sdiot(find_card_route_map);
                    rmap.put("Data", Qy.qryGPRSInfo(card_no));
                }

                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("YYWL")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_YiYuan Yy = new Query_YiYuan(find_card_route_map);
                    rmap.put("Data", Yy.queryInfo(iccid));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_YiYuan Yy = new Query_YiYuan(find_card_route_map);
                    rmap.put("Data", Yy.queryInfo(iccid));
                } else if (function_name.equals("queryCardActiveTime")) {
                    //实例化  查询 类
                    Query_YiYuan Yy = new Query_YiYuan(find_card_route_map);
                    rmap.put("Data", Yy.queryInfo(iccid));
                }
                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("ShuoLang")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_Shuolang Q_Sl = new Query_Shuolang(find_card_route_map);
                    rmap.put("Data", Q_Sl.queryInfo(card_no));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_Shuolang Q_Sl = new Query_Shuolang(find_card_route_map);
                    rmap.put("Data", Q_Sl.queryInfo(card_no));
                }
                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("ZCWL")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    ZhonChuang_Api Q_Sl = new ZhonChuang_Api(find_card_route_map);
                    rmap.put("Data", Q_Sl.queryRequst("queryCardTraffic", iccid));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    ZhonChuang_Api Q_Sl = new ZhonChuang_Api(find_card_route_map);
                    rmap.put("Data", Q_Sl.queryRequst("queryCardStatus", iccid));
                } else if (function_name.equals("queryOnlineStatus")) {
                    //实例化  查询 类
                    ZhonChuang_Api Q_Sl = new ZhonChuang_Api(find_card_route_map);
                    rmap.put("Data", Q_Sl.queryRequst("queryCardSessionInfo", iccid));
                } else if (function_name.equals("onOffStatus")) {
                    //实例化  查询 类
                    ZhonChuang_Api Q_Sl = new ZhonChuang_Api(find_card_route_map);
                    rmap.put("Data", Q_Sl.queryRequst("queryCardOnOff", iccid));
                } else if (function_name.equals("simStopReason")) {
                    //实例化  查询 类
                    ZhonChuang_Api Q_Sl = new ZhonChuang_Api(find_card_route_map);
                    rmap.put("Data", Q_Sl.queryRequst("diagnosisCard", iccid));
                }


                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("YKWL")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_YK Qy = new Query_YK(find_card_route_map);
                    rmap.put("Data", Qy.queryTraffic(iccid));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_YK Qy = new Query_YK(find_card_route_map);
                    rmap.put("Data", Qy.queryCardStatus(iccid));
                } else if (function_name.equals("changeCardStatus")) {
                    //实例化  查询 类
                    ServiceAccept_YK Sa = new ServiceAccept_YK(find_card_route_map);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "0";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "2";//停机
                    }
                    rmap.put("Data", Sa.changeCardStatus(iccid, stop));
                } else if (function_name.equals("MachineCardBinding")) {
                    //实例化  业务变更 类
                    ServiceAccept_YK Sa = new ServiceAccept_YK(find_card_route_map);
                    rmap.put("Data", Sa.IMEIRe(iccid));
                } else if (function_name.equals("queryCardActiveTime")) {
                    //实例化  查询 类
                    Query_YK Qy = new Query_YK(find_card_route_map);
                    rmap.put("Data", Qy.getSIMInfo(iccid));
                }
                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("TenngYu")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_Ty Qy = new Query_Ty(find_card_route_map);
                    rmap.put("Data", Qy.queryCardInfo(iccid));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_Ty Qy = new Query_Ty(find_card_route_map);
                    rmap.put("Data", Qy.queryCardInfo(iccid));
                } else if (function_name.equals("changeCardStatus")) {
                    //实例化  查询 类
                    ServiceAccept_Ty Sa = new ServiceAccept_Ty(find_card_route_map);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "1";//停机
                    } else if (Is_Stop.equals("off")) {
                        stop = "0";//正常
                    }
                    rmap.put("Data", Sa.changeCardStatus(iccid, stop));
                } else if (function_name.equals("queryCardActiveTime")) {
                    //实例化  查询 类
                    Query_Ty Qy = new Query_Ty(find_card_route_map);
                    rmap.put("Data", Qy.queryCardInfo(iccid));
                }
                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("YiDong_ECv2") || cd_code.equals("YiDong_ECv2_Combo")) {
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_EcV2 Qy = new Query_EcV2(find_card_route_map);
                    if (cd_code.equals("YiDong_ECv2_Combo")) {
                        rmap.put("Data", Qy.gprsrealtimeinfo(iccid));
                    } else {
                        rmap.put("Data", Qy.queryCardFlow(iccid));
                    }

                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_EcV2 Qy = new Query_EcV2(find_card_route_map);
                    rmap.put("Data", Qy.queryCardStatus(iccid));
                } else if (function_name.equals("queryOnlineStatus")) {
                    //实例化  查询 类
                    Query_EcV2 Qy = new Query_EcV2(find_card_route_map);
                    rmap.put("Data", Qy.sim_session(iccid));
                } else if (function_name.equals("queryOffering")) {
                    //实例化  查询 类
                    Query_EcV2 Qy = new Query_EcV2(find_card_route_map);
                    rmap.put("Data", Qy.querycardprodinfod(iccid));
                }

                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("DianXin_CMP_5G")) {//电信CMP5G
                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_DX5G Qy = new Query_DX5G(find_card_route_map);
                    rmap.put("Data", Qy.queryTraffic(card_no, "0"));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_DX5G Qy = new Query_DX5G(find_card_route_map);
                    rmap.put("Data", Qy.queryCardMainStatus(card_no, null));
                } else if (function_name.equals("changeCardStatus")) {
                    //实例化
                    ServiceAccept_DX5G Sr = new ServiceAccept_DX5G(find_card_route_map);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "20";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "19";//停机
                    }
                    rmap.put("Data", Sr.disabledNumberService(card_no, stop));
                } else if (function_name.equals("FunctionApnStatus")) {
                    //实例化  查询 类
                    ServiceAccept_DX5G Sr = new ServiceAccept_DX5G(find_card_route_map);
                    String option = Is_Break.equals("0") ? "DEL" : "ADD";
                    rmap.put("Data", Sr.apnNetStatusAction(card_no, option));
                } else if (function_name.equals("queryGroupMember")) {
                    Query_DX5G Qy = new Query_DX5G(find_card_route_map);
                    rmap.put("Data", Qy.getSIMList(startNum, null, null, null, null, null, null, null));
                } else if (function_name.equals("InternetDisconnection")) {
                    //实例化 电信 CMP 业务变更 类
                    ServiceAccept_DX5G Ser = new ServiceAccept_DX5G(find_card_route_map);
                    action = action.equals("1") ? "2" : action;//兼容 订购
                    rmap.put("Data", Ser.offNetAction(card_no, quota, action));
                }


                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else if (cd_code.equals("MwFengZuShou")) {

                if (function_name.equals("queryFlow")) {
                    //实例化  查询 类
                    Query_Mw Qy = new Query_Mw(find_card_route_map);
                    rmap.put("Data", Qy.queryInfo(card_no));
                } else if (function_name.equals("queryCardStatus")) {
                    //实例化  查询 类
                    Query_Mw Qy = new Query_Mw(find_card_route_map);
                    rmap.put("Data", Qy.queryInfo(card_no));
                } else if (function_name.equals("changeCardStatus")) {
                    //实例化
                    Query_Mw Qy = new Query_Mw(find_card_route_map);
                    String stop = null;
                    if (Is_Stop.equals("on")) {
                        stop = "12";//正常
                    } else if (Is_Stop.equals("off")) {
                        stop = "11";//停机
                    }
                    rmap.put("Data", Qy.ChangeState(card_no, stop));
                } else if (function_name.equals("queryCardActiveTime")) {
                    //实例化  查询 类
                    Query_Mw Qy = new Query_Mw(find_card_route_map);
                    rmap.put("Data", Qy.queryInfo(card_no));
                }

                rmap.put("Message", "操作成功");
                rmap.put("cd_code", cd_code);
            } else {
                rmap.put("cd_code", 500);
                rmap.put("Message", "划分的通道 通道编号配置错误请检查通道编号！");
            }

        } else if (cd_status == "2" || cd_status == "3") {
            rmap.put("cd_code", 500);
            rmap.put("Message", "划分通道 已停用或已删除！");
        } else {
            rmap.put("cd_code", 500);
            rmap.put("Message", "未找到 划分通道！");
        }
        return rmap;

    }
}
