package com.yunze.system.order;

import com.alibaba.fastjson.JSON;
import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资费退订
 * */
@Slf4j
@Component
public class BscribeEls {

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;

    @RabbitHandler
    @RabbitListener(queues = "admin_Unsubscribe_queue")
    public void Unsubscribe(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            PbEx(msg);

        } catch (Exception e) {
            log.error(">>错误 - 资费退订 消费者:{}<<", e.getMessage().toString());
        }
    }


    @RabbitHandler
    @RabbitListener(queues = "dlx_admin_Unsubscribe_queue")
    public void DlxUnsubscribe(String msg) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            PbEx(msg);

        } catch (Exception e) {
            log.error(">>错误 - 资费退订 消费者:{}<<", e.getMessage().toString());
        }
    }




    public void  PbEx(String msg){
        Map<String, Object> map = JSON.parseObject(msg);
        List<Map<String, Object>> Unsubscribe = (List<Map<String, Object>>) map.get("Unsubscribe");
        Map<String, Object> task_map = (Map<String, Object>) map.get("task_map");
        String create_by = map.get("create_by").toString();
        String newUpdte = map.get("newUpdte").toString();
        String newDelete = map.get("newDelete").toString();
        String newOrder = map.get("newOrder").toString();
        Map<String, Object> OrderMap = (Map<String, Object>) map.get("OrderMap");

        execution(map,OrderMap,Unsubscribe,task_map,create_by,newUpdte,newDelete,newOrder);//执行卡划分
    }


    @Transactional
    public String execution(Map<String, Object> map, Map<String, Object> OrderMap, List<Map<String, Object>> unsubscribe,
                           Map<String, Object> task_map, String create_by, String newUpdte, String newDelete,String newOrder) {

        String Outcolumns[] = {"iccid","操作描述","执行时间","执行结果","执行人"};
        String keys[] = {"iccid","description","time","result","agentName"};

        map.remove("agent_id");
        //备份 order 表
        List<Map<String,Object>> listOr =yzOrderMapper.backupsOr(map);
        if(listOr != null && listOr.size()>0){
            OutCSV(listOr,newDelete,create_by);
        }

        //遍历listOr 与 订单匹对
        List<String> stringList = new ArrayList<>();
        for (Map<String, Object> mapList : listOr) {
            String Str = mapList.get("ord_no").toString();
            stringList.add(Str);
        }

        // 备份 flow 表
        Map<String,Object> Ramp = new HashMap<>();
        Ramp.put("ordNoID",stringList);
        List<Map<String,Object>> listFl = yzCardFlowMapper.backupsFl(Ramp);
        if(listFl != null && listFl.size()>0){
            OutCSVFlow(listFl,newUpdte,create_by);
            //System.out.println(listFl);
        }


        // 下面执行  订单状态【退款】并删除资费计划！
        if(unsubscribe != null && unsubscribe.size()>0){
            List<String> newString = new ArrayList<>();
            for(Map<String, Object> Bscribe : unsubscribe){
                String Str = Bscribe.get("ord_no").toString();
                newString.add(Str);
            }
            String obj = map.get("radio").toString();
            if(obj.equals("update")){
                Map<String,Object> ordMap = new HashMap<>();
                ordMap.put("ordNoID",newString);
                boolean bool = yzOrderMapper.updState(ordMap);
                if(bool){
                    ordMap.put("delOrd",newString);
                    yzCardFlowMapper.delOrdArr(ordMap);
                }else {
                    return "订单状态【退款】并删除资费计划！操作失败";
                }
            }
            if(obj.equals("delete")){
                Map<String,Object> DelMap = new HashMap<>();
                DelMap.put("delFlo",newString);
                boolean bool = yzOrderMapper.delFloArr(DelMap);
                if(bool){
                    DelMap.put("delOrd",newString);
                    yzCardFlowMapper.delOrdArr(DelMap);
                }else {
                    return "删除订单并删除资费计划";
                }
            }
        }

        //下面是资费订购操作
        String value = map.get("valueIs").toString();
        if(value.equals("true")){
            if(unsubscribe.size()>0){
                Map<String,Object> Arrs = new HashMap<>();

                List<Map<String,Object>> list = new ArrayList<>();
                for (Map<String,Object> unsuMap : unsubscribe){
                    Map<String,Object> Amap = new HashMap<>();
                    String ord_no = VeDate.getNo(8);//获取八位随机数
                    unsuMap.remove("ord_no");
                    unsuMap.remove("status");
                    unsuMap.remove("packet_id");
                    unsuMap.remove("validate_type");
                    Amap.put("ord_no",ord_no);
                    Amap.putAll(OrderMap);
                    Amap.put("iccid",unsuMap.get("iccid"));
                    list.add(Amap);
                }
                Arrs.put("order_arrs",list);
                int  sInt = yzOrderMapper.importOrder(Arrs);
                if(sInt>0){
                    writeCSV.OutCSVObj(unsubscribe,newOrder,"平台批量成功 充值 数据 ["+sInt+"] 条",create_by,"充值成功",Outcolumns,keys);
                    yzExecutionTaskMapper.set_end_time(task_map);//任务结束
                }
            }
        }

        yzExecutionTaskMapper.set_end_time(task_map);
        return "当前筛选条件下需要的数据 [ " + unsubscribe.size() + " ] 条  指令已下发详情查看 【执行日志管理】 ！";
    }



    /**
     * 输出 备份   yz_order 表
     * @param list 输出数据
     * @param fileName_flow 文件名
     * @param agentName 执行人
     */
    public void OutCSV(List<Map<String, Object>> list , String fileName_flow, String agentName){

        List<Map<String, Object>> out_list = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> out_map= new HashMap<String, Object>();

            Map<String,Object> cardIndex=list.get(i);
            String id =cardIndex.get("id")+"";
            String ord_no =cardIndex.get("ord_no")+"";
            String ord_type =cardIndex.get("ord_type")+"";
            String ord_name =cardIndex.get("ord_name")+"";
            String iccid =cardIndex.get("iccid")+"";
            String wx_ord_no =cardIndex.get("wx_ord_no")+"";
            String status =cardIndex.get("status")+"";
            String price =cardIndex.get("price")+"";
            String account =cardIndex.get("account")+"";
            String packet_id =cardIndex.get("packet_id")+"";
            String pay_type =cardIndex.get("pay_type")+"";
            String cre_type =cardIndex.get("cre_type")+"";
            String is_profit =cardIndex.get("is_profit")+"";
            String add_package =cardIndex.get("add_package")+"";
            String show_status =cardIndex.get("show_status")+"";
            String open_id =cardIndex.get("open_id")+"";
            String agent_id =cardIndex.get("agent_id")+"";
            String profit_type =cardIndex.get("profit_type")+"";
            String create_time =cardIndex.get("create_time")+"";
            String validate_type =cardIndex.get("validate_type")+"";
            String info =cardIndex.get("info")+"";
            String add_parameter =cardIndex.get("add_parameter")+"";
            String add_package_time =cardIndex.get("add_package_time")+"";
            String del_flag =cardIndex.get("del_flag")+"";
            String is_profit_sharing =cardIndex.get("is_profit_sharing")+"";
            String profit_sharing_time =cardIndex.get("profit_sharing_time")+"";

            //String tableName ="zy_"+cardType;

            String Outcolumns[] = {"id","ord_no","ord_type","ord_name","iccid","wx_ord_no","status","price","account","packet_id","pay_type",
                    "cre_type","is_profit","add_package","show_status","open_id","agent_id","profit_type","create_time","validate_type",
                    "info","add_parameter","add_package_time","del_flag","is_profit_sharing","profit_sharing_time",
                   "执行时间","执行人"};

            String keys[] = {"id","ord_no","ord_type","ord_name","iccid","wx_ord_no","status","price","account","packet_id","pay_type",
                    "cre_type","is_profit","add_package","show_status","open_id","agent_id","profit_type","create_time","validate_type",
                    "info","add_parameter","add_package_time","del_flag","is_profit_sharing","profit_sharing_time",
                    "time","agentName"};

            String Time= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out_map.put("id",id);
            out_map.put("ord_no",ord_no);
            out_map.put("ord_type",ord_type);
            out_map.put("ord_name",ord_name);
            out_map.put("iccid",iccid);
            out_map.put("wx_ord_no",wx_ord_no);
            out_map.put("status",status);
            out_map.put("price",price);
            out_map.put("account",account);
            out_map.put("packet_id",packet_id);
            out_map.put("pay_type",pay_type);
            out_map.put("cre_type",cre_type);
            out_map.put("is_profit",is_profit);
            out_map.put("add_package",add_package);
            out_map.put("show_status",show_status);
            out_map.put("open_id",open_id);
            out_map.put("agent_id",agent_id);
            out_map.put("profit_type",profit_type);
            out_map.put("create_time",create_time);
            out_map.put("validate_type",validate_type);
            out_map.put("info",info);
            out_map.put("add_parameter",add_parameter);
            out_map.put("add_package_time",add_package_time);
            out_map.put("del_flag",del_flag);
            out_map.put("is_profit_sharing",is_profit_sharing);
            out_map.put("profit_sharing_time",profit_sharing_time);

            out_map.put("time",Time);
            out_map.put("agentName",agentName);

            out_list.add(out_map);
            if ((i+1)%50==0 || (i+1)==list.size()){
                //执行导出
                if(out_list.size()>0){
                    writeCSV.Write(fileName_flow,out_list,Outcolumns,null,keys);
                    out_list = new ArrayList<>();
                }
            }
        }
    }

    /**
     * 备份 flow 表
     * @param list 输出数据
     * @param fileName_flow 文件名
     * @param agentName 执行人
     */
    public void OutCSVFlow (List<Map<String, Object>> list , String fileName_flow, String agentName){

        List<Map<String, Object>> out_list = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> out_map= new HashMap<String, Object>();

            Map<String,Object> cardIndex=list.get(i);
            String id =cardIndex.get("id")+"";
            String package_id =cardIndex.get("package_id")+"";
            String packet_id =cardIndex.get("packet_id")+"";
            String ord_no =cardIndex.get("ord_no")+"";
            String true_flow =cardIndex.get("true_flow")+"";
            String error_flow =cardIndex.get("error_flow")+"";
            String start_time =cardIndex.get("start_time")+"";
            String end_time =cardIndex.get("end_time")+"";
            String create_time =cardIndex.get("create_time")+"";
            String ord_type =cardIndex.get("ord_type")+"";
            String status =cardIndex.get("status")+"";
            String packet_type =cardIndex.get("packet_type")+"";
            String use_flow =cardIndex.get("use_flow")+"";
            String iccid =cardIndex.get("iccid")+"";
            String error_time =cardIndex.get("error_time")+"";
            String validate_type =cardIndex.get("validate_type")+"";
            String validate_time =cardIndex.get("validate_time")+"";
            String use_true_flow =cardIndex.get("use_true_flow")+"";
            String use_so_flow =cardIndex.get("use_so_flow")+"";

            String Outcolumns[] = {"id","package_id","packet_id","ord_no","true_flow","error_flow","start_time","end_time","create_time",
                    "ord_type","status","packet_type","use_flow","iccid","error_time","validate_type","validate_time","use_true_flow",
                    "use_so_flow","执行时间","执行人"};
            String keys[] = {"id","package_id","packet_id","ord_no","true_flow","error_flow","start_time","end_time","create_time",
                    "ord_type","status","packet_type","use_flow","iccid","error_time","validate_type","validate_time","use_true_flow",
                    "use_so_flow","time","agentName"};
            String Time= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            out_map.put("id",id);
            out_map.put("package_id",package_id);
            out_map.put("packet_id",packet_id);
            out_map.put("ord_no",ord_no);
            out_map.put("true_flow",true_flow);
            out_map.put("error_flow",error_flow);
            out_map.put("start_time",start_time);
            out_map.put("end_time",end_time);
            out_map.put("create_time",create_time);
            out_map.put("ord_type",ord_type);
            out_map.put("status",status);
            out_map.put("packet_type",packet_type);
            out_map.put("use_flow",use_flow);
            out_map.put("iccid",iccid);
            out_map.put("error_time",error_time);
            out_map.put("validate_type",validate_type);
            out_map.put("validate_time",validate_time);
            out_map.put("use_true_flow",use_true_flow);
            out_map.put("use_so_flow",use_so_flow);
            out_map.put("time",Time);
            out_map.put("agentName",agentName);

            out_list.add(out_map);
            if ((i+1)%50==0 || (i+1)==list.size()){
                //执行导出
                if(out_list.size()>0){
                    writeCSV.Write(fileName_flow,out_list,Outcolumns,null,keys);
                    out_list = new ArrayList<>();
                }
            }
        }
    }


}























































