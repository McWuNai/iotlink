package com.yunze.system.service.impl.house;

import com.yunze.common.mapper.yunze.house.OutboundRecordsMapper;
import com.yunze.system.service.house.IOutboundRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出库记录Service业务层处理
 * 
 * @author yunze
 * @date 2025-01-11
 */
@Service
public class OutboundRecordsServiceImpl implements IOutboundRecordsService {

    @Resource
    private OutboundRecordsMapper outboundRecordsMapper;

    /**
     * 查询出库记录
     * 
     * @param id 出库记录主键
     * @return 出库记录
     */
    @Override
    public Map<String, Object> selectOutboundRecordsById(Long id) {
        return outboundRecordsMapper.selectOutboundRecordsById(id);
    }

    /**
     * 查询出库记录列表
     * 
     * @param paramMap 查询参数
     * @return 出库记录
     */
    @Override
    public List<Map<String, Object>> selectOutboundRecordsList(Map<String, Object> paramMap) {
        return outboundRecordsMapper.selectOutboundRecordsList(paramMap);
    }

    @Override
    public Map<String, Object> selectOutboundRecordsListWithPage(Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();

        // 获取分页参数
        Integer currentPage = paramMap.get("pageNum") != null ? Integer.parseInt(paramMap.get("pageNum").toString())
                : 1;
        Integer pageSize = paramMap.get("pageSize") != null ? Integer.parseInt(paramMap.get("pageSize").toString())
                : 10;

        // 查询总数
        Integer total = outboundRecordsMapper.selectOutboundRecordsCount(paramMap);
        total = total != null ? total : 0;

        // 使用PageUtil处理分页
        com.yunze.common.utils.yunze.PageUtil pu = new com.yunze.common.utils.yunze.PageUtil(total, currentPage,
                pageSize);
        paramMap.put("StarRow", pu.getStarRow());
        paramMap.put("PageSize", pu.getPageSize());

        // 查询数据
        List<Map<String, Object>> list = outboundRecordsMapper.selectOutboundRecordsListWithPage(paramMap);

        // 返回结果
        result.put("Pu", pu);
        result.put("Data", list);
        result.put("total", total);

        return result;
    }

    /**
     * 新增出库记录
     * 
     * @param paramMap 出库记录参数
     * @return 结果
     */
    @Override
    public int insertOutboundRecords(Map<String, Object> paramMap) {
        return outboundRecordsMapper.insertOutboundRecords(paramMap);
    }

    /**
     * 修改出库记录
     * 
     * @param paramMap 出库记录参数
     * @return 结果
     */
    @Override
    public int updateOutboundRecords(Map<String, Object> paramMap) {
        return outboundRecordsMapper.updateOutboundRecords(paramMap);
    }

    /**
     * 批量删除出库记录
     * 
     * @param ids 需要删除的出库记录主键
     * @return 结果
     */
    @Override
    public int deleteOutboundRecordsByIds(Long[] ids) {
        return outboundRecordsMapper.deleteOutboundRecordsByIds(ids);
    }

    /**
     * 删除出库记录信息
     * 
     * @param id 出库记录主键
     * @return 结果
     */
    @Override
    public int deleteOutboundRecordsById(Long id) {
        return outboundRecordsMapper.deleteOutboundRecordsById(id);
    }
}
