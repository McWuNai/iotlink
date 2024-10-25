package com.yunze.system.card;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.*;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 导出 消费 者
 */
@Slf4j
@Component
public class OutCat {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;



    @RabbitHandler
    @RabbitListener(queues = "admin_OutCard_queue")
    public void DbCard(String msg, Channel channel) throws IOException {
        try {
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            Map<String,Object> Pmap = JSON.parseObject(msg);
            //System.out.println(Pmap);
            String create_by = Pmap.get("create_by").toString();//上传新文件名
            Map<String, Object> map = (Map<String, Object>) Pmap.get("map");
            Map<String, Object> User = (Map<String, Object>) Pmap.get("User");

            List<String> outCardIccidArr = (List<String>) Pmap.get("outCardIccidArr");
            List<Map<String,Object>> userArr = (List<Map<String, Object>>) Pmap.get("userArr");
            List<Map<String, Object>> stateOptions = (List<Map<String, Object>>) Pmap.get("stateOptions");
            List<Map<String, Object>> card_types = (List<Map<String, Object>>) Pmap.get("card_types");
            List<Map<String, Object>> customize_whether = (List<Map<String, Object>>) Pmap.get("customize_whether");
            List<Map<String, Object>> cardConnection_type = (List<Map<String, Object>>) Pmap.get("cardConnection_type");


            String newName = (String) Pmap.get("newName");
            Map<String,Object> task_map = (Map<String, Object>) Pmap.get("task_map");

            String prefix = "admin_OutCard_queue";
            //执行前判断 redis 是否存在 执行数据 存在时 不执行
            Object  isExecute = redisCache.getCacheObject(prefix+":"+ create_by);
            if(isExecute==null){
                redisCache.setCacheObject(prefix+":"+ create_by, "", 3, TimeUnit.SECONDS);//3 秒缓存 避免 重复消费
                OutDataCard( map,User,outCardIccidArr,userArr,stateOptions,card_types,customize_whether, newName,task_map,cardConnection_type);
            }
        } catch (Exception e) {
            log.error(">>错误 - 连接管理导出 消费者:{}<<", e.getMessage().toString());
        }
    }



    public void OutDataCard(Map<String, Object> map, Map<String, Object> User,List<String> outCardIccidArr,List<Map<String,Object>> userArr,List<Map<String, Object>> stateOptions,
                            List<Map<String, Object>> card_types,List<Map<String, Object>> customize_whether,String newName,Map<String,Object> task_map,List<Map<String, Object>> cardConnection_type){

        yzExecutionTaskMapper.add(task_map);//添加执行 任务表

        Map<String,Object> outCardMap = new HashMap<String,Object>();
        outCardMap.put("outCardIccidArr",outCardIccidArr);
        List<Map<String,Object>> outCardArr = yzCardMapper.outCard(outCardMap);

        Map<String, Object> Dept = (Map<String, Object>) User.get("dept");
        String  agent_id = Dept.get("deptId").toString();
        if(agent_id.equals("100")){
            yzCardFlowMapper.queryPackage_simple(map);
        }else{
            Map<String,Object> PackageMap = new HashMap<String,Object>();
            PackageMap.put("agent_id",agent_id);
            yzCardFlowMapper.queryAgentPackage_simple(PackageMap);
        }
        map.put("iccid_arrs",outCardIccidArr);
        //卡板信息导出需要用到的套餐数据
        List<Map<String,Object>> CardFlowArr = yzCardFlowMapper.outCardFlow(map);

        //获取通道信息
        Map<String,Object> find_simpleMap = new HashMap<String,Object>();
        find_simpleMap.putAll(map);
        find_simpleMap.put("cd_operator_type",null);//
        find_simpleMap.put("channel_id",null);//
        List<Map<String,Object>> routeArr = yzCardRouteMapper.find_simple(find_simpleMap);
        //获取 用户信息

        try {
            WriteOutCard(outCardArr,CardFlowArr,stateOptions,card_types,routeArr,userArr,customize_whether,newName,task_map,cardConnection_type);
        }catch (Exception e){
             log.error("导出卡信息异常 [导出数据 Exception] "+e.getCause().toString() );
        }
    }




