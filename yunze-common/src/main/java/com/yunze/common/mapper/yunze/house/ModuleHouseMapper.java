package com.yunze.common.mapper.yunze.house;

import com.yunze.common.core.domain.entity.ModuleHouse;

import java.util.List;
import java.util.Map;

public interface ModuleHouseMapper {
    /**
     * 查询总数
     * 
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String, Object> map);

    List<Map<String, Object>> list(Map<String, Object> map);

    List<Map<String, Object>> test(Map<String, Object> map);

    void ins(ModuleHouse moduleHouse);

    void del(Map<String, Object> map);

    Map<String, Object> selectModuleBySerialNumber(ModuleHouse moduleHouse);

    Map<String, Object> selectModuleBySN(ModuleHouse moduleHouse);

    List<String> isExistence(Map<String, Object> map);

    /**
     * 验证中箱号或卷盘号是否存在于模组明细库中且状态为在库状态
     * 状态说明：0=在库，-1=出库
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 
     * @param map 查询参数，包含boxNumber、reelNumber、boxNumberList、reelNumberList
     * @return 查询结果
     */
    Map<String, Object> validateBoxOrReelInStock(Map<String, Object> map);

    /**
     * 将中箱号或卷盘号在模组明细库中的状态修改为出库状态
     * 状态说明：0=在库，1=出库
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 
     * @param map 更新参数，包含boxNumberList、reelNumberList、updateBy
     * @return 更新结果
     */
    int updateModuleStatusToOutbound(Map<String, Object> map);

    /**
     * 统计中箱号或卷盘号在模组明细库中符合验证条件的总数量
     * 状态说明：0=在库，1=出库
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 
     * @param map 查询参数，包含boxNumberList、reelNumberList
     * @return 统计结果
     */
    int countModulesInStock(Map<String, Object> map);

    /**
     * 根据中箱号或卷盘号查询模组明细数据
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 
     * @param map 查询参数，包含boxNumberList、reelNumberList
     * @return 模组明细列表
     */
    List<Map<String, Object>> selectModulesByBoxOrReel(Map<String, Object> map);

    /**
     * 将中箱号或卷盘号在模组明细库中的状态修改为在库状态
     * 状态说明：0=在库，1=出库
     * 支持多个值：中箱号或卷盘号可以用半角英文逗号分隔多个值
     * 
     * @param map 更新参数，包含boxNumberList、reelNumberList、update_by、update_time
     * @return 更新结果
     */
    int updateModuleStatusToInStock(Map<String, Object> map);
}
