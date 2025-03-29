package com.yunze.web.controller.yunze.card.api;

import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.CardFlowSyn;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import com.yunze.system.service.impl.yunze.YzCardServiceImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.yunze.framework.datasource.DynamicDataSourceContextHolder.log;

@RestController
@RequestMapping("/apiPush")
public class PushApiController {
    @Resource
    private YzCardServiceImpl yzCardServiceImpl;

    @Resource
    private CardFlowSyn cardFlowSyn;

    @Resource
    private YzCardMapper yzCardMapper;

    @Resource
    private GetShowStatIdArr getShowStatIdArr;

    @PostMapping ("/getCardDataUsage")
    public void getFlowUsed(@RequestBody Map<String, Object> requestBody) {
        // 因为"data"字段是嵌套对象，所以需要用Map来接收
        Map<String, Object> data = (Map<String, Object>) requestBody.get("data");
        try {
            HashMap<String, Object> Parammap = new HashMap<>();
            Parammap.put("iccid", data.get("iccid"));
            Map<String, Object> Route = yzCardServiceImpl.findRoute(Parammap);
            if (Route != null) {
                String cd_status = Route.get("cd_status").toString();
                if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                    // 获取 卡用量 开卡日期 更新 card info
                    double Use = Double.parseDouble(data.get("dataUsage").toString());
                    if (Use >= 0) {
                        try {
                            Map<String, Object> RMap = cardFlowSyn
                                    .CalculationFlow(Parammap.get("iccid").toString(), Use, Route);
                            int status_ShowId = (int) yzCardMapper.find(Parammap).get("status_ShowId");
                            if (Double.parseDouble(RMap.get("remaining").toString()) > 0.00
                                    && status_ShowId == 5) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("iccid", Parammap.get("iccid").toString());
                                map.put("status_ShowId", "1");
                                yzCardServiceImpl.singleState(map);
                            }
                        } catch (Exception e) {
                            log.error("用量内部计算错误！" + e.getMessage());
                        }
                    } else {
                        log.error("接口超频返回暂无数据返回，请稍后重试！");
                    }
                } else {
                    String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                    log.info("同步用量 操作失败！" + " 通道 [" + statusVal + "]");
                }
            } else {
                log.error(" iccid [" + Parammap.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
            }
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            log.error("<br/> yunze:card:SynFlow  " + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
    }

    /*@PostMapping ("/getCardDailyDataUsage")
    public void getCardDailyDataUsage(@RequestBody Map<String, Object> requestBody) {
        // 因为"data"字段是嵌套对象，所以需要用Map来接收
        Map<String, Object> data = (Map<String, Object>) requestBody.get("data");
        Map<String, Object> outData = new HashMap<>();
        outData.put("iccid", data.get("iccid"));
        outData.put("dailyDataUsage", data.get("dailyDataUsage"));
        outData.put("api_type", 1);
        yzCardServiceImpl.pushApi(outData);
    }*/

    @PostMapping ("/getCardStatus")
    public void getCardStatus(@RequestBody Map<String, Object> requestBody) {
        // 因为"data"字段是嵌套对象，所以需要用Map来接收
        Map<String, Object> data = (Map<String, Object>) requestBody.get("data");
        /*Map<String, Object> outData = new HashMap<>();
        outData.put("iccid", data.get("iccid"));
        outData.put("currentState", data.get("currentState"));
        outData.put("api_type", 2);
        yzCardServiceImpl.pushApi(outData);*/

        String status_id = data.get("currentState").toString();
        Map<String, Object> Upd_Map = new HashMap<>();
        Upd_Map.put("iccid",data.get("iccid"));
        Upd_Map.put("status_id",status_id);
        Upd_Map.put("status_ShowId",getShowStatIdArr.GetShowStatId(status_id));
        yzCardMapper.updStatusId(Upd_Map);
    }
}
