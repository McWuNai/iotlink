package com.yunze.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 公司信息配置
 * 
 * @author Wu_Nai
 */
@RestController
@RequestMapping("/system/calculate")
public class SysCalculateController extends MyBaseController {

    private final String KeyPrefix = "yunze:calculate:company:config";

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

            // 解析JSON获取identifier
            Map<String, Object> paramMap = JSON.parseObject(Pstr);
            String identifier = (String) paramMap.get("identifier");

            if (identifier == null || identifier.trim().isEmpty()) {
                return Myerr("identifier参数不能为空");
            }

            // 动态构建Key，将identifier加入Key中
            String key = KeyPrefix + ":" + identifier;

            // 查询公司配置列表是否存在
            if (redisCache.getCacheObject(key) != null) {
                redisCache.deleteObject(key);
            }

            // 缓存公司配置列表
            redisCache.setCacheObject(key, Pstr);

            // 构建成功响应
            return MyRetunSuccess("保存成功", null);

        } catch (Exception e) {
            logger.error("保存公司配置失败: ", e);
            return Myerr("保存公司配置失败");
        }
    }

    @PreAuthorize("@ss.hasPermi('system:calculate:getCompanyConfigList')")
    @GetMapping("/getCompanyConfigList")
    public String getCompanyConfigList(@RequestParam(required = false) String identifier) {
        try {
            // 如果identifier为空，返回错误
            if (identifier == null || identifier.trim().isEmpty()) {
                return Myerr("identifier参数不能为空");
            }

            // 动态构建Key，将identifier加入Key中
            String key = KeyPrefix + ":" + identifier;

            // 查询公司配置列表是否存在
            String data = redisCache.getCacheObject(key);
            if (data != null) {
                return MyRetunSuccess(data, null);
            }
            return MyRetunSuccess(null, null);
        } catch (Exception e) {
            logger.error("获取公司配置失败: ", e);
            return Myerr("获取公司配置失败");
        }
    }

    /**
     * 获取所有公司配置的账号名列表
     */
    @PreAuthorize("@ss.hasPermi('system:calculate:getIdentifierList')")
    @GetMapping("/getIdentifierList")
    public String getIdentifierList() {
        try {
            // 使用通配符匹配所有相关的Key
            String pattern = KeyPrefix + ":*";
            Collection<String> keys = redisCache.keys(pattern);

            List<String> identifierList = new ArrayList<>();
            if (keys != null && !keys.isEmpty()) {
                // 从每个Key中提取identifier
                for (String key : keys) {
                    // Key格式: yunze:calculate:company:config:{identifier}
                    // 提取identifier部分
                    if (key.startsWith(KeyPrefix + ":")) {
                        String identifier = key.substring(KeyPrefix.length() + 1);
                        if (identifier != null && !identifier.trim().isEmpty()) {
                            identifierList.add(identifier);
                        }
                    }
                }
            }

            return MyRetunSuccess(identifierList, null);
        } catch (Exception e) {
            logger.error("获取账号名列表失败: ", e);
            return Myerr("获取账号名列表失败");
        }
    }

    /**
     * 删除指定账号名的Redis存储内容和键值
     */
    @PreAuthorize("@ss.hasPermi('system:calculate:deleteCompanyConfig')")
    @PostMapping("/deleteCompanyConfig")
    public String deleteCompanyConfig(@RequestBody String Pstr) {
        try {
            // 解密请求数据
            Pstr = AesEncryptUtil.desEncrypt(Pstr);

            // 解析JSON获取identifier
            Map<String, Object> paramMap = JSON.parseObject(Pstr);
            String identifier = (String) paramMap.get("identifier");

            if (identifier == null || identifier.trim().isEmpty()) {
                return Myerr("identifier参数不能为空");
            }

            // 动态构建Key，将identifier加入Key中
            String key = KeyPrefix + ":" + identifier;

            // 检查Key是否存在
            if (redisCache.getCacheObject(key) == null) {
                return Myerr("指定的配置不存在");
            }

            // 删除Redis中的键值
            boolean deleted = redisCache.deleteObject(key);
            if (deleted) {
                return MyRetunSuccess("删除成功", null);
            } else {
                return Myerr("删除失败");
            }
        } catch (Exception e) {
            logger.error("删除公司配置失败: ", e);
            return Myerr("删除公司配置失败");
        }
    }
}