    /**
     * 写入 套餐详细信息
     * @param CarMap
     * @param CardFlowArr
     * @return
     */
    protected Map<String,Object> WriteCarFlow (Map<String,Object> CarMap,List<Map<String,Object>> CardFlowArr){

        Map<String,Object> Rmap = new HashMap<String,Object>();
        String iccid = CarMap.get("iccid")!=null?CarMap.get("iccid").toString():null;
        boolean bool = false;
        if(CardFlowArr!=null && iccid!=null){
            for (int i = 0; i < CardFlowArr.size(); i++) {
                Map<String,Object> FlowMap =  CardFlowArr.get(i);
                String Ficcid = FlowMap.get("iccid").toString();
                if(Ficcid.equals(iccid)){
                    CarMap.put("true_flow",FlowMap.get("true_flow"));
                    CarMap.put("use_so_flow_sum",FlowMap.get("use_so_flow_sum"));
                    CarMap.put("Percentage",FlowMap.get("Percentage"));
                    CarMap.put("surplus",FlowMap.get("surplus"));
                    CarMap.put("packet_name",FlowMap.get("packet_name"));
                    CarMap.put("packet_valid_time",FlowMap.get("packet_valid_time"));
                    CarMap.put("packet_valid_name",FlowMap.get("packet_valid_name"));
                    CardFlowArr.remove(FlowMap);
                    bool = true;
                    break;
                }
            }
        }
        //字段 默认值
        if(!bool) {
            CarMap.put("true_flow","");
            CarMap.put("use_so_flow_sum","");
            CarMap.put("Percentage","");
            CarMap.put("surplus","");
            CarMap.put("packet_name","");
            CarMap.put("packet_valid_time","");
            CarMap.put("packet_valid_name","");
        }

        Rmap.put("CarMap",CarMap);
        Rmap.put("CardFlowArr",CardFlowArr);
        return Rmap;
    }





