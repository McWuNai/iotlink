package com.yunze.system.service.house;

import com.yunze.common.core.domain.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

public interface IModuleHouseService {

    Map<String, Object> list(Map<String, Object> map);

    void del(Map<String, Object> parammap);

    String importModule(MultipartFile file, HashMap<String, Object> map);

    String exportModule(Map<String, Object> map, SysUser currentUser);

    /**
     * 验证中箱号或卷盘号是否存在于模组明细库中且状态为在库状态
     * 状态说明：0=在库，-1=出库
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 验证规则：所有号码都必须满足在库状态，任何一个号码不满足要求都会导致整个验证失败
     * 
     * @param boxNumber  中箱号（支持逗号分隔的多个值）
     * @param reelNumber 卷盘号（支持逗号分隔的多个值）
     * @return 验证结果，包含详细的成功和失败信息
     */
    Map<String, Object> validateBoxOrReelInStock(String boxNumber, String reelNumber);

    /**
     * 将中箱号或卷盘号在模组明细库中的状态修改为出库状态
     * 状态说明：0=在库，1=出库
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 
     * @param boxNumber  中箱号（支持逗号分隔的多个值）
     * @param reelNumber 卷盘号（支持逗号分隔的多个值）
     * @param updateBy   更新人
     * @return 更新结果
     */
    Map<String, Object> updateModuleStatusToOutbound(String boxNumber, String reelNumber, String updateBy);

    /**
     * 统计中箱号或卷盘号在模组明细库中符合验证条件的总数量
     * 状态说明：0=在库，-1=出库
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 
     * @param boxNumber  中箱号（支持逗号分隔的多个值）
     * @param reelNumber 卷盘号（支持逗号分隔的多个值）
     * @return 统计结果，包含总数量
     */
    Map<String, Object> countModulesInStock(String boxNumber, String reelNumber);
}
