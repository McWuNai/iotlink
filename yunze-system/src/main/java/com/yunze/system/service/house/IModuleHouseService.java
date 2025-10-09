package com.yunze.system.service.house;

import com.yunze.common.core.domain.entity.ModuleHouse;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IModuleHouseService {

    Map<String,Object> list(Map<String,Object> map);


    void del(Map<String, Object> parammap);

    String importModule(MultipartFile file, HashMap<String, Object> map);
}
