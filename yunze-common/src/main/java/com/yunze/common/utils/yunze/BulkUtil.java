package com.yunze.common.utils.yunze;

import com.yunze.common.mapper.yunze.bulk.YzBulkBusinessDtailsMapper;
import com.yunze.common.mapper.yunze.bulk.YzBulkBusinessMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: zhang feng
 * @Date: 2022/05/18/16:58
 * @Description:
 */
@Component
public class BulkUtil {

    @Resource
    private YzBulkBusinessMapper yzBulkBusinessMapper;
    @Resource
    private YzBulkBusinessDtailsMapper yzBulkBusinessDtailsMapper;


    public  int Dadd( Map<String, Object> bulkDtailsMap){
      return  yzBulkBusinessDtailsMapper.add(bulkDtailsMap);
    }



    public  int DupdateArr(String b_id,Object bus_arrs,String end_time,String state_id,String mydescribe){
        Map<String, Object> bulkDtailsMap = new HashMap<>();
        bulkDtailsMap.put("b_id",b_id);
        if(bus_arrs!=null){
            bulkDtailsMap.put("bus_arrs",bus_arrs);
        }
        if(end_time!=null){
            bulkDtailsMap.put("end_time",end_time);
        }
        if(state_id!=null){
            bulkDtailsMap.put("state_id",state_id);
        }
        if(mydescribe!=null){
            bulkDtailsMap.put("mydescribe",mydescribe);
        }
        return  yzBulkBusinessDtailsMapper.updateArr(bulkDtailsMap);
    }



    public  int Dupdate(String b_id,String card_number,String end_time,String state_id,String mydescribe){
        Map<String, Object> bulkDtailsMap = new HashMap<>();
        bulkDtailsMap.put("b_id",b_id);
        if(card_number!=null){
            bulkDtailsMap.put("card_number",card_number);
        }
        if(end_time!=null){
            bulkDtailsMap.put("end_time",end_time);
        }
        if(state_id!=null){
            bulkDtailsMap.put("state_id",state_id);
        }
        if(mydescribe!=null){
            bulkDtailsMap.put("mydescribe",mydescribe);
        }
        return  yzBulkBusinessDtailsMapper.update(bulkDtailsMap);
    }



    public  int update( Map<String, Object> bulkDtailsMap){
        return  yzBulkBusinessMapper.update(bulkDtailsMap);
    }


    public void smsDupdate(Map<String, Object> updMap) {

    }

    public void smsUpdate(Map<String, Object> bulkMap) {

    }

    public void smsDadd(Map<String, Object> bulkDtailsMap) {
    }

    public void smsDupdateArr(String bId, List<Map<String, Object>> outListDifferent, String stringDate, String number, String s) {

    }
}
