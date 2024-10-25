package com.yunze.apiCommon.upstreamAPI.TengYu;

import java.util.Map;

/**
 * 腾宇 API 接口
 */
public class TengYu_Api {


	//构造赋值
	public TengYu_Api(Map<String, Object> init_map){
		API_ID = init_map.get("cd_username").toString();
		API_PWD = init_map.get("cd_pwd").toString();
		API_KEY = init_map.get("cd_key").toString();
	}

	// 服务器请求地址   https://www.m2miot.info/api
	protected  static String server_Ip = "https://www.m2miot.info/api";
	// user_id
	protected static String API_ID = null;
	// 秘钥密码
	protected static String API_PWD = null;
	// key
	protected static String API_KEY = null;


	// 配置调用的名称
	protected static String Config_name = "";






}
