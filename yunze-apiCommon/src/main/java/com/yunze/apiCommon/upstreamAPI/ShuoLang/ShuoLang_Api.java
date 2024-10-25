package com.yunze.apiCommon.upstreamAPI.ShuoLang;

import com.yunze.apiCommon.utils.MD5Util;

import java.text.MessageFormat;
import java.util.Map;

/**
 * 硕朗 接口
 */
public class ShuoLang_Api {




	//构造赋值
	public ShuoLang_Api(Map<String, Object> init_map){

		userId = init_map.get("cd_username").toString();
		apikey = init_map.get("cd_pwd").toString();
		server_Ip = init_map.get("cd_key").toString();
	}

	// 服务器请求地址     http://1.1.1.1:32003  [硕朗通用对外接口]
	protected  static String server_Ip = null;
	// 秘钥key
	public static String userId  = null;
	//秘钥密码
	public static String apikey = null;

	// 配置调用的名称
	protected static String Config_name = "";



	public static String getSign(String times){
		String sign = null;
		String md5Str= MessageFormat.format("userId={0}&apikey={1}&times={2}", userId, apikey, times+"");
		return MD5Util.MD5Encode(md5Str).toUpperCase();
	}








}
