package com.yunze.web.view;

import lombok.Data;

/**
 * return: 返回交给前端调起支付的信息
 */
@Data
public class ReturnViewPayInfo {

    private String appId;

    private String timeStamp;
    private String nonceStr;
    private String prepayId;
    private String paySign;
    private String signType;
}
