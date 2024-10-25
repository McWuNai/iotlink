package com.yunze.cn.mapper;


import java.util.List;
import java.util.Map;

/**
 * 系统用户 企业用户用
 */
public interface YzUserMapper {

    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> selMap(Map map);


    /**
     * 查询总数
     * @param map
     * @return
     * @throws Exception
     */
    public int selMapCount(Map map);


    /**
     * 获取用户 权限过滤
     * @param map
     * @return
     */
    public List<String> getUserID(Map<String, Object> map);


    /**
     * 获取部门id
     * @param map 权限过滤
     * @return
     */
    public List<String> getDeptID(Map<String, Object> map);


}
