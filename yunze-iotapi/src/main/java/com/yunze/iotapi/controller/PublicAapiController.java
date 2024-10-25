package com.yunze.iotapi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.utils.AesEncryptUtil;
import com.yunze.apiCommon.utils.ApiUtil_NoStatic;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.iotapi.utils.AgentCheckShiro;
import com.yunze.iotapi.utils.ResponseJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  物联网卡 通道管理
 */
@Controller
@RequestMapping("/Api")
public class PublicAapiController {

    @Resource
    private ApiUtil_NoStatic apiUtil_NoStatic;

    /**
     * 流量查询
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryFlow")
    @ResponseBody
    public JSONObject queryFlow(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("queryFlow--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        String num = null;
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            if(Param.get("iccid")==null){
                num = (String) Param.get("msisdn");
            }else {
                num = (String) Param.get("iccid");
            }
            Map<String,Object> data = apiUtil_NoStatic.queryFlow(Param);
            System.out.println("iot查询的号码为："+num+"接口返回为 "+data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 历史流量查询
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryFlowHis")
    @ResponseBody
    public JSONObject queryFlowHis(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("queryFlowHis--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.queryFlowHis(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }




    /**
     * 单卡生命周期
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryCardStatus")
    @ResponseBody
    public JSONObject queryCardStatus(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("queryCardStatus--------");
        String map = (String) request.getAttribute("map");

        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.queryCardStatus(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 修改生命周期(停复机)
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/changeCardStatus")
    @ResponseBody
    public JSONObject changeCardStatus(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("changeCardStatus--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.changeCardStatus(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 查询是否实名
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryRealNameStatus")
    @ResponseBody
    public JSONObject queryRealNameStatus(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("queryRealNameStatus--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.queryRealNameStatus(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 查询APN设置信息
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryAPNInfo")
    @ResponseBody
    public JSONObject queryAPNInfo(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("queryAPNInfo--------");
        String map = (String) request.getAttribute("map");

        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.queryAPNInfo(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }




    /**
     * 单端断网
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/FunctionApnStatus")
    @ResponseBody
    public JSONObject FunctionApnStatus(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("FunctionApnStatus--------");
        String map = (String) request.getAttribute("map");

        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.FunctionApnStatus(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }






    /**
     * 限速
     * 0-  恢复正常
     * 1-  上下行限速1Mbps 20996414
     * 2-  上下行限速2Mbps 20997204
     * 4-  上下行限速4Mbps 21997854
     * 5    128Kbps
     * 10	1Kbps
     * 11	512Kbps
     * 13	3Mbps
     * 14	5Mbps
     * 15	7Mbps
     * 16	10Mbps
     * 17	20Mbps
     * 18	30Mbps
     * 19	40Mbps
     * 20	50Mbps
     * 21	60Mbps
     * 22	70Mbps
     * 23	80Mbps
     * 24	90Mbps
     * 25	100Mbps
     * 26	110Mbps
     * 27	120Mbps
     * 28	130Mbps
     * 29	140Mbps
     * 30	150Mbps
     * 32	64Kbps
     * 33	256Kbps
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/SpeedLimit")
    @ResponseBody
    public JSONObject SpeedLimit(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("SpeedLimit--------");
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.SpeedLimit(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 机卡重绑
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/unbundling")
    @ResponseBody
    public JSONObject MachineCardBinding(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("unbundling--------");
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.unbundling(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }




  /*  @RequestMapping(value = "monthflow", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public JsonResult<?> monthflow(HttpServletRequest request*//*, @RequestBody(required = false) Object body*//*) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        *//*MonitorPushFlow pushFlow = new MonitorPushFlow();
        pushFlow.setIccid("monthflow");
        pushFlow.setDay(Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
        pushFlow.setDataOrigin(StringUtils.substring(getIpAddr(request) + (new Gson().toJson(parameterMap)), 0, 5000));
        pushFlow.setCreatetime((int) (System.currentTimeMillis() / 1000));
        monitorPushFlowService.insertOne(pushFlow);*//*



     *//*   MonitorPushFlow pushFlow2 = new MonitorPushFlow();
    pushFlow2.setIccid("monthflow body");
    pushFlow2.setDay(Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
    pushFlow2.setDataOrigin(StringUtils.substring(new Gson().toJson(body), 0, 5000));
    pushFlow2.setCreatetime((int) (System.currentTimeMillis() / 1000));
    monitorPushFlowService.insertOne(pushFlow2);*//*

        return JsonResult.success("ok", pushFlow.getDataOrigin());
    }
*/

