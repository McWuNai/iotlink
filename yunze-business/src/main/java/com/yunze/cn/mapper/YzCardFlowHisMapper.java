package com.yunze.cn.mapper;





import java.util.List;
import java.util.Map;


/**
 * 资费用量历史记录表 数据层
 * @author root
 */
public interface YzCardFlowHisMapper
{


    /**
     * 获取资费组简要信息
     * @param map
     * @return
     */
    public String total_flow(Map<String, Object> map);


    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map<String, Object> map);

    /**
     * 编辑
     * @param map
     * @return
     */
    public int edit(Map<String, Object> map);

    /**
     * 查询 是否存在
     * @param map
     * @return
     */
    public Integer isExist(Map<String, Object> map);


}
