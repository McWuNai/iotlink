package com.yunze.system.service.yunze.commodity;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IYzWxByProductPicInfoService {



    /**
     * 新增
     * @param map
     * @return
     */
    public boolean save(Map<String, Object> map);


    /**
     * 修改
     * @param map
     * @return
     */
    public boolean edit(Map<String, Object> map);



    /**
     * 获取图片组
     * @param map
     * @return
     */
    public List<Map<String, Object>> findList(Map<String, Object> map);

    /**
     * 编辑
     * @param map
     * @return
     */
    public String Prdedit(MultipartFile file[],Map<String, Object> map);




}
