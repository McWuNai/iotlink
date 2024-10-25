package com.yunze.system.service.impl.yunze.bulk;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.bulk.YzBulkBusinessDtailsMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.mapper.yunze.bulk.YzBulkBusinessMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.service.yunze.bulk.IYzBulkBusinessDtailsService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class YzBulkBusinessDtailsServiceImpl implements IYzBulkBusinessDtailsService {

    @Resource
    private YzBulkBusinessDtailsMapper yzBulkBusinessDtailsMapper;
    @Resource
    private YzBulkBusinessMapper yzBulkBusinessMapper;
    @Resource
    private SysDictDataMapper dictDataMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;


    @Override
    public Map<String, Object> getList(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzBulkBusinessDtailsMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzBulkBusinessDtailsMapper.selMap(map));
        return omp;
    }

    @Override
    public Map<String, Object> successArr(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzBulkBusinessDtailsMapper.publicCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzBulkBusinessDtailsMapper.successArr(map));
        return omp;
    }

    @Override
    public String exportallorders(Map<String, Object> map, SysUser User) {
        Object MapAgent_id = map.get("agent_id");
        //导出时 未选中 当前 企业编号时 且登录 部门不是 总平台 赋值部门
        if(MapAgent_id==null && User.getDeptId()!=100){
            List<String> agent_idArr = new ArrayList<String>();
            agent_idArr.add(""+User.getDeptId());
            map.put("agent_id",agent_idArr);
        }
        map.remove("pageNum");
        map.remove("pageSize");
        List<Map<String, Object>> outCardIccidArr = null;
        //权限过滤
        if(map.get("agent_id")!=null){
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if(!Different.Is_existence(agent_id,100)){
                List<String>  bIdArr =  yzBulkBusinessMapper.findIdArr(map);
                //map.put("bIdArr",bIdArr);
                if(bIdArr!=null && bIdArr.size()>0){
                    //判断 下载 bid 是否在其中
                    if(!Different.Is_existence(bIdArr,map.get("b_id").toString())){
                        return "导出任务编号数据不在您名下！导出任务取消！";
                    }
                }
            }
        }
        outCardIccidArr = yzBulkBusinessDtailsMapper.exportallorders(map);

        if(outCardIccidArr!=null && outCardIccidArr.size()>0){
            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            String newName = "";

            String  agent_id = User.getDept().getDeptId().toString();
            String task_name = "";

            String obj = map.get("type").toString().trim();

            switch (obj){
                case "success":
                    task_name="成功状态导出 [导出] ";
                    newName = UUID.randomUUID().toString().replace("-","")+"_ExportScs";
                break;
                case "fail":
                    task_name="失败状态导出 [导出] ";
                    newName = UUID.randomUUID().toString().replace("-","")+"_ExportFai";
                break;
                case "Pending":
                    task_name="待处理状态导出 [导出] ";
                    newName = UUID.randomUUID().toString().replace("-","")+"_ExportRend";
                break;
            }
            String SaveUrl = "/getcsv/"+newName+".csv";
            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "35");
            yzExecutionTaskMapper.add(task_map);//添加执行 任务表

            //获取字典信息
            List<SysDictData> state_id = dictDataMapper.selectDictDataByType("yz_bulk_businessDtails_state_id");//状态

            //导卡 路由队列
            String polling_queueName = "admin_ExportaBusiness_queue";
            String polling_routingKey = "admin.ExportaBusiness.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr",outCardIccidArr);
                start_type.put("map",map);
                start_type.put("state_id",state_id);

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            }catch (Exception e){
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return "状态查询导出 导入 操作失败！";
            }
        }else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }


}


















