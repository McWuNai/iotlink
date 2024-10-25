package com.yunze.task.yunze.card;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.mapper.yunze.polling.YzSynCardInfoMapper;
import com.yunze.common.utils.yunze.WriteCSV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

@Slf4j
@Component
public class CardSynInfoTaskMQ {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzSynCardInfoMapper yzSynCardInfoMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;


    /**
     * 检查是否有需要执行 新增的 卡号数据
     *
     * @param msg
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_cardSynInfoAddDetect_queue")
    public void SynPacketCount(String msg) {
        //1.状态 正常 且开启了需要同步上游成员 时
        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", null);
        /*findRouteID_Map.put("cd_lunxun","notChoose");//是否轮询 不选择（只要是状态正常就去获取）*/
        findRouteID_Map.put("sync_upstream", "1");// 同步上游卡号数据

        Map<String, Object> User = new HashMap<>();
        Map<String, Object> dept = new HashMap<>();
        dept.put("deptName", "系统自动同步");
        dept.put("deptId", "100");
        User.put("dept", dept);


        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            for (int i = 0; i < channelArr.size(); i++) {
                Map<String, Object> ChannelObj = channelArr.get(i);
                String channel_id = ChannelObj.get("cd_id").toString();
                String cd_name = ChannelObj.get("cd_name").toString();


                ChannelObj.put("channel_id", channel_id);
                ChannelObj.put("is_new", "1");//是否 需要新增到 card_info 表 【是】
                User.put("userName", "通道[" + channel_id + "]");
                String sync_data_type = ChannelObj.get("sync_data_type") != null ? ChannelObj.get("sync_data_type").toString() : null;//'上游返回的数据-同步类型'
                //String upstream_card_count = ChannelObj.get("upstream_card_count")!=null ?ChannelObj.get("upstream_card_count").toString():"0";//'上游成员卡总数'
                String sync_field_arr[] = ChannelObj.get("sync_field") != null ? ChannelObj.get("sync_field").toString().split(",") : null;//'自动同步的字段 如 activate_date,open_date'
                String sync_change_notification = ChannelObj.get("sync_change_notification") != null ? ChannelObj.get("sync_change_notification").toString() : null;//'上游成员变更是是否通知



                List<Map<String, Object>> recentSyncArr = yzSynCardInfoMapper.getSyncCardInfo(ChannelObj);// 获取标记了 需要新增的卡号
                if (recentSyncArr != null && recentSyncArr.size() > 0) {
                    List<Map<String, Object>> addArr = new ArrayList<>();//准备新增的数组
                    boolean imsi_bool = false, activate_date_bool = false, open_date_bool = false, remark_bool = false;//判断那些需要 修改或新增的字段
                    if (sync_field_arr != null) {
                        for (int j = 0; j < sync_field_arr.length; j++) {
                            String str_toLowerCase = sync_field_arr[j] != null ? sync_field_arr[j].toLowerCase() : "";
                            switch (str_toLowerCase) {
                                case "imsi":
                                    imsi_bool = true;
                                    break;
                                case "activate_date":
                                    activate_date_bool = true;
                                    break;
                                case "open_date":
                                    open_date_bool = true;
                                    break;
                                case "remark":
                                    remark_bool = true;
                                    break;
                            }
                        }
                    }
                    for (int j = 0; j < recentSyncArr.size(); j++) {
                        Map<String, Object> recentSync_Map = recentSyncArr.get(j);

                        String msisdn = recentSync_Map.get("msisdn") != null ? recentSync_Map.get("msisdn").toString() : null;
                        String iccid = recentSync_Map.get("iccid") != null ? recentSync_Map.get("iccid").toString() : null;
                        String imsi = recentSync_Map.get("imsi") != null ? recentSync_Map.get("imsi").toString() : null;
                        String activate_date = recentSync_Map.get("activate_date") != null ? recentSync_Map.get("activate_date").toString() : null;
                        String open_date = recentSync_Map.get("open_date") != null ? recentSync_Map.get("open_date").toString() : null;
                        String remark = recentSync_Map.get("remark") != null ? recentSync_Map.get("remark").toString() : null;
                        if (msisdn != null && iccid != null) {
                            //默认同步字段 msisdn，iccid channel_id 且不能为空
                            Map<String, Object> add_Map = new HashMap<>();
                            add_Map.put("msisdn", msisdn);
                            add_Map.put("iccid", iccid);
                            add_Map.put("channel_id", channel_id);
                            if (imsi_bool) {
                                add_Map.put("imsi", imsi);
                            }
                            if (activate_date_bool) {
                                add_Map.put("activate_date", activate_date);
                            }
                            if (open_date_bool) {
                                add_Map.put("open_date", open_date);
                            }
                            if (remark_bool) {
                                add_Map.put("remark", remark);
                            }
                            addArr.add(add_Map);
                        }
                    }
                    if (addArr != null && addArr.size() > 0) {
                        //模仿生成 批量上传的文件 生成 svc 再转换成 excle,将 新增指令 发送到 新增消费者去执行 【统一新增入口】
                        try {
                            String newName = UUID.randomUUID().toString().replace("-", "") + "_CardImport";
                            String keys[] = {"msisdn", "iccid", "imsi", "open_date", "activate_date", "agent_id", "channel_id", "is_pool", "batch_date", "remarks", "status_id", "package_id", "imei", "type", "network_type", "is_sms", "sms_number", "gprs", "user_id", "test_period_last_time", "silent_period_last_time"};
                            writeCSV.Write(newName, addArr, keys, null, keys, "/upload/uploadCard", true);
                            String flieUrlRx = "/upload/uploadCard/";
                            writeCSV.CsvOrExcle(flieUrlRx+newName, null, false,true);
                            uploadCard(User, newName+".xls",sync_change_notification,cd_name,addArr.size());
                            //修改 该批次卡的 需要新增状态 改为 0
                            Map<String,Object> upd_Map = new HashMap<>();
                            upd_Map.put("is_new","0");
                            upd_Map.put("updArr",addArr);
                            int upd_count = yzSynCardInfoMapper.updateArr(upd_Map);
                            log.info(" ==== {} [is_new = 0 ] 卡数量 {} 成功数量 {}", JSON.toJSONString(ChannelObj),addArr.size(),upd_count);
                        } catch (Exception e) {
                            log.error("模仿生成 批量上传的文件 CsvOrExcle 异常 {}",e.getMessage());
                        }

                    }
                }
            }
        }
    }


    /**
     * dept userName
     * @param User     dept userName
     * @param ReadName
     * @return
     */
    public String uploadCard(Map<String, Object> User, String ReadName,String sync_change_notification,String cd_name,int size) {
        String newName = UUID.randomUUID().toString().replace("-", "") + "_CardImport";
        try {
            /**
             * 获取当前项目的工作路径
             */
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            String flieUrlRx = "/upload/uploadCard/";
            ReadName = flieUrlRx + ReadName;
            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_saveCard_queue";
            String polling_routingKey = "admin.saveCard.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {

                Map<String, Object> task_map = new HashMap<String, Object>();
                Map<String, String> Dept = (Map<String, String>)User.get("dept");
                String SaveUrl = "/getcsv/"+newName+".csv";
                String task_name ="物联卡管理 [自动导入] ";
                String  create_by = " [ "+Dept.get("deptName")+" ] - "+" [ "+User.get("userName")+" ] ";
                task_map.put("auth",create_by);
                task_map.put("task_name",task_name);
                task_map.put("url",SaveUrl);
                task_map.put("agent_id", Dept.get("deptId"));
                task_map.put("type","3");

                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("newName", newName);//输出文件名
                start_type.put("User", User);//登录用户信息
                start_type.put("pTaskMap", task_map);//
                yzExecutionTaskMapper.add(task_map);//添加执行 任务表

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });

                //判断通道信息变更时是否需要通知 需要通知时进行 通知
                if(sync_change_notification!=null && sync_change_notification.equals("1")){
                    try {
                        Map<String, Object> send_type = new HashMap<>();

                        Map<String, Object> CcMessage_Map = new HashMap<>();
                        CcMessage_Map.put("taskId",task_map.get("id"));// 获取 执行任务表的ID
                        CcMessage_Map.put("describe"," 卡号自动新增 ["+cd_name+"] | ["+size+"] 张卡号 ");
                        CcMessage_Map.put("heartext"," 卡号自动新增 ["+cd_name+"] | ["+size+"] 张卡号 ");
                        send_type.put("CcMessage_Map",CcMessage_Map);
                        send_type.put("trigger_type","13");
                        rabbitTemplate.convertAndSend("admin_exchange", "admin.EmaiSynCardInfo.queue", JSON.toJSONString(send_type), message -> {
                            // 设置消息过期时间 30 分钟 过期
                            message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                            return message;
                        });
                    } catch (Exception e) {
                        log.error("通道变更邮件抄送异常 {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("导入 卡号 失败 {}" , e.getMessage());
                return ("物联卡管理 导入 操作失败！");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return "上传excel异常";
        }
        return "导入指令 已发送 ! ";
    }



}
