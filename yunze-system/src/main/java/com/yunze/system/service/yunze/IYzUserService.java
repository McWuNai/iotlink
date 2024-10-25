package com.yunze.system.service.yunze;


import java.util.List;
import java.util.Map;

/**
 * 用户 业务层
 */
public interface IYzUserService {



    /**
     * 查询
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String,Object> selMap(Map map);


    /* 获取用户 权限过滤
     * @param map
     * @return
     */
    public List<String> getUserID(Map<String, Object> map);


    /* 获取部门id
     * @param map
     * @return
     */
    public List<String> getDeptID(Map<String, Object> map);





}
