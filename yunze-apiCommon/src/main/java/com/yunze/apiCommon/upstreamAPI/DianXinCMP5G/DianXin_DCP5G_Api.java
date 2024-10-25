package com.yunze.apiCommon.upstreamAPI.DianXinCMP5G;


import java.util.*;

/**
 * 电信CMP5G API 接口
 */
public class DianXin_DCP5G_Api {



	//构造赋值
	public DianXin_DCP5G_Api(Map<String, Object> init_map){

		 appKey = init_map.get("cd_username").toString();
		 secretKey = init_map.get("cd_pwd").toString();
		 timestamp = SignUtil.getCurTimestamp();

		//请求头部参数
		headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("AppKey", appKey);
		headers.put("Timestamp", timestamp);


	}
	// 服务器请求地址     https://cmp-api.ctwing.cn:20164
	protected  static String server_Ip = "https://cmp-api.ctwing.cn:20164";
	// 用户登录名称
	public static String appKey = null;

	public static String secretKey = null;

	public static Map<String, String> headers = null;

	public static String timestamp = null;
	// 配置调用的名称
	protected static String Config_name = "";



	public static String getSign(Map<String,Object> map) {
		List<String> list=new ArrayList<>(map.keySet());
		Collections.sort(list);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<list.size();i++){
			String k =list.get(i);
			String v=(String )map.get(k);
			if(sb.length()==0){
				sb.append(k).append("=").append(v);
			}else {
				sb.append("&").append(k).append("=").append(v);
			}
		}
		//System.out.println(sb);
		String Sign = "";
		try {
			 Sign = SignUtil.signMD5(sb + secretKey + timestamp);
		}catch (Exception e){
		}
		headers.put("Sign", Sign);
		return Sign; //生成sign加密值;
	}




}
