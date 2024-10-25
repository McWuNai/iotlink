package com.yunze.web.controller.yunze.card;

import com.yunze.common.core.controller.BaseController;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.system.service.yunze.IYzCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usage")
public class YzCardUsageController extends BaseController {
    @Autowired
    private IYzCardService yzCardService;

    /***
     *日用量统计
     * @param query
     * @return
     */
    @PostMapping("/cardUsage")
    public AjaxResult cardUsage(@RequestBody String query) {
        try {
            return yzCardService.distinguishCardType(query);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
    @PostMapping("/cardMonthUsage")
    public AjaxResult cardMonthUsage(@RequestBody String query) {
        try {
            return yzCardService.cardMonthUsage(query);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}