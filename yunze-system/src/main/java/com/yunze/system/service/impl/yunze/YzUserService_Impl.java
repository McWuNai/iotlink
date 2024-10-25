package com.yunze.system.service.impl.yunze;


import com.yunze.common.annotation.DataScope;
import com.yunze.common.mapper.yunze.YzUserMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户 业务实现类
 */
@Service
public class YzUserService_Impl implements IYzUserService {


    @Resource
    private YzUserMapper yzUserMapper;



    @Override
    public Map<String, Object> selMap(Map map)  {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzUserMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzUserMapper.selMap(map));
        return omp;
    }

    @Override
    @DataScope(deptAlias = "d" , userAlias = "u" , isMap=true)
    public List<String> getUserID(Map<String, Object> map) {
        return yzUserMapper.getUserID(map);
    }

    @Override
    @DataScope(deptAlias = "d" , userAlias = "u" , isMap=true)
    public List<String> getDeptID(Map<String, Object> map) {
        return yzUserMapper.getDeptID(map);
    }


}
