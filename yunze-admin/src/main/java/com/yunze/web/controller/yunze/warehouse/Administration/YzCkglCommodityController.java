package com.yunze.web.controller.yunze.warehouse.Administration;

import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 *查询商品信息
 */

@Api("查询商品信息")
@RestController
@RequestMapping("yunze/commodity")
public class YzCkglCommodityController extends MyBaseController {

    @Autowired
    private YzCkglCommodityService yzCkglCommodityService;

    /**多选框查询*/
    @PreAuthorize("@ss.hasPermi('yunze:Warehousing:information')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String FindInfo(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityService.commodity(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:commodity:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("多选框查询 操作失败！");
    }

    /**载入查询*/
    @PreAuthorize("@ss.hasPermi('yunze:Warehousing:information')")
    @PostMapping(value = "/queryloading", produces = { "application/json;charset=UTF-8" })
    public String SynArr(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityService.queryloading(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:commodity:queryloading  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("载入查询 操作失败！");
    }


    /**
     * 查询
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:commodity:list')")
    @PostMapping(value = "/SelMap", produces = { "application/json;charset=UTF-8" })
    public String SelMap(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> SelMap  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("多选框查询 操作失败！");
    }


    /**
     * 商品 修改
     * @return
     */
    @Log(title = "C端商品", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:CkglCommodity:upd')")
    @PostMapping(value = "/upd", produces = { "application/json;charset=utf-8" })
    public String upd(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool =yzCkglCommodityService.upd(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("修改操作失败");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("修改 商品 操作失败！");
    }


    /**
     * 商品 新增
     * @return
     */
    @Log(title = "C端商品", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:CkglCommodity:add')")
    @PostMapping(value = "/add", produces = { "application/json;charset=utf-8" })
    public String add(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));

            Map<String,Object> Rmap = yzCkglCommodityService.save(Parammap);
            boolean bool = (boolean) Rmap.get("bool");
            String Msg =  Rmap.get("Msg").toString();
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr(Msg);
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("新增商品 操作失败！");
    }


    /**
     * 查询单条 商品
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:CkglCommodity:find')")
    @PostMapping(value = "/find", produces = { "application/json;charset=utf-8" })
    public String find(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess( yzCkglCommodityService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询单条 商品 操作失败！");
    }




}




















