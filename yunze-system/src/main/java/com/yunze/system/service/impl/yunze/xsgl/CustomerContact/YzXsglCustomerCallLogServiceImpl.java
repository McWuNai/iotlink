package com.yunze.system.service.impl.yunze.xsgl.CustomerContact;

import com.yunze.common.mapper.yunze.xsgl.CustomerContact.YzXsglCustomerCallLogMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.xsgl.CustomerContact.YzXsglCustomerCallLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@Service
public class YzXsglCustomerCallLogServiceImpl implements YzXsglCustomerCallLogService {

    @Resource
    private YzXsglCustomerCallLogMapper yzXsglCustomerCallLogMapper;

    @Override
    public boolean NewCustomerAdd(Map map) {
        return yzXsglCustomerCallLogMapper.NewCustomerAdd(map)>0;
    }

    @Override
    public Map<String, Object> getList(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzXsglCustomerCallLogMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzXsglCustomerCallLogMapper.getList(map));
        return  omp;
    }

    @Override
    public boolean deleteId(Map map) {
        return yzXsglCustomerCallLogMapper.deleteId(map)>0;
    }

    @Override
    public boolean updateId(Map map) {
        return yzXsglCustomerCallLogMapper.updateId(map)>0;
    }

    @Override
    public Map<String, Object> assignmentID(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        omp.put("CMap",yzXsglCustomerCallLogMapper.assignmentID(map));
        return omp;
    }
}


















