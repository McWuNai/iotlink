package com.yunze.cn.service.impl;




import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.cn.mapper.YzCardMapper;
import com.yunze.cn.service.IYzCardService;

import javax.annotation.Resource;
import java.util.*;

import com.yunze.cn.util.GetShowStatIdArr;
import org.springframework.stereotype.Component;

/**
 * 卡信息 业务实现类
 * 
 * @author root
 */
@Component
public class YzCardServiceImpl implements IYzCardService
{

    @Resource
    private YzCardMapper yzCardMapper;

    @Resource
    private InternalApiRequest internalApiRequest;

    @Override
    public Map<String, Object> findIccid(Map<String, Object> map) {
        return  yzCardMapper.findIccid(map);
    }

    @Override
    public String findOperatorType(Map<String, Object> map) {
        return yzCardMapper.findOperatorType(map);
    }

    @Resource
    private GetShowStatIdArr getShowStatIdArr;

    @Override
    public void singleState(Map<String, Object> map) {
        Map<String, Object> rMap = new HashMap<>();
        boolean bool = false;
        String message = "单卡灵活变更状态 操作失败";
        Map<String, Object> Route = yzCardMapper.findRoute(map);
        if (Route != null) {
            String cd_status = Route.get("cd_status").toString();
            String iccid = Route.get("iccid").toString();
            Map<String, Object> Obj = new HashMap<>();
            Object ShowId = map.get("status_ShowId");
            Obj.put("operType", ShowId);//API 状态
            Obj.put("iccid", iccid);
            if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                Map<String, Object> CsFble = internalApiRequest.changeCardStatusFlexible(Obj, Route);
                String code = CsFble.get("code") != null ? CsFble.get("code").toString() : "500";
                if (code.equals("200")) {
                    String statusCode = map.get("status_ShowId").toString();
                    Map<String, Object> Upd_Map = new HashMap<>();
                    Upd_Map.put("status_id", statusCode);
                    Upd_Map.put("status_ShowId", getShowStatIdArr.GetShowStatId(statusCode));
                    Upd_Map.put("iccid", map.get("iccid").toString());
                    try {
                        yzCardMapper.updStatusId(Upd_Map);//变更卡状态
                        bool = true;
                        message = "操作成功！";
                    } catch (Exception e) {
                        message = "DB保存状态操作失败！" + e.getMessage().toString();
                    }
                } else {
                    if (CsFble.get("Message") == null) {
                        message = "网络繁忙稍后重试！";
                    } else {
                        message = CsFble.get("Message").toString();
                    }
                }
            } else {
                message = "未知状态！";
            }
        } else {
            message = " iccid [" + map.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！";
        }
        rMap.put("bool",bool);
        rMap.put("message",message);
    }


}