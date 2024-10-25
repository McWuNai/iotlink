package com.yunze.common.mapper.yunze.polling;

import org.springframework.amqp.rabbit.connection.PublisherCallbackChannelImpl;

import java.util.List;
import java.util.Map;

public interface YzCardPollingErrorMapper {



    /**
     * 修改
     * @param map
     * @return
     */
    public int updObj(Map map);



    public int upd(Map map);

    public int save(Map map);



    /**
     * 删除
     * @param map
     * @return
     */
    public int del(Map map);

    /**
     *查询是否存在
     * @param map
     * @return
     */
    public String findEx(Map map);


    /**
     * 查询
     * @param map
     * @return
     */
    public List<Map<String,Object>> findGroup (Map map);

    public List<Map<String,Object>> selMap (Map map);


    public Integer selMapCount(Map map);

    /**
     * 三表联查
     * @param map
     * @return
     */
    public List<Map<String,Object>> selMapRelevance (Map map);




    /**
    * 批量删除
    * */
    public int delArr(Map<String, Object> map);

    /**
     * 批量修改
     * */
    public int updArr(Map<String,Object> map);


    /**
     * 删除指定日期之前的数据
     * @param map
     * @return
     */
    public int delTime(Map map);

}
