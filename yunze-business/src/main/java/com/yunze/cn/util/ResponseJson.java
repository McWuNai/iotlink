package com.yunze.cn.util;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @title: ResponseJson
 * @projectName yunze
 * @description: TODO
 * @date 2021/10/23 00239:30
 */
public class ResponseJson extends HashMap<String ,Object>  {

    public static String SUCCESS = "200";
    public static String SUCCESSMessage = "操作成功！";

    /**
     * @Author
     * @Description:成功且带数据
     * @date 2021/10/23 0023 9:55
     * @Param
     * @return
     **/
    public static Result success(Object object){
        Result response=new Result();
        response.setCode(SUCCESS);
        response.setMsg(SUCCESSMessage);
        response.setData(object);
        return response;
    }

    /**
     * @Author
     * @Description:成功不带数据
     * @date 2021/10/23 0023 9:58
     * @Param
     * @return
     **/
    public static Result success(){
        return success(null);
    }

    /**
     * @Author
     * @Description:   失败
     * @date 2021/10/23 0023 10:00
     * @Param
     * @return
     **/
    public static Result error(String code, String msg){
        Result response=new Result();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }


    /**
     * my 成功返回函数
     */
    public static String MyRetunSuccess(Object obj,String msg)
    {
        msg = msg!=null?msg:"操作成功";
        Map<String, Object> Rmap =  new HashMap<String, Object>();
        Rmap.put("code",200);
        Rmap.put("msg",msg);
        Rmap.put("Data",obj);
        try {
            return AesEncryptUtil.encrypt(JSON.toJSONString(Rmap));
        }catch (Exception e){
            System.err.println(e);
            return "数据返回加密失败……";
        }
    }



    public static String Myerr(String Msg)
    {
        Map<String ,Object> map = new HashMap<String,Object>();
        map.put("code",500);
        map.put("msg",Msg);
        try {
            return AesEncryptUtil.encrypt(JSON.toJSONString(map));
            //return JSON.toJSONString(map);
        }catch (Exception e){
            System.err.println(e);
            return "数据返回加密失败……";
        }
    }


}
