package com.yunze.common.utils.yunze;
/**
 * AES 128bit 加密解密工具类
 *
 */

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class AesEncryptUtil {

    //使用AES-128-CBC加密模式，key需要为16位,key和iv可以相同！
    private static String KEY = "YunZeIot@5iot.cn";

    private static String IV = "www_5iot_cn@2022";


    /**
     * 加密方法
     * @param data  要加密的数据
     * @param key 加密key
     * @param iv 加密iv
     * @return 加密的结果
     * @throws Exception
     */
    public static String encrypt(String data, String key, String iv) throws Exception {
        try {

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");//"算法/模式/补码方式"
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = data.getBytes("utf-8");
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];

            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes("utf-8"), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes("utf-8"));

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);
            //System.out.println("加密后："+byteToHexString(encrypted));

            return new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密方法
     * @param data 要解密的数据
     * @param key  解密key
     * @param iv 解密iv
     * @return 解密的结果
     * @throws Exception
     */
    public static String desEncrypt(String data, String key, String iv) throws Exception {
        try {
            byte[] encrypted1 = new Base64().decode(data);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes("utf-8"), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes("utf-8"));

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original,"utf-8");
            return originalString.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用默认的key和iv加密
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        return encrypt(data, KEY, IV);
    }

    /**
     * 使用默认的key和iv解密
     * @param data
     * @return
     * @throws Exception
     */
    public static String desEncrypt(String data) throws Exception {
        return desEncrypt(data, KEY, IV);
    }

    public static String byteToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length);
        String sTemp;
        for (int i = 0; i < bytes.length; i++) {
            sTemp = Integer.toHexString(0xFF & bytes[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
       Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", new String("山东济南-2021-06-05".getBytes(),"ISO-8859-1"));
        AesEncryptUtil Ae = new AesEncryptUtil();

        String data =  Ae.encrypt(JSON.toJSONString(new String("山东济南-2021-06-05".getBytes(),"ISO-8859-1")));
        //System.out.println(data);
        String Pstr="lm298sHftC3El8Rt6Gt1escjkSRrge2aFu7O9eOdto6Ncr9AE7Hn5kqC8hkqd2iAmkH3/rymOTOlMYezatts6YK04VWmg9+QZ81vnttQMfvOU13oWYUyyOY32OLYPcu886FtaU9EYzBKT3bFHGA0tDZ7QXk1SzEEEy2JZAFF/+gAYffCLO3lwSUqDy1YWZHV2L8eMUoGay0rQTNPzzAHukLy+oV8GfWrZD6sXHUAB5S+UL8hGI6WuhlFBD1OF0JMQlsEmKTOTQInalwYgwsh6LVw3ypt5WBuAzSrmmGwPFyvlN/AxPb6obDRG8Dli+f//29HSK8v+K5b/+BIW9tRC8qVfJzHEWoyXUfRxRM0MjcjGrxVBghbmieG7vP+E6Vp8EZJYt2r8efRvAbNT9Do2fob2DniIAgFiLma/CDmR+8ulpJfmECLhT66V0MYygLCYuGX7eS+G1xQD3jnAGklC9r3v1+GBDyORVpjuXx2y3lQfxBUQsgTWeeQqYHA+Oqz5GnQ7oJ0p+i2QP1kMPXISEHJIU8o61jScyaF9RbFsYy2k80u20drhIYB4jq6galpydMeLwy1yqL1YtjAZIwpfhejG6ueoF+p7y8Pw/ApW8e1SYsEFVsDVuvwIUzODx+sRnRATaB4CpetxSm2YyYOPr357BW5SqXp6FU6RRvjdSY=";
        Pstr = Pstr.replace("%2F", "/");//转义 /
        //String pstr = "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";
        String data1 = Ae.desEncrypt(Pstr);
        System.out.println(data1);

    }






    }