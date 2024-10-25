package com.yunze.cn.mapper;


import java.util.List;
import java.util.Map;

/**
 * 微信 公众号用户
 */
public interface YzWeChatUserMapper {

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
     * 新增
     * @param map
     * @return
     * @throws Exception
     */
    public int save(Map map);

    /**
     * 修改
     * @param map
     * @return
     * @throws Exception
     */
    public int upd(Map map);


    /**
     * 查询单条
      * @param map
     * @return
     */
    public Map<String, Object> find(Map map);


    /**
     * 查询绑定状态
     * @param map
     * @return
     */
    public Map<String, Object> findBindtatus(Map map);



}
