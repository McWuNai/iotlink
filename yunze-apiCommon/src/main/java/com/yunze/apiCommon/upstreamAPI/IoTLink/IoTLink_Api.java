package com.yunze.apiCommon.upstreamAPI.IoTLink;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.MD5Util;

import java.util.*;

/**
 * IoTLink 系统 API 接口
 */
public class IoTLink_Api {




	//构造赋值
	public IoTLink_Api(Map<String, Object> init_map){

		String cd_pwd[] = init_map.get("cd_pwd").toString().split(",");

		appId = init_map.get("cd_username").toString();
		password = cd_pwd[0];
		access_key = cd_pwd[1];
		server_Ip = init_map.get("cd_key").toString()+"/route/open";
	}

	// 服务器请求地址
	protected  static String server_Ip = "";
	// appId
	public static String appId = null;
	//秘钥密码
	public static String password= null;
	//秘钥密码
	public static String access_key = null;

	/**
     * Sign 加密类
	 * @param map
     * @param key
     * @return
     */
	public static String getSign(Map<String,Object> map,String key){
		List<String> list=new ArrayList<>(map.keySet());
		Collections.sort(list);
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

	public static Map<String, Object> getPmap(Map<String, Object> Parmap){
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> verify = new HashMap<String, Object>();
		String timeStamp = ""+System.currentTimeMillis() ;
		verify.put("password", password);
		verify.put("appId", appId);
		verify.put("timeStamp", timeStamp);
		verify.put("sign", getSign(verify, access_key));
		map.put("verify", verify);
		map.put("Param", Parmap);
		return map;
	}


	public static void main(String[] args) {

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> Parmap = new HashMap<String, Object>();
		Map<String, Object> verify = new HashMap<String, Object>();

		String appId = "";
		String password = "";
		String access_key = "http://";
		String timeStamp = ""+System.currentTimeMillis() ;

		verify.put("password", password);
		verify.put("appId", appId);
		verify.put("timeStamp", timeStamp);
		verify.put("sign", getSign(verify, access_key));

		Parmap.put("type", "iccid");
		Parmap.put("cardno", "898604");


		map.put("verify", verify);
		map.put("Param", Parmap);

		server_Ip +="/queryFlow";
		String result = null;

		String data = JSON.toJSONString(map);
		System.out.println(data);

		result = HttpUtil.post(server_Ip, data);// 返回结果字符串
		System.out.println(result);
	}




}
