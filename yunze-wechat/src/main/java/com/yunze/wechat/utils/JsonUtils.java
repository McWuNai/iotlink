package com.yunze.wechat.utils;

import me.chanjar.weixin.mp.util.json.WxMpGsonBuilder;

/**
 * @author
 */
public class JsonUtils {
	public static String toJson(Object obj) {
		return WxMpGsonBuilder.create().toJson(obj);
	}
}
