package com.yunze.common.mapper.yunze;


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

    /**
     * 查询卡总数 平台资费组
     */
    public List<Map<String,Object>> cardCount(Map map);

    /**
     * 修改 平台资费组 卡总数
     */
    public Integer upCountC(Map map);

    /*
    *  中间表 新增
    */
    public boolean ruepkeAdd(List<Map<String,String>> ruepkeArr);

    /*
    * 删除 中间表
    */
    public boolean deleteID(Map map);

    /*
    * 查询通道中间表 进行赋值、
    */
    public List<Map<String,Object>> ruepkeFz (Map map);

    /**
     * 通道选择后加载出对应的资费计划
     */
    public List<Map<String,Object>> packageName(Map map);

    /**
     * 选择资费组时加载出对应的通道
     */
    public List<Map<String,Object>> channelName(Map map);

    /**
     * 导出
     */
    public List<Map<String, Object>> exportallorders(Map map);


    /**
     * 修改 用量总和 、已用流量 、剩余流量 、已用百分比 卡总数  代理
     * @param map
     * */
    public Integer updAmountAgent(Map map);
}
