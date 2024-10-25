package com.yunze.common.mapper.yunze.cwgl.Firm;

import java.util.List;
import java.util.Map;

public interface YzCwglFirmAccountMapper {

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
     * 合同状态 = 删除
     * @param map
     * @return
     */
    public int delId(Map map);


    /**
     * 查询今日注册数量
     * @return
     */
    public Integer sel_data_count();


    /**
     * 查询 公司简要信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> findArr(Map map);

    /**
     * 删除
     * */
    public Integer deletefatid(Map map);

    /**
     * 查询 公司名称
     * */
    public List<Map<String,Object>> sleName();

}
