package com.yunze.system.service.yunze;


import com.yunze.common.core.domain.entity.SysUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡板信息 业务层
 * 
 * @author root
 */
public interface IYzCardFlowService
{
    /**
     *查询套餐数据
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryflow(Map<String, Object> map);



    /**
     * 查询当前套餐简要信息
     * @param map
     * @return
     */
    public Map<String,Object> queryflow_simple(Map<String, Object> map);

    /**
     * 获取资费组简要信息
      * @param map
     * @return
     */
    public List<Map<String,Object>> queryPackage_simple(Map<String, Object> map);


    /**
     * 查询 资费计划 简要信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> queryPacket_simple(Map<String, Object> map);




    /**
     * 查询 资费计划 简要信息 [单条]
     * @param map
     * @return
     */
    public Map<String,Object> FindPacket(Map<String, Object> map);


    /**
     * 查询 订购资费查询 */
    public Map<String,Object> getList(Map map);

    /**查询 到期时间*/
    public List<Map<String,Object>> sleTime(Map map);


    /**订购资费导出*/
     public String exportflow(Map<String, Object> map, SysUser User);



}
