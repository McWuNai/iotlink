package com.yunze.apiCommon.aspect;

import com.cu.api.ApiException;
import com.yunze.apiCommon.utils.RateLimiterUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Aspect
@Component
@EnableAspectJAutoProxy
public class RateLimitingAspect {

    @Resource
    private RateLimiterUtil rateLimiterUtil;

    private static final int MAX_RETRIES = 3; // 设置最大重试次数

    @Pointcut(value = "execution(* com.yunze.apiCommon.upstreamAPI.PublicApiService.inside(..)) && args(Param, function_name, find_card_route_map)", argNames = "Param,function_name,find_card_route_map")
    public void rateLimitedMethods(Map<String, Object> Param, String function_name, Map<String, Object> find_card_route_map) {
    }

    @Around(value = "rateLimitedMethods(Param, function_name, find_card_route_map)", argNames = "joinPoint,Param,function_name,find_card_route_map")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint, Map<String, Object> Param, String function_name, Map<String, Object> find_card_route_map) throws Throwable {
        int retryCount = 0;
        while (retryCount <= MAX_RETRIES) {
            try {
                // 应用速率限制
                rateLimiterUtil.acquireForSpecificMethod(find_card_route_map.get("cd_username"), function_name);

                // 尝试执行目标方法
                return joinPoint.proceed();
            } catch (ApiException e) {
                if (isTooManyRequestsError(e)) {
                    handleTooManyRequestsError(retryCount);
                    retryCount++;
                    continue;
                }
                // 对于其他类型的 ApiException，记录堆栈跟踪并重新抛出异常
                System.err.println("其他类型的 API 异常: " + e.getMessage());
                throw e;
            } catch (Throwable t) {
                // 对于非 APIException 的异常，不予理会并抛出
                System.err.println("非 APIException 异常: " + t.getMessage());
                throw t;
            }
        }
        throw new RuntimeException("达到了最大重试次数");
    }

    private boolean isTooManyRequestsError(ApiException e) {
        return e.getCause() instanceof IOException && e.getMessage().contains("429 Too Many Requests");
    }

    private void handleTooManyRequestsError(int retryCount) {
        if (retryCount < MAX_RETRIES) {
            // 等待一段时间后重试，使用指数退避算法
            long sleepTime = (long) Math.pow(2, retryCount) * 1000; // 每次等待时间翻倍，单位毫秒
            try {
                Thread.sleep(sleepTime);
                System.out.println("遇到 429 Too Many Requests 错误，正在重试... (" + (retryCount + 1) + "/" + (MAX_RETRIES + 1) + ")");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println("线程睡眠被中断: " + ie.getMessage());
            }
        } else {
            System.out.println("达到最大重试次数: 429 Too Many Requests");
        }
    }
}