    /**
     * 输出 导卡操作数据
     * @param list 输出数据
     * @param fileName_flow 文件名
     * @param description  操作描述
     * @param agentName 执行人
     * @param result 执行结果
     */
    public void OutCSV(List<Map<String, String>> list , String fileName_flow, String description, String agentName, String result){

        List<Map<String, Object>> out_list = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> out_map= new HashMap<String, Object>();

            Map<String,String> cardIndex=list.get(i);
            String iccid =cardIndex.get("iccid").toString();
            //String tableName ="zy_"+cardType;
            //当前时间

            String Outcolumns[] = {"iccid","操作描述","执行时间","执行结果","执行人"};
            String keys[] = {"iccid","description","time","result","agentName"};
            String Time= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out_map.put("iccid",iccid);
            out_map.put("description",description);
            out_map.put("time",Time);
            out_map.put("result",result);
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
     * 获取 运营商类型 的通道id
     * @param map
     * @return
     */
    private Map<String,Object> getChannelIdArr (Map<String,Object> map){
        //判断是否选择 运营商类型
        map.put("selLianTong",false);
        Object cd_operator_type=map.get("cd_operator_type");
        if(cd_operator_type!=null){
            String Str_cd_operator_type = cd_operator_type.toString();
            if(Str_cd_operator_type!="" && Str_cd_operator_type.length()>0){
                List<Map<String, Object>> smap = yzCardRouteMapper.find_simple(map);
                //添加 【符合 运营类型】 的 通道 查询条件
                List<String> channel_id = new ArrayList<String>();
                if(smap!=null && smap.size()>0){
                    for (int i = 0; i < smap.size() ; i++) {
                        channel_id.add(smap.get(i).get("cd_id").toString());
                    }
                }
                //未找到相匹配的通道时 将 通道id传参 -1 使查询不到相对应数据
                if(channel_id.size()==0){
                    channel_id.add("-1");
                }
                map.put("channel_id",channel_id);
                //如果是联通的查询 条件 且 条件是 卡号 查询长度大于19 或 起止 条件 是 iccid
                if(Str_cd_operator_type.equals("2")){
                    Object type = map.get("type");
                    Object value = map.get("value");
                    if(value!=null && value.toString().length()>0 && type!=null){
                        map.put("selLianTong",true);
                    }
                    Object StartAndEndtype = map.get("StartAndEndtype");
                    Object StartValue = map.get("StartValue");
                    Object EndValue = map.get("EndValue");
                    if(StartAndEndtype!=null && StartAndEndtype.equals("3")  && StartValue!=null && EndValue !=null){
                        map.put("selLianTong",true);
                    }
                }

            }else{
                Object c_id = map.get("channel_id");
                if(c_id!=null ){
                    String Str_c_id = c_id.toString();
                    if(Str_c_id!="" && Str_c_id.length()>0) {
                        List<String> channel_id = new ArrayList<String>();
                        channel_id.add(c_id.toString());
                        map.put("channel_id",channel_id);
                    }
                }
            }
        }else{
            Object c_id = map.get("channel_id");
            if(c_id!=null ){
                String Str_c_id = c_id.toString();
                if(Str_c_id!="" && Str_c_id.length()>0) {
                    List<String> channel_id = new ArrayList<String>();
                    channel_id.add(c_id.toString());
                    map.put("channel_id",channel_id);
                }
            }
        }
        return  map;
    }




    /**
     * 字典获取
     * @param CarMap 写入Map
     * @param DictDataArr 字典 list
     * @param basis 获取依据 字段 与 dictValue 判断
     * @param fieldName 返回字段名称
     * @return
     */
    public Map<String,Object> WriteDic (Map<String,Object> CarMap,List<Map<String, Object>> DictDataArr,String basis,String fieldName){

        Map<String,Object> Rmap = new HashMap<String,Object>();
        String status_id = CarMap.get(basis)!=null?CarMap.get(basis).toString():null;
        boolean bool = false;
        if(status_id!=null){
            for (int i = 0; i < DictDataArr.size(); i++) {
                Map<String, Object>  dictData =  DictDataArr.get(i);
                String dictValue = dictData.get("dictValue").toString();
                if(dictValue.equals(status_id)){
                    CarMap.put(fieldName,dictData.get("dictLabel"));
                    bool = true;
                    break;
                }
            }
        }
        //字段 默认值
        if(!bool){
            CarMap.put(fieldName,"");
        }
        return CarMap;
    }



    /**
     *
     * 导出写入 卡信息
     * @param outCardArr 卡主信息
     * @param CardFlowArr 套餐用量信息
     * @param stateOptions 卡状态 - 字典表
     * @param card_types 卡类型  - 字典表
     * @param routeArr API 通道信息
     * @param userArr 所属用户信息
     * @param customize_whether 自定义是否 - 字典表
     * @param newName 导出文件名
     * @param task_map 执行日志结束 修改map
     * @param cardConnection_type 断开网状态 - 字典表
     * @return
     */
    protected String  WriteOutCard (List<Map<String,Object>> outCardArr,List<Map<String,Object>> CardFlowArr,List<Map<String, Object>> stateOptions,List<Map<String, Object>> card_types,
                                    List<Map<String,Object>> routeArr,List<Map<String,Object>> userArr,List<Map<String, Object>> customize_whether,String newName,Map<String, Object> task_map,List<Map<String, Object>> cardConnection_type){

        List<Map<String,Object>> out_list = new ArrayList<Map<String,Object>>();
        //循环添加单卡数据信息
        for (int i = 0; i < outCardArr.size(); i++) {
            Map<String,Object> CarMap =  outCardArr.get(i);
            //写入 套餐详细信息
            Map<String,Object> WriteCarFlowMap =  WriteCarFlow(CarMap,CardFlowArr);
            CarMap = (Map<String, Object>) WriteCarFlowMap.get("CarMap");  // 更新 CarMap
            CardFlowArr = (List<Map<String, Object>>) WriteCarFlowMap.get("CardFlowArr");  // 更新 CardFlowArr

            //写入 卡状态 status_value
            CarMap  =  WriteDic(CarMap,stateOptions,"status_id","status_value");
            //写入 卡类型 type_value
            CarMap  =  WriteDic(CarMap,card_types,"type","type_value");
            //写入 gprs是否开启 gprs_value
            CarMap  =  WriteDic(CarMap,customize_whether,"gprs","gprs_value");
            //写入 未订购资费 是否 轮询停机 gprs_value
            CarMap  =  WriteDic(CarMap,customize_whether,"is_Disconnected","Disconnected_value");
            //写入 断开网状态 connection_status
            CarMap  =  WriteDic(CarMap,cardConnection_type,"connection_status","connection_value");

            //写入 API 通道信息
            CarMap  =  WriteCarRout(CarMap,routeArr);
            //写入 所属用户信息
            CarMap  =  WriteCarUser(CarMap,userArr);

            //删除导出不需要的字段
            CarMap.remove("status_id");
            CarMap.remove("type");
            CarMap.remove("package_id");
            CarMap.remove("agent_id");
            CarMap.remove("channel_id");
            CarMap.remove("gprs");
            CarMap.remove("remaining");
            CarMap.remove("is_Disconnected");
            CarMap.remove("connection_status");

            out_list.add(CarMap);
        }

        String Outcolumns[] = {"虚拟编号","MSISDN","ICCID","IMSI","IMEI","出库日期","发货日期","激活日期","到期日期","周期用量","分组","未订购资费是否停机"  ,"所属企业","备注","总量","已使用量(MB)","使用率","剩余用量","资费计划","资费周期","资费时间单位","卡状态","断开网状态","卡类型","GPRS是否开启","API通道","运营商类型","所属用户"};
        String keys[] = {"vid","msisdn","iccid","imsi","imei","out_of_stock","deliver_date","activate_date","due_expire_time","used","customize_grouping","Disconnected_value"  ,"dept_name","remarks","true_flow","use_so_flow_sum","Percentage","surplus","packet_name","packet_valid_time","packet_valid_name","status_value","connection_value","type_value","gprs_value","cd_name","dict_label","user_name"};
        writeCSV.Write(newName,out_list,Outcolumns,null,keys);

        yzExecutionTaskMapper.set_end_time(task_map);//任务结束
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }



    /**
     * 写入 所属用户信息
     * @param CarMap
     * @param userArr
     * @return
     */
    protected Map<String,Object> WriteCarUser (Map<String,Object> CarMap,List<Map<String,Object>> userArr){

        Map<String,Object> Rmap = new HashMap<String,Object>();
        String user_id = CarMap.get("user_id")!=null?CarMap.get("user_id").toString():null;
        boolean bool = false;
        if(userArr!=null && user_id !=null){
            for (int i = 0; i < userArr.size(); i++) {
                Map<String,Object> UserMap =  userArr.get(i);
                String Fuser_id = UserMap.get("user_id").toString();
                if(Fuser_id.equals(user_id)){
                    CarMap.put("user_name",UserMap.get("user_name"));
                    bool = true;
                    break;
                }
            }
        }
        if(!bool){
            CarMap.put("user_name","");
        }
        return CarMap;
    }


    /**
     * 写入 API 通道信息
     * @param CarMap
     * @param routeArr
     * @return
     */
    protected Map<String,Object> WriteCarRout (Map<String,Object> CarMap,List<Map<String,Object>> routeArr){

        Map<String,Object> Rmap = new HashMap<String,Object>();
        String channel_id = CarMap.get("channel_id")!=null?CarMap.get("channel_id").toString():null;
        boolean bool = false;
        if(routeArr!=null && channel_id!=null){
            for (int i = 0; i < routeArr.size(); i++) {
                Map<String,Object> RoutMap =  routeArr.get(i);
                String cd_id = RoutMap.get("cd_id").toString();
                if(cd_id.equals(channel_id)){
                    CarMap.put("cd_name",RoutMap.get("cd_alias"));
                    CarMap.put("dict_label",RoutMap.get("dict_label"));
                    bool = true;
                    break;
                }
            }
        }
        if(!bool){
            CarMap.put("cd_name","");
            CarMap.put("dict_label","");
        }
        return CarMap;
    }









}
