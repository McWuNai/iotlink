package com.yunze.common.utils.yunze;

import java.util.Iterator;
import java.util.Map;

public class UrlUtil {

	/**
	 * 将参数集合转换为get请求地址
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String getUrl(String url, Map<String, Object> params) {
		// 添加url参数
		if (params != null) {
			Iterator<String> it = params.keySet().iterator();
			StringBuffer sb = null;
			while (it.hasNext()) {
				String key = it.next();
				Object value = "";
				if (params.get(key) != null) {
					value = params.get(key);
				}

				if (sb == null) {
					sb = new StringBuffer();
					sb.append("?");
				} else {
					sb.append("&");
				}
				sb.append(key);
				sb.append("=");
				sb.append(value);
			}
			url += sb.toString();
		}
		//System.out.println(url);
		return url;
	}

	/**
	 * 中文转Unicode
	 * 
	 * @param gbString
	 *            待转换的字符串
	 * @return 字符串
	 */
	public static String gbEncoding(String gbString) { 
		char[] utfBytes = gbString.toCharArray(); 
		String unicodeBytes = "";
		for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {
			String hexB = Integer.toHexString(utfBytes[byteIndex]); // 转换为16进制整型字符串
			if (hexB.length() <= 2) {
				hexB = "00" + hexB;
			}
			unicodeBytes = unicodeBytes + "\\u" + hexB;
		}
		//System.out.println("unicodeBytes is: " + unicodeBytes);
		return unicodeBytes;
	}

	/**
	 * Unicode转中文
	 * 
	 * @param dataStr
	 *            待转换的字符串
	 * @return 字符串
	 */
	public static String decodeUnicode(String dataStr) {
		int start = 0;
		int end = 0;
		final StringBuffer buffer = new StringBuffer();
		while (start > -1) {
			end = dataStr.indexOf("\\u", start + 2);
			String charStr = "";
			if (end == -1) {
				charStr = dataStr.substring(start + 2, dataStr.length());
			} else {
				charStr = dataStr.substring(start + 2, end);
			}
			char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
			buffer.append(new Character(letter).toString());
			start = end;
		}
		return buffer.toString();
	}
}
