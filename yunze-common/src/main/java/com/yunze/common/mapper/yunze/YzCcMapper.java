package com.yunze.common.mapper.yunze;

import java.util.List;
import java.util.Map;

public interface YzCcMapper {


    /**
     * 查询所有
     * */
    public List<Map<String,Object>> ListCc(Map map);

    /**
     * 查询总数
     * @param map
     * @return
     */
    public int selMapCount(Map map);

    public int CcAdd(Map<String, Object> map);

    /**
     * 查询 对应自动化规则表  名字 自动化管理抄送名字
     */
    public List<Map<String, Object>>  NameAdd();
    /**
     * 修改
     * */
    public int UpdateCc(Map<String, Object> map);

}
