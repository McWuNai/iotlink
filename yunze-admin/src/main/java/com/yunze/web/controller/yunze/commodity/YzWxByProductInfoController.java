package com.yunze.web.controller.yunze.commodity;


import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.impl.yunze.commodity.YzWxByProductInfoServiceImpl;
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
 * 商品管理
 * 2021-9-22
 */
@Api("商品管理")
@RestController
@RequestMapping("/yunze/productInfo")
public class YzWxByProductInfoController extends MyBaseController {

    @Resource
    private YzWxByProductInfoServiceImpl yzWxByProductInfoServiceImpl;

    /**
     * 获取商品 名称
     */
    @PreAuthorize("@ss.hasPermi('yunze:order:list')")
    @PostMapping(value = "/findName", produces = {"application/json;charset=UTF-8"})
    public String findName(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByProductInfoServiceImpl.findName(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productInfo/findName  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("获取商品 名称 操作失败！");
    }


    /**
     * 获取商品 列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:product:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String list(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByProductInfoServiceImpl.selMap(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productInfo/list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("获取商品 列表 操作失败！");
    }


    /**
     * 新增 商品
     */
    @PreAuthorize("@ss.hasPermi('yunze:product:add')")
    @PostMapping(value = "/add", produces = {"application/json;charset=UTF-8"})
    public String add(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzWxByProductInfoServiceImpl.save(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！", null);
            }else{
                return Myerr("新增 商品 操作失败！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productInfo/add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("新增 商品 操作失败！");
    }





    /**
     * 编辑 商品
     */
    @PreAuthorize("@ss.hasPermi('yunze:product:edit')")
    @PostMapping(value = "/edit", produces = {"application/json;charset=UTF-8"})
    public String edit(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzWxByProductInfoServiceImpl.edit(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！", null);
            }else{
                return Myerr("编辑 商品 操作失败！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productInfo/edit  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("编辑 商品 操作失败！");
    }


    /**
     * 查询单条 商品
     */
    @PreAuthorize("@ss.hasPermi('yunze:product:edit')")
    @PostMapping(value = "/findData", produces = {"application/json;charset=UTF-8"})
    public String findData(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByProductInfoServiceImpl.findData(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productInfo/findData  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("编辑 商品 操作失败！");
    }



}
















































































