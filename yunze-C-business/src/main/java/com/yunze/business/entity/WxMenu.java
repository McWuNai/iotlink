package com.yunze.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@TableName(value = "yz_wx_menu")
@Getter
@Setter
public class WxMenu {

    @TableId(value = "id",type= IdType.ID_WORKER_STR)
    private String id;
    private int menuId;
    private String menuName;
    private int menuPid;
    private String menuUrl;
    private int menuStatus;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    private String createUser;
    private String appId;
    private String menuType;
    private String mediaId;
}
