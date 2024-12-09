package com.yunze.iotapi.interceptor;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.mapper.YzIpWhitelistMapper;
import com.yunze.apiCommon.utils.AesEncryptUtil;
import com.yunze.apiCommon.utils.Different;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.iotapi.utils.ResponseJson;
import com.yunze.iotapi.service.impl.AgentAccountServiceImpl;

import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



import java.io.ByteArrayOutputStream;




@WebFilter(filterName = "authFilter", urlPatterns = "/*")
public class Option_Filter implements Filter {

    @Resource
    private RedisUtil redisUtil;


    @Resource
    private AgentAccountServiceImpl agentAccountService;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzIpWhitelistMapper yzIpWhitelistMapper;



    public static final String CLOSE_LIMIT_PRE = "close_limit_pre";

    private int closeSeconds = 1;
    private int times = 0;




   /* @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        this.redisTemplate = redisTemplate;
    }
*/
//java获取raw
   public static String readRaw(HttpServletRequest request) {
       String result = "";
       try {
           InputStream inputStream = request.getInputStream();
           ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
           byte[] buffer = new byte[1024];
           int len;
           while ((len = inputStream.read(buffer)) != -1) {
               outSteam.write(buffer, 0, len);
           }
           outSteam.close();
           inputStream.close();
           result = new String(outSteam.toByteArray(), "UTF-8");
       } catch (Exception e) {
           e.printStackTrace();
       }
       return result;
   }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {


        //System.out.println("=====  正在进入过滤器 ====== ");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json; charset=utf-8");

        String ServletPath =    request.getServletPath();
        //System.out.println("过滤器："+JSON.toJSONString(request.getParameterMap()));

       /* System.out.println(request.getServletPath());
        System.out.println(request.getContextPath());
        System.out.println(request.getServletContext().getContextPath());
        System.out.println(request.getPathInfo());
        System.out.println(request.getRequestURI());
        System.out.println(request.getRequestURL());
        System.out.println(request.getServletContext().getRealPath("/"));*/



      /*  System.out.println(JSON.toJSONString(request.getParameter("token")));
        System.out.println(JSON.toJSONString(request.getParameter("token")));
        System.out.println(JSON.toJSONString(request.getParameter("token")));
        System.out.println(JSON.toJSONString(request.getParameter("token")));*/

        //权限校验
        JSONObject jsonObject = new ResponseJson();//返回

        jsonObject.put("status","-1");
        jsonObject.put("message","未知错误。");


        String result = "";
        //获取url
        String requestUrl = request.getRequestURI();

        if(ServletPath!=null && ServletPath.length()>7){
            //System.out.println("请求路径 ： "+requestUrl);

            //判断回调充值回调反馈 【模拟用户接口 一般处于注释状态】【start】
            int sIndex = ServletPath.indexOf("/open/iotlink/callback");
            if(sIndex!=-1){
                ServletPath = ServletPath.substring(sIndex);
                if(ServletPath.equals("/open/iotlink/callback")){
                    String jsonParam = HttpUtil.getJsonParam(request);
                    request.setAttribute("map",jsonParam);
                    filterChain.doFilter(request, response);
                    return ;
                }
            }
            //【end】
            ServletPath = ServletPath.substring(0,6);
            String url[]  = ServletPath.split("/");
            //System.out.println(url[0]+" = "+url[1]);

            if (url[1].equals("v5")){
                //System.out.println("/v5 > 》》》》》》》》》》》》。");

                request.setAttribute("map",JSON.toJSONString(request.getParameterMap()));
                //System.out.println(request.getAttribute("map"));

                filterChain.doFilter(request, response);
                return ;
            }else if (url[1].equals("DXV2")){
                //System.out.println("/DXV2 > 放行》》》》》》》》》》》》。");

                request.setAttribute("map",JSON.toJSONString(request.getParameterMap()));
                //System.out.println(request.getAttribute("map"));

                filterChain.doFilter(request, response);
                return ;
            }else if (url[1].equals("LTCMP")){
                System.out.println("/LTCMP > 放行》》》》》》》》》》》》。");
                request.setAttribute("map",JSON.toJSONString(request.getParameterMap()));
                //System.out.println(request.getAttribute("map"));
                filterChain.doFilter(request, response);
                return ;
            }else if (url[1].equals("set")){
                String params = readRaw(request);
                System.out.println("set "+params);
                String ip = IpUtils.getIpAddr(request);
                Map<String, Object> fMap = new HashMap<>();
                fMap.put("ip",ip);
                Map<String, Object> ipMap  =   yzIpWhitelistMapper.findWIp(fMap);
                System.out.println(ip);
                if(ipMap!=null){
                    System.out.println("/set > 放行》》》》》》》》》》》》。"+ipMap.get("wname"));
                    request.setAttribute("map",params);
                    filterChain.doFilter(request, response);
                    return ;
                }else{
                    jsonObject.put("status","1003");
                    jsonObject.put("message","无访问权限");
                    jsonObject.put("result",null);
                    sendMessage(response,jsonObject.toJSONString());
                    return;
                }
            }else if (url[1].equals("call")){
                System.out.println("/call > 》》》》》》》》》》》》。");
                String params = readRaw(request);
                System.out.println("set "+params);
                request.setAttribute("map",params);
                filterChain.doFilter(request, response);
                return ;
            }else if (url[1].equals("open")){
                //System.out.println(" open  验证开始 》》》》》》》》》》》》》》》");

                String jsonParam = HttpUtil.getJsonParam(request);
                Map<String, Object> ParamMap = new HashMap<String, Object>();
                ParamMap.putAll(JSON.parseObject((String) jsonParam));
                //转发
                Release(jsonParam,request,response,requestUrl,ParamMap,jsonObject,filterChain,requestUrl.split("/"));
                return ;
            }
        }

        //System.out.println("=====  当前请求 url为  ====== {" + requestUrl + "} ");
        String jsonParam = HttpUtil.getJsonParam(request);
        //System.out.println(" 请求 json 数据为  ： {" + jsonParam + "} ");

            try{
                //请求数据解密
                jsonParam = AesEncryptUtil.desEncrypt(jsonParam);
            }catch (Exception e){
                //System.out.println("通道解密数据异常");
                jsonObject.put("status","-500");
                jsonObject.put("message","数据解密异常");
                sendMessage(response,jsonObject.toJSONString());
                return;
            }

        Map<String, Object> ParamMap = new HashMap<String, Object>();

        ParamMap.putAll(JSON.parseObject((String) jsonParam));

        //转发
        Release(jsonParam,request,response,requestUrl,ParamMap,jsonObject,filterChain,null);



        /*PrintWriter writer = null;
        try {
            writer = response.getWriter();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.print(result);
            writer.flush();
            writer.close();
        }*/

    }


