package com.yunze.cn.mapper;


import java.util.List;
import java.util.Map;

/***
 * 资费组 数据层
 */
public interface YzCardPackageMapper {

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
     * 查询 资费组
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

}
