package com.yunze.iotapi.controller.openApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.upstreamAPI.PublicApiService;
import com.yunze.apiCommon.utils.ApiUtil_NoStatic;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;
import com.yunze.common.mapper.yunze.YzUserMapper;
import com.yunze.iotapi.service.impl.OpenApiServiceImpl;
import com.yunze.iotapi.utils.AgentCheckShiro;
import com.yunze.iotapi.utils.LogAnnotation;
import com.yunze.iotapi.utils.ResponseJson;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 *  物联网卡 通道管理
 */
//@Controller
//@RequestMapping("/open")
@RestController
@RequestMapping("/open")
public class PublicOpenAapiController {

    @Resource
    private PublicApiService publicApiService;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private ApiUtil_NoStatic apiUtil_NoStatic;

    @Resource
    private OpenApiServiceImpl openApiServiceImpl;
    @Resource
    private YzUserMapper yzUserMapper;


    /**
     * 卡信息查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/cardInfo")
    @ResponseBody
    @LogAnnotation(action = "卡信息查询")
    public JSONObject cardInfo(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = publicApiService.cardInfo(Param);
            return new ResponseJson().successOpen(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 卡套餐详情
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/cardPacketInfo")
    @ResponseBody
    @LogAnnotation(action = "卡套餐详情")
    public JSONObject cardPacketInfo(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Object all = Param.get("all")!=null?Param.get("all"):"0";
            Param.put("all",all);
            Map<String,Object> data = publicApiService.cardPackageInfo(Param);
            return new ResponseJson().successOpen(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }




    /**
     * 流量查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simDataUsage")
    @ResponseBody
    @LogAnnotation(action = "流量查询")
    public JSONObject simDataUsage(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> Rdata =  openApiServiceImpl.simDataUsage(Param);
            boolean bool = (boolean) Rdata.get("bool");
            String code = Rdata.get("code").toString();
            if(bool){
                return new ResponseJson().successOpen(Rdata.get("Data"));
            }else{
                return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 历史流量查询
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simDataUsageMonthly")
    @ResponseBody
    @LogAnnotation(action = "历史流量查询")
    public JSONObject simDataUsageMonthly(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> Rdata = apiUtil_NoStatic.queryFlowHis(Param);
            String code = Rdata.get("code").toString();
            Map<String, Object> returnMap = new HashMap<String, Object>();
            if(code.equals("200")){
                returnMap.put("useAmount",Rdata.get("Use"));
                returnMap.put("useUnit","MB");
                return new ResponseJson().successOpen(returnMap);
            }else{
                return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }




    /**
     * 单卡生命周期
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simStatus")
    @ResponseBody
    @LogAnnotation(action = "单卡生命周期查询")
    public JSONObject simStatus(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");

            Map<String,Object> Rdata =  openApiServiceImpl.simStatus(Param);
            boolean bool = (boolean) Rdata.get("bool");
            String code = Rdata.get("code").toString();
            if(bool){
                return new ResponseJson().successOpen(Rdata.get("Data"));
            }else{
                return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 修改生命周期(停复机)
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/changeSimStatus")
    @ResponseBody
    @LogAnnotation(action = "单卡停复机")
    public JSONObject changeSimStatus(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Object Status = Param.get("Status");
            if(Status!=null && !Status.equals("") && Status.toString().length()>0){
                String Str_Status = Status.toString();
                if(Str_Status.equals("1") || Str_Status.equals("0")){
                    String Is_Stop = Str_Status.equals("1")?"off":Str_Status.equals("0")?"on":null;
                   if(Is_Stop!=null){
                       Param.put("Is_Stop",Is_Stop);
                       Param.remove("Status");
                       Map<String,Object> Rdata =  openApiServiceImpl.changeSimStatus(Param,Is_Stop);
                       boolean bool = (boolean) Rdata.get("bool");
                       String code = Rdata.get("code").toString();
                       if(bool){
                           return new ResponseJson().successOpen(Rdata.get("Data"));
                       }else{
                           return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
                       }
                   }else{
                       return new ResponseJson().errorOpen("400","参数错误！请按文档说明传入 Status !！");
                   }
                }else{
                    return new ResponseJson().errorOpen("400", "参数错误！请按文档说明传入 Status ！");
                }
            }else{
                return new ResponseJson().errorOpen("400", "缺少参数！请按文档说明传入 ！");
            }

            } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 查询是否实名
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simRealNameQuery")
    @ResponseBody
    @LogAnnotation(action = "查询是否实名")
    public JSONObject simRealNameQuery(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");

            Map<String,Object> Rdata = apiUtil_NoStatic.queryRealNameStatus(Param);
            String code = Rdata.get("code").toString();
            Map<String, Object> returnMap = new HashMap<String, Object>();
            if(code.equals("200")){
                returnMap.put("statusCode", Rdata.get("Is_status"));
                returnMap.put("statusMessage", Rdata.get("Is_statusName"));
                return new ResponseJson().successOpen(returnMap);
            }else{
                return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }





    /**
     * 单独断网
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simApnFunction")
    @ResponseBody
    @LogAnnotation(action = "单卡单独断网")
    public JSONObject simApnFunction(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Object StatusBreak = Param.get("StatusBreak");
            if(StatusBreak!=null && !StatusBreak.equals("") && StatusBreak.toString().length()>0){
                String Str_StatusBreak = StatusBreak.toString();
                if(Str_StatusBreak.equals("1") || Str_StatusBreak.equals("0")){
                    Param.put("Is_Break",Str_StatusBreak);
                    Param.remove("StatusBreak");
                    Map<String,Object> Rdata =  openApiServiceImpl.simApnFunction(Param,Str_StatusBreak);
                    boolean bool = (boolean) Rdata.get("bool");
                    String code = Rdata.get("code").toString();
                    if(bool){
                        return new ResponseJson().successOpen(Rdata.get("Data"));
                    }else{
                        return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
                    }
                }else{
                    return new ResponseJson().errorOpen("400", "参数错误！请按文档说明传入 StatusBreak ！");
                }
            }else{
                return new ResponseJson().errorOpen("400", "缺少参数！请按文档说明传入 ！");
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }


    /*
     * 机卡重绑
     * @param request
     * @param response
     * @return*/
    @RequestMapping(value = "/MachineCardBinding")
    @ResponseBody
    public JSONObject MachineCardBinding(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Object contactName = Param.get("contactName");
            Object contactPhone = Param.get("contactPhone");
            if (contactName != null && !contactName.equals("") && contactName.toString().length() > 0 && contactPhone != null && !contactPhone.equals("") && contactPhone.toString().length() > 0) {
                Param.put("bind_type",null);
                Param.put("imei",null);
                Map<String, Object> Rdata = apiUtil_NoStatic.unbundling(Param);
                String code = Rdata.get("code").toString();
                String Message = Rdata.get("Message")!=null?Rdata.get("Message").toString():"";
                if (code.equals("200")) {
                    return new ResponseJson().successOpen(Message);
                } else {
                    return new ResponseJson().errorOpen(code, Message);
                }
            } else {
                return new ResponseJson().errorOpen("400", "缺少参数！请按文档说明传入 ！");
            }
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }


