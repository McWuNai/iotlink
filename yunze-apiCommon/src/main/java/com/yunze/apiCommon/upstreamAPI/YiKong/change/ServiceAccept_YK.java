package com.yunze.apiCommon.upstreamAPI.YiKong.change;

import com.alibaba.fastjson.JSONObject;
import com.yunze.apiCommon.upstreamAPI.YiKong.YiKong_Api;

import java.util.Map;

/**
 * 变更 接口
 */
public class ServiceAccept_YK extends YiKong_Api {


    public ServiceAccept_YK(Map<String, Object> init_map) {
        super(init_map);
    }




    /**
     * 单卡卡状态操作
     * @param iccid
     * @param typeid
     * 0 - 激活
     * 1 - 去激活
     * 2 - 停机保号
     * @return
     */
    public String changeCardStatus(String iccid, String typeid) {
        String method =   "disabledNumber";
        JSONObject json = new JSONObject();
        json.put("method", method);
        json.put("iccid", iccid);
        json.put("typeid", typeid);

        String Str = null;
        try {
            Str = send(null,json);
        }catch (Exception e){
            System.out.println("ServiceAccept_Rht disabledNumber 异常 "+e.getMessage());
        }
        return Str;
    }



    /**
     * 单卡机卡重绑接口
     * @param iccid
     * @return
     */
    public String IMEIRe(String iccid) {
        String method =   "IMEIRe";
        JSONObject json = new JSONObject();
        json.put("method", method);
        json.put("iccid", iccid);
        String Str = null;
        try {
            Str = send(null,json);
        }catch (Exception e){
            System.out.println("ServiceAccept_Rht IMEIRe 异常 "+e.getMessage());
        }
        return Str;
    }

}
