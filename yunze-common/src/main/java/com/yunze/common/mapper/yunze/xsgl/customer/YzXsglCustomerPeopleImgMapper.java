package com.yunze.common.mapper.yunze.xsgl.customer;

import java.util.List;
import java.util.Map;

public interface YzXsglCustomerPeopleImgMapper {



    /**
     * 联系人图片批量新增
     * @param map
     * @return
     */
    public int saveArr(Map<String, Object> map);

    /**
     * 获取联系人图片组
     * @param map
     * @return
     */
    public List<Map<String, Object>> contactsList(Map<String, Object> map);


    /**
     * 查询联系人图片列表
     * @param map
     * @return
     */
    public List<Map<String,Object>> liaisonList(Map map);

    /**
     * 查询联系人总数
     * @param map
     * @return
     */
    public Integer selMapCount(Map map);

    /**
     * 删除联系人单个图片
     * @param map
     * @return
     */
    public int deleteUrl(Map map);

    /**
     * 修改 无效状态
     * @param map
     * @return
     */
    public int upInvalid(Map map);

    /**
     * 修改 有效状态
     * @param map
     * @return
     */
    public int upEffective(Map map);


}
