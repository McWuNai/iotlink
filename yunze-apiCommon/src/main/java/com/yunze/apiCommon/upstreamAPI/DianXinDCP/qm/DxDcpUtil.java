package com.yunze.apiCommon.upstreamAPI.DianXinDCP.qm;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.Vo.DxDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DxDcpUtil
 * @Description
 * @Author QiMing
 * @Date 2022/11/7 15:49
 * @Version 1.0
 */
@Slf4j
@Component
public class DxDcpUtil {

    @Resource
    private RestTemplate restTemplate;

    private String username;
    private String password;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public DxDcpUtil(Map<String, Object> init_map) {
        Object cd_username1 = init_map.get("cd_username");
        if (null != cd_username1) {
            String cd_username[] = init_map.get("cd_username").toString().split(",");
            String cd_pwd[] = init_map.get("cd_pwd").toString().split(",");
            username = cd_username[0];
            password = cd_pwd[0];
        }
    }

    /**
     * @Title: getToken
     * @Description: //
     * @Param: []
     * @Return: java.lang.String
     * @Date: 2022/11/7 14:10
     * @Author: QiMing
     */
    public String getToken(String username, String password) {
        String token = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "client_id");
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity(map, httpHeaders);
        try {
            ResponseEntity<Map> exchange = restTemplate.exchange("https://global.ct10649.com/iot/api/auth/token", HttpMethod.POST, httpEntity, Map.class);
            Map<String, Object> body = exchange.getBody();
            if (null == body) {
                return getToken(username, password);
            }
            Object access_token = body.get("access_token");
            token = "Bearer " + access_token;
        } catch (Exception e) {
            log.error("com.yunze.apiCommon.upstreamAPI.DianXinDCP.qm.DxApi.getToken ---- {}", e.getMessage());
        }
        return token;
    }

    private String url = "https://global.ct10649.com/iot/api/subscriptions/details?additionalFields=DATES";

    public DxDetail subscriptions(String iccid, Map<String, Object> init_map) {
        DxDcpUtil dxDcpUtil = new DxDcpUtil(init_map);
        Object sica_token_dx = redisTemplate.opsForValue().get(dxDcpUtil.password + "SICA_TOKEN_DX");
        String token = sica_token_dx == null ? "" : sica_token_dx.toString();
        if (StringUtils.isEmpty(token)) {
            token = getToken(dxDcpUtil.username, dxDcpUtil.password);
            if (!StringUtils.isEmpty(token.trim())) {
                redisTemplate.opsForValue().set(dxDcpUtil.password + "SICA_TOKEN_DX", token, 25, TimeUnit.MINUTES);
            }
        }

        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        list.add(iccid);
        map.put("identifierValues", list);
        map.put("identifierType", "ICC");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, token);
        HttpEntity<String> httpEntity = new HttpEntity(map, httpHeaders);
        try {
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            if (200 != exchange.getStatusCodeValue()) {
                return null;
            }
            String body = exchange.getBody();
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONArray jsonArray = JSONObject.parseArray(jsonObject.get("subscriptions").toString());
            DxDetail vo = JSONObject.parseObject(jsonArray.get(0).toString(), DxDetail.class);
            return vo;
        } catch (Exception e) {
            log.error("com.yunze.apiCommon.upstreamAPI.DianXinDCP.qm.DxApi.subscriptions ---- {}", e.getMessage());
        }
        return null;
    }
}
