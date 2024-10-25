package com.yunze.apiCommon.upstreamAPI.YiDongEC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.apiCommon.upstreamAPI.ShuoLang.SL_Api;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.UrlUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 移动 EC 系统 API 接口
 */
public class YD_EC_Api {

	private RedisUtil redisUtil;
	public YD_EC_Api(){};


	//构造赋值
	public YD_EC_Api(Map<String, Object> init_map, String cd_code){
		redisUtil = new RedisUtil();
		appId = init_map.get("cd_username").toString();
		password = init_map.get("cd_pwd").toString();
		if(cd_code.equals("YiDong_EC_TengYu")){
			server_Ip = init_map.get("cd_key").toString();
		}else{
			server_Ip = "https://api.iot.10086.cn/v5/ec";
		}
		this.cd_code = cd_code;
		String R_token = null;
		try {
			R_token = (String) redisUtil.redisTemplate.opsForValue().get(password+":"+ appId);
		}catch (Exception e){
			//System.out.println("移动获取 token 异常");
			//System.out.println(e);
		}
		//  去 Redis 查询 当前 的 token
		//System.out.println("R_token = "+R_token);
		if(R_token!=null && R_token!=""){
			token = R_token;
		}else{
			if(cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_Combo")){
				//获取token
				SetToken(appId,password);
			}else if(cd_code.equals("YiDong_EC_TOKE_ShuoLang")){
				TokenMap = (Map<String, Object>) JSONObject.parseObject(init_map.get("cd_key").toString());
				SetTokenShuoLang(TokenMap);
			}else if(cd_code.equals("ECV5_token_MW")){
				GetTokenMengWang(init_map);
			}




		}

    }
	
	// 服务器请求地址
	protected  static String server_Ip = "https://api.iot.10086.cn/v5/ec";
	// user_id 					  
	protected static String appId = null;
	// 秘钥密码 	
	protected static String password = null;
	// 配置调用的名称
	protected static String Config_name = "";
	// 企业 编号
	protected static String transid =null;
	//token 获取请求参数
	protected static Map<String,Object> TokenMap = null;
	//通道编码
	protected static String cd_code = null;

	// token
	public static String token = null;


	/**
	 * 获取 token
	 * @param appid
	 * @param pwd
	 */
	public  void SetToken(String appid, String pwd) {
		String method = server_Ip + "/get/token";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", appid);
		params.put("password", pwd);
		params.put("transid", appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()));

		String reqUrl = UrlUtil.getUrl(method, params);
		//System.err.println(reqUrl);
		String res = HttpUtil.get(reqUrl);
		//System.out.println("res = " + res);
		JSONObject json = JSON.parseObject(res);
		Object status = json.get("status");
		if (status != null && status.toString().equals("0")) {
			Map<String, String> map = ((List<Map<String, String>>) json.get("result")).get(0);
			token = map.get("token");
			//System.out.println("token = " + token);
			redisUtil.redisTemplate.opsForValue().set(password + ":" + appId, token, 10, TimeUnit.MINUTES);
			//redisUtil.redisTemplate.expire(appId+"_"+password, 60*50, TimeUnit.SECONDS);
		}

	}



	/**
	 * 获取 硕朗 token
	 */
	public  void SetTokenShuoLang(Map<String,Object> TokenMap) {
		try {
			if(TokenMap!=null){
				SL_Api sL_Api = new SL_Api();
				Map<String,Object> TMap = sL_Api.getSuolangToken(TokenMap);
				if(TMap!=null){
					Object Otoken = TMap.get("token");
					if(Otoken!=null){
						token = Otoken.toString();
						try {
							redisUtil.redisTemplate.opsForValue().set(password + ":" + appId, token, 10, TimeUnit.MINUTES);
						}catch (Exception e){
							System.out.println("[SetTokenShuoLang] redisUtil.redisTemplate.opsForValue().set 异常"+e.getMessage() );
						}
					}
				}
			}
		}catch (Exception e){
			System.out.println("[SetTokenShuoLang] 异常 "+e.getMessage());
		}
	}


	/**
	 * 获取 梦网 token 【主动推送型】
	 */
	public  void GetTokenMengWang(Map<String, Object> init_map) {
		try {
			token = init_map.get("cd_key").toString();
			redisUtil.redisTemplate.opsForValue().set(password + ":" + appId, token, 10, TimeUnit.MINUTES);
		}catch (Exception e){
			System.out.println("[GetTokenMengWang] 异常 "+e.getMessage()+" "+JSON.toJSONString(init_map));
		}
	}





	/**
	 *  检查返回数据为 token 失效 时 重新 获取 token
	 * @param Str
	 * @param json
	 * @param url
	 * @return
	 */
	public  String  repeat (String Str, JSONObject json, String url){
		if (Str != null) {
			JSONObject rmap = JSONObject.parseObject(Str);
			//{"status":"12021","message":"TOKEN不存在或已过期，请重新获取","result":[]}
			if (rmap.get("status").toString().equals("12021")) {

				//除雨哥那边不需要 传入token的外 重新获取token
				if(!cd_code.equals("YiDong_EC_TengYu")){
					// 重置 token , 再次执行
					if(cd_code.equals("YiDong_EC") || cd_code.equals("YiDong_EC_Combo")){
						SetToken(appId, password);
					}else if(cd_code.equals("YiDong_EC_TOKE_ShuoLang")){
						SetTokenShuoLang(TokenMap);
					}
					json.put("token", token);
					Str = HttpUtil.post(url, json.toJSONString());
				}else{
					try {
						Str = HttpUtil.sendPostJson(url, json.toJSONString());
					}catch (Exception e){
						System.out.println("repeat HttpUtil.sendPostJson "+e.getMessage());
					}
				}
			}
		}
		return Str;
	}

	/**
	 * 数据发送
	 * @param url
	 * @param json
	 * @return
	 */
	public String send(String url, JSON json){
		String Str = null;
		if(!cd_code.equals("YiDong_EC_TengYu")){
			Str = HttpUtil.post(url, json.toJSONString());
		}else {
			try {
				Str = HttpUtil.sendPostJson(url, json.toJSONString());
			}catch (Exception e){
				System.out.println(url+" <<<< >>>>  "+json.toJSONString()+" HttpUtil.sendPostJson 异常 "+e.getMessage());
			}
		}
		return Str;
	}

}
