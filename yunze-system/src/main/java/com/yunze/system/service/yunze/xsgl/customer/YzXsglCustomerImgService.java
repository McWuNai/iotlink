package com.yunze.system.service.yunze.xsgl.customer;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface YzXsglCustomerImgService {

    /**
     * 客户图片
     * @param map
     * @return
     */
    public String guestFile (MultipartFile file[], Map<String, Object> map);


    /**
     * 加载图片信息
     * */
    public List<Map<String, Object>> urlID(Map<String, Object> map);

    /**
     * 查询客户图片列表
     * @param map
     * @return
     */
    public Map<String,Object> contactsList(Map map);

    /**
     * 删除单个图片
     * @param map
     * @return
     */
    public boolean singleUrl(Map map);

    /**
     * 修改 图片状态 无效
     * @param map
     * @return
     */
    public boolean picStatus(Map map);


    /**
     * 修改 图片状态 有效
     * @param map
     * @return
     */
    public boolean effectiveStatus(Map map);
}
