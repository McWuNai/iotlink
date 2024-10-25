package com.yunze.common.mapper.yunze.sysgl;

import java.util.Map;

public interface YzAgentAssetDetailsMapper {

    /**
     * 判断 代理下是否已生成 首页数据
     * @param map
     * @return
     */
    public Map<String,Object> findExist(Map<String, Object> map);



     /**
      * 新增 资产明细数据
      */
     public Integer addAssets(Map map);





}
