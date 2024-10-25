package com.yunze.apiCommon.utils;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 内部请求获取请求参数
 */
@Component
public class GetApiParam {

    /**
     * 封装查询api的必要参数
     * @param pMap
     * @return
     */
    public Map<String,Object> getApiParams(Map<String,Object> pMap){
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> Yzmap = new HashMap<String, Object>();
        Map<String, Object> Parmap = new HashMap<String, Object>();

        String appId = "";
        String password = "";
        String access_key = "";
        String timeStamp = System.currentTimeMillis() + "";


        Yzmap.put("password", password);
        Yzmap.put("appId", appId);
        Yzmap.put("timeStamp", timeStamp);

        Yzmap.put("password", password);
        Yzmap.put("appId", appId);
        Yzmap.put("timeStamp", timeStamp);
        String sign = getSign(Yzmap, access_key);
        Yzmap.put("sign", sign);

        if(!pMap.isEmpty()){
            for(String key:pMap.keySet()){
                Parmap.put(key, pMap.get(key).toString());
            }
        }
        map.put("verify", Yzmap);
        map.put("Param", Parmap);
        return map;
    }

    /**
     * Sign 加密
     * @param map
     * @param key
     * @return
     */
    public static String getSign(Map<String, Object> map, String key) {//入参为：appid,password,version,iccid,timestamp,sign
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {// 循环取键值进行判断
            String m = iterator.next();// 键
            if (m.startsWith("sign")) {
                iterator.remove();// 移除map中以a字符开头的键对应的键值对
            }
            if (m.startsWith("iccids")) {
                iterator.remove();
            }
        }
        List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            String k = list.get(i);
            String v = (String) map.get(k);
            sb.append(k).append("=").append(v).append("&");
        }
        String signstr = sb.append("key=").append(key).toString();
        String sign = MD5Util.MD5Encode(signstr).toUpperCase();
        return sign;
    }
}
