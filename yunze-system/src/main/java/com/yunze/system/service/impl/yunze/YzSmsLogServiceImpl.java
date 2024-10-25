package com.yunze.system.service.impl.yunze;


import com.yunze.common.annotation.DataScope;
import com.yunze.common.mapper.yunze.YzSmsLogMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzSmsLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 短信日志 业务实现类
 *2022年9月2日14:52:45
 * @author root
 */
@Service
@Slf4j
public class YzSmsLogServiceImpl implements IYzSmsLogService {

    @Resource
    public YzSmsLogMapper yzSmsLogMapper;

    /**
     * 查询短信日志的列表,2022年9月2日14:52:42
     * @param map
     * @return
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "a", isMap = true)
    public Map<String, Object> getList(Map<String, Object> map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzSmsLogMapper.MapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzSmsLogMapper.selMap(map));
        return  omp;
    }
}
