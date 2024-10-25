package com.yunze.wechat;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

@SpringBootApplication
@EnableEurekaClient
@ComponentScan("com.yunze")
@MapperScan("com.yunze.cn.mapper")
@MapperScan("com.yunze.business.dao")
@MapperScan("com.yunze.apiCommon.mapper")
public class WechatApplication {

    private static final Logger LOG =LoggerFactory.getLogger(WechatApplication.class);

    public static void main(String[] args) {
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
        SpringApplication app= new SpringApplication(WechatApplication.class);
        Environment env=app.run(args).getEnvironment();
        LOG.info("启动成功");
        LOG.info("wechat地址：http://localhost:{}",env.getProperty("server.port"));




    }

}
