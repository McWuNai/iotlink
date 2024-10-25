package com.yunze.apiCommon.upstreamAPI.SDYiDong;


import com.yunze.apiCommon.utils.MD5Util;
import com.yunze.apiCommon.utils.SHA256Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 山东 移动老系统 接口
 */
public class Sdiot_Api {




	//构造赋值
	public Sdiot_Api(Map<String, Object> init_map){

		appkey = init_map.get("cd_username").toString();
		appsecret = init_map.get("cd_pwd").toString();
		provinceid = init_map.get("cd_key").toString();
		token = getToken();
	}

	// 服务器请求地址     http://223.99.141.141:10110/sdiot
	protected  static String server_Ip = "http://223.99.141.141:10110/sdiot";
	// 秘钥key
	public static String appkey  = null;
	//秘钥密码
	public static String appsecret = null;
	//省份编码，山东省为：“531”
	public static String provinceid = null;

	public static String token = null;
	// 配置调用的名称
	protected static String Config_name = "";



	public static String getSign(Map<String,Object> map,String key){
		List<String> list=new ArrayList<>(map.keySet());
       /* for (int i = 0; i < list.size(); i++) {
        	//System.out.println(list.get(i));
		}*/
		Collections.sort(list);
		////System.out.println("后----");
       /* for (int i = 0; i < list.size(); i++) {
        	//System.out.println(list.get(i));
		}*/
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

	public  String getToken(){
		return SHA256Util.getSHA256StrJava(appkey+appsecret).toLowerCase();
	}







}
