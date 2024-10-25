package com.yunze.apiCommon.upstreamAPI.ShuYou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.utils.HttpUtil;
import com.yunze.apiCommon.utils.MD5Util;
import com.yunze.apiCommon.utils.UrlUtil;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 书佑 系统 API 接口
 */
public class ShuYou_DX_Api {


	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cd_username", "cy0330eb4008489df8");
		map.put("cd_pwd", "69c70477f3b4ddcdb731122a0390b721");
		ShuYou_DX_Api Qy = new ShuYou_DX_Api(map);

		//System.out.println(Qy.SimDetails("8986112025308004905"));
	}


	/**
	 * 3.1.1	查询设备信息详情
	 * @param iccid
	 * @return
	 */
	public String SimDetails(String iccid){
		JSONObject Outdata = new JSONObject();

		long time = System.currentTimeMillis();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("iccid", iccid);
		params.put("timestamp", time);
		params.put("transId",appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()) );
		params.put("appId", appId);
		params.put("sign", getSign(params));
		String url = server_Ip + "third/open/SimDetails";
		//params.put("url","https://api.iot.10086.cn/v2/gprsrealtimeinfo");//请求连接
		String reqUrl = UrlUtil.getUrl(url, params);
		//System.out.println(reqUrl);
		//System.out.println(JSON.toJSONString(params));
		String res = HttpUtil.post(url, JSON.toJSONString(params));

		return res;
	}


	/**
	 * 3.1.4	设备当前流量使用情况查询
	 * @param iccid
	 * @return
	 */
	public String SimQueryTraffic(String iccid){
		JSONObject Outdata = new JSONObject();
		long time = System.currentTimeMillis();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("appId", appId);
		params.put("iccid", iccid);
		params.put("timestamp", time);
		params.put("transId",appId + new SimpleDateFormat("YYYYMMDDHHMMSS").format(new Date()) );
		params.put("sign", getSign(params));
		String url = server_Ip + "third/open/SimQueryTraffic";
		//params.put("url","https://api.iot.10086.cn/v2/gprsrealtimeinfo");//请求连接
		String reqUrl = UrlUtil.getUrl(url, params);
		//System.out.println(reqUrl);
		String res = HttpUtil.get(reqUrl);

		return res;
	}






	//构造赋值
	public ShuYou_DX_Api(Map<String, Object> init_map){

		appId = init_map.get("cd_username").toString();
		appKey = init_map.get("cd_pwd").toString();

    }


	public  String getSign(Map<String, Object> params) {
		List<String> list=new ArrayList<>(params.keySet());
       /* for (int i = 0; i < list.size(); i++) {
        	//System.out.println(list.get(i));
		}*/
		Collections.sort(list);
		StringBuffer sb=new StringBuffer();
		sb.append(appKey);
		for(int i=0;i<list.size();i++){
			String k =list.get(i);
			Object v=params.get(k);
			//System.out.println("key="+k+"and value=" +params.get(k));
			sb.append(k).append(v);
		}
		sb.append(appKey);
		//System.out.println(sb);
		return MD5Util.MD5Encode(sb.toString());
	}

	// 服务器请求地址
	protected  static String server_Ip = "http://api.shuyouiot.com/";
	// user_id 					  
	protected static String appId = "";
	//Key值(apikey)：
	protected static String appKey = null;









}
