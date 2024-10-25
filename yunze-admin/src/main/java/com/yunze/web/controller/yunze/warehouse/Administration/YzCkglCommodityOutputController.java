package com.yunze.web.controller.yunze.warehouse.Administration;

import com.alibaba.fastjson.JSON;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityOutputService;
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
 * 出库表
 * 2022-1-5
 */
@Api("出库管理")
@RestController
@RequestMapping("/yunze/output")
public class YzCkglCommodityOutputController extends MyBaseController {

    @Autowired
    private YzCkglCommodityOutputService yzCkglCommodityOutputService;

    /**
     * 查询列表
     */
    @PreAuthorize("@ss.hasPermi('warehouse:outku:list')")
    @PostMapping(value = "/listoutku", produces = {"application/json;charset=UTF-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityOutputService.SleOutKu(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:output:listoutku  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("入库查询 操作失败！");
    }


    /**
     * 新增出库信息
     */
    @PreAuthorize("@ss.hasPermi('yunze:output:outofWarehouse')")
    @PostMapping(value = "/outofWarehouse", produces = {"application/json;charset=UTF-8"})
    public String add_CODS(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzCkglCommodityOutputService.add_CODS(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:output:outofWarehouse  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("出库信息 操作失败！");
    }

}



















