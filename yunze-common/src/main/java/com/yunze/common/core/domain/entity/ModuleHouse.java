package com.yunze.common.core.domain.entity;

import com.yunze.common.annotation.Excel;
import com.yunze.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleHouse extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "销售员")
    private String salesperson;

    @Excel(name = "客户名称")
    private String customerName;

    @Excel(name = "销售订单号")
    private String salesOrderNumber;

    @Excel(name = "发货单单号")
    private String deliveryOrderNumber;

    @Excel(name = "发货时间")
    private String deliveryTime;

    @Excel(name = "客户销售订单号")
    private String customerSalesOrderNumber;

    @Excel(name = "最终客户")
    private String endCustomer;

    @Excel(name = "物流单号")
    private String logisticsNumber;

    @Excel(name = "收件人")
    private String consignee;

    @Excel(name = "客户收件人电话")
    private String consigneePhone;

    @Excel(name = "收件地址")
    private String deliveryAddress;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "型号")
    private String model;

    @Excel(name = "序列号")
    private String serialNumber;

    @Excel(name = "中箱号")
    private String middleBoxNumber;

    @Excel(name = "串号")
    private String deviceSerial;

    @Excel(name = "设备号")
    private String deviceID;

    @Excel(name = "SN")
    private String sn;

    @Excel(name = "原SN")
    private String originalSN;

    @Excel(name = "MAC")
    private String mac;

    @Excel(name = "ICCID")
    private String iccid;

    @Excel(name = "IMSI")
    private String imsi;

    @Excel(name = "卷盘号")
    private String reelNumber;

    @Excel(name = "固件版本")
    private String firmwareVersion;
}
