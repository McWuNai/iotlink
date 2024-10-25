package com.yunze.apiCommon.upstreamAPI.YiKong;

import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.utils.HttpUtil;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 安徽翼控物联网 框架 API 接口
 * 2021-10-22
 */
public class YiKong_Api {


	//构造赋值
	public YiKong_Api(Map<String, Object> init_map){
		API_ID = init_map.get("cd_username").toString();
		API_PWD = init_map.get("cd_pwd").toString();
		API_KEY = init_map.get("cd_key").toString();
	}

	// 服务器请求地址    http://global.anhuiyk.cn:8006/m2m_api/v1/query/
	protected  static String server_Ip = "http://global.anhuiyk.cn:8006/m2m_api/v1/query/";
	// user_id
	protected static String API_ID = null;
	// 秘钥密码
	protected static String API_PWD = null;
	// key
	protected static String API_KEY = null;


	// 配置调用的名称
	protected static String Config_name = "";



	/**
	 * 异或算法加密
	 * @param str 数据
	 * @return 返回解密/加密后的数据
	 */
	public  String XorEncryptAndBaseNew(String str,String key) throws UnsupportedEncodingException {
		byte[] b1 = str.getBytes();
		byte[] b2 = key.getBytes();
		byte[] out = new byte[b1.length];
		for (int i = 0; i < b1.length; i++) {
			out[i] = (byte) (b1[i] ^ b2[i % b2.length]);
		}
		String EncodeStr = Base64.getEncoder().encodeToString(out);
		return EncodeStr;
	}


	public String send(String Url ,JSONObject json) throws Exception {
		json.put("pwd",API_PWD);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id",API_ID);
		map.put("info",XorEncryptAndBaseNew(json.toJSONString(),API_KEY));
		Url = Url!=null?Url:server_Ip;
		return HttpUtil.doPost(Url, map);
	}


}