    /**
     * 查询卡是否在网状态
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/simSession")
    @ResponseBody
    @LogAnnotation(action = "查询在网状态")
    public JSONObject simSession(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");

            Map<String,Object> Rdata = apiUtil_NoStatic.queryOnlineStatus(Param);
            String code = Rdata.get("code").toString();
            Map<String, Object> returnMap = new HashMap<String, Object>();
            if(code.equals("200")){
                returnMap.put("type", Rdata.get("type"));
                returnMap.put("typeName", Rdata.get("typeName"));
                returnMap.put("imei",Rdata.get("imei"));
                returnMap.put("statusCode", Rdata.get("statusCode"));
                returnMap.put("statusMessage", Rdata.get("statusMessage"));
                returnMap.put("cd_code",Rdata.get("cd_code"));
                returnMap.put("Data", Rdata.get("Data"));
                return new ResponseJson().successOpen(returnMap);
            }else{
                return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 获取共享token
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/sharedToken")
    @ResponseBody
    @LogAnnotation(action = "共享token")
    public JSONObject sharedToken(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            Map<String,Object> data = publicApiService.getToken(Param);
            Map<String, Object> returnMap = new HashMap<String, Object>();
            if(data!=null){
                String token = data.get("token").toString();
                if(token!=null){
                    returnMap.put("Data", data);
                    return new ResponseJson().successOpen(returnMap);
                }else{
                    return new ResponseJson().errorOpen("500","内部获取token失败");
                }
            }else{
                return new ResponseJson().errorOpen("500","未找到对应token获取");
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 资费计划 可用查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/tariffplan")
    @ResponseBody
    @LogAnnotation(action = "资费计划 可用查询")
    public JSONObject tariffplan(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");

        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            Map<String, Object> agentAccount = (Map<String, Object>) request.getAttribute("agentAccount");
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            String companyId  =  Param.get("companyId").toString();
            String agentID = agentAccount.get("agent_id").toString();
                 //判断 传入的 企业编号 是否与操企业编号一致
                if(agentID.equals(companyId)){
                    return new ResponseJson().successOpen(yzCardRouteMapper.findTariffplan(Param));
                }else{
                    return new ResponseJson().errorOpen("400", "操作企业编号不是您所在的企业请核对后重试！");
                }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 企业 预存余额查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/balance")
    @ResponseBody
    @LogAnnotation(action = "企业 预存余额查询")
    public JSONObject balance(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            Map<String, Object> agentAccount = (Map<String, Object>) request.getAttribute("agentAccount");
            ParamMap.putAll(JSON.parseObject((String) map));
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            String companyId  =  Param.get("companyId").toString();
            String agentID = agentAccount.get("agent_id").toString();
            //判断 传入的 企业编号 是否与操企业编号一致
            if(agentID.equals(companyId)){
                Map<String, Object> YcParamMap = new HashMap<String, Object>();
                YcParamMap.put("companyName",agentAccount.get("agent_name").toString());

                Map<String,Object> FindDeptAmountMap = new HashMap<>();
                FindDeptAmountMap.put("dept_id",agentID);
                Map<String,Object> dMap =  yzUserMapper.findDeptAmount(FindDeptAmountMap);
                Double companyBalance = 0.0;
                Double companyCredit = 0.0;
                if(dMap!=null){
                    companyBalance =  dMap.get("deposit_amount")!=null?Double.parseDouble(dMap.get("deposit_amount").toString()):companyBalance;
                    companyCredit =  dMap.get("be_usable_line_of_credit")!=null?Double.parseDouble(dMap.get("be_usable_line_of_credit").toString()):companyCredit;
                }
                YcParamMap.put("companyBalance",companyBalance);
                YcParamMap.put("companyCredit",companyCredit);
                return new ResponseJson().successOpen(YcParamMap);
            }else{
                return new ResponseJson().errorOpen("400", "操作企业编号不是您所在的企业请核对后重试！");
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }



    /**
     * 资费计划订购
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/subscribe")
    @ResponseBody
    @LogAnnotation(action = "资费计划订购")
    public JSONObject subscribe(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            Map<String, Object> agentAccount = (Map<String, Object>) request.getAttribute("agentAccount");
            ParamMap.putAll(JSON.parseObject( map));
            Map<String,Object> Rdata =  openApiServiceImpl.subscribe(ParamMap,agentAccount);
            boolean bool = (boolean) Rdata.get("bool");
            String code = Rdata.get("code").toString();
            if(bool){
                return new ResponseJson().successOpen(Rdata.get("Message"));
            }else{
                return new ResponseJson().errorOpen(code,Rdata.get("Message").toString());
            }
        } catch (Exception e) {
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }


    @RequestMapping(value = "/iotlink/callback")
    @ResponseBody
    @LogAnnotation(action = "资费订购回调")
    public JSONObject callback(HttpServletRequest request, HttpServletResponse response) {
        String map = (String) request.getAttribute("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            if(ParamMap!=null){
                return new ResponseJson().successOpen("已成功接收消息");
            }else {
                return new ResponseJson().errorOpen("500", "返回消息保存异常！");
            }
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().errorOpen("500", "操作失败请稍后重试！");
        }
    }




    public static void main(String[] args) {
        //  String url="http://hsapi.e114.xin:9002/Api/yunze/v1";


        Map<String, Object> Yzmap = new HashMap<String, Object>();

        // for(int i=0;i<300;i++){
       String geturl = "http://192.168.100.207:9080/route/open/";
        //String geturl = "http://192.168.100.207:9080/open/";
        // String geturl = "http://api.5iot.cn/route/open/";
        //String geturl = "http://admin.sdyunze.com:9080/route/open/";

        String appId = "ShanDongYunZe";
        String password = "IoTlink@2022";
        String access_key = "www.5iot.com/doc.5iot.com/demo.5iot.com";




        String timeStamp = System.currentTimeMillis() + "";


        Yzmap.put("password", password);
        Yzmap.put("appId", appId);
        //Yzmap.put("timeStamp", timeStamp);
        Yzmap.put("timeStamp", "1626335004");

        AgentCheckShiro agentCheckShiro = new AgentCheckShiro();

        String sign = agentCheckShiro.getSign(Yzmap, access_key);
        Yzmap.put("sign", sign);

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> Parmap = new HashMap<String, Object>();
        //Parmap.put("dateTime", "202011");
        geturl+="subscribe";//资费充值
        //geturl +="simDataUsage";//单卡流量查询
        //geturl +="queryFlowHis";//单卡历史流量查询
        //geturl +="simStatus";//单卡生命周期
        //geturl +="changeCardStatus";//单卡生命周期 变更
        //geturl += "queryRealNameStatus";//查询是否实名
        //geturl +="queryAPNInfo";//查询APN设置信息
       // geturl +="FunctionApnStatus";//单端断网
        //geturl +="SpeedLimit";//限速
        //geturl +="todayUse";//今日使用量
        //geturl +="queryOnlineStatus";//查询卡是否在网状态
       //geturl +="cardInfo";//卡信息
       //geturl +="test";//
        //geturl +="cardPacketInfo";//卡套餐详情
        //geturl +="sharedToken";//获取共享token
        // geturl +="simSession";
        // geturl +="simApnFunction";
        // geturl +="MachineCardBinding";//机卡解绑




        // Parmap.put("dateTime", "202011");
       // Parmap.put("Is_Stop", "on"); // 停复机
       // Parmap.put("Is_Break", "0"); // 【断网复机】 是否   0 开机 1 停机
       // Parmap.put("speedValue", "0"); // 【限速】 标准 值
       // Parmap.put("Is_Spee", "0"); // 【限速】 是否   0 增加 1 删除
       // Parmap.put("AutomaticRecovery", "0"); // 是否 【限速】月初是否恢复   0 自动恢复 1 不自动恢复

        map.put("verify", Yzmap);
        String result = null;
        Parmap.put("iccid", "89860466151980503267");
        Parmap.put("companyId","100");
        Parmap.put("effectiveType","1");
        Parmap.put("blocPlantId","YZ202201140123296552");
        Parmap.put("callbackAddress","http://192.168.100.207:9080/route/iotlink/callback");
        //Parmap.put("type", "iccid");
        //Parmap.put("StatusBreak", "1");
        //Parmap.put("cardno", "89860799999999999999");
        //Parmap.put("refreshToken", "1");

        //Parmap.put("contactName", "ZhangSan");
        //Parmap.put("contactPhone", "18666666666");




        map.put("Param", Parmap);

        try {
            String data = JSON.toJSONString(map);
            System.out.println(data);
            for (int i=0;i<1;i++){
                String qerUrl = UrlUtil.getUrl(geturl, map);
                System.out.println(qerUrl);
                System.out.println(JSON.toJSONString(data));
                //result = HttpUtil.get(qerUrl);// 返回结果字符串
                result = HttpUtil.post(geturl, data);// 返回结果字符串
                System.out.println("result↓↓↓↓↓↓");
                System.out.println(result);
                System.out.println("result↑↑↑↑↑↑");
            }
        } catch (Exception e) {
            //System.out.println(e);
        }

        //for(String keys:map.keySet()){

    }

}