    /**
     * 转发
     * @param jsonParam
     * @param request
     * @param response
     * @param requestUrl
     * @param ParamMap
     * @param jsonObject
     * @param filterChain
     * @throws Exception
     */
    public  void Release(String jsonParam,HttpServletRequest request,HttpServletResponse response,String requestUrl,Map<String, Object> ParamMap,JSONObject jsonObject, FilterChain filterChain,String url[]) throws Exception {


        Map<String, Object> verify = (Map<String, Object>) ParamMap.get("verify");
        Map<String, Object> Param = (Map<String, Object>) ParamMap.get("Param");
        //System.out.println(verify);
        String appId = null;
        if(verify!=null){
            appId =  verify.get("appId").toString();
        }

        if(null==requestUrl||null==appId){
            sendMessage(response,jsonObject.toJSONString());
            return;
        }else{

            //获取当前用户的调用频率
            //System.out.println("preHandle===[获取当前用户的调用频率]======");
            Map<String,Object> pMap = new HashMap<>();
            pMap.put("app_id",appId);
            Map<String,Object> agentAccount =agentAccountService.getOne(pMap);

            if(null==agentAccount){
                jsonObject.put("status","1001");
                jsonObject.put("message","账户查询失败");
                jsonObject.put("result",null);
                sendMessage(response,jsonObject.toJSONString());
                return;
            }
            times = Integer.parseInt(agentAccount.get("times").toString());
            boolean flag=isClose(appId,requestUrl,times);
            if(flag){
                jsonObject.put("status","1002");
                jsonObject.put("message","访问频率超限，请稍后访问");
                jsonObject.put("result",null);
                sendMessage(response,jsonObject.toJSONString());
                return;
            }

            //判断是否是 系统账户
            int Typesys = Integer.parseInt(agentAccount.get("typesys").toString());
            if(Typesys==1){
                if (!StringUtils.isEmpty(requestUrl)) {

                    //根据url去查询模板配置
                    //jsonParam = getJsonParam(request);
                    //System.out.println("放行》》》》》》》》》》》》。");
                    request.setAttribute("map",jsonParam);
                    filterChain.doFilter(request, response);
                }
            }else{

                //判断访问路径
                String qurl = url[3];
                List<String> defaultUrl = new ArrayList<String>();
                defaultUrl.add("cardInfo");
                defaultUrl.add("cardPacketInfo");
                String[] optionUrl = {};
                if(agentAccount.get("openurl")!=null){
                    String Openurl = agentAccount.get("openurl").toString();
                    if(Openurl!=null && Openurl!="" && Openurl.length()>0){
                        optionUrl = Openurl.indexOf(",") != -1?Openurl.split(","): new String[]{Openurl};
                    }
                }

        /*        optionUrl.add("queryFlow");
                optionUrl.add("queryFlowHis");
                optionUrl.add("queryCardStatus");
                optionUrl.add("changeCardStatus");
                optionUrl.add("FunctionApnStatus");
                optionUrl.add("queryOnlineStatus");*/

                //默认允许访问 接口
                if(Different.Is_existence(defaultUrl,qurl)){

                    Map pmap = new HashMap<>();
                    pmap.put("agent_id",agentAccount.get("agent_id"));
                    pmap.put("type",Param.get("type"));
                    pmap.put("cardno",Param.get("cardno"));
                   Integer count =  yzCardRouteMapper.IsExistence(pmap);
                    if(count!=null && count>0){
                        Param.put("agent_id",agentAccount.get("agent_id"));
                        ParamMap.put("Param",Param);
                        request.setAttribute("map", JSON.toJSONString(ParamMap));
                        filterChain.doFilter(request, response);
                    }else{
                        jsonObject.put("status","1004");
                        jsonObject.put("message","未找到企业对应号码信息请核对后重试！");
                        jsonObject.put("result",null);
                        sendMessage(response,jsonObject.toJSONString());
                        return;
                    }
                }else if(Different.Is_existence(optionUrl,qurl)){
                    //特殊操作访问接口
                    //【获取token】【查询资费计划可用】【预存余额查询】 接口直接放行 不需要简检验 号码
                    if(qurl.equals("sharedToken") || qurl.equals("tariffplan") || qurl.equals("balance")){
                        request.setAttribute("map",jsonParam);
                        request.setAttribute("agentAccount",agentAccount);
                        filterChain.doFilter(request, response);
                    }else{
                        Map pmap = new HashMap<>();
                        pmap.put("agent_id",agentAccount.get("agent_id"));
                        pmap.put("type","iccid");
                        pmap.put("cardno",Param.get("iccid"));
                        Integer count =  yzCardRouteMapper.IsExistence(pmap);
                        if(count!=null && count>0){
                            request.setAttribute("map",jsonParam);
                            request.setAttribute("agentAccount",agentAccount);
                            filterChain.doFilter(request, response);
                        }else{
                            jsonObject.put("status","1004");
                            jsonObject.put("message","未找到企业对应号码信息请核对后重试！");
                            jsonObject.put("result",null);
                            sendMessage(response,jsonObject.toJSONString());
                            return;
                        }
                    }
                }else{
                    jsonObject.put("status","1003");
                    jsonObject.put("message","无访问权限");
                    jsonObject.put("result",null);
                    sendMessage(response,jsonObject.toJSONString());
                    return;
                }

            }
            // 转发
        }
    }


    public boolean isClose(String appId, String method, int times) {
        String key = CLOSE_LIMIT_PRE + appId + "_api" + method;
        Long count = redisUtil.increment(key, 1);
        if (count == 1) {
            redisUtil.expire(key, 60, TimeUnit.SECONDS);
        }
        if (count > times) {
            //System.out.println(appId + "--当前账户访问太过频繁，频率为--" + count);
            return true;
        }
        //System.out.println(appId + "--测试批量一分钟调用次数：" + count);
        return false;
    }

    /**
     * @Author:
     * @Return: 发送消息
     * @Description:
     * @Date: 2021/11/9 0009 13:19
     */
    public static void sendMessage(ServletResponse response, String str) throws Exception {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.print(str);
        writer.close();
        response.flushBuffer();
    }

}
