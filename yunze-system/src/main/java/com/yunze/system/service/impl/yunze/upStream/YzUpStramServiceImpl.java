package com.yunze.system.service.impl.yunze.upStream;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.upStream.upStreamMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.WriteCSV;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.service.yunze.upStream.YzUpStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 2022年9月26日13:48:04
 */
@Service
@Slf4j
public class YzUpStramServiceImpl implements YzUpStreamService {

    @Resource
    private upStreamMapper upStreamMapper;

    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private SysDictDataMapper dictDataMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;


    @Override
    public Map<String, Object> getList(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = upStreamMapper.MapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", upStreamMapper.getList(map));
        return omp;
    }

    @Override
    public String exportData(Map<String, Object> map, SysUser User) {
        map.remove("pageNum");
        map.remove("pageSize");

        String create_by = " [ " + User.getDept().getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
        String newName = UUID.randomUUID().toString().replace("-", "") + "_UpStreamOut";
        String agent_id = User.getDept().getDeptId().toString();

        String task_name = "上游集团群组 [导出] ";
        String SaveUrl = "/getcsv/" + newName + ".csv";

        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth", create_by);
        task_map.put("task_name", task_name);
        task_map.put("url", SaveUrl);
        task_map.put("agent_id", agent_id);
        task_map.put("type", "4");
      //  yzExecutionTaskMapper.add(task_map);//添加执行 任务表
//        List<Map<String, Object>> outMemberGrouplArr = memberGroupMapper.outChannel(map);


        List<SysDictData> channel_code = dictDataMapper.selectDictDataByType("channel_code");

        //判断是否有条数


        //发送队列
        //1.创建路由 绑定 生产队列 发送消息
        //导卡 路由队列
        //admin_OutMemberGroup_queue
        String polling_queueName = "admin_OutUpStream_queue";
        String polling_routingKey = "admin.OutUpStream.queue";
        String polling_exchangeName = "admin_exchange";//路由

        try {
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表
            Map<String, Object> start_type = new HashMap<>();
            start_type.put("channel_code", channel_code);
            //start_type.put("outOutMemberGroupArr", outMemberGrouplArr);
            start_type.put("map", map);
            start_type.put("newName", newName);
            start_type.put("create_by", create_by);
            start_type.put("User", User);
            start_type.put("task_map", task_map);

            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("导出 上游集团群组 失败 " + e.getMessage().toString());
            return "导出 上游集团群组 失败!!!";
        }

        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";


////        1.判断 是否查询到数据
//        if (outChannelArr.size() > 0) {
////
////                    //操作编号转化为汉字
////        for (Map m:outChannelArr) {
////            long l = Long.valueOf(String.valueOf(m.get("channel_id")));
////            for (SysDictData dict:channel_code){
////                Long dictSort = dict.getDictSort();
////                System.out.println(dictSort);
////
////                if (l == dictSort ){
////                    m.put("channel_id",dict.getDictLabel()).toString();
////                }
////            }
////        }
////        //结束转化
//
//
//            String Outcolumns[] = {"接入号", "iccid", "imsi", "激活时间", "开卡时间", "创建时间", "更新时间", "最近一次通道时间", "所属通道", "备注", "是否修改 0否 1是", "是否新增 0否 1是"};
//            String keys[] = {"msisdn", "iccid", "imsi", "activate_date", "open_date", "create_date", "update_date", "syn_Time", "channel_id", "remark", "inconsistent_iccid", "is_new"};
//            writeCSV.Write(newName, outChannelArr, Outcolumns, null, keys);

//            return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
//        } else {
//            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
//        }

    }
}
