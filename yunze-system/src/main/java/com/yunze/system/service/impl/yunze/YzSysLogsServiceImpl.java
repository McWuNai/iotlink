package com.yunze.system.service.impl.yunze;

import com.yunze.common.mapper.yunze.YzSysLogsMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzLogsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class YzSysLogsServiceImpl implements IYzLogsService {

    @Resource
    private YzSysLogsMapper yzSysLogsMapper;


    @Override
    public Map<String, Object> selMap(Map map) {
        //System.out.println("CardRouteServiceImpl > sel_Map = ");
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzSysLogsMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzSysLogsMapper.selMap(map));
        return omp;
    }

    @Override
    public Map<String, Object> getById(HashMap<String, Object> map) {
        return yzSysLogsMapper.getById(map);
    }
}














































