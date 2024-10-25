package com.yunze.system.service.yunze.xsgl.customer;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface YzXsglCustomerPeopleImgService {

    /**
     * 编辑联系人图片
     * @param map
     * @return
     */
    public String Prdedit(MultipartFile file[], Map<String, Object> map);


    /**
     * 获取图片组
     * @param map
     * @return
     */
    public List<Map<String, Object>> contactsList(Map<String, Object> map);


    /**
     * 查询联系人图片列表
     * @param map
     * @return
     */
    public Map<String,Object> liaisonList(Map map);

    /**
     * 删除联系人单个图片
     * @param map
     * @return
     */
    public boolean deleteUrl(Map map);


    /**
     * 修改 无效状态
     * @param map
     * @return
     */
    public boolean upInvalid(Map map);

    /**
     * 修改 有效状态
     * @param map
     * @return
     */
    public boolean upEffective(Map map);


}
