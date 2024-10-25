package com.yunze.system.service.impl.yunze;

import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzInfoChangeMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzInfoChangeService;
import com.yunze.system.service.yunze.IYzUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class YzInfoChangeServiceImpl implements IYzInfoChangeService {

    @Resource
    private YzInfoChangeMapper yzInfoChangeMapper;
    @Resource
    private YzCardMapper yzCardMapper;



    @Override
    public Map<String, Object> selMap(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        //权限过滤
        if(map.get("agent_id")!=null){
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if(!Different.Is_existence(agent_id,100)){
                List<String>  IccidArr =  yzCardMapper.findBelongingCard(map);
                map.put("IccidArr",IccidArr);
            }
        }
        Integer rowCount = yzInfoChangeMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzInfoChangeMapper.ListInfo(map));
        return omp;
    }

    @Override
    public List<Map<String,Object>> selTime(HashMap<String, Object> map) {
        return yzInfoChangeMapper.selTime(map);
    }
}
