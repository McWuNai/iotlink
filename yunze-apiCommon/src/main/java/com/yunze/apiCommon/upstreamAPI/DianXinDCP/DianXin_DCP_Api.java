package com.yunze.apiCommon.upstreamAPI.DianXinDCP;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 电信CMP API 接口
 */
public class DianXin_DCP_Api {




	//构造赋值
	public DianXin_DCP_Api(Map<String, Object> init_map){

		String cd_username[] = init_map.get("cd_username").toString().split(",");
		String cd_pwd[] = init_map.get("cd_pwd").toString().split(",");

		appid = cd_username[1] ;
		secretKey = cd_pwd[1];
		customerNo = init_map.get("cd_key").toString();

	}

	// 服务器请求地址
	protected  static String server_Ip = "https://si.global.ct10649.com";
	// 用户登录名称
	public static String appid = null;

	public static String secretKey = null;

	public static String customerNo = null;




	// 配置调用的名称
	protected static String Config_name = "";



	/**
	 * 登录获取token
	 * @return
	 */
	public static String loginToken() {
		String token = "";
		//请求参数头部
		Map<String, String> headers = null;
		//请求参数 Json
		JSONObject params = null;
		//返回信息 map
		Map<String, Object> rMap = null;

		// 拼接请求参数
		params = new JSONObject();
		params.put("username", appid);
		params.put("password", secretKey);

		//请求头部参数
		headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("X-Access-Token", token);

		String res = HttpUtil.post(server_Ip+"/api/dcpsiapi/login", params.toJSONString(), headers);
		//System.out.println(res);
		Map<String, Object> obj = JSON.parseObject(res);
		if ((obj.get("status").toString()).equals("200")) {
			Map<String, String> data = (Map<String, String>)obj.get("data");
			token = data.get("token");
			System.out.println(token);
		}
		return  token;
	}


}
