package com.yunze.system.service.impl.yunze;

import com.yunze.common.mapper.yunze.card.YzCardApiOfferinginfoMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzCardApiOfferinginfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class YzCardApiOfferinginfoServiceImpl implements IYzCardApiOfferinginfoService {

    @Resource
    private YzCardApiOfferinginfoMapper yzCardApiOfferinginfoMapper;

    @Override
    public Map<String, Object> getList(Map map) {
        //System.out.println("CardRouteServiceImpl > sel_Map = ");
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzCardApiOfferinginfoMapper.MapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCardApiOfferinginfoMapper.getList(map));
        return omp;
    }

    @Override
    public boolean deleteInfo(Map map) {
        return yzCardApiOfferinginfoMapper.deleteInfo(map)>0;
    }

    @Override
    public boolean updateInfo(Map map) {
        return yzCardApiOfferinginfoMapper.updateInfo(map)>0;
    }
}
