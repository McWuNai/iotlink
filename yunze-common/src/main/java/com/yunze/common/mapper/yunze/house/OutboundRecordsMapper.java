package com.yunze.common.mapper.yunze.house;

import java.util.List;
import java.util.Map;

/**
 * 出库记录Mapper接口
 * 
 * @author yunze
 * @date 2025-01-11
 */
public interface OutboundRecordsMapper {

    /**
     * 查询出库记录
     * 
     * @param id 出库记录主键
     * @return 出库记录
     */
    public Map<String, Object> selectOutboundRecordsById(Long id);

    /**
     * 查询出库记录列表
     * 
     * @param paramMap 查询参数
     * @return 出库记录集合
     */
    public List<Map<String, Object>> selectOutboundRecordsList(Map<String, Object> paramMap);

    /**
     * 分页查询出库记录列表
     * 
     * @param map 查询参数
     * @return 出库记录列表
     */
    List<Map<String, Object>> selectOutboundRecordsListWithPage(Map<String, Object> map);

    /**
     * 统计出库记录总数
     * 
     * @param map 查询参数
     * @return 总数
     */
    Integer selectOutboundRecordsCount(Map<String, Object> map);

    /**
     * 新增出库记录
     * 
     * @param paramMap 出库记录参数
     * @return 结果
     */
    public int insertOutboundRecords(Map<String, Object> paramMap);

    /**
     * 修改出库记录
     * 
     * @param paramMap 出库记录参数
     * @return 结果
     */
    public int updateOutboundRecords(Map<String, Object> paramMap);

    /**
     * 删除出库记录
     * 
     * @param id 出库记录主键
     * @return 结果
     */
    public int deleteOutboundRecordsById(Long id);

    /**
     * 批量删除出库记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOutboundRecordsByIds(Long[] ids);
}
