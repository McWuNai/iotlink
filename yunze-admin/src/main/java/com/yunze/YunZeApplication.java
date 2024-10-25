package com.yunze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author yunze
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class YunZeApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(YunZeApplication.class, args);
        System.out.println("     物联网综合业务支撑云平台 启动成功    \n" +
                "                         ..,].                \n" +
                "                      .,@\\`.]@@@@.               \n" +
                "                    ./@@@@@@@@@@@^               \n" +
                "       ..      ...,@@@@@@@@@@@@@/.               \n" +
                "       .=\\`..,]/@@@@@@@@@@/[.. ..                \n" +
                "       ...[O@@@@@@@@@/`........                  \n" +
                "         ../@@@@@/`..,]@@@@@@@@O`..              \n" +
                "       ../@@@@/.../@@@@@@@@@@@@@@@^              \n" +
                "      .=@@@@/..,@@@@@@@@[...,@@@@@@`.            \n" +
                "     .=@@@@.../@@@@@@`.     ..@@@@@^.            \n" +
                "    .=@@@@..,@@@@@@`..      .=@@@@O..            \n" +
                "    .O@@@`..@@@@@@.       ../@@@@/.            ..\n" +
                "    ,@@@/ ./@@@@@.    ...]@@@@@/`.           .,/.\n" +
                "    ,@@@^..@@@@@^.    ...[[[....          ../@`. \n" +
                "    .@@@^ =@@@@@\\.                     ../@@/..  \n" +
                "    ..\\@@ .@@@@@@^.                ..]@@@@/..    \n" +
                "      ..\\..=@@@@@@@]...    .....]/@@@@@@/..      \n" +
                "           .,@@@@@@@@@@@@@@@@@@@@@@@@/`.         \n" +
                "              \\@@@@@@@@@@@@@@@@@@@[...           \n" +
                "               ..[\\@@@@@@@@@/[`..                \n" +
                "                    .     ..                     ");

    }
}
