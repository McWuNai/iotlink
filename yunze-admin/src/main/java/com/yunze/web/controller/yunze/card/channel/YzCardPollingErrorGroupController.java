package com.yunze.web.controller.yunze.card.channel;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.IYzCardPollingErrorGroupService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * 轮询错误概要
 * 2022-5-16
 * */

@Api("轮询错误概要")
@RestController
@RequestMapping("/yunze/group")
public class YzCardPollingErrorGroupController extends MyBaseController {


    @Autowired
    private IYzCardPollingErrorGroupService IYzCardPollingErrorGroupService;


    @PreAuthorize("@ss.hasPermi('yunze:group:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String listGroup(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(IYzCardPollingErrorGroupService.listGroup(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:group:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("轮询错误概要 操作失败！");
    }

    /**
     * 单个删除轮询错误概要
     * */
    @PreAuthorize("@ss.hasPermi('yunze:group:del')")
    @PostMapping(value = "/del", produces = {"application/json;charset=UTF-8"})
    public String DeleteId(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = IYzCardPollingErrorGroupService.del(Parammap);
            if(bool){
                return MyRetunSuccess("删除成功",null);
            }else{
                return Myerr("删除失败");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:group:del  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单个删除轮询错误概要 操作失败！");
    }


    /**
     * 单个修改轮询错误概要
     * */
    @PreAuthorize("@ss.hasPermi('yunze:group:upd')")
    @PostMapping(value = "/upd", produces = {"application/json;charset=UTF-8"})
    public String UpdateId(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = IYzCardPollingErrorGroupService.upd(Parammap);
            if(bool){
                return MyRetunSuccess("修改成功",null);
            }else{
                return Myerr("修改失败");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:group:upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("单个修改轮询错误概要 操作失败！");
    }


    /**
     * 批量删除轮询错误概要
     * */
    @PreAuthorize("@ss.hasPermi('yunze:group:delArr')")
    @PostMapping(value = "/delArr", produces = {"application/json;charset=UTF-8"})
    public String DeleteArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = IYzCardPollingErrorGroupService.delArr(Parammap);
            if(bool){
                return MyRetunSuccess("批量删除成功",null);
            }else{
                return Myerr("批量删除失败");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:group:delArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量删除轮询错误概要 操作失败！");
    }


    /**
     * 批量修改轮询错误概要
     * */
    @PreAuthorize("@ss.hasPermi('yunze:group:updArr')")
    @PostMapping(value = "/updArr", produces = {"application/json;charset=UTF-8"})
    public String UpdateArr(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = IYzCardPollingErrorGroupService.updArr(Parammap);
            if(bool){
                return MyRetunSuccess("批量修改成功",null);
            }else{
                return Myerr("批量修改失败");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:group:updArr  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量修改轮询错误概要 操作失败！");
    }

}
