package com.yunze.apiCommon.upstreamAPI.MengWang;

import com.yunze.apiCommon.utils.MD5Util;

import java.util.Map;

/**
 * 梦网（蜂助手）系统 第三方API 接口
 * 2022-06-30
 */
public class Mw_Api {


	//构造赋值
	public Mw_Api(Map<String, Object> init_map){

		channelNo = init_map.get("cd_username").toString();
		String[] names= init_map.get("cd_pwd").toString().replace("，",",").split(",");
		userId = names[0];
		userpws = names[1];
		server_Ip = init_map.get("cd_key").toString();

    }
	
	// 服务器请求地址				  https://tx.phone580.com/iotcard-buss/api/out-open
	protected  static String server_Ip = "";;
	// SP 编码
	protected static String channelNo = null;
	// user_id
	protected static String userId = null;
	//密码
	protected static String userpws = null;
	//版本号 固定值
	protected static String version = "1.0";

	protected static String sign = null;



	protected static void setSign(String txnDate,String str){
		str = str+userId;
		sign = MD5Util.MD5Encode(str).toLowerCase();
	}


}
