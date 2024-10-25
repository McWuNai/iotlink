package com.yunze.system.service.impl.yunze.warehouse.Administration;

import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityMapper;
import com.yunze.common.utils.yunze.ID_Generate;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class YzCkglCommodityServiceImpl implements YzCkglCommodityService {

    @Resource
    private YzCkglCommodityMapper yzCkglCommodityMapper;

    @Override
    public List<Map<String, Object>> commodity(Map map) {
        return yzCkglCommodityMapper.commodity(map);
    }

    @Override
    public List<Map<String, Object>> queryloading(Map map) {
        return yzCkglCommodityMapper.queryloading(map);
    }

    @Override
    public Map<String, Object> selMap(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzCkglCommodityMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCkglCommodityMapper.selMap(map));
        return  omp;
    }

    @Override
    public Map<String, Object> save(Map map) {
        Map<String, Object> Rmap=new HashMap<String, Object>();
        boolean bool = false;
        String Message = "";
        Integer isExist = yzCkglCommodityMapper.isExist(map);
        if(isExist!=null && isExist>0){
            Message = "【"+map.get("Cy_Name")+"】名称商品以存在！无需重复添加！！";
        }else{
            //独立到MQ 队列
            Integer Cy_ID_CT = yzCkglCommodityMapper.sel_data_count();//查询今日注册供应商数量
            Cy_ID_CT=Cy_ID_CT!=null?Cy_ID_CT:0;//如果今日添加为0会反回null,给个默认值0
            String Cy_ID = ID_Generate.G_id("CCY", Cy_ID_CT, 4);//生成ID
            map.put("Cy_ID", Cy_ID);//赋值id
            bool = yzCkglCommodityMapper.save(map)>0;
        }
        Rmap.put("Msg",Message);
        Rmap.put("bool",bool);
        return Rmap;
    }

    @Override
    public boolean upd(Map map) {

        return yzCkglCommodityMapper.upd(map)>0;
    }

    @Override
    public Map<String, Object> find(Map map) {
        return  yzCkglCommodityMapper.find(map);
    }






}


























