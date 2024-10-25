package com.yunze.apiCommon.upstreamAPI.XuYu;

import com.yunze.apiCommon.utils.MD5Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 旭宇系统 API 接口
 * 2022-05-12 版本
 */
public class XunYu_Api {




	//构造赋值
	public XunYu_Api(Map<String, Object> init_map){
		// 代理编号
		agencyCode = init_map.get("cd_username").toString();
		// 服务器请求地址
		server_Ip = init_map.get("cd_pwd").toString();
		// key
		key = init_map.get("cd_key").toString();

	}

	// 服务器请求地址     https://icmp.shingsou.com/open
	protected  static String server_Ip = "https://icmp.shingsou.com/open";
	// 代理编号
	public static String agencyCode = null;
	// 秘钥key
	public static String key = "";


	// 配置调用的名称
	protected static String Config_name = "";



	public static String getSign(Map<String,Object> map){
		List<String> list=new ArrayList<>(map.keySet());
		//Collections.sort(list);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<list.size();i++){
			String k =list.get(i);
			String v=(String )map.get(k);
			sb.append(k).append("=").append(v).append("&");
		}
		String signstr=sb.append("key=").append(key).toString();
		String sign = MD5Util.MD5Encode(signstr).toUpperCase();
		return sign;
	}



	public static String getSign(String str){
		StringBuffer sb=new StringBuffer(str);
		String signstr=sb.append("&key=").append(key).toString();
		String sign = MD5Util.MD5Encode(signstr).toUpperCase();
		return sign;
	}
}
