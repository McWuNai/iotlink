package com.yunze.system.service.impl.yunze.warehouse.Administration;
import com.yunze.common.mapper.yunze.fhgl.Ship.FhglShippingApplicationFormalDetailsMapper;
import com.yunze.common.mapper.yunze.fhgl.Ship.FhglShippingApplicationMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStockMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStorageDetailsMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStorageMapper;
import com.yunze.common.utils.yunze.ID_Generate;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityStoctService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YzCkglCommodityStorageStockServiceImpl implements YzCkglCommodityStoctService {

    @Resource
    private YzCkglCommodityStockMapper yzCkglCommodityStockMapper;
    @Resource
    private YzCkglCommodityStorageDetailsMapper yzCkglCommodityStorageDetailsMapper;
    @Resource
    private YzCkglCommodityStorageMapper yzCkglCommodityStorageMapper;
    @Resource
    private FhglShippingApplicationMapper fhglShippingApplicationMapper;
    @Resource
    private FhglShippingApplicationFormalDetailsMapper fhglShippingApplicationFormalDetailsMapper;

    @Override
    public Map<String, Object> selMap(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzCkglCommodityStockMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCkglCommodityStockMapper.selMap(map));
        return omp;
    }

    @Override
    public boolean upd(Map map) {
        return yzCkglCommodityStockMapper.upd_Stock_Quantity(map) > 0;
    }

    @Override
    @Transactional
    public boolean add_CSK(Map<String, Object> map) {
        boolean bool = false;
        //获取需要修改的数组
        List<Map<String, Object>> arr = (List<Map<String, Object>>) map.get("arr");
        List<Map<String, Object>> add_arr = new ArrayList<Map<String, Object>>();//新增list
        //遍历 list
        for (int i = 0; i < arr.size(); i++) {
            Map<String, Object> O_map = arr.get(i);

            //计算商品 库存 = 商品入库总数 - 商品出库总数
            Integer Imp_Count = yzCkglCommodityStorageDetailsMapper.sle_quantity(O_map);//查询 商品入库总数
            Imp_Count = Imp_Count != null ? Imp_Count : 0;
            Integer Out_Count = yzCkglCommodityStorageMapper.sle_warehouse(O_map);//查询 商品出库总数
            Out_Count = Out_Count != null ? Out_Count : 0;
            Integer Quantity = (Imp_Count - Out_Count);
            O_map.put("Quantity", Quantity);//当前数量 刷新赋值
            Integer sel_Ex = yzCkglCommodityStockMapper.sel_Ex(O_map);//查询 商品 是否存在

            //存在进行 修改
            if (sel_Ex > 0) {
                bool = yzCkglCommodityStockMapper.update_quantity(O_map) > 0;
            } else {
                add_arr.add(O_map);
            }
        }
        //新增
        if (add_arr.size() > 0) {

            Integer CSK_ID_CT = yzCkglCommodityStockMapper.sel_data_count();//查询今日注册供应商分类数量
            CSK_ID_CT = CSK_ID_CT != null ? CSK_ID_CT : 0;//如果今日添加为0会反回null,给个默认值0
            List arr_IDs = ID_Generate.G_id("CCSK", CSK_ID_CT, 4, add_arr.size());//生成ID

            for (int i = 0; i < arr_IDs.size(); i++) {//给供应商员工信息添加编号
                Map<String, Object> GSAT_Map = add_arr.get(i);
                GSAT_Map.put("CSK_CyID", arr_IDs.get(i));
            }
            map.put("add_arr", add_arr);
            bool = yzCkglCommodityStockMapper.add_CSK(map) > 0;
        }
        map.get("SAN_ID");
        fhglShippingApplicationMapper.normalUpdate(map); // 出库 变更状态 --》 2 已出库

        return bool;
    }


}












































