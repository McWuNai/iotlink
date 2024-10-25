package com.yunze.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@TableName(value = "yz_wx_autoreply")
public class WxAutoreply {

    @TableId(value = "id",type= IdType.AUTO)
    private int id;
    private int autoId;
    private String keywords;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String createUser;
    private String appId;
}
