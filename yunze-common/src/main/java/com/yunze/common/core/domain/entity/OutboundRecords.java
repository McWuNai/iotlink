package com.yunze.common.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunze.common.annotation.Excel;
import com.yunze.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 出库记录表对象 outbound_records
 * 
 * @author yunze
 * @date 2025-01-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutboundRecords extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 收货单位 */
    @Excel(name = "收货单位")
    private String receivingUnit;

    /** 收货地址 */
    @Excel(name = "收货地址")
    private String receivingAddress;

    /** 货品名称 */
    @Excel(name = "货品名称")
    private String productName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specificationModel;

    /** 中箱号 */
    @Excel(name = "中箱号")
    private String boxNumber;

    /** 卷盘号 */
    @Excel(name = "卷盘号")
    private String reelNumber;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 数量 */
    @Excel(name = "数量")
    private BigDecimal quantity;

    /** 发货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发货日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date shippingDate;

    /** 收货联系电话 */
    @Excel(name = "收货联系电话")
    private String contactPhone;

    /** 备注 */
    @Excel(name = "备注")
    private String remarks;

    /** 快递单号 */
    @Excel(name = "快递单号")
    private String courierNumber;

    /** 供货单位 */
    @Excel(name = "供货单位")
    private String supplierUnit;

    /** 供货联系人 */
    @Excel(name = "供货联系人")
    private String supplierContactPerson;

    /** 供货联系电话 */
    @Excel(name = "供货联系电话")
    private String supplierContactPhone;
}
