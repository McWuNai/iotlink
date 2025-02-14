package com.yunze.web.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpUtil {

    public static Map<String, Object> getHttp(String baseUrl, Map<String, String> params) {
        HttpURLConnection conn = null;
        try {
            // 构建URL并添加查询参数
            String queryString = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + urlEncode(entry.getValue()))
                    .collect(Collectors.joining("&"));
            URL url = new URL(baseUrl + "?" + queryString);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 检查响应码
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("GET request failed: " + responseCode);
                return Collections.emptyMap(); // 或者根据需求返回特定的错误信息Map
            }

            // 读取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // 将json字符串转换为map
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content.toString(), Map.class);

        } catch (IOException e) {
            // 更好的错误处理：打印堆栈跟踪或记录日志
            e.printStackTrace(); // 或者使用日志框架如SLF4J记录错误
            return Collections.emptyMap(); // 根据你的业务逻辑决定如何处理错误
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new RuntimeException("参数编码错误: ", e);
        }
    }
}