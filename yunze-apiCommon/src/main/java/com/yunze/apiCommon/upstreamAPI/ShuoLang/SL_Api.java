package com.yunze.apiCommon.upstreamAPI.ShuoLang;

import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.MD5Util;
import com.yunze.apiCommon.utils.UrlUtil;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 硕朗物理网 系统 API 接口
 */
@Component
public class SL_Api {



	/**
	 *  硕朗秘钥共享token 获取
	 * @param api
	 * @return
	 */
	public  Map<String, Object> getSuolangToken(Map<String, Object> api) {
		Map<String, Object> Rmap = new HashMap<>();
		//System.out.println("=[api]");
		//System.out.println(api.toString());
		String rstr = null;
		String userId = api.get("userId").toString();
		String channel_id = api.get("channel_id").toString();
		String channel_pname = api.get("channel_pname").toString();
		String apikey = api.get("apikey").toString();
		Long times = System.currentTimeMillis();
		Map<String, Object> params = new HashMap<String, Object>();

		params.put("userId", userId);
		params.put("channel_id", channel_id);
		params.put("channel_pname", channel_pname);
		params.put("times", times+"");
		String md5Str=MessageFormat.format("userId={0}&apikey={1}&times={2}", userId, apikey, times+"");
		params.put("sign", MD5Util.MD5Encode(md5Str).toUpperCase());

		//System.out.println(params.toString());
		//System.out.println(JsonKit.toJson(params));
		String param_url = UrlUtil.getUrl(api.get("method").toString(), params);

		String res = HttpUtil.get(param_url);
		System.out.println(res);
		JSONObject dataJson = JSONObject.parseObject(res);

		//System.out.println("SuoLang 共享 TOken 获取  "+ EscapeUtil.unescape(dataJson.getString("msg")));
		if (dataJson.getString("code").equals("0")) {
			JSONObject data = (JSONObject)dataJson.get("data");
			/*rstr = "{\"status\":\"0\",\"message\":\"正确\", \"result\":[ { \"token\": \""
					+ data.get("accesstoken").toString() + "\"}]}";*/
			Rmap.put("token",data.get("accesstoken").toString());
		} else {
			Rmap.put("token",null);
			//rstr = "{\"status\":\"12022\",\"message\":\"PASSWORD 鉴权不通过\",\"result\":[{\"token\":\"-1\"}]}";
		}
		//System.out.println("rstr  = " + rstr);
		return Rmap;
	}


}
