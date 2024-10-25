package com.yunze.apiCommon.upstreamAPI.YiYuan;

import com.cu.api.internal.utils.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 进制转换
 * @Auther: zhang feng
 * @Description:
 */
public class BaseUtile {

    /**
     * 字符串转 二进制  Sha1 运算
     * @param psw
     * @return
     */
    public static byte[] getSha1(String psw) {
        if(StringUtils.isEmpty(psw)){
            return null;
        }else{
            return DigestUtils.sha1(psw);
        }
    }

    /**
     * 二进制转十六进制
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            stringBuilder.append(i );
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    /**
     * 十六进制转二进制
     * @param hex
     * @return
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0789abcdef".indexOf(c);
        return b;
    }



    /**
     * 将字节数组转换成十六进制字符串进行输出
     * @param bArr
     * @return
     */
    public static final String bytesToHexString2(byte[] bArr) {
        StringBuffer sb = new StringBuffer(bArr.length);
        String sTmp;

        for (int i = 0; i < bArr.length; i++) {
            sTmp = Integer.toHexString(0xFF & bArr[i]);
            if (sTmp.length() < 2)
                sb.append(0);
            sb.append(sTmp.toUpperCase());
        }

        return sb.toString();
    }

    /**
     * 将字节数组转换成十六进制字符串进行输出
     * @param bytes
     * @return
     */
    public static final String bytesToHexFun3(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }



}
