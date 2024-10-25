package com.yunze;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.yunze.common.mapper.mysql")
@MapperScan("com.yunze.apiCommon.mapper.mysql")
public class CardSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardSmsApplication.class);
    }
}
