package com.yunze.apiCommon.upstreamAPI.ZCWL;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;


/**
 * 中创 系统 API 接口
 */
public class ZhonChuang_Api {




	//构造赋值
	public ZhonChuang_Api(Map<String, Object> init_map){
		appId  = init_map.get("cd_username").toString();
		appkey  = init_map.get("cd_pwd").toString();
		if(init_map.get("cd_key")!=null){
			domain = init_map.get("cd_key").toString();
		}else{
			domain = "http://zy.zyllz.com";
		}
	}
	// 用户登录名称
	public static String appId = "";
	// 秘钥key
	public static String appkey = "";
	public static String v = "2.0";
	// 服务器请求地址
	public static String domain = "http://zy.zyllz.com";//默认

	public static void  main(String [] args){
		/*queryRequst("queryCardStatus");*/
		//queryRequst("queryCardTraffic","89860487212071174747");



		queryRequst("queryCardSessionInfo","89860484052180043850");



		/*queryRequst("queryCardTraffic");*/
		/*queryRequst("queryCardPackage");*/
		/*queryRequst("sign");*/
		/*orderCardPackage();*/
	}

	static private Map getCommonParam(String iccid){
		Map<String,String> map = new HashMap();
		map.put("iccid",iccid);
		map.put("appId",appId);
		map.put("v",v);
		map.put("timestamp",System.currentTimeMillis()+"");

		return map;
	}

	public static JSONObject queryRequst(String apiMethod, String iccid){
		String url = domain + ":8898/iot/api/"+apiMethod;
		Map<String,String> map = getCommonParam(iccid);
		if("sign".equals(apiMethod)){
			map.put("appKey", appkey);
		}
		map.put("sign", sign(map,appkey));
		JSONObject json =(JSONObject) JSONObject.toJSON(map);
		return doPosts(url,json);
	}



	public static JSONObject doPosts(String url, JSONObject json){

		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		JSONObject response = null;
		try {
			StringEntity s = new StringEntity(json.toString());
			s.setContentEncoding("UTF-8");
			s.setContentType("application/json");//发送json数据需要设置contentType
			post.setEntity(s);
			HttpResponse res = httpclient.execute(post);
			if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = EntityUtils.toString(res.getEntity());// 返回json格式：
				response = JSON.parseObject(result);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//System.out.println(response);
		return response;
	}

	/**
	 * 使用<code>secret</code>对paramValues按以下算法进行签名： <br/>
	 * uppercase(hex(sha1(secretkey1value1key2value2...secret))
	 *
	 //     * @param paramNames  需要签名的参数名
	 * @param paramValues 参数列表
	 * @param secret
	 * @return
	 */
	public static String sign(Map<String, String> paramValues, String secret) {
		return sign(paramValues,null,secret);
	}

	/**
	 * 对paramValues进行签名，其中ignoreParamNames这些参数不参与签名
	 * @param paramValues
	 * @param ignoreParamNames
	 * @param secret
	 * @return
	 */
	public static String sign(Map<String, String> paramValues, List<String> ignoreParamNames, String secret) {
		try {
			byte[] sha1Digest = getSHA1Digest(getSignStr(paramValues, ignoreParamNames, secret));
			return byte2hex(sha1Digest);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getSignStr(Map<String, String> paramValues, List<String> ignoreParamNames, String secret){
		StringBuilder sb = new StringBuilder();
		List<String> paramNames = new ArrayList<>(paramValues.size());
		paramNames.addAll(paramValues.keySet());
		if(ignoreParamNames != null && ignoreParamNames.size() > 0){
			for (String ignoreParamName : ignoreParamNames) {
				paramNames.remove(ignoreParamName);
			}
		}
		Collections.sort(paramNames);

		sb.append(secret);
		for (String paramName : paramNames) {
			sb.append(paramName).append(paramValues.get(paramName));
		}
		sb.append(secret);

		return sb.toString();
	}

	public static String utf8Encoding(String value, String sourceCharsetName) {
		try {
			return new String(value.getBytes(sourceCharsetName), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static byte[] getSHA1Digest(String data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			bytes = md.digest(data.getBytes("UTF-8"));
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse);
		}
		return bytes;
	}

	/**
	 * 二进制转十六进制字符串
	 *
	 * @param bytes
	 * @return
	 */
	private static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}




}
