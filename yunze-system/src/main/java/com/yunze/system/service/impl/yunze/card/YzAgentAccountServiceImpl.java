package com.yunze.system.service.impl.yunze.card;

import com.yunze.common.mapper.yunze.card.YzAgentAccountMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.system.service.yunze.card.IYzAgentAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YzAgentAccountServiceImpl implements IYzAgentAccountService {

    @Resource
    private YzAgentAccountMapper yzAgentAccountMapper;


    @Override
    public Map<String, Object> getList(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzAgentAccountMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzAgentAccountMapper.getList(map));
        return omp;
    }

    @Override
    public Map<String, Object> save(Map map) {
        Map<String, Object> Rmap = new HashMap<String, Object>();
        String Message = "操作失败！";
        boolean bool = false;
        Integer exAppId =  yzAgentAccountMapper.is_exAppId(map);
         exAppId = exAppId != null && exAppId.toString().length()>0? exAppId : 0;
        if(exAppId==0){
            map.put("id", VeDate.getNo(4));
            bool = yzAgentAccountMapper.save(map)>0;
        }else {
            Message = "以存在 app_id 【"+map.get("app_id")+"】！";
        }
        if(bool){
            Message = "操作成功！";
        }
        Rmap.put("bool",bool);
        Rmap.put("Message",Message);
        return Rmap;
    }

    @Override
    public boolean upd(Map map) {
        boolean bool = yzAgentAccountMapper.upd(map)>0;
        return bool;
    }

    @Override
    public boolean del(Map map) {
        boolean   bool = yzAgentAccountMapper.del(map)>0;
        return bool;
    }

    @Override
    public List<String> is_exaAgentId(Map map) {
        return yzAgentAccountMapper.is_exaAgentId(map);
    }
}
