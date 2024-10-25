package com.yunze.common.mapper.yunze.card;

import java.util.List;
import java.util.Map;

public interface YzApplicationforRenewalPrimaryMapper {

    /**
     * 查询总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> selMap(Map map);


    public Integer save(Map map);

    public Integer upd(Map map);


    public Map <String,Object> find(Map map);

    /**
     * 获取需要通知的 状态为 审核中 的续费申请
     * @param map
     * @return
     */
    public List<Map <String,Object>> findEmailCC(Map map);


    /**
     * 获取 订单生成申请 状态为 已审核 的-
     * @param map
     * @return
     */
    public List<Map <String,Object>> findAppOrderGr(Map map);


}
