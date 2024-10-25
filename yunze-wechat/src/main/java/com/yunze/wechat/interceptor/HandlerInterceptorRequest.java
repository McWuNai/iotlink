package com.yunze.wechat.interceptor;

import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.cn.util.CheckIsNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class HandlerInterceptorRequest extends HandlerInterceptorAdapter {

    @Resource
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler)
            throws Exception {

        HttpSession session = request.getSession();
        //判断当前url
        String url=request.getRequestURI();

        String urlEureka="http://" + request.getServerName() //服务器地址
                + ":"
                + request.getServerPort()           //端口号
                + request.getRequestURI();
        System.out.println("ereuke====="+urlEureka);
        System.out.println(url);
        //针对对外接口暂时不做拦截，后期做登录校验
        if(url.contains("/system/qu-flow-info")
                ||url.contains("/system/change-TF")
                ||url.contains("/system/netChange")
                ||url.contains("/system/qu-iot-auth")
                ||url.contains("/system/qu-status-info")){
            return true;
        }
        if(urlEureka.contains("/system/account/saveUser")
                ||url.equals("/agent/prestore/notifyNative")
        ||url.contains("/selfapi/zy-api")||urlEureka.contains("/cancelCard")){
            return true;
        }

        //登录验证
        System.out.println("前端token" + request.getHeader("token"));
        String wtoken = request.getHeader("token");

        //不是公众号走下面
        if(!url.startsWith("/web")){

            //获取当前登录的账户id
            String userId=null;
            if(!CheckIsNull.isNull(wtoken)){
                userId =wtoken.split(" ")[1].split("-")[0];
                System.out.println("userId为："+userId);
            }
            String stoken = null;
            if(url.startsWith("/agent")){
                if("/agent/getAccount".equals(url)||url.equals("/agent/agent-packet/uploadBR")
                        ||url.contains("/agent/agentExportExcel")||url.contains("/agent/pro_data/proExportExcel")){
                    return true;
                }
                stoken = (String)redisUtil.get(userId+"-auth-agent-token");
            }else{
                if("/system/account/getAccount".equals(url)||"/system/uploadExcel".equals(url) ||url.contains("/wechat/portal")
                        ||"/system/replace/uploadReplace".equals(url)
                        ||"/system/replace/plImportReplace".equals(url)
                        ||"/system/replace/initFlow".equals(url)
                ||url.contains("/system/exportExcel")
                ||"/system/order/exportSucOrder".equals(url)
                ||"/system/imei-contro/up-ha-im".equals(url)
                ||"/system/imei-contro/up-sup-file".equals(url)){//todo
                    return true;
                }
                stoken = (String)redisUtil.get(userId+"-auth-sys-token");
            }
            System.out.println("后端保存token："+stoken);
            if (wtoken == null || stoken == null) {
                returnJson(response, "500-webnolg");
                return false;
            } else if (!stoken.equals(wtoken.split(" ")[1])) {
                redisUtil.delKey(wtoken);
                returnJson(response, "500-lanjie");
                return false;
            } else {
                return true;
            }
        }else{
            //获取当前登录的账户id
            String userId=null;
            if(!CheckIsNull.isNull(wtoken)){
                userId =wtoken.split(" ")[1].split("-_yunzeweb_-")[0];
                System.out.println("userId为："+userId);
            }
            if(url.startsWith("/web/authorize")||url.startsWith("/web/gt-info")
                    ||url.startsWith("/web/ck-bind")||url.startsWith("/web/bind-card")
                    ||url.startsWith("/web/weixin/pay/weixinPayNotify")
                    ||url.startsWith("/web/CardReplace/findSimpleData")
                    ||url.startsWith("/web/CardReplace/update_info")
                    ||url.startsWith("/web/CardReplace/findPackage")
                    ||url.startsWith("/web/weixin/pay/changeCardWeixinPay")
                    ||url.startsWith("/web/weixin/question/get-questions")
                    ||url.startsWith("/web/weixin/shopping/cy-readImage")
                    ||url.startsWith("/web/weixin/shopping/setToken")
            ){
                return true;
            }
            //登录验证
            System.out.println("前端web-token" + request.getHeader("token"));
            String webToken = request.getHeader("token");
            String sysToken = (String)redisUtil.get(userId+"-yzAuth-web-token");

            System.out.println("后端保存token："+sysToken);
            if (webToken == null || sysToken == null) {
                returnJson(response, "500-webnolg");
                return false;
            } else if (!sysToken.equals(webToken.split(" ")[1])) {
                redisUtil.delKey(webToken);
                redisUtil.delKey(sysToken);
                returnJson(response, "500-weblanjie");
                return false;
            } else {
                return true;
            }
        }
    }

    //返回json数据
    private void returnJson(HttpServletResponse response, Object json) throws Exception{
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json.toString());

        } catch (IOException e) {
        } finally {
            if (writer != null)
                writer.close();
        }
    }

}
