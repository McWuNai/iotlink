package com.yunze.system.service.impl.yunze.bulk;

import com.yunze.common.mapper.yunze.bulk.YzBulkBusinessMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.bulk.IYzBulkBusinessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@Service
public class YzBulkBusinessServiceImpl implements IYzBulkBusinessService {

    @Resource
    private YzBulkBusinessMapper yzBulkBusinessMapper;

    @Override
    public Map<String, Object> getList(Map map) {

        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzBulkBusinessMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzBulkBusinessMapper.selMap(map));
        return omp;
    }
}
