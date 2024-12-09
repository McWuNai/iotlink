package com.yunze.iotapi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.utils.ip.IpUtils;
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
 * @Auther: zhang feng
 * @Date: 2022/05/25/17:25
 * @Description:
 */
@Controller
@RequestMapping("/set")
public class MySet {


    @Resource
    private YzCardRouteMapper yzCardRouteMapper;


    /**
     * 参数 序列化
     * @param map
     * @return
     */
    public Map<String, Object> getParamMap(String map){
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null && map.length()>2){
            map = map.substring(1,map.length());
            map = map.substring(0,map.length()-1);
            if(map.indexOf(",")!=-1){
                String []lieArr = map.split(",");
                for (int i = 0; i < lieArr.length; i++) {
                    String objArr[] = lieArr[i].split("=");
                    ParamMap.put(""+objArr[0].trim(),objArr[1].trim());
                }
            }else if(map.indexOf("=")!=-1){
                String objArr[] = map.split("=");
                ParamMap.put(""+objArr[0].trim(),objArr[1].trim());
            }
        }

        return ParamMap;
    }



    /**
     * 设置token
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/token")
    @ResponseBody
    public JSONObject queryFlow(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("token--------");
        String map = (String) request.getAttribute("map");
        System.out.println(map);
        String ip = IpUtils.getIpAddr(request);
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        try {
            ParamMap.putAll(JSON.parseObject((String) map));

            if(ParamMap.get("appid")!=null && ParamMap.get("token")!=null){
                String appid = ParamMap.get("appid").toString();
                String token = ParamMap.get("token").toString();
                ParamMap.put("cd_username",appid);
                Map<String,Object> rout = yzCardRouteMapper.findUsername(ParamMap);
                if(rout !=null){
                    Map<String,Object> updMap = new HashMap<>();
                    updMap.put("cd_id",rout.get("cd_id"));
                    updMap.put("cd_key",token);
                    yzCardRouteMapper.updateKey(updMap);
                }
            }



            return new ResponseJson().successOpen(" ip :"+ip+" 推送成功！");
        } catch (Exception e) {
            //System.out.println(e);
            return new ResponseJson().errorOpen("500", " ip :"+ip+" 推送成功！");
        }
    }



}
