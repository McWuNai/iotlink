package com.yunze.apiCommon.upstreamAPI.YiDongEC;

import com.alibaba.fastjson.JSONObject;


/**
 * EC 官方返回数据格式
 */
public class ResJson extends JSONObject {

    private String status;
    private String message;
    private Object result;

    public ResJson(String status, String message, Object result) {
        this.status = status;
        this.message = message;
        this.result = result;
      /*  try {
            this.result = AesEncryptUtil.desEncrypt(result.toString(),"Api@iotyunze00","2020_12_10163622");
        }catch (Exception e){
            this.result = "返回数据加密失败";
            //System.out.println(e);
        }*/

    }

    public ResJson() {

    }

    public ResJson success(Object result){
        this.put("status","0");
        this.put("message","正确");
      /*  try {
            result = AesEncryptUtil.desEncrypt(result.toString(),"Api@iotyunze00","2020_12_10163622");
        }catch (Exception e){
            result = "返回数据加密失败";
            //System.out.println(e);
        }*/
        this.put("result",result);
        //System.out.println(this);
        return this;
    }

    public ResJson error(String status, String message, Object result){
        this.put("status",status);
        this.put("message",message);
        this.put("result",result);
        //System.out.println(this);
        return this;
    }
}
