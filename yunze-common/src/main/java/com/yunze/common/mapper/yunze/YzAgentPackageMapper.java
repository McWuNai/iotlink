package com.yunze.common.mapper.yunze;


import java.util.List;
import java.util.Map;

/***
 * 资费组 数据层
 */
public interface YzAgentPackageMapper {

    /**
     *查询
     * @param map
     * @return
     */
    public List<Map<String,Object>> selMap(Map<String, Object> map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public List<String> selMapCount(Map<String, Object> map);



    /**
     * 查询 package_id 是否存在
     * @param map
     * @return
     */
    public int isExist(Map<String, Object> map);


    /**
     * 新增资费组
     * @param map
     * @return
     */
    public int add(Map<String, Object> map);


    /**
     * 查询单条 资费组
     * @param map
     * @return
     */
    public List<Map<String,Object>> find(Map<String, Object> map);


    /**
     * 修改 资费组信息
     * @param map
     * @return
     */
    public int update(Map<String, Object> map);



    /**
     * 划分更新
     * @param map
     * @return
     */
    public int updatePackage(Map<String, Object> map);


    /**
     * 从 代理资费组 复制资费组到 代理
     * @param map
     * @return
     */
    public int addAgent(Map<String, Object> map);


    /**
     * 查询卡总数 代理资费组
     */
    public List<Map<String,Object>> agentCount (Map map);

    /**
     * 修改 代理资费组 卡总数
     */
    public Integer upCountA(Map map);

    /**
     * 导出
     */
    public List<Map<String, Object>> exportallorders(Map map);
}
