package com.yunze.common.utils.yunze;

import java.io.UnsupportedEncodingException;

/**
 * @Auther: zhang feng
 * @Date: 2021/06/17/15:57
 * @Description:
 */
public class EncodingUtil {


    /**
     * 将原正确编码的字符串src，转化为编码为srcCharset的字符串
     * <p>
     * 前提是：确保原字符串的编码是无损（完整的）. 无需知道原字符串的具体编码，
     * 转化为目标编码的字符串由java库自动实现，无需自己手动实现。
     * <p>
     * 如果原编码字符串不能转化为目标编码，将会抛出UnsupportedEncodingException
     *
     * @param src
     * @param srcCharset
     * @param destCharet
     * @return 转换后的字符串
     * @throws UnsupportedEncodingException
     */
    public static String convertEncoding_Str(String src, String srcCharset, String destCharet)
            throws UnsupportedEncodingException {
        byte[] bts = src.getBytes(srcCharset);
        return new String(bts, destCharet);
    }


    /**
     * 将编码为srcCharset的字节数组src转化为编码为destCharet的字节数组
     *
     * @param src
     * @param srcCharset
     * @param destCharet
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] convertEncoding_ByteArr(byte[] src, String srcCharset, String destCharet)
            throws UnsupportedEncodingException {
        String s = new String(src, srcCharset);
        return s.getBytes(destCharet);
    }


    /**
     * 将字节数组byteArr转化为2位16进制字符串,每个16进制之间用空格分割
     *
     * @param byteArr
     * @return
     */
    public static String byteToHex(byte... byteArr) {
        if (null == byteArr || byteArr.length == 0)
            return "";
        StringBuffer sb = new StringBuffer();
        String tmp = null;

        for (byte b : byteArr) {

            tmp = Integer.toHexString(b);
            // 切记：byte进行运算时，会自动转化为int，否则可能会出错
            if (b >>> 31 == 1) { // 最高位为1，负数
                sb.append(tmp.substring(6));
            } else { // 最高位为0，正数
                if (tmp.length() < 2)
                    sb.append('0');
                sb.append(tmp);
            }
            sb.append(' ');
        }
        sb.deleteCharAt(sb.length() - 1); // delete last space
        return sb.toString();
    }


    public static String getEncoding(String str)
    {
        String encode;

        encode = "UTF-16";
        try {
            if (str.equals(new String(str.getBytes(), encode))){
                return encode;
            }
        } catch (Exception ex) {
        }

        encode = "ASCII";
        try {
            if (str.equals(new String(str.getBytes(), encode))){
                return "字符串<< " + str + " >>中仅由数字和英文字母组成，无法识别其编码格式";
            }
        } catch (Exception ex) {
        }

        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(), encode))){
                return encode;
            }
        } catch (Exception ex) {
        }

        encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(), encode))){
                return encode;
            }
        } catch (Exception ex) {
        }

        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(), encode))){
                return encode;
            }
        } catch (Exception ex) {
        }

        /*
         *......待完善
         */

        return "未识别编码格式";
    }



}
