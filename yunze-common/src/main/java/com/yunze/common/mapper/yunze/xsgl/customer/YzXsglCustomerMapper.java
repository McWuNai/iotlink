package com.yunze.common.mapper.yunze.xsgl.customer;

import java.util.List;
import java.util.Map;

public interface YzXsglCustomerMapper {

    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map map);

    /**
     * 查询 总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);


    /**
     *查询
     * @param map
     * @return
     */
    public List<Map <String,Object>> selMap(Map map);


    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public Map <String,Object> find(Map map);


    /**
     * 基础修改
     * @param map
     * @return
     */
    public int upd(Map map);

    /**
     * 修改 客户状态  是否回收 优惠折扣 客户所属人
     * @param map
     * @return
     */
    public int updInfo(Map map);


    /**
     * 查询 客户是否已存在
     * @param map
     * @return
     */
    public Integer IsExist(Map map);


    /**
     * 查询 总管理 或 非总管理 下  客户
     * @param map
     * @return
     */
    public List<Map <String,Object>> IsAdmin(Map map);


    /**
     *客户划分
     * @param map
     * @return
     */
    public int updDivide(Map map);


    /**
     * 查询 客户信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> selInfo(Map map);


    /**
     * 查询客户信息 id 名称
     * @param map
     * @return
     */
    public List<Map <String,Object>> findCustomerArr(Map map);


    /**
     * 查询企业名称是否已存在
     * @param map
     * @return
     */
    public List<String> Is_exist(Map<String,Object> map);

    /**
     * sys_dept 查询系统企业是否存在
     * @param map
     * @return
     */
    public List<Map <String,Object>> Is_existSysDept(Map<String,Object> map);



    /**
     *批量新增系统企业
     * @param
     * @return
     */
    public int saveSysDept(List<Map <String,String>> saveSysDeptlist);


    /**
     * 新增默认生成 企业账户密码
     */
    public int userAdd(List<Map<String,String>> userList);

    /*
    * 新增 用户和角色
    */
    public int userDept(List<Map<String,String>> rooleList);


    /**
     *获取部门最大排序
     * @return
     */
    public Integer getMaxOrderNum();

    /**
     * 获取 dept_name
     */
    public Map<String,Object> getDeptName(Map map);

    /**
     * 客户信息 新增默认生成 企业账户密码
     */
    public int userCustomer(Map map);

    /**
     * 获取 dept_id
     */
    public Map<String,Object> deptID(Map map);
}
