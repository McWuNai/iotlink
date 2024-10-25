package com.yunze.system.service.impl.yunze.Firm;

import com.yunze.common.mapper.yunze.cwgl.Firm.YzCwglFirmAccountMapper;
import com.yunze.common.utils.yunze.ID_Generate;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.cwgl.Firm.IYzCwglFirmAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class YzCwglFirmAccountServiceImpl implements IYzCwglFirmAccountService {
    @Resource
    private YzCwglFirmAccountMapper yzCwglFirmAccountMapper;




    @Override
    public Map<String, Object> selMap(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzCwglFirmAccountMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCwglFirmAccountMapper.selMap(map));
        return  omp;
    }

    @Override
    public Map<String, Object> find(Map map) {
        return yzCwglFirmAccountMapper.find(map);
    }

    @Override
    public boolean upd(Map map) {
        map.get("Fat_ID");
        return yzCwglFirmAccountMapper.upd(map)>0;
    }


    @Override
    public boolean save(Map map) {
        Integer SAN_ID_CT = yzCwglFirmAccountMapper.sel_data_count();
        SAN_ID_CT=SAN_ID_CT!=null?SAN_ID_CT:0;//如果今日添加为0会反回null,给个默认值0 //FSAN 标识
        String SAN_ID= ID_Generate.G_id("CFAT", SAN_ID_CT, 4);//生成ID
        map.put("Fat_ID", SAN_ID);//赋值id

        return yzCwglFirmAccountMapper.save(map)>0;

    }

    @Override
    public boolean delId(Map map) {
        return  yzCwglFirmAccountMapper.delId(map)>0;
    }

    @Override
    public boolean deletefatid(Map map) {
        return yzCwglFirmAccountMapper.deletefatid(map) > 0;
    }

    @Override
    public List<Map<String, Object>> sleName() {
        return yzCwglFirmAccountMapper.sleName();
    }


    @Override
    public List<Map<String, Object>> findArr(Map map) {
        return yzCwglFirmAccountMapper.findArr(map);
    }



}
