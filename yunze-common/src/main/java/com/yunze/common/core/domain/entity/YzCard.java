package com.yunze.common.core.domain.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.yunze.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * yz_card_info
 *
 * @author root
 */
@Data
@HeadStyle(fillForegroundColor = 9)
@ContentRowHeight(14)
@HeadRowHeight(14)
@ColumnWidth(20)
@AllArgsConstructor
@NoArgsConstructor
@ExcelIgnoreUnannotated
public class YzCard extends BaseEntity {

    @ExcelProperty(value = "MSISDN")
    private String msisdn;

    @ExcelProperty(value = "ICCID")
    private String iccid;

    @ExcelProperty(value = "IMSI")
    private String imsi;
    @ExcelIgnore
    private Integer status_ShowId;
    @ExcelProperty(value = "卡状态")
    private String card_status;

    @ExcelProperty(value = "IMEI")
    private String imei;

    @ExcelProperty(value = "激活日期")
    @DateTimeFormat("yyyy-MM-dd hh:mm:ss")
    private String activate_date;

    @ExcelProperty(value = "到期日期")
    @DateTimeFormat("yyyy-MM-dd hh:mm:ss")
    private String due_expire_time;
    @ExcelProperty(value = "周期用量")
    private Double used;
    @ExcelProperty(value = "分组")
    private Double customize_grouping;

    @ExcelProperty(value = "所属企业")
    private String dept_name;

    @ExcelProperty(value = "总量")
    private Double true_flow;

    @ExcelProperty(value = "已使用量(MB)")
    private Double use_so_flow_sum;

    @ExcelProperty(value = "剩余用量")
    private Double remaining;
    @ExcelProperty(value = "资费计划")
    private String packet_name;
    @ExcelIgnore
    private String package_id;

    @ExcelIgnore
    private String network_type;
    @ExcelProperty(value = "网络类型")
    private String networkDes;
    @ExcelProperty(value = "API通道")
    private String cd_alias;
    @ExcelIgnore
    private char cd_operator_type;
    @ExcelProperty(value = "运营商类型")
    private String operatorType;
    @ExcelProperty(value = "备注")
    private String remarks;
    @ExcelIgnore
    @DateTimeFormat("yyyy-MM-dd hh:mm:ss")
    private String open_date;
    @ExcelIgnore
    private String agent_id ;
    @ExcelIgnore
    private Integer channel_id ;

    @ExcelIgnore
    private String  is_pool;

    @ExcelIgnore
    @DateTimeFormat("yyyy-MM-dd hh:mm:ss")
    private String batch_date ;
    @ExcelIgnore
    private String status_id;
    @ExcelIgnore
    private String  type;

    @ExcelIgnore
    private Integer  is_sms ;

    @ExcelIgnore
    private String  sms_number;

    @ExcelIgnore
    private Integer gprs;

    @ExcelIgnore
    private Integer user_id;
    @ExcelIgnore
    private Double level;


}
