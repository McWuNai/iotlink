package com.yunze.iotapi.utils;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.yunze.apiCommon.utils.MD5Util;
import com.yunze.iotapi.service.impl.AgentAccountServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Component
public class AgentCheckShiro {

    @Resource
    private AgentAccountServiceImpl agentAccountService;



    /**
     * @Author:
     * @Return: 检查空值
     * @Description:
     * @Date: 2021/11/6 0006 18:04
     */
    public boolean checkNull(Map<String, Object> pageMap, HttpServletResponse response){
        JSONObject json=new JSONObject();
        for(String key :pageMap.keySet()){
            if(StringUtils.isNullOrEmpty((String)pageMap.get(key))){
                json.put("code",-1);
                json.put("msg",key+"不能为空");
                json.put("data","");
                responseOutWithJson(response,json);
                return false;
            }
        }
        return true;
    }

    /**
     * @Author:
     * @Return: 检查sign
     * @Description:
     * @Date: 2021/11/6 0006 18:15
     */
    public boolean checkAccount(Map<String,Object> map, HttpServletResponse response) {
        String appId=(String )map.get("appId");
        String password =(String )map.get("password");
        JSONObject json=new JSONObject();
        //查询是否有此账户
        Map<String,Object> pMap = new HashMap<>();
        pMap.put("app_id",appId);
        pMap.put("password",password);
        Map<String,Object> agentAccount = agentAccountService.getOne(pMap);
        if(null==agentAccount){
            json.put("code",99);
            json.put("msg","appId或者password错误");
            json.put("data","");
            response.reset();
            responseOutWithJson(response,json);
            return false;
        }
        return true;
    }



    /**
     * @Author:
     * @Return: 检查sign是否准确
     * @Description:
     * @Date: 2021/11/6 0006 18:54
     */
    public boolean checkSign(Map<String,Object> map, HttpServletRequest request, HttpServletResponse response){
        JSONObject json=new JSONObject();
        String appId=(String )map.get("appId");
        String sign =(String )map.get("sign");
        String  now =(String )map.get("timeStamp");
        Long timeCha =System.currentTimeMillis()-Long.parseLong(now);
        if(timeCha>60*1000){
            json.put("code",-11);
            json.put("msg","时效已过，请重新调用");
            json.put("data","");
            responseOutWithJson(response,json);
            return false;
        }

        //数据库获取对应key
        Map<String,Object> pMap = new HashMap<>();
        pMap.put("app_id",appId);
        Map<String,Object> agentAccount = agentAccountService.getOne(pMap);



        String key = agentAccount.get("access_key").toString();
        //批量查询的通过
        String url=request.getServletPath();
        if(url.contains("Batch")){
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()){// 循环取键值进行判断
                String m = iterator.next();// 键
                if(m.startsWith("iccids")){
                    iterator.remove();// 移除map中以a字符开头的键对应的键值对
                }
            }
        }
        String signData=getSign(map,key);
        if(!sign.equals(signData)){
            json.put("code",-12);
            json.put("msg","签名不正确");
            json.put("data","");
            responseOutWithJson(response,json);
            return false;
        }


        return true;

    }

    public String getSign(Map<String,Object> map,String key){//入参为：appid,password,version,iccid,timestamp,sign
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()){// 循环取键值进行判断
            String m = iterator.next();// 键
            if(m.startsWith("sign")){
                iterator.remove();// 移除map中以a字符开头的键对应的键值对
            }
            if(m.startsWith("iccids")){
                iterator.remove();
            }
        }
        List<String> list=new ArrayList<>(map.keySet());
        Collections.sort(list);
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<list.size();i++){
            String k =list.get(i);
            String v=(String )map.get(k);
            sb.append(k).append("=").append(v).append("&");
        }
        String signstr=sb.append("key=").append(key).toString();
        //System.out.println(signstr);
        String sign = MD5Util.MD5Encode(signstr).toUpperCase();
        //System.out.println(sign);
        return sign;
    }





    /**
     * @Author:
     * @Return: 以json格式返回
     * @Description:
     * @Date: 2021/11/6 0006 18:07
     */
    protected void responseOutWithJson(HttpServletResponse response, JSONObject responseObject) {
        //将实体对象转换为JSON Object转换
        JSONObject responseJSONObject = JSONObject.parseObject(responseObject.toString());
        // PrintWriter out = null;
        OutputStream out=null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            out=response.getOutputStream();
            out.write(responseJSONObject.toJSONString().getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try{
                    out.close();
                }catch(IOException es){
                    es.printStackTrace();
                }

            }

        }
    }

}
