package com.yunze.iotapi.controller.MyEC;

import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.mapper.mysql.YzCardRouteMapper;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.ResJson;
import com.yunze.iotapi.service.impl.AgentAccountServiceImpl;
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
 *  适配返回移动官方数据
 */
@Controller
@RequestMapping("/v5/ec")
public class ECAapiController {

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;

    @Resource
    private AgentAccountServiceImpl agentAccountService;


    /**
     * 获取token
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/get/token")
    @ResponseBody
    public JSONObject getToken(HttpServletRequest request, HttpServletResponse response) {
        //System.out.println("/get/token--------");

        String appid = request.getParameter("appid").toString();
        String password = request.getParameter("password").toString();
        String transid = request.getParameter("transid").toString();
        //System.out.println("appid = "+appid+"  password = "+password+" transid = "+transid);

        List<JSONObject> result = new ArrayList<JSONObject>();
        JSONObject jobj =  new JSONObject();
        try {

            Map<String,Object> pMap = new HashMap<>();
            pMap.put("app_id",appid);
            pMap.put("password",password);
            Map<String,Object> agentAccount =agentAccountService.getOne(pMap);
            //System.out.println(agentAccount);
            if(null!=agentAccount){
                jobj.put("token",agentAccount.get("times"));
            }else{
                jobj.put("token","-1");
                result.add(jobj);
                return new ResJson("12022","PASSWORD 鉴权不通过",result);
            }
            result.add(jobj);
            //System.out.println(result);
            return new ResJson().success(result);
        } catch (Exception e) {
            //System.out.println(e);
            jobj.put("token","-1");
            result.add(jobj);
            return new ResJson("12022","PASSWORD 鉴权不通过",result);
        }
    }


    /**
     * 单卡本月流量累计使用量查询
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/query/sim-data-usage")
    @ResponseBody
    public JSONObject simDataUsage(HttpServletRequest request, HttpServletResponse response) {
        //System.out.println("/query/sim-data-usage--------");

        String msisdn = request.getParameter("msisdn").toString();
        ////System.out.println("msisdn = "+msisdn);

        List<JSONObject> result = new ArrayList<JSONObject>();
        JSONObject jobj =  new JSONObject();
        try {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("iccid",msisdn);
            ////System.out.println(map.toString());
            String used = yzCardRouteMapper.find_Out_used(map);
            ////System.out.println(used);
            if(null!=used){
                Double Mb = Double.parseDouble(used);
                Mb  = Mb*1024;
                jobj.put("dataAmount",Mb);
            }else{
                return new ResJson("11007","查询号码全部非法，请确认号码正确性",result);
            }
            result.add(jobj);
            ////System.out.println(result);
            return new ResJson().success(result);
        } catch (Exception e) {
            //System.out.println(e);
            jobj.put("token","-1");
            result.add(jobj);
            return new ResJson("12022","PASSWORD 鉴权不通过",result);
        }
    }







}

