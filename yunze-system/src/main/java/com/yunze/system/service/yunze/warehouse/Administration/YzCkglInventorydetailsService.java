package com.yunze.system.service.yunze.warehouse.Administration;

import com.yunze.common.core.domain.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public interface YzCkglInventorydetailsService {

    public String ImportStock(MultipartFile file, Map<String,String> map, SysUser User,HashMap<String, Object> parammap) throws IOException;

    public Map<String,Object> inventorydetails(Map map);


    /**导出*/
    public String ExportDetails(Map<String, Object> map,SysUser User);

    /**
     * 导入至卡信息
     */
    public String ImportCard (Map map , SysUser User);

    /**划分卡信息*/
    String detailsDividCard(Map<String,Object> map);



}
