package com.yunze.common.core.domain.entity;

import com.yunze.common.annotation.Excel;
import com.yunze.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysAutoPolling extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /** 卡号 */
    @Excel(name = "卡号")
    private String iccid;

}
