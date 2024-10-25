package com.yunze.apiCommon.upstreamAPI.XuYu.change;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.XuYu.XunYu_Api;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 变更 接口
 */
public class serviceAccept_XuYu extends XunYu_Api {


    public serviceAccept_XuYu(Map<String, Object> init_map) {
        super(init_map);
    }

    /**
     * 停复机
     * @param accessNums 接入号
     * @param operType 0停机；1复机
     * @return
     */
    public static String changeCardStatus(String accessNums,String operType){
        String Rstr = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("msisdn",accessNums);
            params.put("agencyCode",agencyCode);
            params.put("operType",operType);
            String sign = getSign(params);
            params.put("key",key);
            params.put("sign",sign);
            String url = server_Ip + "/updateCardStatusSingle";
            Rstr = HttpUtil.sendPostJson(url, JSON.toJSONString(params));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Rstr;
    }





}
