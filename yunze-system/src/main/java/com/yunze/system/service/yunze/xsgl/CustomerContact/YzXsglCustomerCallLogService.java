package com.yunze.system.service.yunze.xsgl.CustomerContact;

import java.util.Map;

public interface YzXsglCustomerCallLogService {


    /**
     * 新增 客户联系记录
     * @param map
     * @return
     */
    public boolean NewCustomerAdd(Map map);


    /**
     * 查询 客户联系记录
     * @param map
     * @return
     */
    public Map<String,Object> getList(Map map);


    /**
     * 删除 客户联系记录
     */
    public boolean deleteId(Map map);

    /**
     * 修改 客户联系记录
     */
    public boolean updateId(Map map);

    /**
     * 赋值 企业名称 管理员
     * @param map
     * @return
     */
    public Map <String,Object> assignmentID(Map map);

}
