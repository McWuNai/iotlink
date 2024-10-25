package com.yunze.common.utils.yunze;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

/**
 * @Auther: zhang feng
 * @Date: 2021/08/20/12:24
 * @Description:
 */
public class OutECImei {


    public String getSign(Map<String,Object> map,String key){//入参为：appid,password,version,iccid,timestamp,sign
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()){// 循环取键值进行判断
            String m = iterator.next();// 键
            if(m.startsWith("sign")){
                iterator.remove();// 移除map中以a字符开头的键对应的键值对
            }
            if(m.startsWith("iccids")){
                iterator.remove();
            }
        }
        List<String> list=new ArrayList<>(map.keySet());
        Collections.sort(list);
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<list.size();i++){
            String k =list.get(i);
            String v=(String )map.get(k);
            sb.append(k).append("=").append(v).append("&");
        }
        String signstr=sb.append("key=").append(key).toString();
        //System.out.println(signstr);
        String sign =MD5Util.MD5Encode(signstr).toUpperCase();
        //System.out.println(sign);
        return sign;
    }


}
