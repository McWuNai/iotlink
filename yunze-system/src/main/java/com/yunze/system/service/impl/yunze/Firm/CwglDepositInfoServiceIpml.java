package com.yunze.system.service.impl.yunze.Firm;


import com.alibaba.fastjson.JSON;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.cwgl.CwglDepositInfoDetailsMapper;
import com.yunze.common.mapper.yunze.cwgl.CwglDepositInfoMapper;
import com.yunze.common.mapper.yunze.cwgl.Firm.YzCwglFirmAccountMapper;
import com.yunze.common.utils.yunze.Different;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.mapper.SysUserMapper;
import com.yunze.system.service.yunze.IYzUserService;
import com.yunze.system.service.yunze.cwgl.Firm.CwglDepositInfoService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Service
public class CwglDepositInfoServiceIpml implements CwglDepositInfoService {

    @Resource
    private CwglDepositInfoMapper cwglDepositInfoMapper;
    @Resource
    private CwglDepositInfoDetailsMapper cwglDepositInfoDetailsMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private SysUserMapper userMapper;
    @Resource
    private IYzUserService iYzUserService;
    @Resource
    private SysDictDataMapper dictDataMapper;
    @Resource
    private YzCwglFirmAccountMapper yzCwglFirmAccountMapper;



    @Override
    public Map<String, Object> sel_CT_data_DS(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = cwglDepositInfoMapper.sel_CT_data_Count_DS(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", cwglDepositInfoMapper.sel_CT_data_DS(map));
        return  omp;
    }

    @Override
    @Transactional
    public boolean add_CDIO(Map<String, Object> map) {
        boolean bool=false,
                upd_bool=false,
                add_bool=false;
        Map<String, Object> CDIO_map=(Map<String, Object>) map.get("CDIO_map");
        upd_bool=  cwglDepositInfoMapper.upd_CDIO(CDIO_map)>0;//修改入款记录信息

        cwglDepositInfoDetailsMapper.del_DIDS_all(map);//删除 入款记录详情

        List<Map<String, Object>> Ds_arr = (List<Map<String, Object>>) map.get("Ds_arr");
        if(Ds_arr.size()!=0) {

            //新增数据 新增
            map.put("Ds_arr", Ds_arr);//赋值需新增的数据

            //新增数据 新增
            map.put("Ds_arr", Ds_arr);//赋值需新增的数据
            add_bool= cwglDepositInfoDetailsMapper.add_DIDS(map)>0;//新增 入款记录详情

        }else {add_bool=true;}

        if(upd_bool==true && add_bool==true) {
            bool=true;
        }
        return bool;
    }

    @Override
    public boolean updateID(Map<String, Object> map) {
        return cwglDepositInfoMapper.updateID(map)>0;
    }

    @Override
    public List<Map<String,Object>> find_CDIDS(Map map) {
        return cwglDepositInfoDetailsMapper.find_CDIDS(map);
    }



    @Override
    public List<Map<String,Object>> sel_Year_data_CT(Map map) {
        return cwglDepositInfoMapper.sel_Year_data_CT(map);
    }

    @Override
    public Map<String, Object> sleDetails(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = cwglDepositInfoMapper.countSel(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", cwglDepositInfoMapper.sleDetails(map));
        return  omp;
    }

    @Override
    public List<Map<String, Object>> sel_Month_data_CT(Map map) {
        System.out.println(map);
        return cwglDepositInfoMapper.sel_Month_data_CT(map);
    }

    /** 入款信息导出 */
    @Override
    public String ExportRemittance(Map<String, Object> map, SysUser User) {
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
                List<String>  user_id =  iYzUserService.getUserID(map);
                map.put("user_id",user_id);
            }
        }
        //查询入款信息
        outCardIccidArr = cwglDepositInfoMapper.ExportIncome(map);

        if(outCardIccidArr!=null && outCardIccidArr.size()>0){
            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            String newName = UUID.randomUUID().toString().replace("-","")+"_ExportRemittance";

            String  agent_id = User.getDept().getDeptId().toString();
            String task_name ="入款信息导出 [导出] ";
            String SaveUrl = "/getcsv/"+newName+".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "16");

            //获取字典信息
            List<SysDictData> stateArr = dictDataMapper.selectDictDataByType("cwgl_Deposit_state");//入款状态

            //获取 用户信息

            List<Map<String, Object>> userArr = userMapper.find_simple();

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_ExportRemittance_queue";
            String polling_routingKey = "admin.ExportRemittance.queue";
            String polling_exchangeName = "admin_exchange";//路由

            try {
                //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr",outCardIccidArr);
                start_type.put("userArr",userArr);
                start_type.put("stateArr",stateArr);
                start_type.put("map",map);
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e){
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return "入款信息导出 导入 操作失败！";
            }
        }else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }


    /** 入款详情导出 */
    @Override
    public String ExportIncome(Map<String, Object> map, SysUser User) {
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
                List<String>  user_id =  iYzUserService.getUserID(map);
                map.put("user_id",user_id);
            }
        }
        //查询入款详情
        outCardIccidArr = cwglDepositInfoMapper.ExportRemittance(map);

        if(outCardIccidArr!=null && outCardIccidArr.size()>0){
            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            String newName = UUID.randomUUID().toString().replace("-","")+"_ExportIncome";

            String  agent_id = User.getDept().getDeptId().toString();
            String task_name ="入款详情导出 [导出] ";
            String SaveUrl = "/getcsv/"+newName+".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth",create_by);
            task_map.put("task_name",task_name);
            task_map.put("url",SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "17");

            //获取字典信息
            List<SysDictData> stateArr = dictDataMapper.selectDictDataByType("cwgl_Deposit_state");//入款状态
            List<SysDictData> FatNameArr = dictDataMapper.selectDictDataByType("Company_account");//公司账户 字典类型
            List<Map<String, Object>> sleName = yzCwglFirmAccountMapper.sleName();//查询 公司账户
            //获取 用户信息
            List<Map<String, Object>> userArr = userMapper.find_simple();

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_routingKey = "admin.ExportIncome.queue";
            String polling_exchangeName = "admin_exchange";//路由

            try {
                //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr",outCardIccidArr);
                start_type.put("userArr",userArr);
                start_type.put("stateArr",stateArr);
                start_type.put("FatNameArr",FatNameArr);
                start_type.put("sleName",sleName);
                start_type.put("map",map);
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e){
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return "入款详情导出 导入 操作失败！";
            }
        }else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }


    @Override
    public boolean add_DIDS(Map<String, Object> map) {
        return cwglDepositInfoDetailsMapper.add_DIDS(map)>0;
    }

    @Override
    public boolean del_DIDS_all(Map<String, Object> map) {
        return cwglDepositInfoDetailsMapper.del_DIDS_all(map)>0;
    }

}
