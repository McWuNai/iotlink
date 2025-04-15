package com.yunze.apiCommon.utils;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimiterUtil {

    private final ConcurrentMap<String, RateLimiter> globalRateLimiters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RateLimiter> specificMethodRateLimiters = new ConcurrentHashMap<>();
    private final HashMap<String, Double> tokenCountMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 这个方法会在类初始化后立即执行
        this.tokenCountMap.put("queryFlow", 5.0);
        this.tokenCountMap.put("queryCardActiveTime", 5.0);
        this.tokenCountMap.put("queryFlowHis", 5.0);
        this.tokenCountMap.put("queryCardStatus", 5.0);
        this.tokenCountMap.put("changeCardStatus", 10.0);
        this.tokenCountMap.put("changeCardStatusFlexible", 10.0);
        this.tokenCountMap.put("queryRealNameStatus", 5.0);
        this.tokenCountMap.put("queryAPNInfo", 5.0);
        this.tokenCountMap.put("SpeedLimit", 1.0);
        this.tokenCountMap.put("FunctionApnStatus", 5.0);
        this.tokenCountMap.put("MachineCardBinding", 5.0);
        this.tokenCountMap.put("realNameRemove", 5.0);
        this.tokenCountMap.put("queryBatchFlow", 5.0);
        this.tokenCountMap.put("wsQueryIdByName", 10.0);
        this.tokenCountMap.put("addRatePlan", 10.0);

        System.out.println("tokenCountMap 初始化完成");
    }

    // 全局限流器，每个账号每秒最多10个请求
    /*public RateLimiter getGlobalRateLimiter(String cd_userName) {
        return globalRateLimiters.computeIfAbsent(cd_userName, userName -> RateLimiter.create(130.0));
    }*/

    // 特定方法限流器，每个账号每秒最多N个请求
    public RateLimiter getSpecificMethodRateLimiter(String cd_userName, String methodName) {
        // 使用用户名和方法名作为复合键
        String compositeKey = buildCompositeKey(cd_userName, methodName);
        return specificMethodRateLimiters.computeIfAbsent(compositeKey, k -> RateLimiter.create(tokenCountMap.get(methodName)));
    }

    private String buildCompositeKey(String cd_userName, String methodName) {
        return cd_userName + ":" + methodName;
    }

    public void acquireForSpecificMethod(Object cd_userName, String methodName) {
        // 将 cd_userName 转换为字符串
        String userNameStr = cd_userName.toString();

        // 获取全局限流器
        //RateLimiter globalRateLimiter = getGlobalRateLimiter(userNameStr);

        //获取方法名对应的令牌预设值

        // 获取特定方法限流器
        RateLimiter specificMethodRateLimiter = getSpecificMethodRateLimiter(userNameStr, methodName);

        // 尝试获取特定方法的令牌，若无足够令牌，则等待
        specificMethodRateLimiter.acquire(1);

        // 尝试获取全局的令牌，若无足够令牌，则等待
        //globalRateLimiter.acquire(1);
    }

    // 用于单重限流检查
    /*public void acquireForNormalMethod(Object cd_userName) {
        RateLimiter globalRateLimiter = getGlobalRateLimiter(cd_userName.toString());

        // 尝试获取全局的令牌，若无足够令牌，则等待
        globalRateLimiter.acquire(1);
    }*/
}
