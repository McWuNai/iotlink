package com.yunze.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@TableName(value = "yz_wechat_user")
@Data
@Getter
@Setter
public class WxUser {

    @TableId(value = "id",type = IdType.ID_WORKER_STR)
    private String  id;
    private String userName;
    private String openid;
    private String headImage;
    private String iccid;
    private int sex;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String city;
    private String province;
    private String country;
    private int status;
    private String appId;
    private int bindStatus;
}
