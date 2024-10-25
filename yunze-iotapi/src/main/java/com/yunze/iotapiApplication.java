package com.yunze;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},scanBasePackages={"com.yunze"})
@MapperScan("com.yunze.iotapi.mapper.mysql")
@MapperScan("com.yunze.common.mapper.yunze")
@MapperScan("com.yunze.apiCommon.mapper")
@ServletComponentScan(basePackages = {"com.yunze.iotapi.interceptor"})
public class iotapiApplication {


    public static void main(String[] args) {

        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
        SpringApplication.run(iotapiApplication.class,args);

    }

}

