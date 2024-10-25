package com.yunze.system.service.impl.yunze.card;


import com.yunze.common.core.domain.entity.SysAutoPolling;
import com.yunze.common.exception.CustomException;

import com.yunze.common.mapper.yunze.card.AutoPollingMapper;
import com.yunze.common.utils.StringUtils;
import com.yunze.system.service.yunze.card.IAutoPollingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AutoPollingServiceImpl implements IAutoPollingService {

    @Resource
    private AutoPollingMapper autoPollingMapper;


    @Override
    public Map<String, Object> list(Map<String, Object> map) {
        Map<String, Object> rMap = new HashMap<String, Object>();
        Integer total = autoPollingMapper.selMapCount(map);
        rMap.put("total",total);
        int pageNum = (int) map.get("pageNum");
        map.put("pageNum",pageNum-1);
        List<Map<String, Object>> list = autoPollingMapper.list(map);
        rMap.put("data",list);
//        test();
        return rMap;
    }

    @Override
    public void del(Map<String, Object> map) {
        autoPollingMapper.del(map);
    }

    @Override
    public String importAuto(List<SysAutoPolling> list) {
        if (StringUtils.isNull(list) || list.size() == 0) {
            throw new CustomException("导入轮询数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysAutoPolling autoPolling : list) {
            try {
                // 验证是否存在这个卡号
                Map<String,Object> autoPollingMap = autoPollingMapper.selectPollingByIccid(autoPolling);
                if (StringUtils.isNull(autoPollingMap)) {//轮询中不存在这个卡号
                    Map<String,Object> cardMap = autoPollingMapper.selectCardByIccid(autoPolling);
//                    channel_id
                    if (cardMap!=null) {
                        cardMap.put("iccid", autoPolling.getIccid());
                        autoPollingMapper.ins(cardMap);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、卡号 " + autoPolling.getIccid() + " 导入成功");
                    }else {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、卡号 " + autoPolling.getIccid() + " 不存在");
                    }
                }  else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、卡号 " + autoPolling.getIccid() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、卡号 " + autoPolling.getIccid() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new CustomException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    public void test(){
        String startStr= "898604D6102280801000";
        String endStr= "898604D6102280801500";
        Map<String, Object> map = new HashMap<>();
        map.put("startStr",startStr);
        map.put("endStr",endStr);
        List<Map<String, Object>> test = autoPollingMapper.test(map);
        for (int i = 0; i < test.size(); i++) {
            autoPollingMapper.ins(test.get(i));
        }


        String startStrA= "89860812192370000000";
        String endStrA= "89860812192370000000";
        map.put("startStr",startStrA);
        map.put("endStr",endStrA);
        List<Map<String, Object>> testA = autoPollingMapper.test(map);
        for (int i = 0; i < testA.size(); i++) {
            autoPollingMapper.ins(testA.get(i));
        }
    }
}
