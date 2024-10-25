package com.yunze.apiCommon.upstreamAPI.YiYuan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 移远 系统 API 接口
 */
public class YiYuan_Api {




	//构造赋值
	public YiYuan_Api(Map<String, Object> init_map){
		appKey  = init_map.get("cd_username").toString();
		Secret  = init_map.get("cd_pwd").toString();
	}

	// 服务器请求地址     http://api.quectel.com/openapi/router
	protected  static String server_Ip = "http://api.quectel.com/openapi/router";
	// 用户登录名称
	public static String appKey  = null;
	// 秘钥key
	public static String Secret = null;


	// 配置调用的名称
	protected static String Config_name = "";



	public static String getSign(Map<String,Object> map){
		List<String> list=new ArrayList<>(map.keySet());
		Collections.sort(list);
		StringBuffer sb=new StringBuffer();
		sb.append(Secret);
		for(int i=0;i<list.size();i++){
			String k =list.get(i);
			String v=(String )map.get(k);
			sb.append(k).append(v);
		}
		sb.append(Secret);
		String sign =  BaseUtile.bytesToHexString2(BaseUtile.getSha1(sb.toString()));
		return sign;
	}

}
