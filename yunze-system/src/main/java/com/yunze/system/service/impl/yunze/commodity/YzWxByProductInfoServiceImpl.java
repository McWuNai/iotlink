package com.yunze.system.service.impl.yunze.commodity;

import com.yunze.common.mapper.yunze.commodity.YzWxByProductCategoryMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductInfoMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.commodity.IYzWxByProductInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YzWxByProductInfoServiceImpl implements IYzWxByProductInfoService {

    @Resource
    private YzWxByProductInfoMapper yzWxByProductInfoMapper;
    @Resource
    private YzWxByProductCategoryMapper yzWxByProductCategoryMapper;


    @Override
    public List<Map<String, Object>> findName(Map<String, Object> map) {
        return yzWxByProductInfoMapper.findName(map);
    }

    @Override
    public Map<String, Object> selMap(Map<String, Object> map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        PageUtil pu = null;
        List<Map<String,Object>> Rlist = null;
        Integer  rowCount = yzWxByProductInfoMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        pu = new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        Rlist = yzWxByProductInfoMapper.selMap(map);
        omp.put("Pu", pu);
        omp.put("Data",Rlist );
        return omp;
    }

    @Override
    public boolean save(Map<String, Object> map) {

       String MaxCore =  yzWxByProductInfoMapper.getMaxCore(map);
       //找到
       if(MaxCore!=null && MaxCore.length()>0){//33010100
            String Prefix = MaxCore.substring(0,3);
           String suffix = MaxCore.substring(3,MaxCore.length());
           int len = suffix.length();
           int suffix_int = Integer.parseInt(suffix.substring(0,3));
           suffix_int = suffix_int+1;
           String suffix_str = ""+suffix_int;
           //补 0
           if(suffix_str.length()<len){
              int fsize = len - suffix_str.length();
               for (int i = 0; i < fsize; i++) {
                   suffix_str = suffix_str+"0";
               }
           }
           map.put("product_core",Prefix+suffix_str);
       }else{//分类下还没有商品 获取分类编号
           Map<String, Object> findCodeMap = new HashMap<>();
           findCodeMap.put("category_id",map.get("one_category_id"));
           String Code = yzWxByProductCategoryMapper.findCode(findCodeMap);
           String Prefix = Code.split("0")[0];
           int len = Code.length();
           if(Prefix.length()<3){
               Prefix +="00";
               len = len-2;
           }
           Prefix +="101";
           for (int i = 0; i < len-3; i++) {
               Prefix = Prefix+"0";
           }
           map.put("product_core",Prefix);
       }
        return yzWxByProductInfoMapper.save(map)>0;
    }

    @Override
    public boolean edit(Map<String, Object> map) {
        return yzWxByProductInfoMapper.edit(map)>0;
    }

    @Override
    public Map<String, Object> findData(Map<String, Object> map) {
        return yzWxByProductInfoMapper.findData(map);
    }
}
