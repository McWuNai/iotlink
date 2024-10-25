package com.yunze.web.core.config;

import com.alibaba.fastjson.JSON;
import com.yunze.common.constant.HttpStatus;
import com.yunze.common.core.controller.BaseController;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * web层通用数据处理
 */
public class MyBaseController  extends BaseController
{

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());




    /**
     * my 成功返回函数
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected String MyRetunSuccess(Object obj,String msg)
    {
        LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
        String deptId = "";
        if(loginUser!=null){
             deptId = loginUser.getUser().getDeptId().toString();
        }
        msg = msg!=null?msg:"操作成功";
        Map<String, Object> Rmap =  new HashMap<String, Object>();
        Rmap.put("code",HttpStatus.SUCCESS);
        msg = deptId.equals("100")?msg:"操作成功!";//分为内部返回和 外部返回
        Rmap.put("msg",msg);
        Rmap.put("deptId",deptId);
        Rmap.put("Data",obj);
        try {
            return AesEncryptUtil.encrypt(JSON.toJSONString(Rmap));
        }catch (Exception e){
            System.err.println(e);
            return "数据返回加密失败……";
        }
    }


    protected String Myerr(String Msg)
    {
        LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
        String deptId = "";
        if(loginUser!=null){
             deptId = loginUser.getUser().getDeptId().toString();
        }
        //LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
        //String deptId = loginUser.getUser().getDeptId().toString();
        Map<String ,Object> map = new HashMap<>();
        Msg = deptId.equals("100")?Msg:"操作失败!";//分为内部返回和 外部返回
        map.put("code",HttpStatus.ERROR);
        map.put("msg",Msg);
        map.put("deptId",deptId);
        try {
            return AesEncryptUtil.encrypt(JSON.toJSONString(map));
            //return JSON.toJSONString(map);
        }catch (Exception e){
            System.err.println(e);
            return "数据返回加密失败……";
        }
    }


}
