package com.yunze.apiCommon.upstreamAPI.YiDongEcV2;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.SHA256Util;
import com.yunze.apiCommon.utils.UrlUtil;
import com.yunze.apiCommon.utils.VeDate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 移动 ECv2 系统 API 接口
 */
public class YD_EcV2_Api {


	//构造赋值
	public YD_EcV2_Api(Map<String, Object> init_map){
		appId = init_map.get("cd_username").toString();
		password = init_map.get("cd_pwd").toString();
		ecID = init_map.get("cd_key").toString();
		token = getToken();
    }
	
	// 服务器请求地址
	protected  static String server_Ip = "https://api.iot.10086.cn/v2";
	// user_id 					  
	protected static String appId = null;
	// 秘钥密码 	
	protected static String password = null;
	// 配置调用的名称
	protected static String Config_name = "";
	// 企业 编号
	protected static String ecID =null;
	// 请求 编号
	protected static String transid =null;
	//token 获取请求参数
	protected static Map<String,Object> TokenMap = null;
	// token
	protected static String token = null;


	/**
	 * 获取 token
	 */
	public String getToken() {
		transid = VeDate.getTransid(appId,8);
		return SHA256Util.getSHA256StrJava(appId+password+transid).toLowerCase();
	}

	/**
	 * 数据发送
	 * @param url
	 * @param map
	 * @return
	 */
	public String send(String url, Map<String,Object> map){
		String Str = null;
		try {
			Str = HttpUtil.doPost(url, map);
		}catch (Exception e){
			System.out.println("url : "+url+" map : "+map+" 返回结果异常 "+e.getMessage());
		}
		/*System.out.println(url);
		System.out.println(map);
		System.out.println(Str);*/
		return Str;
	}


}
