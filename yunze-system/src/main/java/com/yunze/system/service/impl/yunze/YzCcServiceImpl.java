package com.yunze.system.service.impl.yunze;

import com.yunze.common.mapper.yunze.YzCcMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzCcService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class YzCcServiceImpl implements IYzCcService {

    @Resource
    private YzCcMapper yzCcMapper;

    @Override
    public Map<String, Object> ListCc(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzCcMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCcMapper.ListCc(map));
        return omp;
    }

    @Override
    public boolean YzCcAdd(Map<String, Object> map) {
        return yzCcMapper.CcAdd(map)>0;
    }

    @Override
    public List<Map<String, Object>> NameAdd() {
        return yzCcMapper.NameAdd();
    }

    @Override
    public boolean UpdateCc(Map<String, Object> map) {
        return yzCcMapper.UpdateCc(map)>0;
    }

}
