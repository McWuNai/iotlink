package com.yunze.system.service.impl.yunze.warehouse.Administration;

import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityOutputDetailsMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityOutputMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStockMapper;
import com.yunze.common.utils.yunze.ID_Generate;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityOutputService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class YzCkglCommodityOutputServiceImpl implements YzCkglCommodityOutputService {
    @Resource
    private YzCkglCommodityStockMapper yzCkglCommodityStockMapper;
    @Resource
    private YzCkglCommodityOutputMapper yzCkglCommodityOutputMapper;
    @Resource
    private YzCkglCommodityOutputDetailsMapper yzCkglCommodityOutputDetailsMapper;
    @Resource
    private YzCkglCommodityStorageStockServiceImpl yzCkglCommodityStorageStockService;

    @Override
    public Map<String, Object> SleOutKu(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzCkglCommodityOutputMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCkglCommodityOutputMapper.SleOutKu(map));
        return omp;
    }

    @Override
    public Map<String, Object> add_CODS(Map<String, Object> map) {

        Map<String, Object> COT_Map = (Map<String, Object>) map.get("COT_Map");
        Map<String, Object> Rmap = new HashMap<>();
        String Message = "";
        boolean bool = false, add_CODS = false,add_COT = false,bool_CSK = false;

        Integer CSE_ID_CT = yzCkglCommodityOutputMapper.sel_data_count();//查询今日注册供应商数量
        CSE_ID_CT = CSE_ID_CT != null ? CSE_ID_CT : 0;//如果今日添加为0会反回null,给个默认值0
        String COT_ID = ID_Generate.G_id("CCSE", CSE_ID_CT, 4);//生成ID

        COT_Map.put("COT_ID", COT_ID);//赋值id
        map.put("COT_ID", COT_ID);//赋值采购详情所属id
        map.put("CODS_CyID", COT_ID);
        map.put("CODS_Output_Quantity", COT_ID);
        add_COT = yzCkglCommodityOutputMapper.add_COT(COT_Map) > 0; //新增商品出库
        add_CODS = yzCkglCommodityOutputDetailsMapper.add_CODS(map) > 0;//添加申请
        bool_CSK = yzCkglCommodityStorageStockService.add_CSK(map);// 新增商品库存

        if (add_COT == true && add_CODS == true && bool_CSK == true) {
            bool = yzCkglCommodityStockMapper.update_quantity(COT_Map) > 0;

        }
        if (add_CODS) {
            Message = "操作成功！";
        }else if(add_COT){
            Message = "操作成功！";
        }
        Rmap.put("bool", bool);
        Rmap.put("Msg", Message);
        return Rmap;
    }

}
