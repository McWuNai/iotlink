package com.yunze.wechat.aop;

import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.business.entity.SysLogs;
import com.yunze.business.service.impl.SysLogsServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

@Component
@Aspect
public class LogicAspect {

    public static final Log LOG = LogFactory.getLog(LogicAspect.class);

    @Resource
    private RedisUtil redisUtil;
    private SysLogsServiceImpl sysLogsService;

    /**
     * 描述： 定义切入点 只拦截controller
     */
    @Pointcut("execution(* com.yunze.wechat.controller..*.*(..))")
                    public void logPointcut(){
    }

    /**
     * 描述： 环绕通知
     */
    @Around("logPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("环绕通知---------------");
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
                LOG.info("请求地址:" + request.getRequestURI());
                LOG.info("用户IP:" + IpAddressUtils.getIpAdrress(request));
                LOG.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
                LOG.info("参数: " + Arrays.toString(joinPoint.getArgs()));
                // 2.打印出参，返回结果
                long endTime = System.currentTimeMillis();
                // 3.出参打印
                LOG.info("RESPONSE:{}"+result );
                LOG.info("执行时间 : "+(end - start) + " ms!");
                LOG.info("==Method  End=");

                //插入系统日志表
                String userId=request.getHeader("userId");
                Map<String,Object> account=(Map<String,Object>)redisUtil.get(userId+"loginUser");
                String userName="sys";
                if(null!=account){
                    userName=account.get("userName").toString();
                }
                SysLogs sysLogs=new SysLogs();
                sysLogs.setIp(IpAddressUtils.getIpAdrress(request));
                sysLogs.setRequestArgs(Arrays.toString(joinPoint.getArgs()));
                sysLogs.setResponseArgs(result.toString());
                sysLogs.setResUrl(request.getRequestURI());
                sysLogs.setClassMethod(joinPoint.getSignature().getDeclaringTypeName());
                sysLogs.setUserName(userName);
                sysLogs.setProject("wechat");
                sysLogsService.save(sysLogs);
                return result;
            } catch (Exception e) {
                LOG.info("RESPONSE ERROR:{}"+ e.getMessage());
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
