package com.yunze.apiCommon.mapper;

import java.util.List;
import java.util.Map;
public interface YzCardRouteMapper {

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    List<Map<String, String>> sel_Map(Map map);

    /**
     * 查询 单卡所属通道信息
     *
     * @param map
     * @return
     */
    public Map<String, Object> find_route(Map<String, Object> map);


    /**
     * 修改记录
     *
     * @param map 表字段 map 对象
     */
    public int update(Map<String, Object> map);

    /**
     * 修改 通道状态
     *
     * @param map 表字段 map 对象
     */
    public int update_cd_status(Map<String, Object> map);


    /**
     * 查询总数
     *
     * @param map
     * @return
     * @throws Exception
     */
    public int sel_Map_Count(Map map);


    /**
     * 删除记录
     *
     * @param map 表字段 map 对象
     */
    public int delete(Map<String, Object> map);


    /**
     * 新增
     *
     * @param map 表字段 map 对象
     */
    public int add(Map<String, Object> map);


    /**
     * 查询 用户 创建 通道信息
     *
     * @param map 表字段 map 对象
     */
    public List<Map<String, Object>> find_route_list(Map<String, Object> map);


    /**
     * 查询 通道简要信息
     */
    public List<Map<String, Object>> find_simple(Map<String, Object> map);


    /**
     * 查询 通道简要信息
     */
    public List<Map<String, Object>> find_sp();

    /**
     * 查询 通道简要信息  状态为 正常 划分通道用
     */
    public List<Map<String, Object>> find_cr();

    /**
     * 查询 通道信息
     *
     * @return
     */
    public Map<String, Object> find(Map<String, Object> map);


    /**
     * 导出通道信息
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> outChannel(Map<String, Object> map);


    /**
     * 查询卡信息
     *
     * @param map
     * @return
     */
    public Map<String, Object> findCard(Map map);


    //查询卡 iccid
    public Map<String, Object> findIccid(Map map);


    //查询资费计划
    public List<Map<String, Object>> findFlow(Map map);


    //接口 获取资费计划
    public List<Map<String, Object>> findFlowArr(Map map);


    /**
     * 获取轮询通道信息
     *
     * @return
     */
    public List<Map<String, Object>> findRouteID(Map map);

    /**
     * 查询通道id
     *
     * @return
     */
    public List<String> findIDArr();

    /**
     * 查询 卡表里已经划分 的 通道信息
     *
     * @return
     */
    public List<String> findCardChannelID();


    /**
     * 查询 设置上停机阈值 的 通道信息
     *
     * @return
     */
    public List<String> findRemindRatioChannelID();

    /**
     * 查询 设置未订购停机 的 通道信息
     *
     * @return
     */
    public List<String> findIsDisconnectedChannelID();


    /**
     * 查询 通道信息
     *
     * @param map
     * @return
     */
    public Map<String, Object> getRoute(Map<String, Object> map);


    /**
     * 平台使用量
     * @param map
     * @return
     */
    public String find_Out_used(Map<String, Object> map);


    /**
     * 查询 企业是否可订购 资费计划
     * @param map
     * @return
     */
    public Integer findSubscribe(Map<String, Object> map);


    /**
     * 查询 代理 可用  资费计划
     * @param map
     * @return
     */
    public List<Map<String, Object>> findTariffplan(Map map);


    /**
     * 查询代理所属卡号
     * @param map
     * @return
     */
    public Integer IsExistence(Map<String, Object> map);


    /**
     * 查询 通道简要信息 运营类型 通道id arr 类型
     */
    public List<Map<String, Object>> find_simpleOperatorArr(Map<String, Object> map);


    /**
     * 通道 id 查询卡总数
     */
    public List<Map<String, Object>> countId(Map map);

    /*
     * 通道 id 修改 卡总数
     */
    public Integer upCount(Map map);

    /**
     * 查询自费组树结构,进行赋值
     * @param map
     */
    public List<Map<String,Object>> ruepkeFz(Map map);

    /**
     * 删除中间表通道ID
     */
    public boolean deleteID(Map map);


    /**
     * 选择运营商时加载出对应的通道
     */
    public List<Map<String,Object>> operatorName(Map map);

    /**
     * 通道名查找通道名称和密码
     * @param map
     * @return
     */
    public Map<String,Object> findUsername(Map map);


    /**
     * 修改 key
     * @param map
     * @return
     */
    public int updateKey(Map map);

    public int updImei(Map map);



    /**
     * 修改短信发送状态
     * @param map
     * @return
     */
    public int updbusiCardSms(Map map);

    /**
     * 新增短信收发记录
     * @param map
     * @return
     */
    public int addbusiCardSms(Map map);


    /**
     * 查询短信编号是否存
     * @param map
     * @return
     */
    public Integer isbusiCardSms(Map map);


}
