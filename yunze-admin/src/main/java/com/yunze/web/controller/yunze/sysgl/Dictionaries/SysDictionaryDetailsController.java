package com.yunze.web.controller.yunze.sysgl.Dictionaries;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.controller.BaseController;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.yunze.sysgl.Dictionaries.ISysDictionaryDetailsService;
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
 * 出库入库管理
 *
 */

@Api("出库入库")
@RestController
@RequestMapping("yunze/Administration")
public class SysDictionaryDetailsController extends MyBaseController {

    @Autowired
    private ISysDictionaryDetailsService ISysDictionaryDetailsService;

    /**查询一级分类*/

    @PreAuthorize("@ss.hasPermi('yunze:Administration:type')")
    @PostMapping(value = "/type", produces = { "application/json;charset=UTF-8" })
    public String getType(@RequestBody String Pstr){
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(ISysDictionaryDetailsService.classification(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:Administration:type  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("大分类 操作失败！");
    }
}
