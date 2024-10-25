package com.yunze.apiCommon.upstreamAPI.TengYu.change;

import com.yunze.apiCommon.upstreamAPI.TengYu.TengYu_Api;
import com.yunze.apiCommon.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 变更 接口
 */
public class ServiceAccept_Ty extends TengYu_Api {


    public ServiceAccept_Ty(Map<String, Object> init_map) {
        super(init_map);
    }




    /**
     * 单卡卡状态操作
     * @param iccid
     * @param operType
     * 0:申请停机(已激活转已停机)
     * 1:申请复机(已停机转已激活)
     * @return
     */
    public String changeCardStatus(String iccid, String operType) {
        String method =   "/card/changeSimStatus";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("appid", API_ID);
        params.put("iccid", iccid);
        params.put("operType", operType);
        String res = null;
        try {
            res = HttpUtil.doPost(server_Ip+method, params);
        }catch (Exception e){
            System.out.println("iccid "+iccid+" operType "+operType+" 异常 "+e.getMessage());
        }
        return res;
    }





}
