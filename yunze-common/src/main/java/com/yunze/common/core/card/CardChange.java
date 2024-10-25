package com.yunze.common.core.card;

import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.config.MyDictionary;
import com.yunze.common.mapper.yunze.YzCardInfoChangeMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.utils.yunze.GetShowStatIdArr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 卡信息 变更 操作记录 日志 输出
 * @Auther: zhang feng
 * @Date: 2021/09/15/8:21
 * @Description:
 */
@Slf4j
@Component
public class CardChange {

    @Resource
    private MyDictionary myDictionary;
    @Resource
    private YzCardInfoChangeMapper yzCardInfoChangeMapper;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;
    /**
     * 停 开 机
     * @param Parammap
     * @param map
     * @param Add_Map 新增变更记录
     * @param Upd_Map 卡状态 变更Map
     */
    public  void OnOff (Map<String,Object> Parammap, Map<String,Object> map,Map<String,Object> Add_Map,Map<String,Object> Upd_Map){
        boolean bool = false;
        String remark = "";
        String execution_status = "2";
        try {
            Map<String,Object> Rmap =  internalApiRequest.changeCardStatus(Parammap,map);
            String code =  Rmap.get("code")!=null?Rmap.get("code").toString():"500";
            String iccid = Upd_Map.get("iccid").toString();
            if(code.equals("200")){
                //更新 card info
                Object status_id = Upd_Map.get("status_id");
                String status_ShowId = getShowStatIdArr.GetShowStatId(status_id.toString());
                Upd_Map.put("status_ShowId",status_ShowId);
                bool = yzCardMapper.updStatusId(Upd_Map)>0;
                if(bool){
                    remark = "执行成功！";
                    execution_status = "1";
                }else{
                    log.error(">>DB - 达量停机轮询消费者 同步达量停机失败:{} | {}<<",iccid,Upd_Map);
                }
            }else{
                if(Rmap.get("Message")!=null){
                    remark = Rmap.get("Message").toString().length()>100?Rmap.get("Message").toString().substring(0,100):Rmap.get("Message").toString();
                }
                log.info(">>API - CardChange  OnOff 操作失败 :{} | {}<<",iccid,Rmap);
            }
        }catch (Exception e){
            remark = e.getMessage().length()>100?e.getMessage().substring(0,100):e.getMessage();
        }
        Add_Map.put("remark",remark);
        Add_Map.put("execution_status",execution_status);
        boolean addBool =  yzCardInfoChangeMapper.save(Add_Map)>0;
        log.info(map.get("iccid")+" yzCardMapper.updStatusId bool ： "+bool+" 新增卡信息变更记录 addBool :"+addBool);
    }


    /**
     * 断 开 网
     * @param Parammap
     * @param map
     * @param Add_Map 新增变更记录
     * @param Upd_Map 断开网 变更Map
     */
    public  void ChangeConnection (Map<String,Object> Parammap, Map<String,Object> map,Map<String,Object> Add_Map,Map<String,Object> Upd_Map){
        boolean bool = false;
        String remark = "";
        String execution_status = "2";
        try {
            Map<String,Object> Rmap =  internalApiRequest.FunctionApnStatus(Parammap,map);
            String code =  Rmap.get("code")!=null?Rmap.get("code").toString():"500";
            String iccid = Upd_Map.get("iccid").toString();
            if(code.equals("200")){
                //更新 card info
                bool = yzCardMapper.updConnectionStatus(Upd_Map)>0;
                if(bool){
                    remark = "执行成功！";
                    execution_status = "1";
                }else{
                    log.error(">>DB - 达量停机轮询消费者 同步达量停机失败:{} | {}<<",iccid,Upd_Map);
                }
            }else{
                if(Rmap.get("Message")!=null){
                     remark = Rmap.get("Message").toString().length()>100?Rmap.get("Message").toString().substring(0,100):Rmap.get("Message").toString();
                }
                log.info(">>API - CardChange  ChangeConnection 操作失败 :{} | {}<<",iccid,Rmap);
            }
        }catch (Exception e){
            remark = e.getMessage().length()>100?e.getMessage().substring(0,100):e.getMessage();
        }
        Add_Map.put("remark",remark);
        Add_Map.put("execution_status",execution_status);
        boolean addBool =  yzCardInfoChangeMapper.save(Add_Map)>0;
        log.info(map.get("iccid")+" yzCardMapper.updConnectionStatus bool ： "+bool+" 新增卡信息变更记录 addBool :"+addBool);
    }




}
