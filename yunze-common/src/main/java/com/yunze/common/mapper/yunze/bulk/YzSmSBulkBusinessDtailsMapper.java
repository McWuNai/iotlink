package com.yunze.common.mapper.yunze.bulk;

import java.util.List;
import java.util.Map;

public interface YzSmSBulkBusinessDtailsMapper {


    public int add(Map map);

    public int update(Map map);

    public int updateArr(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map<String,Object>> selMap(Map map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map<String, Object> map);


    /**
     *查询 状态为 成功
     * @param map
     * @return
     */
    public List<Map<String,Object>> successArr(Map map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer publicCount(Map<String, Object> map);

    /**
     * 导出
     */
    public List<Map<String, Object>> exportallorders(Map map);


    /**
     * 查询需要短信发送的卡号执行短信发送
     * @param map
     * @return
     */
    public List<Map<String, Object>> findSMSbulkArr(Map map);


}
