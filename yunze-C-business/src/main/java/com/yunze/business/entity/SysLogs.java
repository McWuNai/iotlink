package com.yunze.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 系统级日志
 * </p>
 *
 * @author yunxi
 * @since 2021-12-21
 */
@Getter
@Setter
@Data
@TableName("yz_sys_logs")
public class SysLogs implements Serializable {

private static final long serialVersionUID=1L;

        /**
         * id
         */
        private String id;

        /**
         * 请求url
         */
        private String resUrl;

        /**
         * 创建时间
         */
        @TableField(fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 执行的类方法
         */
        private String classMethod;

        /**
         * ip
         */
        private String ip;

        /**
         * 请求参数
         */
        private String requestArgs;

        /**
         * 响应参数
         */
        private String responseArgs;

        /**
         * 操作人
         */
        private String userName;

        private String project;
        
        /**
         * 方法名
         */
        private String actionName;



        }