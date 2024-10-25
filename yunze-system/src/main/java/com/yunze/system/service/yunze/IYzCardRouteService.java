package com.yunze.system.service.yunze;


import com.yunze.common.core.domain.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IYzCardRouteService {



    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String,Object> sel_Map(Map map);


    /**
     * 删除记录
     *
     * @param map 表字段 map 对象
     */
    public boolean delete(Map<String, Object> map);

    /**
     * 查询 单卡所属通道信息
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String, Object> find_route(Map map);



    /**
     * 修改记录
     *
     * @param map 表字段 map 对象
     */
    public boolean update(Map<String, Object> map);


    /**
     * 修改 通道状态
     *
     * @param map 表字段 map 对象
     */
    public boolean update_cd_status(Map<String, Object> map);



    /**
     * 新增
     *
     * @param map 表字段 map 对象
     */
    public boolean  add(Map<String, Object> map);




    /**
     * 更改 用户所属 id 下的 卡号 控制类型
     * @param map
     * @return
     */
    public Map<String, Object> updateControlType(Map<String, Object> map);



    /**
     * 查询 用户 创建 通道信息
     *
     * @param map 表字段 map 对象
     * @return
     */
    public List<Map<String, Object>> find_route_list(Map<String, Object> map);


    /**
     * 查询 简要信息
     * @return
     */
    public List<Map<String, Object>> find_sp();

    /**
     * 查询 通道简要信息  状态为 正常 划分通道用
     */
    public List<Map<String, Object>>  find_cr();

    /**
     * 查询 通道信息
     * @return
     */
    public Map<String, Object>  find(Map<String, Object> map);


    /**
     * 查询 卡号 分配 通道 状态
     * @param map
     * @return
     */
    public String exportData(Map<String, Object> map, SysUser User);


    /**
     * 查询自费组树结构,进行赋值
     * @param map
     * @return
     */
    public List<Map<String,Object>> RouteList(Map map);


    /**
     * 选择运营商时加载出对应的通道
     */
    public List<Map<String,Object>> operatorName(Map map);




}















