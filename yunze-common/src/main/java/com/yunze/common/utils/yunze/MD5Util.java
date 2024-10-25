package com.yunze.common.utils.yunze;
 
import java.security.MessageDigest;

/**
 * 1.MD5加密字符串（32位大写）
 * 2.MD5加密字符串（32位小写）
 * <p>
 * MD5在线加密：https://md5jiami.51240.com/
 * 3.将二进制字节数组转换为十六进制字符串
 * 4.Unicode中文编码转换成字符串
 */
public class MD5Util {
 
	 private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7",  
	            "8", "9", "a", "b", "c", "d", "e", "f"};  
	  
	    /** 
	     * 转换字节数组为16进制字串 
	     * @param b 字节数组 
	     * @return 16进制字串 
	     */  
	    public static String byteArrayToHexString(byte[] b) {  
	        StringBuilder resultSb = new StringBuilder();  
	        for (byte aB : b) {  
	            resultSb.append(byteToHexString(aB));  
	        }  
	        return resultSb.toString();  
	    }  
	  
	    /** 
	     * 转换byte到16进制 
	     * @param b 要转换的byte 
	     * @return 16进制格式 
	     */  
	    private static String byteToHexString(byte b) {  
	        int n = b;  
	        if (n < 0) {  
	            n = 256 + n;  
	        }  
	        int d1 = n / 16;  
	        int d2 = n % 16;  
	        return hexDigits[d1] + hexDigits[d2];  
	    }  
	  
	    /** 
	     * MD5编码 
	     * @param origin 原始字符串 
	     * @return 经过MD5加密之后的结果 
	     */  
	    public static String MD5Encode(String origin) {  
	        String resultString = null;  
	        try {  
	            resultString = origin;  
	            MessageDigest md = MessageDigest.getInstance("MD5");  
	            md.update(resultString.getBytes("UTF-8"));  
	            resultString = byteArrayToHexString(md.digest());  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        return resultString;  
	    }  
}
