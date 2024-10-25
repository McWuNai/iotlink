package com.yunze.system.service.yunze.warehouse.Administration;


import com.yunze.common.core.domain.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface YzCkglCommodityStorageService {
    /**新增入库信息表*/
    public Map<String, Object> warehousingadd(Map map);

    /**查询商品信息列表*/
    public Map<String,Object> slestorage(Map map);

    /**
     *  查询详情查看
     */
    public List<Map<String,Object>> detailedId (Map map);

    /**导出*/
    public String ImportStock(MultipartFile file, Map<String,String> map, SysUser User, HashMap<String, Object> parammap) throws IOException;

}
