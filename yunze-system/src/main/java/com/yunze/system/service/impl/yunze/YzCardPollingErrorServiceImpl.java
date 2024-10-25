package com.yunze.system.service.impl.yunze;

import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzCardPollingErrorService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@Service
public class YzCardPollingErrorServiceImpl implements IYzCardPollingErrorService {

    @Resource
    private YzCardPollingErrorMapper yzCardPollingErrorMapper;

    @Override
    public Map<String, Object> listError(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzCardPollingErrorMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCardPollingErrorMapper.selMap(map));
        return omp;
    }

    @Override
    public boolean upd(Map map) {
        return yzCardPollingErrorMapper.updObj(map)>0;
    }

    @Override
    public boolean del(Map map) {
        return yzCardPollingErrorMapper.del(map)>0;
    }

    @Override
    public boolean delArr(Map map) {
        return yzCardPollingErrorMapper.delArr(map)>0;
    }

    @Override
    public boolean updArr(Map map) {

        return yzCardPollingErrorMapper.updArr(map)>0;
    }


}
