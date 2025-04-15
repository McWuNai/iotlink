package com.yunze.web.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wishwork.JSTProperties;
import org.wishwork.JSTService;

@Configuration
public class JSTConfig {

    @Value("${JvShuiTan.serverUrl}")
    private String serverUrl;
    @Value("${JvShuiTan.appKey}")
    private String appKey;
    @Value("${JvShuiTan.appSecret}")
    private String appSecret;

    @Bean
    public JSTService init(){
        JSTProperties properties = JSTProperties.builder()
                .serverUrl(serverUrl)
                .appKey(appKey)
                .appSecret(appSecret)
                .build();
        return new JSTService(properties);
    }

    @Bean
    public JSTService testInit(){
        JSTProperties properties = JSTProperties.builder()
                .serverUrl("https://dev-api.jushuitan.com")
                .appKey("bc6444c400d647aa9b9eba55a207d5c7")
                .appSecret("089e06e18b1f4166b1d0baf7f75a91a6")
                .build();
        return new JSTService(properties);
    }
}
