package com.yunze.iotapi.controller.MyEC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.PublicApiService;
import com.yunze.iotapi.utils.ResponseJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 *  东信 移动 V2 接口调用
 */
@Controller
@RequestMapping("/DXV2")
public class DxECV2AapiController {

    @Resource
    private PublicApiService publicApiService;


    /**
     * 东信ECV2 接口 调用
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/EcV2option")
    @ResponseBody
    public JSONObject EcV2option(HttpServletRequest request, HttpServletResponse response) {
        //System.out.println("EcV2option--------");
        String map = (String) request.getAttribute("map");

        //System.out.println(map);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));
            //System.out.println(ParamMap);
            Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
            //System.out.println(Param);
            Map<String,Object> data = publicApiService.EcV2(Param);
            //System.out.println(data);
            return new ResponseJson().success(data);
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().error("500", "操作失败请稍后重试！");
        }
    }




}

