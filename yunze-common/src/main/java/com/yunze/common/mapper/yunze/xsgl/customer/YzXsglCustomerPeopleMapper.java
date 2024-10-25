package com.yunze.common.mapper.yunze.xsgl.customer;

import java.util.List;
import java.util.Map;

public interface YzXsglCustomerPeopleMapper {

    /**
     * 新增
     * @param map
     * @return
     */
    public int save(Map map);

    /**
     * 删除所属
     * @param map
     * @return
     */
    public int delId(Map map);


    /**
     *查询所属
     * @param map
     * @return
     */
    public List<Map <String,Object>> findArr(Map map);


    /**
     * 修改
     * @param map
     * @return
     */
    public int upd(Map map);


    /**
     * 查询客户下 简要联系人信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> fnidPeopleArr(Map map);


}
