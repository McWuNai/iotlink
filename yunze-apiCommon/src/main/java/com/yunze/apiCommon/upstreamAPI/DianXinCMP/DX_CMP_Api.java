package com.yunze.apiCommon.upstreamAPI.DianXinCMP;

import java.util.Map;

/**
 * 电信 CMP 系统 API 接口
 */
public class DX_CMP_Api {


	//构造赋值
	public DX_CMP_Api(Map<String, Object> init_map){
       
		user_id = init_map.get("cd_username").toString();
		 // 秘钥密码 	
		password = init_map.get("cd_pwd").toString();
		// key
		 key = init_map.get("cd_key").toString();
		
		 key1 = key.substring(0, 3);
		 key2 = key.substring(3, 6);
		 key3 = key.substring(6, 9);
    }
	
	// 服务器请求地址
	protected   String server_Ip = "http://api.ct10649.com:9001";
	// user_id 					  
	protected  String user_id = null;
	// 秘钥密码 	
	protected  String password = null;
	
	// 配置调用的名称
	protected  String Config_name = "";
	//
	protected  String key =null;
	
	protected  String    key1 = null;
	protected  String    key2 = null;
	protected  String    key3 = null;
	


	
	
}
