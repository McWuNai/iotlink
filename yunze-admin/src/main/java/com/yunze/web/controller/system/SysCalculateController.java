package com.yunze.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 公司信息配置
 * 
 * @author Wu_Nai
 */
@RestController
@RequestMapping("/system/calculate")
public class SysCalculateController extends MyBaseController
{

    private final String Key = "yunze:calculate:company:config";

    @Resource
    private RedisCache redisCache;

    /**
     * 保存计算公司信息
     */
    @PreAuthorize("@ss.hasPermi('system:calculate:saveCompanyConfig')")
    @PostMapping("/saveCompanyConfig")
    public String saveCompanyConfig(@RequestBody String Pstr) {
        try {
            // 解密请求数据
            Pstr = AesEncryptUtil.desEncrypt(Pstr);

            //查询公司配置列表是否存在
            if (redisCache.getCacheObject(Key) != null) {
                redisCache.deleteObject(Key);
            }

            // 缓存公司配置列表
            redisCache.setCacheObject(Key, Pstr);

            // 构建成功响应
            return MyRetunSuccess("保存成功", null);

        } catch (Exception e) {
            logger.error("保存公司配置失败: ", e);
            return Myerr("保存公司配置失败");
        }
    }

    @PreAuthorize("@ss.hasPermi('system:calculate:getCompanyConfigList')")
    @GetMapping("/getCompanyConfigList")
    public String getCompanyConfigList() {
        try {
            //查询公司配置列表是否存在
            String data = redisCache.getCacheObject(Key);
            if (data != null) {
                return MyRetunSuccess(data, null);
            }
            return MyRetunSuccess(null,null);
        } catch (Exception e) {
            logger.error("获取公司配置失败: ", e);
            return Myerr("获取公司配置失败");
        }
    }
}
