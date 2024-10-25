package com.yunze.apiCommon.upstreamAPI.IoTLink.change;


import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.upstreamAPI.IoTLink.IoTLink_Api;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;


public class serviceAccept_IoTLink extends IoTLink_Api {


    public serviceAccept_IoTLink(Map<String, Object> init_map) {
        super(init_map);
    }


    /**
     * 卡状态变更
     * @param iccid
     * @param Status
     * @return
     */
    public  String changeSimStatus(String iccid,String Status) {
        String api_url = server_Ip + "/changeSimStatus";
        Map<String, Object> Parmap = new HashMap<String, Object>();
        Parmap.put("iccid", iccid);
        Parmap.put("Status", Status);
        String data = JSON.toJSONString(getPmap(Parmap));
        return HttpUtil.post(api_url, data);
    }



}
