package com.yunze.iotapi.config;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.iotapi.service.impl.AgentAccountServiceImpl;
import com.yunze.iotapi.service.impl.SysLogsServiceImpl;

import com.yunze.iotapi.utils.LogAnnotation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogicAspectSys {

    public static final Log LOG = LogFactory.getLog(LogicAspectSys.class);

    @Resource
    private SysLogsServiceImpl sysLogsService;
    @Resource
    private AgentAccountServiceImpl agentAccountService;


    /**
     * 描述： 定义切入点
     */
    @Pointcut("execution(* com.yunze.iotapi.controller.openApi.*.*(..))")
                    public void logPointcut(){
    }

    /**
     * 描述： 环绕通知
     */
    @Around("logPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        //System.out.println("环绕通知---------------");
        LOG.info("==Method  start=");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            try {
                //获取sql
                //String sql= SqlUtils.getMybatisSql(joinPoint,sqlSessionFactory);
                // LOG.info("执行sql:"+sql);

                HttpServletRequest Myrequest = (HttpServletRequest)joinPoint.getArgs()[0];
                String RequestArgs =   (String) Myrequest.getAttribute("map");

                LOG.info("请求地址:" + request.getRequestURI());
                LOG.info("用户IP:" + IpUtils.getIpAddr(Myrequest));
                LOG.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
                LOG.info("参数: " + RequestArgs);
                // 2.打印出参，返回结果
                long endTime = System.currentTimeMillis();
                // 3.出参打印
                LOG.info("RESPONSE:{}"+result );
                LOG.info("执行时间 : "+(end - start) + " ms!");
                LOG.info("==Method  End=");

                //插入系统日志表




                Map<String, Object> ParamMap = new HashMap<String, Object>();
                String appId = null;
                Map<String,Object> agentAccount = null;
                if(RequestArgs!=null && !RequestArgs.equals("")){
                    ParamMap.putAll(JSON.parseObject(RequestArgs));
                    Map<String, Object> verify = (Map<String, Object>) ParamMap.get("verify");
                    //System.out.println(verify);
                    if(verify!=null && !verify.equals("") && verify.get("appId")!=null){
                        appId =  verify.get("appId").toString();
                        Map<String,Object> pMap = new HashMap<>();
                        pMap.put("app_id",appId);
                         agentAccount = agentAccountService.getOne(pMap);
                    }
                }
                String userName="sys";
                if(agentAccount!=null){
                    userName = agentAccount.get("agent_name").toString();
                }
                //日志阶段
                RequestArgs  = RequestArgs.length()>500?RequestArgs.substring(0,500):RequestArgs;
                //获取注解上的方法名
                MethodSignature methodSignature= (MethodSignature) joinPoint.getSignature();
                Method method=methodSignature.getMethod();
                LogAnnotation logAnnotation= method.getAnnotation(LogAnnotation.class);
                String actionName=null;
                if(null!=logAnnotation){
                    actionName=logAnnotation.action();
                    Map<String,Object> sysLogs = new HashMap<>();


                    sysLogs.put("id", VeDate.getStringDateSSS());
                    sysLogs.put("ip",IpUtils.getIpAddr(Myrequest));
                    sysLogs.put("request_args",RequestArgs);
                    sysLogs.put("response_args",result.toString());
                    sysLogs.put("res_url",request.getRequestURI());
                    sysLogs.put("class_method",joinPoint.getSignature().getDeclaringTypeName());
                    sysLogs.put("user_name",userName);
                    sysLogs.put("project","iotapi");
                    sysLogs.put("action_name",actionName);


                    boolean flag = sysLogsService.save(sysLogs)>0;
                }
                return result;
            } catch (Exception e) {
                LOG.info("AOP切面报错:{}"+ e.getMessage());
                throw e;
            }
        } catch (Throwable e) {
            long end = System.currentTimeMillis();
            LOG.info("URL:" + request.getRequestURI());
            LOG.info("IP:" + request.getRemoteAddr());
            LOG.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
            LOG.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));
            LOG.info("执行时间: " + (end - start) + " ms!");
            LOG.info("==Method  End=");
            throw e;
        }
    }
}
