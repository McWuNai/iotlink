package com.yunze.web.controller.yunze.card;

import com.yunze.common.core.domain.AjaxResult;
import com.yunze.system.service.impl.yunze.YzCardServiceImpl;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Wang
 * @Date: 2024/09/12/11:33
 * @Description:
 */
@Api("卡信息导入导出")
@RestController
@RequestMapping("/yunze/card")
public class YzCardInfoReadController extends MyBaseController {

    @Resource
    private YzCardServiceImpl yzCardServiceImpl;

    /**
     * 导入
     * @param file
     * @return
     */
    @PostMapping(value = "/importCardInfoData", produces = {"application/json;charset=utf-8"})
    public AjaxResult importCardInfoData(MultipartFile file) throws IOException {
        return AjaxResult.success(yzCardServiceImpl.uploadCardInfo(file));
    }
    /**
     * 导出
     * @param Pstr
     *
     */
    @PostMapping(value = "/exportCardInfoData", produces = {"application/json;charset=utf-8"})
    public void exportCardInfoData(@RequestBody String Pstr, HttpServletResponse response) throws Exception {
        yzCardServiceImpl.exportCardInfo(Pstr,response);
    }
}
