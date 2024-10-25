package com.yunze.apiCommon.utils;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SMSUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	// 判断一个字符串是否含有中文
	public static boolean isChinese(String str) {
		if (str == null)
			return false;
		for (char c : str.toCharArray()) {
			if (isChinese(c))
				return true;// 有一个中文字符就返回
		}
		return false;
	}

	// 判断一个字符是否是中文
	public static boolean isChinese(char c) {
		return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
	}

	/**
	 * UCS2解码
	 * 
	 * @param src
	 *            UCS2 源串
	 * @return 解码后的UTF-16BE字符串
	 */
	public static String DecodeUCS2(String src) {
		byte[] bytes = new byte[src.length() / 2];

		for (int i = 0; i < src.length(); i += 2) {
			bytes[i / 2] = (byte) (Integer.parseInt(src.substring(i, i + 2), 16));
		}
		String reValue = null;
		try {
			reValue = new String(bytes, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return reValue;

	}

	/**
	 * UCS2编码
	 * 
	 * @param src
	 *            UTF-16BE编码的源串
	 * @return 编码后的UCS2串
	 */
	public static String EncodeUCS2(String src) {

		byte[] bytes = null;

		try {
			bytes = src.getBytes("UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer reValue = new StringBuffer();
		StringBuffer tem = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			tem.delete(0, tem.length());
			tem.append(Integer.toHexString(bytes[i] & 0xFF));
			if (tem.length() == 1) {
				tem.insert(0, '0');
			}
			reValue.append(tem);
		}
		return reValue.toString().toUpperCase();
	}

	// 发送长短信
	public static List<byte[]> getLongByte(String message) {
		List<byte[]> list = new ArrayList<byte[]>();
		try {
			byte[] messageUCS2 = message.getBytes("UnicodeBigUnmarked");// 转换为byte[]
			int messageUCS2Len = messageUCS2.length;// 长短信长度
			int maxMessageLen = 140;
			if (messageUCS2Len > maxMessageLen) {// 长短信发送
				// int tpUdhi = 1;
				// 长消息是1.短消息是0
				// int msgFmt = 0x08;//长消息不能用GBK
				int messageUCS2Count = messageUCS2Len / (maxMessageLen - 6) + 1;// 长短信分为多少条发送
				byte[] tp_udhiHead = new byte[6];
				Random random = new Random();
				random.nextBytes(tp_udhiHead);// 随机填充tp_udhiHead[3] 标识这批短信
				tp_udhiHead[0] = 0x05;
				tp_udhiHead[1] = 0x00;
				tp_udhiHead[2] = 0x03;
//				tp_udhiHead[3] = 0x0A;
				tp_udhiHead[4] = (byte) messageUCS2Count;
				tp_udhiHead[5] = 0x01;// 默认为第一条
				for (int i = 0; i < messageUCS2Count; i++) {
					tp_udhiHead[5] = (byte) (i + 1);
					byte[] msgContent;
					if (i != messageUCS2Count - 1) {// 不为最后一条
						msgContent = byteAdd(tp_udhiHead, messageUCS2, i * (maxMessageLen - 6),
								(i + 1) * (maxMessageLen - 6));
						list.add(msgContent);
					} else {
						msgContent = byteAdd(tp_udhiHead, messageUCS2, i * (maxMessageLen - 6), messageUCS2Len);
						list.add(msgContent);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// 超过140字符长短信编码
	private static byte[] byteAdd(byte[] tpUdhiHead, byte[] messageUCS2, int i, int j) {
		byte[] msgb = new byte[j - i + 6];
		System.arraycopy(tpUdhiHead, 0, msgb, 0, 6);
		System.arraycopy(messageUCS2, i, msgb, 6, j - i);
		return msgb;
	}
}
