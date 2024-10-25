package com.yunze.common.mapper.yunze;

import java.util.List;
import java.util.Map;

public interface YzCardBriefMapper {

    /**
     * 批量新增‘
     * @param map
     */
    public Integer batchCard(Map<String,Object> map);

    /**
     * 批量更新卡信息
     * @param map
     * @return
     */
    public Integer batchUpd(Map<String, Object> map);

    /**
     * 查询 yz_card_brief 没有主表 iccid 的
     * @return
     * */
    public List<String> selList(Map map);

    /**
     * 查询 yz_card_brief 没有主表 iccid 的
     * @return
     * */
    public Integer selMapCount();
}
