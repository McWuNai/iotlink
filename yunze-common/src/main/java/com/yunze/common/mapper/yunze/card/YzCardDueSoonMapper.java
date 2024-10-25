package com.yunze.common.mapper.yunze.card;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 2022年8月8日15:31:08
 * 定义查询服务即将失效的企业
 */
public interface YzCardDueSoonMapper {
    /**
     * 2022年8月8日15:33:00
     * 查询即将失效的企业
     * map:
     * String:对应字段
     * obj:对应时间对象
     */
    List<Map> SelectCardDueSoon(Map<String, Object> map);
}