    /**
     * 查询卡是否在网状态
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryOnlineStatus", produces = { "application/json;charset=UTF-8" })
    @ResponseBody
    public JSONObject queryOnlineStatus(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");

        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
            Map<String,Object> data = apiUtil_NoStatic.queryOnlineStatus(Param);
            System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }

    /**
     * 查询卡是否机卡绑定
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryCardBIndStatus")
    @ResponseBody
    public JSONObject queryCardBIndStatus(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");

        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            System.out.println(Param);
          /*  Map<String,Object> data = apiUtil_NoStatic.queryCardBindStatus(Param);
            System.out.println(data);*/
            return new ResponseJson().success(null);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 查询IMei
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryCardImei")
    @ResponseBody
    public JSONObject queryCardImei(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.queryCardImei(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 单卡停机原因查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simStopReason")
    @ResponseBody
    public JSONObject simStopReason(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.simStopReason(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 单卡开关机状态实时查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/onOffStatus")
    @ResponseBody
    public JSONObject onOffStatus(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.onOffStatus(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 单卡已开通APN信息查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/apnInfo")
    @ResponseBody
    public JSONObject apnInfo(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.apnInfo(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }

    /**
     * 物联卡机卡分离状态查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/cardBindStatus")
    @ResponseBody
    public JSONObject cardBindStatus(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.cardBindStatus(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }

    /**
     * 单卡状态变更历史查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simChangeHistory")
    @ResponseBody
    public JSONObject simChangeHistory(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.simChangeHistory(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }




    /**
     * 单卡查询 激活日期
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryCardActiveTime")
    @ResponseBody
    public JSONObject queryCardActiveTime(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.queryCardActiveTime(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 单卡 状态灵活变更
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/changeCardStatusFlexible")
    @ResponseBody
    public JSONObject changeCardStatusFlexible(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.changeCardStatusFlexible(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }




    /**
     * 上游订购套餐查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/queryOffering")
    @ResponseBody
    public JSONObject queryOffering(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject(map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = apiUtil_NoStatic.queryOffering(Param);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }



    public static void main(String[] args) {
        //  String url="http://hsapi.e114.xin:9002/Api/yunze/v1";


        Map<String, Object> Yzmap = new HashMap<String, Object>();

        // for(int i=0;i<300;i++){
        //String geturl = "http://127.0.0.1:9080/route/Api/";
        String geturl = "http://127.0.0.1:9080/route/Api/";

        String appId = "adminOption";
        String password = "asdasdasczx";
        String access_key = "asdfdrqads69846532065";
        String timeStamp = System.currentTimeMillis() + "";

        Yzmap.put("password", password);
        Yzmap.put("appId", appId);
        Yzmap.put("timeStamp", timeStamp);

        AgentCheckShiro agentCheckShiro = new AgentCheckShiro();

        String sign = agentCheckShiro.getSign(Yzmap, access_key);
        Yzmap.put("sign", sign);

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> Parmap = new HashMap<String, Object>();
        //Parmap.put("dateTime", "202011");

        //geturl +="queryFlow";//单卡流量查询
        geturl +="cardInfo";//单卡查询
        //geturl +="queryFlowHis";//单卡历史流量查询
        // geturl +="queryCardStatus";//单卡生命周期
        //geturl +="changeCardStatus";//单卡生命周期 变更
        //geturl += "queryRealNameStatus";//查询是否实名
        //geturl +="queryAPNInfo";//查询APN设置信息
        // geturl +="FunctionApnStatus";//单端断网
        //geturl +="SpeedLimit";//限速
        //geturl +="todayUse";//今日使用量
        //geturl +="queryOnlineStatus";//查询卡是否在网状态
        //  geturl +="queryCardActiveTime";//查询 激活日期
        //geturl +="MachineCardBinding";//解绑
        //geturl +="queryCardImei";//单卡 imei查询


        // Parmap.put("dateTime", "202011");
        // Parmap.put("Is_Stop", "on"); // 停复机
        // Parmap.put("Is_Break", "0"); // 【断网复机】 是否   0 开机 1 停机
        // Parmap.put("speedValue", "0"); // 【限速】 标准 值
        // Parmap.put("Is_Spee", "0"); // 【限速】 是否   0 增加 1 删除
        // Parmap.put("AutomaticRecovery", "0"); // 是否 【限速】月初是否恢复   0 自动恢复 1 不自动恢复


        List<String> sArr = new ArrayList<>();
        sArr.add("89860422152080038591");


        //Parmap.put("imei", null);
        map.put("verify", Yzmap);
        Parmap.put("bind_type", "2");
        try {
            //System.out.println(data);
            for (int i=0;i<sArr.size();i++){
                String result = null;
                Parmap.put("iccid", sArr.get(i));
                map.put("Param", Parmap);
                String data = AesEncryptUtil.encrypt(JSON.toJSONString(map), "Apishandongyunze", "2021_06_10_11_32");
                result = HttpUtil.post(geturl, data);// 返回结果字符串
                System.out.println(result);
            }
        } catch (Exception e) {
            //System.out.println(e);
        }

        //for(String keys:map.keySet()){

    }

}

