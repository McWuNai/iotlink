package com.yunze.system.service.yunze;


import com.yunze.common.core.domain.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 资费组 业务层
 * @author root
 */
public interface IYzAgentPackageService
{

    /**
     * 查询当前套餐简要信息
     * @param map
     * @return
     */
    public Map<String,Object> selMap(Map<String, Object> map);


    /**
     * 新增资费组
     * @param map
     * @return
     */
    public boolean add(Map<String, Object> map);



    /**
     * 查询单条 资费组
     * @param map
     * @return
     */
    public Map<String,Object> find(Map<String, Object> map);


    /**
     *  资费组
     * @param map
     * @return
     */
    public List<Map<String,Object>> findPackage(Map<String, Object> map);



    /**
     * 修改 资费组信息
     * @param map
     * @return
     */
    public boolean update(Map<String, Object> map);



    /**导出*/
    public String exportallorders(Map<String, Object> map, SysUser User);


    /**导出*/
    public String exportPacket(Map<String, Object> map, SysUser User);
}
