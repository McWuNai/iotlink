package com.yunze.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@TableName(value = "yz_wx_config")
@Data
@Getter
@Setter
public class WxConfig {

    @TableId(value = "id")
    private String id;
    private String appId;
    private String appSecret;
    private String appToken;
    private String originId;
    private String mchId;
    private String paternerKey;
    private String indexUrl;
    private String backUrl;
    private int status;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    private String createUser;
    private String companyId;
    private String agentId;
    private String nativeUrl;
    private String authTxt;
}
