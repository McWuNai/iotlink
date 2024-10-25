package com.yunze.common.config;

import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.common.mapper.yunze.YzCardMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 字典类
 * @Auther: zhang feng
 * @Date: 2021/09/14/13:44
 * @Description:
 */
@Component
public class MyDictionary {
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private YzCardMapper yzCardMapper;


    /**
     * 获取字典数据
     *
     * @param dict_type
     * @param dict_value
     * @return
     */
    public String getdictLabel(String dict_type, String dict_value) {
        Map<String, Object> findDict = new HashMap<>();
        findDict.put("dict_type", dict_type);
        String dict_label = null;
        List<Map<String, Object>> dict_dataArr = getDictDataArr(dict_type);
        try {
                Map<String, Object> KMap = getKey(dict_dataArr, dict_value);
                dict_label = KMap.get("dict_label").toString();
        } catch (Exception e) {
            System.out.println("{getdictLabel 异常}" + e.getMessage());
        }
        return dict_label;
    }



    /**
     * 获取字典数据 key
     *
     * @param dict_type
     * @param dict_label
     * @return
     */
    public String getdictValue(String dict_type, String dict_label) {
        Map<String, Object> findDict = new HashMap<>();
        findDict.put("dict_type", dict_type);
        String dict_value = null;
        List<Map<String, Object>> dict_dataArr = getDictDataArr(dict_type);
        try {
            Map<String, Object> KMap = getVal(dict_dataArr, dict_label);
            dict_value = KMap.get("dict_value").toString();
        } catch (Exception e) {
            System.out.println("{getdictValue 异常}" + e.getMessage());
        }
        return dict_value;
    }





    /**
     * 获取字典 数组
     * @param dict_type
     * @return
     */
    public List<Map<String, Object>> getDictDataArr(String dict_type) {
        Map<String, Object> findDict = new HashMap<>();
        findDict.put("dict_type", dict_type);
        List<Map<String, Object>> dict_dataArr = null;
        try {
            Object obj = redisUtil.redisTemplate.opsForValue().get(dict_type);
            if (obj != null) {
                dict_dataArr = (List<Map<String, Object>>) obj;
                if (dict_dataArr != null && dict_dataArr.size() > 0) {
                } else {
                    dict_dataArr = yzCardMapper.findDict(findDict);
                }
            } else {
                dict_dataArr = yzCardMapper.findDict(findDict);
                if (dict_dataArr != null && dict_dataArr.size() > 0) {
                    redisUtil.redisTemplate.opsForValue().set(dict_type, dict_dataArr, 10 , TimeUnit.MINUTES);
                }
            }
        } catch (Exception e) {
            System.out.println("{getDictDataArr 异常}" + e.getMessage());
        }
        return dict_dataArr;
    }










    /**
     * 获取list map 对应数据
     * @param dict_dataArr
     * @param dict_value
     * @return
     */
    public Map<String, Object> getKey(List<Map<String, Object>> dict_dataArr, String dict_value) {
        if (dict_dataArr != null && dict_dataArr.size() > 0) {
            for (int i = 0; i < dict_dataArr.size(); i++) {
                Map<String, Object> obj = dict_dataArr.get(i);
                if (obj != null && obj.get("dict_value") != null) {
                    if (obj.get("dict_value").toString().equals(dict_value)) {
                        return obj;
                    }
                }
            }
        } else {
            return null;
        }
        return null;
    }



    /**
     * 获取list map 对应数据
     * @param dict_dataArr
     * @param dict_label
     * @return
     */
    public Map<String, Object> getVal(List<Map<String, Object>> dict_dataArr, String dict_label) {
        if (dict_dataArr != null && dict_dataArr.size() > 0) {
            for (int i = 0; i < dict_dataArr.size(); i++) {
                Map<String, Object> obj = dict_dataArr.get(i);
                if (obj != null && obj.get("dict_label") != null) {
                    if (obj.get("dict_label").toString().equals(dict_label)) {
                        return obj;
                    }
                }
            }
        } else {
            return null;
        }
        return null;
    }


}
