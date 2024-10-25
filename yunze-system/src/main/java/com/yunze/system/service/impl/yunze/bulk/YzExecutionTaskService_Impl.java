package com.yunze.system.service.impl.yunze.bulk;


import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.bulk.IYzExecutionTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行日志 业务实现类
 */
@Service
public class YzExecutionTaskService_Impl implements IYzExecutionTaskService {


    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;



    @Override
    public Map<String, Object> sel_Map(Map map)  {
        //System.out.println("CardRouteServiceImpl > sel_Map = ");
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzExecutionTaskMapper.sel_Map_Count(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzExecutionTaskMapper.sel_Map(map));
        return omp;
    }


}
