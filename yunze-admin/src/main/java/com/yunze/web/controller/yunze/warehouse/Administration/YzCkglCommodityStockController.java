package com.yunze.web.controller.yunze.warehouse.Administration;
import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityStoctService;
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
 * 库存清单
 */

@Api("库存清单")
@RestController
@RequestMapping("yunze/InventoryList")
public class YzCkglCommodityStockController extends MyBaseController {

    @Resource
    private YzCkglCommodityStoctService yzCkglCommodityStoctService;

    /**
     * 查询
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:InventoryList:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String list(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {

            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityStoctService.selMap(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:InventoryList:information  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("查询 库存清单 操作失败！");
    }


    /**
     * 手动修改库存
     * @param Pstr
     * @return
     */
    @PreAuthorize("@ss.hasPermi('yunze:InventoryList:upd')")
    @PostMapping(value = "/upd", produces = { "application/json;charset=UTF-8" })
    public String upd (@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            boolean bool = yzCkglCommodityStoctService.upd(Parammap);
            if(bool){
                return MyRetunSuccess("操作成功！",null);
            }else{
                return Myerr("手动修改库存 操作失败");
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/>  InventoryList/upd  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("手动修改库存 操作失败！");
    }

}




















