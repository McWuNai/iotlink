package com.yunze.system.service.yunze.cwgl.Firm;

import java.util.List;
import java.util.Map;

public interface IYzCwglFirmAccountService {


    /**
     * 查询
     * @param map
     * @return
     */
    public Map<String,Object> selMap(Map map);
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
    public boolean upd(Map map);



    /**
     * 新增
     * @param map
     * @return
     */
    public boolean save(Map map);


    /**
     * 真实作废
     * @param map
     * @return
     */
    public boolean delId(Map map);


    /**
     *
     * 获取简要公司信息
     * @param map
     * @return
     */
    public List<Map<String,Object>> findArr(Map map);


    /**
     * 真实删除
     *
     * @return*/
    public boolean deletefatid(Map map);


     /**
      * 查询公司名称
      * */
     public List<Map<String,Object>> sleName();

}
