package com.yunze.web;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

@EnableTransactionManagement
@SpringBootApplication
@EnableEurekaClient
@ComponentScan("com.yunze")
@MapperScan("com.yunze.business.dao")
@MapperScan("com.yunze.cn.mapper")
@MapperScan("com.yunze.apiCommon.mapper")
public class WebApplication {

    private static final Logger LOG =LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
        SpringApplication app= new SpringApplication(WebApplication.class);
        Environment env=app.run(args).getEnvironment();
        LOG.info("启动成功");
        LOG.info("web地址：http://127.0.0.1:{}",env.getProperty("server.port"));




    }

}
