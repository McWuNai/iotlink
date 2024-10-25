package com.yunze.apiCommon.upstreamAPI.DianXinCMP5G;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: zhang feng
 * @Date: 2022/05/25/10:02
 * @Description:
 */
public class SignUtil {
    public static final String ENCODE = "UTF-8";
    private static final String MD5_ALGORITHM = "MD5";
    public static final String DATE_FORMAT_PATTERN ="yyyyMMddHHmmss";
    public static String getCurTimestamp() {
        long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new
                SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }
    public static String signMD5(String reqStr) throws Exception {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        byte[] btInput = reqStr.getBytes();
        MessageDigest mdInst = MessageDigest.getInstance(MD5_ALGORITHM);
        mdInst.update(btInput);
        byte[] md = mdInst.digest();
        int j = md.length;
        char[] str = new char[j * 2];
        int k = 0;

        for (byte byte0 : md) {
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return bytesToHexString(new
                String(str).getBytes(ENCODE)).toUpperCase(Locale.US);
    }
    public static String bytesToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte value : b) {
            String hex = Integer.toHexString(value & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public static String order(String params) throws Exception {
        Map<String, String> map = new HashMap<>();
        try {
            String[] kvs = params.split("&");
            for (String kv :kvs) {
                String[] kav = kv.split("=");
                map.put(kav[0], kav[1]);
            }
        } catch (Exception e) {
            throw new Exception("URL 参数不规范");
        }
        List<String> tmp = new ArrayList<>();
        List<Map.Entry<String, String>> infoIds = new ArrayList<>(map.entrySet());
        infoIds.sort(Map.Entry.comparingByKey());
        for (Map.Entry<String, String> item :infoIds) {
            tmp.add(item.getKey() + "=" + item.getValue());
        }
        return String.join("&", tmp);
    }
    public static void main(String[] args) throws Exception {
        String paramStr = "a=1&c=3&b=2";
        String bodyStr = "{\"name\":\"xf\",\"age\":\"dd\"}";
        paramStr = order(paramStr);
        long timestamp = System.currentTimeMillis();
        String reqStr = paramStr + bodyStr ;
        String secretKey = "d1209cfefe895364e03";
        String Sign = signMD5(reqStr + secretKey + timestamp);
        System.out.println(Sign);
    }


}
