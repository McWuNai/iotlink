package com.yunze.common.mapper.yunze.xsgl.customer;

import java.util.List;
import java.util.Map;

public interface YzXsglCustomerImgMapper {

    /**
     * 联系人图片批量新增
     * @param map
     * @return
     */
    public int guestFile(Map<String, Object> map);


    /**
     * 加载图片信息
     * @param map
     * @return
     */
    public List<Map<String, Object>> urlID(Map<String, Object> map);

    /**
     * 查询客户图片列表
     * @param map
     * @return
      */
    public List<Map<String,Object>> contactsList(Map map);

    /**
     * 查询客户图片总数
     * @param map
     * @return
      */
    public Integer selMapCount(Map map);

    /**
     * 删除单个图片
     * @param map
     * @return
     */
    public int singleUrl(Map map);

    /**
     * 修改 图片状态 无效
     * @param map
     * @return
     */
    public int picStatus(Map map);

    /**
     * 修改 图片状态 有效
     * @param map
     * @return
     */
    public int effectiveStatus(Map map);

}
