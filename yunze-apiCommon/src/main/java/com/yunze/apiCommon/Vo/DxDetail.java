package com.yunze.apiCommon.Vo;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName DxDetail
 * @Description
 * @Author QiMing
 * @Date 2022/11/8 11:14
 * @Version 1.0
 */
@Data
public class DxDetail {

    private String enterprise;
    private Date firstActivatedAt;
    private String iccid;
    private String detectedImei;
    private String imsi;
    private Date lastSubscriptionPackageChangeAt;
    private Date lastSubscriptionChangeDateAt;
    private String msisdn;
    private String organizationId;
    private String state;
}
