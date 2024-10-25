package com.yunze.system.service.impl.yunze;
import com.yunze.common.mapper.yunze.YzCcUrlMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.IYzCcUrlService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class YzCcUrlServiceImpl implements IYzCcUrlService {

    @Resource
    private YzCcUrlMapper yzCcUrlMapper;

    @Override
    public Map<String, Object> ListCcUrl (Map map) {
        //System.out.println("CardRouteServiceImpl > sel_Map = ");
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzCcUrlMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCcUrlMapper.ListCcUrl(map));
        return omp;
    }

    @Override
    public boolean insertCcUrl(Map<String, Object> map) {
        return yzCcUrlMapper.insertCcUrl(map)>0;
    }

    @Override
    public boolean updateCcUrl(Map<String, Object> map) {
        return yzCcUrlMapper.updateCcUrl(map)>0;
    }

    @Override
    public Map<String, Object> DeleteCcUrl(Map map) {

        Map<String, Object> Rmap = new HashMap<>();
        String Message = ""; boolean bool = false;

        Integer CountId = yzCcUrlMapper.CountId(map);
        if(CountId !=null && CountId > 1 ){
            Message = "可以删除";
            bool = yzCcUrlMapper.DeleteCcUrl(map)>0;
            if(bool){
                Message = "操作成功";
            }
        }else {
            Message = "不可以删除";
        }

        Rmap.put("bool",bool);
        Rmap.put("Msg",Message);

        return Rmap;
    }

}
























