package com.yunze.web.controller.yunze.commodity;


import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.impl.yunze.commodity.YzWxByProductCategoryServiceImpl;
import com.yunze.system.service.yunze.commodity.IYzWxByProductCategoryService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 商品分类
 */
@Api("商品分类")
@RestController
@RequestMapping("/yunze/prdCategory")
public class YzWxByProductCategoryController extends MyBaseController {

    @Resource
    private YzWxByProductCategoryServiceImpl yzWxByProductCategoryServiceImpl;

    @Resource
    private IYzWxByProductCategoryService iYzWxByProductCategoryService;

    /**
     * 获取商品 分类
     * */
    @PreAuthorize("@ss.hasPermi('yunze:prdCategory:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String selMap(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByProductCategoryServiceImpl.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/prdCategory/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取商品 分类 异常！");
    }





    /**
     * 商品 分类 新增
     * */
    @PreAuthorize("@ss.hasPermi('yunze:prdCategory:save')")
    @PostMapping(value = "/save", produces = { "application/json;charset=UTF-8" })
    public String save(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzWxByProductCategoryServiceImpl.save(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("保存数据 异常！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/prdCategory/save  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("商品 分类 新增 异常！");
    }


    /**
     * 商品 分类 修改
     * */
    @PreAuthorize("@ss.hasPermi('yunze:prdCategory:upd')")
    @PostMapping(value = "/upd", produces = { "application/json;charset=UTF-8" })
    public String upd(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzWxByProductCategoryServiceImpl.upd(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("保存数据 异常！");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/prdCategory/upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("商品 分类 修改 异常！");
    }





    /**
     * 获取所有 商品分类
     * */
    @PreAuthorize("@ss.hasPermi('yunze:product:list')")
    @PostMapping(value = "/getCategory", produces = { "application/json;charset=UTF-8" })
    public String getCategory(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByProductCategoryServiceImpl.getCategory(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/prdCategory/getCategory  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取所有 商品分类 异常！");
    }




}
















































































