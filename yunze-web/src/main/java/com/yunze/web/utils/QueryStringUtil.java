package com.yunze.web.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class QueryStringUtil {

    /**
     * 将map参数按照字段名的ASCII码从小到大排序后，转换为URL键值对格式的字符串。
     *
     * @param params 需要转换的参数map
     * @return URL键值对格式的字符串
     */
    public static String mapToSortedQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 按照键的字典顺序排序
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    /*public static void main(String[] args) {
        // 示例map
        Map<String, String> params = Map.of(
                "banana", "yellow",
                "apple", "red",
                "grape", "purple"
        );

        // 调用方法并打印结果
        String queryString = mapToSortedQueryString(params);
        System.out.println(queryString); // 输出: apple=red&banana=yellow&grape=purple
    }*/

    /**
     * 对输入字符串执行SHA-1哈希计算。
     *
     * @param input 输入字符串
     * @return SHA-1哈希值的十六进制表示
     */
    public static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
            md.update(inputBytes);
            byte[] digest = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 安全地进行哈希计算，适合生产环境。
     *
     * @param input 输入字符串
     * @return SHA-1哈希值的十六进制表示，如果发生错误则返回null或采取其他适当的措施
     */
    public static String safeSha1(String input) {
        try {
            return sha1(input);
        } catch (Exception e) {
            // 在实际应用中，这里应该记录详细的错误日志
            System.err.println("An error occurred during SHA-1 hashing: " + e.getMessage());
            // 根据业务逻辑决定是返回null还是抛出自定义异常等
            return null;
        }
    }

    /*public static void main(String[] args) {
        String testStr = "Hello, World!";
        System.out.println("Original: " + testStr);
        System.out.println("SHA-1: " + safeSha1(testStr));
    }*/
}
