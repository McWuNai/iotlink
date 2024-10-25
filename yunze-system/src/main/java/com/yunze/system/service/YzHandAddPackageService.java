package com.yunze.system.service;

import com.yunze.common.core.domain.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface YzHandAddPackageService {
    AjaxResult handAddPackage(Map<String, Object> parammap);

    AjaxResult addPackage(MultipartFile file);
}
