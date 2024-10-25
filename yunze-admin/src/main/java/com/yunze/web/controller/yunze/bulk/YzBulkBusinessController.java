package com.yunze.web.controller.yunze.bulk;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.bulk.IYzBulkBusinessService;
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
 * 批量业务受理
 * 2022-05-25
 */
@Api("批量业务受理")
@RestController
@RequestMapping("/yunze/business")
public class YzBulkBusinessController extends MyBaseController {

    @Resource
    private IYzBulkBusinessService IYzBulkBusinessService;

    /**
     * 批量业务受理 列表查询
     */
    @PreAuthorize("@ss.hasPermi('yunze:batchBusiness:list')")
    @PostMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    public String SynFlow(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
            SysUser currentUser = loginUser.getUser();
            String dept_id = currentUser.getDeptId().toString();
            if(!dept_id.equals("100")){
                Parammap.put("agent_id",dept_id);
            }
            return MyRetunSuccess(IYzBulkBusinessService.getList(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> system:batchBusiness:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("批量业务受理 操作失败！");
    }

}
