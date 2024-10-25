package com.yunze.common.mapper.yunze.xsgl.CustomerContact;

import java.util.List;
import java.util.Map;

public interface YzXsglCustomerCallLogMapper {

    /**
     * 新增 客户联系记录
     */
    public int NewCustomerAdd (Map map);

    /**
     *  查询 客户联系记录
     */
    public List<Map<String,Object>> getList (Map map);

    /**
     * 查询 客户联系记录 总数
     */
    public Integer selMapCount (Map map);

    /**
     * 删除 客户联系记录 单条
     */
    public int deleteId(Map map);

    /**
     * 修改 客户联系记录
     */
    public int updateId(Map map);

    /**
     * 赋值 企业名称 管理员
     */
//    List<Map<String,Object>> assignmentID (Map map);
        public Map <String,Object> assignmentID(Map map);
}














