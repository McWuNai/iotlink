package com.yunze.apiCommon.upstreamAPI.LianTongCMP;

import com.cu.api.IIoTGatewayClient;
import com.cu.api.IoTGatewayClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Random;

/**
 * 联通 CMP 系统 API 接口
 */
public class LT_CMP_Api {


	//构造赋值
	public LT_CMP_Api(Map<String, Object> init_map){

		appId = init_map.get("cd_username").toString();
		appSecret = init_map.get("cd_pwd").toString();
		openId = init_map.get("cd_key").toString();

		this.client = new IoTGatewayClient(server_Ip, appId,appSecret);
    }
	
	// 服务器请求地址				  http://api.ct10649.com:9001
	protected  static String server_Ip = "https://gwapi.10646.cn/api/";;
	// user_id 					  
	protected static String appId = null;
	// 秘钥密码 	
	protected static String appSecret = null;
	//版本
	protected static String version = "1.0";
	// 配置调用的名称
	protected static String Config_name = "";
	// 登录用户
	protected static String openId =null;


	protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
	protected static SimpleDateFormat sdf_lip = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	protected static Random random = new Random();
	//接口操作类
	protected static IIoTGatewayClient client = null;

	protected static String timestamp = "";
	protected static String trans_id = "";

	/**
	 * 项目描述	获取序列号
	 * @throws IOException
	 */
	public static String gettrans_id(){
		trans_id = timestamp.replace("-", "").replace(" ", "").replace(":", "");
		return trans_id + new Random().nextInt(999999);
	}


}
