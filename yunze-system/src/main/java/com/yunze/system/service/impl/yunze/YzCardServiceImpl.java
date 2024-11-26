package com.yunze.system.service.impl.yunze;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yunze.apiCommon.upstreamAPI.DianXinCMP5G.Inquire.Query_DX5G;
import com.yunze.apiCommon.upstreamAPI.YiDongEC.Inquire.Query_YD;
import com.yunze.apiCommon.utils.Arith;
import com.yunze.apiCommon.utils.InternalApiRequest;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.SysDept;
import com.yunze.common.core.domain.entity.SysDictData;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.entity.YzCard;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.*;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.mapper.yunze.bulk.YzBulkBusinessMapper;
import com.yunze.common.mapper.yunze.bulk.YzSmSBulkBusinessMapper;
import com.yunze.common.mapper.yunze.card.YzCardApiOfferinginfolistMapper;
import com.yunze.common.mapper.yunze.card.YzCardUsageReminderMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.StringUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.*;
import com.yunze.system.mapper.SysDeptMapper;
import com.yunze.system.mapper.SysDictDataMapper;
import com.yunze.system.mapper.SysUserMapper;
import com.yunze.system.service.yunze.IYzCardService;
import com.yunze.system.service.yunze.IYzUserService;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 卡信息 业务实现类
 *
 * @author root
 */
@Service
public class YzCardServiceImpl implements IYzCardService {
    private static final Logger log = LoggerFactory.getLogger(YzCardServiceImpl.class);

    @Resource
    private YzCardMapper yzCardMapper;
    @Autowired
    private SysDeptMapper deptMapper;
    @Autowired
    private SysDictDataMapper dictDataMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private IYzUserService iYzUserService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisCache redisCache;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private YzCardApiOfferinginfolistMapper yzCardApiOfferinginfolistMapper;
    @Resource
    private YzCardUsageReminderMapper yzCardUsageReminderMapper;
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private InternalApiRequest internalApiRequest;
    @Resource
    private CardFlowSyn cardFlowSyn;
    @Resource
    private GetShowStatIdArr getShowStatIdArr;
    @Resource
    private YzBulkBusinessMapper yzBulkBusinessMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private YzSmSBulkBusinessMapper yzSmSBulkBusinessMapper;
    @Resource
    private YzCardPacketMapper packetMapper;
    @Resource
    private YzCardFlowMapper cardFlowMapper;

    @Override
    public Map<String, Object> selMap(Map<String, Object> map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;

        Map<String, Object> CountMap = new HashMap<>();
        CountMap.putAll(map);
        CountMap.remove("queryParams");
        boolean is_Internal = false;
        //System.out.println(CountMap.remove("queryParams"));
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (!Different.Is_existence(agent_id, 100)) {
                List<String> user_id = iYzUserService.getUserID(CountMap);
                map.put("user_id", user_id);
                CountMap.put("user_id", user_id);
            } else {
                is_Internal = true;//内部人员 部门是 100 的 可看字段增加
            }
        } else {
            is_Internal = true;//内部人员 部门是 100 的 可看字段增加
        }
        map.put("is_Internal", is_Internal);
        map = getChannelIdArr(map);


        CountMap.put("channel_id", map.get("channel_id"));
        PageUtil pu = null;
        List<Map<String, Object>> Rlist = null;
        boolean selLianTong = (boolean) map.get("selLianTong");

        Integer rowCount = null;
        if (selLianTong) {
            rowCount = yzCardMapper.selMapLianTongCount(CountMap);
        } else {
            rowCount = yzCardMapper.selMapCount(CountMap);
        }
      /*
        //同查询条件 缓存 查询总数 120 S
        Object isExecute = redisCache.getCacheObject(JSON.toJSONString(CountMap));
        if (isExecute == null) {
            if (selLianTong) {
                rowCount = yzCardMapper.selMapLianTongCount(CountMap);
            } else {
                rowCount = yzCardMapper.selMapCount(CountMap);
            }
            //redis 存储
            redisCache.setCacheObject(JSON.toJSONString(CountMap), rowCount, 120, TimeUnit.SECONDS);//120 秒缓存
        } else {
            rowCount = Integer.parseInt(isExecute.toString());
        }*/

        rowCount = rowCount != null ? rowCount : 0;
        pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        if (selLianTong) {
            Rlist = yzCardMapper.selMapLianTong(map);
        } else {
            Rlist = yzCardMapper.selMap(map);
        }
        //数据打包'
        //System.out.println(map);
        //System.out.println(yzCardMapper.selMap(map));
        omp.put("Pu", pu);
        omp.put("Data", Rlist);
        omp.put("Pmap", map);
        omp.put("is_Internal", is_Internal);
        return omp;

    }

    @Override
    public Map<String, Object> find(Map<String, Object> map) {

        Map<String, Object> findMap = yzCardMapper.find(map);
        if (findMap == null) {
            findMap = yzCardMapper.findNotRoute(map);
        }
        return findMap;
    }


    @Override
    public String uploadCard(MultipartFile file, boolean updateSupport, SysUser User) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String newName = UUID.randomUUID().toString().replace("-", "") + "_CardImport";
        String flieUrlRx = "/upload/uploadCard/";
        ReadName = flieUrlRx + ReadName;
        try {
            /**
             * 获取当前项目的工作路径
             */
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName );
            File Url = new File(filePath + flieUrlRx +"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);






            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_saveCard_queue";
            String polling_routingKey = "admin.saveCard.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("newName", newName);//输出文件名
                start_type.put("User", User);//登录用户信息
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return ("物联卡管理 导入 操作失败！");
            }


            /*String path = filePath  + ReadName;
            ExcelConfig excelConfig = new ExcelConfig();
            String columns[] = {"msisdn","iccid","imsi","open_date","activate_date","agent_id","channel_id","is_pool","batch_date","remarks","status_id","package_id","imei","type","network_type","is_sms","sms_number","gprs","user_id"};
            String maxVid  = yzCardMapper.findMaxVid();
            maxVid = maxVid!=null?maxVid:"16800000000";
            Long maxVidInt = Long.parseLong(maxVid);
            List<Map<String, String>> list = excelConfig.getExcelListMap(path,columns,maxVidInt);
            String  create_by = " [ "+User.getDept().getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";



            if(list!=null && list.size()>0){
                //筛选出  vid msisdn iccid imsi 的 卡号 重复项
                HashMap<String, Object> map = new HashMap<>();
                map.put("card_arrs",list);
                map.put("type","3");
                List<String>  iccidarr = yzCardMapper.isExistence(map);

                String SaveUrl = "/getcsv/"+newName+".csv";
                String task_name ="连接管理上传 [导入] ";
                Map<String, Object> task_map = new HashMap<String, Object>();
                task_map.put("auth",create_by);
                task_map.put("task_name",task_name);
                task_map.put("url",SaveUrl);
                task_map.put("agent_id", User.getDept().getDeptId());

                executionTaskMapper.add(task_map);//添加执行 任务表

                //1.判断上传数据与数据库现有数据做对比 大于 0 证明有 以存在数据
                if(iccidarr.size()>0){
                    //上传数据>数据库查询 赛选出
                    List<String> list1 = new ArrayList<>();
                    for (int i = 0; i <list.size() ; i++) {
                        list1.add(list.get(i).get("iccid"));
                    }
                    //找出与数据库已存在 相同 ICCID 去除 重复 iccid
                    List<Map<String, String>> Out_list_Different = Different.getIdentical(list1,iccidarr,"iccid");
                    if(Out_list_Different.size()>0){
                        OutCSV(Out_list_Different,newName,"ICCID重复导入失败！",create_by,"导入失败");
                    }

                    list = Different.delIdentical(list,iccidarr,"iccid");//删除相同的数据 进行批量上传
                }
                map.put("card_arrs",list);//更新 list
                map.put("create_by",create_by);
                try {
                    if(list.size()>0){
                        int  sInt = yzCardMapper.importCard(map);
                        if(sInt>0){
                            OutCSV(list,newName,"成功导入卡列表数据 ["+sInt+"] 条",create_by,"导入成功");
                            executionTaskMapper.set_end_time(task_map);//任务结束
                        }
                    }else{
                        executionTaskMapper.set_end_time(task_map);//任务结束
                        return " 上传数据已全部在数据库，无需上传卡信息！ ";
                    }
                }catch (DuplicateKeyException e){
                    System.out.println("===="+e.getCause().toString());
                    String[] solit=e.getCause().toString().split("'");
                    OutCSV(list,newName,e.getCause().toString(),create_by,"导入失败");
                    executionTaskMapper.set_end_time(task_map);//任务结束
                    return "上传excel异常 [插入数据 DuplicateKeyException ] "+e.getCause().toString() ;
                }catch (Exception e){
                    System.out.println("===="+e.getCause().toString());
                    OutCSV(list,newName,e.getCause().toString(),create_by,"导入失败");
                    executionTaskMapper.set_end_time(task_map);//任务结束
                    return "上传excel异常 [插入数据 Exception] "+e.getCause().toString() ;
                }

            }else{
                return "上传表格数据不能为空！";
            }*/
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }

        return "物联卡管理 导入指令 已发送，详细信息请在 【执行日志管理】查询！";
    }

    @Override
    public Map<String, Object> findRoute(Map<String, Object> map) {
        return yzCardMapper.findRoute(map);
    }

    @Override
    public List<Map<String, Object>> getDeptName() {
        return deptMapper.getDeptName();
    }

    @Override
    public String exportData(Map<String, Object> map, SysUser User) {
        Object MapAgent_id = map.get("agent_id");
        //导出时 未选中 当前 企业编号时 且登录 部门不是 总平台 赋值部门
        if (MapAgent_id == null && User.getDeptId() != 100) {
            List<String> agent_idArr = new ArrayList<String>();
            agent_idArr.add("" + User.getDeptId());
            map.put("agent_id", agent_idArr);
        }
        map.remove("pageNum");
        map.remove("pageSize");
        map = getChannelIdArr(map);
        List<String> outCardIccidArr = null;
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (!Different.Is_existence(agent_id, 100)) {
                List<String> user_id = iYzUserService.getUserID(map);
                map.put("user_id", user_id);
            }
        }
        map = getChannelIdArr(map);
        boolean selLianTong = (boolean) map.get("selLianTong");
        if (selLianTong) {
            outCardIccidArr = yzCardMapper.outCardIccidLianTong(map);
        } else {
            outCardIccidArr = yzCardMapper.outCardIccid(map);
        }
        if (outCardIccidArr != null && outCardIccidArr.size() > 0) {
            String create_by = " [ " + User.getDept().getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
            String newName = UUID.randomUUID().toString().replace("-", "") + "_CardOut";

            String agent_id = User.getDept().getDeptId().toString();
            String task_name = "-物联卡管理 [导出] ";
            String SaveUrl = "/getcsv/" + newName + ".csv";

            Map<String, Object> task_map = new HashMap<String, Object>();
            task_map.put("auth", create_by);
            task_map.put("task_name", task_name);
            task_map.put("url", SaveUrl);
            task_map.put("agent_id", agent_id);
            task_map.put("type", "1");

            //获取字典信息
            List<SysDictData> stateOptions = dictDataMapper.selectDictDataByType("yunze_card_status_ShowId");//卡状态
            List<SysDictData> card_types = dictDataMapper.selectDictDataByType("yunze_card_card_type");//卡类型
            List<SysDictData> customize_whether = dictDataMapper.selectDictDataByType("yunze_customize_whether");//系统是否
            List<SysDictData> cardConnection_type = dictDataMapper.selectDictDataByType("yz_cardConnection_type");//断开网状态
            //获取 用户信息
            List<Map<String, Object>> userArr = userMapper.find_simple();


            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_OutCard_queue";
            String polling_routingKey = "admin_OutCard_queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("newName", newName);//输出文件名
                start_type.put("task_map", task_map);//
                start_type.put("create_by", create_by);//
                start_type.put("User", User);
                start_type.put("outCardIccidArr", outCardIccidArr);
                start_type.put("userArr", userArr);
                start_type.put("stateOptions", stateOptions);
                start_type.put("card_types", card_types);
                start_type.put("customize_whether", customize_whether);
                start_type.put("cardConnection_type", cardConnection_type);
                start_type.put("map", map);

                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return "物联卡管理 导入 操作失败！";
            }
        } else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
        return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
    }

    @Override
    public List<String> outCardIccid(Map<String, Object> map) {
        Map<String, Object> CountMap = new HashMap<>();
        CountMap.putAll(map);
        CountMap.remove("pageNum");
        CountMap.remove("pageSize");
        //权限过滤
        List<String> user_id = iYzUserService.getUserID(CountMap);
        map.put("user_id", user_id);
        return yzCardMapper.outCardIccid(map);
    }


    @Override
    public List<String> selId(Map<String, Object> map, boolean selLianTong) {
        Map<String, Object> CountMap = new HashMap<>();
        CountMap.putAll(map);
        CountMap.remove("pageNum");
        CountMap.remove("pageSize");
        //权限过滤
        List<String> user_id = iYzUserService.getUserID(CountMap);
        map.put("user_id", user_id);

        if (selLianTong) {
            return yzCardMapper.selIdLianTong(map);
        } else {
            return yzCardMapper.selId(map);
        }
    }

    @Override
    public boolean updStatusId(Map<String, Object> map) {
        return yzCardMapper.updStatusId(map) > 0;
    }


    @Override
    public String dividCard(Map<String, Object> map) {
        String Message = "";
        map.remove("pageNum");
        map.remove("pageSize");
        map = getChannelIdArr(map);
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (!Different.Is_existence(agent_id, 100)) {
                List<String> user_id = iYzUserService.getUserID(map);
                map.put("user_id", user_id);
            }
        }
        boolean selLianTong = (boolean) map.get("selLianTong");
        List<String> dividIdArr = selId(map, selLianTong);

        if (dividIdArr != null && dividIdArr.size() > 0) {
            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_DistributeCard_queue";
            String polling_routingKey = "admin.DistributeCard.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.putAll(map);
                start_type.put("type", "DistributeCard");//启动类型
                start_type.put("dividIdArr", dividIdArr);//需要划分的数据
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("划卡 失败 " + e.getMessage().toString());
                return ("物联卡管理 划卡 操作失败！");
            }
            Message = "当前筛选条件下需要划分的数据 [ " + dividIdArr.size() + " ] 条 至 [ " + map.get("set_dept_name") + " ] [ " + map.get("set_user_name") + " ] 指令已下发详情查看 【执行日志管理】 ！";
        } else {
            Message = "当前筛选条件下未找到需要划分的数据！请核对后重试！";
        }
        return Message;
    }


    /**
     * 获取 运营商类型 的通道id
     *
     * @param map
     * @return
     */
    private Map<String, Object> getChannelIdArr(Map<String, Object> map) {
        //判断是否选择 运营商类型
        map.put("selLianTong", false);

        if (map.get("cd_operator_type") != null) {
            List<String> cd_operator_type = (List<String>) map.get("cd_operator_type");
            if ( cd_operator_type.size() > 0) {
                List<Map<String, Object>> smap = yzCardRouteMapper.find_simpleOperatorArr(map);
                //添加 【符合 运营类型】 的 通道 查询条件
                List<String> channel_id = new ArrayList<String>();

                if(map.get("channel_id") != null){
                    List<String> Channel = (List<String>) map.get("channel_id");
                    channel_id.addAll(Channel);
                }

                if (smap != null && smap.size() > 0) {
                    for (int i = 0; i < smap.size(); i++) {
                        channel_id.add(smap.get(i).get("cd_id").toString());
                    }
                }
                //未找到相匹配的通道时 将 通道id传参 -1 使查询不到相对应数据
                if (channel_id.size() == 0) {
                    channel_id.add("-1");
                }else {
                    map.put("channel_id", channel_id);
                }
                //如果是联通的查询 条件 且 条件是 卡号 查询长度大于19 或 起止 条件 是 iccid
                boolean LianTong = false;
                for (int i = 0; i < cd_operator_type.size(); i++) {
                    if(cd_operator_type.get(i).equals("2")){
                        LianTong = true;
                    }
                }
                if (LianTong) {
                    Object type = map.get("type");
                    Object value = map.get("value");
                    if (value != null && value.toString().length() > 0 && type != null) {
                        map.put("selLianTong", true);
                    }
                    Object StartAndEndtype = map.get("StartAndEndtype");
                    Object StartValue = map.get("StartValue");
                    Object EndValue = map.get("EndValue");
                    if (StartAndEndtype != null && StartAndEndtype.equals("3") && StartValue != null && EndValue != null) {
                        map.put("selLianTong", true);
                    }
                    List<Map<String, Object>> UpArr = (List<Map<String, Object>>) map.get("UpArr");//导入查询
                    if (UpArr != null && UpArr.size()>0) {
                        map.put("selLianTong", true);
                    }
                }
            }
        }
        return map;
    }


    @Override
    public String importSet(MultipartFile file, Map<String, Object> map) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/upload/importSet/";
        ReadName = flieUrlRx + ReadName;
        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx +"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_CardImportSet_queue", addOrder_routingKey = "admin.CardImportSet.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                //rabbitMQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("连接设置 生产指令  失败 " + e.getMessage().toString());
                return ("连接设置 生产指令 操作失败！");
            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "连接设置 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }


    @Override
    public String importSelImei(MultipartFile file, Map<String, Object> map) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/upload/importSelImei/";
        ReadName = flieUrlRx + ReadName;
        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath +   ReadName);
            File Url = new File(filePath + flieUrlRx +"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);


            String agent_id = map.get("agent_id").toString();
            //新增批量执行表
            Map<String, Object> bulkMap = new HashMap<>();
            String task_name = "【查询IMEI】";
            String code = VeDate.getNo(8);
            SysUser User =   (SysUser)map.get("User");//登录用户信息
            SysDept Dept =User.getDept();
            String  create_by = " [ "+Dept.getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            bulkMap.put("code", code);
            bulkMap.put("task_name",task_name);
            bulkMap.put("auth",create_by);
            bulkMap.put("agent_id",agent_id);
            bulkMap.put("type","9");//查询IMEI 9
            yzBulkBusinessMapper.add(bulkMap);




            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_CardImportSelImei_queue", addOrder_routingKey = "admin.CardImportSelImei.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                //rabbitMQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("bulkMap", bulkMap);//批量任务主表 信息
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("特殊操作查询IMEI 生产指令  失败 " + e.getMessage().toString());
                return ("特殊操作查询IMEI 生产指令 操作失败！");
            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "特殊操作查询IMEI 指令 已发送，连接设置详细信息请在 【批量业务受理】查询！";
    }

    @Override
    public String status(MultipartFile file, Map<String, Object> map) throws IOException {
            String filename = file.getOriginalFilename();
            String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
            String flieUrlRx = "/upload/importSelImei/";
            ReadName = flieUrlRx + ReadName;
            try {
                // 获取当前项目的工作路径
                File file2 = new File("");
                String filePath = file2.getCanonicalPath();
                File newFile = new File(filePath + ReadName);
                File Url = new File(filePath + flieUrlRx + "/1.txt");//tomcat 生成路径
                Upload.mkdirsmy(Url);
                file.transferTo(newFile);

                String agent_id = map.get("agent_id").toString();




                String type = "";
                String task_name = "";
                String Is_remind_ratio = map.get("Is_remind_ratio").toString();

                String Switch_network = map.get("Switch_network").toString();

                if (Is_remind_ratio.equals("3")) {//批量复机 1
                    type = "1";
                    task_name = "【批量复机】";
                }else if (Is_remind_ratio.equals("2")) {//批量停机 2
                    type = "2";
                    task_name = "【批量停机】";
                }else if (Switch_network.equals("3") ) {//批量开网 4
                    type = "4";
                    task_name = "【批量开网】";
                }else if (Switch_network.equals("2")) {//批量断网 3
                    type = "3";
                    task_name = "【批量断网】";
                }


                //新增批量执行表
                Map<String, Object> bulkMap = new HashMap<>();

                String code = VeDate.getNo(8);
                SysUser User =   (SysUser)map.get("User");//登录用户信息
                SysDept Dept =User.getDept();
                String  create_by = " [ "+Dept.getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
                bulkMap.put("code", code);
                bulkMap.put("task_name",task_name);
                bulkMap.put("auth",create_by);
                bulkMap.put("agent_id",agent_id);
                bulkMap.put("type",type);
                yzBulkBusinessMapper.add(bulkMap);



                //1.创建路由 绑定 生产队列 发送消息
                String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_CardImportBatch_queue", addOrder_routingKey = "admin.CardImportBatch.queue",
                        addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
                try {
                    // rabbitMQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                    Map<String, Object> start_type = new HashMap<>();
                    start_type.put("filePath", filePath);//项目根目录
                    start_type.put("ReadName", ReadName);//上传新文件名
                    start_type.put("bulkMap", bulkMap);//批量任务主表 信息
                    start_type.put("map", map);//参数
                    rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                        // 设置消息过期时间 60 分钟 过期
                        message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                        return message;
                    });
                } catch (Exception e) {
                    System.out.println("批量停复机、断开网 生产指令  失败 " + e.getMessage().toString());
                    return ("批量停复机、断开网 生产指令 操作失败！");
                }
            } catch (Exception e) {
                System.out.println(e);
                return "上传excel异常";
            }
            return "批量停复机、断开网 指令 已发送，连接设置详细信息请在 【批量业务受理】查询！";
    }


    @Override
    public Map<String, Object> CardNumberImport(MultipartFile file, Map<String, Object> map) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/upload/cardNumber/";
        ReadName = flieUrlRx + ReadName;
        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx +"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            ExcelConfig excelConfig = new ExcelConfig();
            String columns[] = {"cardNumber"};
            List<Map<String, Object>> list = excelConfig.getExcelListMap(filePath +  ReadName, columns);
            //System.out.println(list.toString());
            //System.out.println(list);
            map.put("UpArr", list);
        } catch (Exception e) {
            System.out.println(e);
        }
        return selMap(map);
    }


    @Override
    public String importSetCardInfo(MultipartFile file, Map<String, Object> map) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/upload/importSetCardInfo/";
        ReadName = flieUrlRx + ReadName;
        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath+ ReadName);
            File Url = new File(filePath + flieUrlRx+"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_importSetCardInfo_queue", addOrder_routingKey = "admin.importSetCardInfo.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                // rabbitMQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("特殊操作变更卡分组、备注 生产指令  失败 " + e.getMessage().toString());
                return ("特殊操作变更卡分组、备注 生产指令 操作失败！");
            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "特殊操作变更卡分组、备注 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    @Override
    public List<String> getCardGrouping(Map<String, Object> map) {
        //查询所属下 分组
        if (map.get("agent_id") != null) {
            map.put("agent_id", yzCardMapper.queryChildrenAreaInfo(map));
        }

        return yzCardMapper.getCardGrouping(map);
    }

    @Override
    public boolean updActivate(Map<String, Object> map) {
        return yzCardMapper.updActivate(map) > 0;
    }


    @Override
    public boolean UpdateFill(Map<String, Object> map) {
        return yzCardMapper.UpdateFill(map) > 0;
    }

    /***停机*/
    @Override
    public String stoppedarr(Map<String, Object> map) {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_Stopped_queue", addOrder_routingKey = "admin.Stopped.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【停机】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【停机】 生产指令 操作失败！");
            }

            return "批量 【停机】 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    /***复机*/
    @Override
    public String machinearr(Map<String, Object> map) {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_Machine_queue", addOrder_routingKey = "admin.Machine.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【复机】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【复机】 生产指令 操作失败！");
            }

            return "批量 【复机】 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    /***断网*/
    @Override
    public String disconnectNetworkarr(Map<String, Object> map) {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_DisconnectNetwork_queue", addOrder_routingKey = "admin.DisconnectNetwork.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【断网】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【断网】 生产指令 操作失败！");
            }

            return "批量 【断网】 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    /***开网*/
    @Override
    public String openNetworkarr(Map<String, Object> map) {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_OpenNetwork_queue", addOrder_routingKey = "admin.OpenNetwork.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【开网】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【开网】 生产指令 操作失败！");
            }

            return "批量 【开网】 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    /**
     * 批量同步用量
     */
    @Override
    public String consumptionarr(Map<String, Object> map) {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_Consumption_queue", addOrder_routingKey = "admin.Consumption.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【同步用量】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【同步用量】 生产指令 操作失败！");
            }

            return "批量 【同步用量】 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    /**
     * 批量同步状态
     */
    @Override
    public String publicmethodarr(Map<String, Object> map) {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_PublicMethod_queue", addOrder_routingKey = "admin.PublicMethod.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【同步状态】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【同步状态】 生产指令 操作失败！");
            }

            return "批量 【同步状态】 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    /**
     * 批量 【同步状态和用量】
     */
    @Override
    public String consumptionandstatearr(Map<String, Object> map) {
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_ConsumptionAndState_queue", addOrder_routingKey = "admin.ConsumptionAndState.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【同步状态和用量】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【同步状态和用量】 生产指令 操作失败！");
            }

            return "批量 【同步状态和用量】 指令 已发送，连接设置详细信息请在 【执行日志管理】查询！";
    }

    @Override
    public Map<String, Object> getIccid(Map<String, Object> map) {
        Map<String, Object> Rmap = null;
        List<Map<String, Object>> selVidArr = yzCardMapper.selVid(map);
        if (selVidArr != null && selVidArr.size() > 0) {
            Rmap = selVidArr.get(0);
            boolean is_Internal = false;
            //权限过滤
            if (map.get("agent_id") != null) {
                List<Integer> agent_id = (List<Integer>) map.get("agent_id");
                if (!Different.Is_existence(agent_id, 100)) {
                } else {
                    is_Internal = true;//内部人员 部门是 100 的 可看字段增加
                }
            } else {
                is_Internal = true;//内部人员 部门是 100 的 可看字段增加
            }
            Rmap.put("is_Internal", is_Internal);
        }
        return Rmap;
    }

    @Override
    public String cancelrealname(MultipartFile file, Map<String, Object> map) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/upload/cancelrealname/";
        ReadName = flieUrlRx +ReadName;

        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx+"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);

            //新增批量执行表
            String agent_id = map.get("agent_id").toString();
            Map<String, Object> bulkMap = new HashMap<>();
            String task_name = "【取消实名】";
            String code = VeDate.getNo(8);
            SysUser User =   (SysUser)map.get("User");//登录用户信息
            SysDept Dept =User.getDept();
            String  create_by = " [ "+Dept.getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            bulkMap.put("code", code);
            bulkMap.put("task_name",task_name);
            bulkMap.put("auth",create_by);
            bulkMap.put("agent_id",agent_id);
            bulkMap.put("type","8");//取消实名 8
            yzBulkBusinessMapper.add(bulkMap);

            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_CardCancelrealname_queue", addOrder_routingKey = "admin.CardCancelrealname.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                // rabbitMQConfig.creatExchangeQueue(addOrder_exchangeName, addOrder_queueName, addOrder_routingKey, addOrder_del_exchangeName, addOrder_del_queueName, addOrder_del_routingKey, null);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("bulkMap", bulkMap);//批量任务主表 信息
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量取消实名 生产指令  失败 " + e.getMessage().toString());
                return ("批量取消实名 生产指令 操作失败！");
            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "批量取消实名 指令 已发送，连接设置详细信息请在 【批量业务受理】查询！";

    }

    @Override
    public Map<String, Object> cardMatch(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<>();
        Map<String, Object> Pmap = new HashMap<>();
        Map<String, Object> cardMatchMap = null;
        String cardNo = map.get("cardNo").toString();
        String PcardNo = map.get("cardNo").toString();

        Integer cardMatchCount = 0;
        Pmap.put("selType", map.get("selType"));

        int frequency = 0;
        while (true) {

            Pmap.put("cardNo", PcardNo);
            cardMatchCount = yzCardMapper.cardMatchCount(Pmap);
            cardMatchCount = cardMatchCount != null ? cardMatchCount : 0;
            if (cardMatchCount > 0) {
                cardMatchMap = yzCardMapper.cardMatchOne(Pmap);
                break;
            } else {
                PcardNo = PcardNo.substring(0, PcardNo.length() - 1);
            }
            frequency += 1;
            if (frequency == 5) {
                break;
            }
        }
        boolean is_Internal = false;
        //权限过滤
        if (map.get("agent_id") != null) {
            List<Integer> agent_id = (List<Integer>) map.get("agent_id");
            if (!Different.Is_existence(agent_id, 100)) {
            } else {
                is_Internal = true;//内部人员 部门是 100 的 可看字段增加
            }
        } else {
            is_Internal = true;//内部人员 部门是 100 的 可看字段增加
        }
        Rmap.put("is_Internal", is_Internal);


        Rmap.put("cardCount", cardMatchCount);
        Rmap.put("cardMatchMap", cardMatchMap);
        Rmap.put("cardPrefix", PcardNo);
        Rmap.put("cardSuffix", cardNo.substring(cardNo.length() - frequency));
        return Rmap;

    }


    @Override
    public String importCardReplace(MultipartFile file, Map<String, Object> map) {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
       String flieUrlRx = "/upload/importCardReplace/";
        ReadName =  flieUrlRx +ReadName;
        SysUser User =   (SysUser)map.get("User");//登录用户信息
        SysDept Dept =User.getDept();
        String  create_by = " [ "+Dept.getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
        String task_name = "特殊操作 [批量变更卡信息] ";
        String newName = UUID.randomUUID().toString().replace("-","")+"_CardInfoReplace";
        String UpdBackupName = UUID.randomUUID().toString().replace("-","")+"__CardInfoReplaceBackup";//设置分组备注前信息备份名称

        String SaveUrl = "/getcsv/"+newName+".csv";
        SaveUrl += ",/getcsv/"+UpdBackupName+".csv";

        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth",create_by);
        task_map.put("task_name",task_name);
        task_map.put("url",SaveUrl);
        task_map.put("agent_id",User.getDeptId() );
        task_map.put("type", "15");
        yzExecutionTaskMapper.add(task_map);//添加执行 任务表


        try {
            // 获取当前项目的工作路径
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath +   ReadName);
            File Url = new File(filePath + flieUrlRx +"/1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_CardImportReplace_queue", addOrder_routingKey = "admin.CardImportReplace.queue";
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("map", map);//参数
                start_type.put("task_map", task_map);//参数
                start_type.put("newName", newName);//参数
                start_type.put("UpdBackupName", UpdBackupName);//参数


                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量更新卡信息 生产指令  失败 " + e.getMessage().toString());
                return ("批量更新卡信息 生产指令 操作失败！");
            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }
        return "批量更新卡信息 指令 已发送，更新卡信息 详细信息请在 【执行日志管理】查询！";
    }

    @Override
    public String ChangeF(Map<String, Object> map, SysUser User) {
            String agent_id = ""+User.getDeptId();

            //新增批量执行表
            Map<String, Object> bulkMap = new HashMap<>();
            String task_name = "勾选 -【灵活变更状态】";
            String code = VeDate.getNo(8);
            SysDept Dept = User.getDept();
            String  create_by = " [ "+Dept.getDeptName()+" ] - "+" [ "+User.getUserName()+" ] ";
            bulkMap.put("code", code);
            bulkMap.put("task_name",task_name);
            bulkMap.put("auth",create_by);
            bulkMap.put("agent_id",agent_id);
            bulkMap.put("type","10");//灵活变更状态 10
            yzBulkBusinessMapper.add(bulkMap);


            //1.创建路由 绑定 生产队列 发送消息
            String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_fileFlexible_queue", addOrder_routingKey = "admin.fileFlexible.queue",
                    addOrder_del_exchangeName = "dlx_" + addOrder_exchangeName, addOrder_del_queueName = "dlx_" + addOrder_queueName, addOrder_del_routingKey = "dlx_" + addOrder_routingKey;
            try {
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("bulkMap", bulkMap);//批量任务主表 信息
                start_type.put("map", map);//参数
                rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 60 分钟 过期
                    message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                    return message;
                });
            } catch (Exception e) {
                System.out.println("批量 【灵活变更状态】 生产指令  失败 " + e.getMessage().toString());
                return ("批量 【灵活变更状态】 生产指令 操作失败！");
            }

            return "批量 【灵活变更状态】 指令 已发送，连接设置详细信息请在 【批量业务受理】查询！";
    }

    @Override
    public Map<String, Object> selCardOpen(String ip, Map<String, Object> map) {
        Map<String, Object> retMap = new HashMap<>();
        List<Map<String, Object>> rList = new ArrayList<>();
        List<String> dList = new ArrayList<>();
        boolean bool = false;
        String Message = "操作取消";

        Integer mCount = 0;
        Integer openSelCardMaxInt = 0;
        Object  openSelCardMax = redisCache.getCacheObject("openSelCard.max");
        if(openSelCardMax!=null && openSelCardMax.toString().length()>0){
            openSelCardMaxInt = Integer.parseInt(openSelCardMax.toString());
        }else{
            Map<String, Object> openSelCardMap = new HashMap<>();
            openSelCardMap.put("config_key","openSelCard.max");
            String openSelCard_str = yzWxByProductAgentMapper.findConfig(openSelCardMap);
            openSelCard_str = openSelCard_str!=null&&openSelCard_str.length()>0?openSelCard_str:"5";//默认60秒五次
            openSelCardMaxInt = Integer.parseInt(openSelCard_str);
            redisCache.setCacheObject("openSelCard.max",openSelCard_str,1,TimeUnit.HOURS);//1 小时 缓存
        }


        Object isExecute = redisCache.getCacheObject(ip);

        if (isExecute != null) {
            mCount = Integer.parseInt(isExecute.toString());
        }

        String rKey = "maxTestPeriodDays";
        Object  testPeriodDaysIsExecute = redisCache.getCacheObject(rKey);
        String maxTestPeriodDays = "180";//默认 180 天
        if(testPeriodDaysIsExecute==null){
            HashMap<String, Object> configMap = new HashMap<>();
            configMap.put("config_key", rKey);
            maxTestPeriodDays =  yzWxByProductAgentMapper.findConfig(configMap);// 最大测试期天数
            redisCache.setCacheObject(rKey, maxTestPeriodDays, 3*60*60, TimeUnit.SECONDS);// 3 小时 【缓存 系统参数】
        }else{
            maxTestPeriodDays = redisCache.getCacheObject(rKey).toString();
        }

        boolean flag = isClose(ip,"selCardOpen",openSelCardMaxInt);
        if(!flag){
            List<String> pList = (List<String>) map.get("cardArr");
            for (int i = 0; i < pList.size(); i++) {
                String cardNo = pList.get(i);
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("value",cardNo);
                Map<String, Object> rMap = yzCardMapper.selCardOpen(pMap);
                if(rMap!=null && rMap.get("iccid")!=null){
                    String dict_label = rMap.get("dict_label").toString();//卡状态
                    String MaxActivate_date = "";
                    if(dict_label.equals("可测试")){//判断是否有开卡日期 推演 开卡日期
                        if(rMap.get("open_date")!=null && rMap.get("open_date").toString().length()>0){
                            String open_date = rMap.get("open_date").toString();//开卡日期
                             MaxActivate_date = VeDate.getBeforeAfterDate(open_date,Integer.parseInt(maxTestPeriodDays));
                        }else{//卡状态 ‘可测试’ 但是 没有开卡日期的 发送获取开卡日期指令
                            Map<String, Object> sendMap = new HashMap<>();
                            sendMap.put("iccid",rMap.get("iccid"));
                            sendMap.put("opType","open");
                            sendGetOpenDate(sendMap);
                        }
                    }
                    rMap.put("MaxActivate_date",MaxActivate_date);
                    rList.add(rMap);
                }else{
                    dList.add(cardNo);
                }
            }
            bool = true;
        }else {
            Message = "您的查询的频次过于频繁请稍后重试！";
        }
        retMap.put("bool",bool);
        retMap.put("Message",Message);
        retMap.put("rList",rList);
        retMap.put("dList",dList);
        return retMap;
    }

    public String addAutoPolling(HashMap<String, Object> parammap) {
        try{
            String iccid = yzCardMapper.selCardIsInAuto(parammap);
            if (iccid == null) {
                yzCardMapper.addAutoPolling(parammap);
                return "添加成功!";
            }else{
                return "卡已添加至自动轮询!请勿重复操作!";
            }


        }catch (Exception e){
            return "添加失败!:"+e.getMessage();
        }


    }

    public boolean isClose(String ip, String method, int times) {
        String key = ip + "_api" + method;
        Long count = redisCache.increment(key, 1);
        if (count == 1) {
            redisCache.expire(key, 60, TimeUnit.SECONDS);
        }
        if (count > times) {
            return true;
        }
        return false;
    }

    @Override
    public List<Map<String,Object>> getListUsage(Map map) {
        return yzCardApiOfferinginfolistMapper.apiIdList(map);
    }

    @Override
    public List<Map<String, Object>> getListReminder(Map map) {
        return yzCardUsageReminderMapper.reminderId(map);
    }

    @Override
    public String CardInfoFlow(Map map) {
            try {
                Map<String, Object> Route = yzCardMapper.findRoute(map);
                if (Route != null) {
                    String cd_status = Route.get("cd_status").toString();
                    if (cd_status != null && cd_status != "" && cd_status.equals("1")) {
                        Map<String, Object> Rmap = internalApiRequest.queryFlow(map, Route);
                        String code = Rmap.get("code") != null ? Rmap.get("code").toString() : "500";
                        if (code.equals("200")) {
                            //获取 卡用量 开卡日期 更新 card info
                            if (Rmap.get("Use") != null && Rmap.get("Use") != "" && Rmap.get("Use").toString().trim().length() > 0) {
                                Double Use = Double.parseDouble(Rmap.get("Use").toString());
                                if (Use >= 0) {
                                    try {
                                        cardFlowSyn.CalculationFlow(map.get("iccid").toString(), Use);
                                    } catch (Exception e) {
                                        return ("用量内部计算错误！" + e.getMessage().toString());
                                    }
                                } else {
                                    return ("接口超频返回暂无数据返回，请稍后重试！");
                                }
                            }
                        } else {
                            return ("网络繁忙稍后重试！" + Rmap.get("Message").toString());
                        }
                        Map<String, Object> newRmap = internalApiRequest.queryCardStatus(map, Route);
                        String newcode = newRmap.get("code") != null ? newRmap.get("code").toString() : "500";
                        if (newcode.equals("200")) {
                            //获取 卡状态 开卡日期 更新 card info
                            if (newRmap.get("statusCode") != null && newRmap.get("statusCode") != "" && newRmap.get("statusCode").toString().trim().length() > 0) {
                                String statusCode = newRmap.get("statusCode").toString().trim();
                                if (!statusCode.equals("0")) {
                                    Map<String, Object> Upd_Map = new HashMap<>();
                                    Upd_Map.put("status_id", statusCode);
                                    Upd_Map.put("status_ShowId", getShowStatIdArr.GetShowStatId(statusCode));
                                    Upd_Map.put("iccid", map.get("iccid").toString());
                                    try {
                                        yzCardMapper.updStatusId(Upd_Map);//变更卡状态
                                    } catch (Exception e) {
                                        return ("DB保存状态操作失败！" + e.getMessage().toString());
                                    }
                                } else {
                                    return ("接口超频返回暂无数据返回，请稍后重试！");
                                }
                            }
                        } else {
                            return ("网络繁忙稍后重试！" + newRmap.get("Message").toString());
                        }
                        return "同步用量和状态 操作成功";
                    } else {
                        String statusVal = cd_status.equals("2") ? "已停用" : cd_status.equals("3") ? "已删除" : "状态未知";
                        return ("同步用量 操作失败！" + " 通道 [" + statusVal + "]");
                    }
                } else {
                    return (" iccid [" + map.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！");
                }
            } catch (Exception e) {
                String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
                System.out.println("<br/> yunze:card:SynFlow  " + " <br/> ip =  " + ip + " <br/> " + e.getCause().toString());
            }
            return ("单卡同步用量和状态 操作失败！");
    }




    @Override
    public boolean UpdateSingle(Map<String, Object> map) {
        return yzCardMapper.UpdateSingle(map) > 0;
    }



    @Override
    public Map<String, Object> singleState(Map<String, Object> map) {
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
                    message = "网络繁忙稍后重试！" ;
                }
            } else {
                message = "未知状态！";
            }
        } else {
            message = " iccid [" + map.get("iccid") + "] 未划分 API通道 ！请划分通道后重试！";
        }
        rMap.put("bool",bool);
        rMap.put("message",message);
        return rMap;
    }

    @Override
    public List<Map<String, Object>> getOrderCard(Map map) {
        return yzOrderMapper.getOrderCard(map);
    }

    @Override
    public String smsCC(MultipartFile file, Map<String, Object> map) {

        return "批量-短信下发 指令 已发送，详细信息请在【短信业务受理】查询！";

    }

    @Override
    public AjaxResult distinguishCardType(String query) {
        //区分运营商类型 移动、电信
        JSONObject object1 = JSON.parseObject(query);
        List<String> objects = new ArrayList<>();
        Map<String, List> map = Maps.newHashMap();
        ArrayList<String> dates = Lists.newArrayList();
        ArrayList<Object> usage = Lists.newArrayList();
        Double Use = 0d;//使用量  MB
        //获取卡信息
        Map<String, Object> route = yzCardMapper.findRoute(object1);
        if (StringUtils.isNull(route)) {
            return AjaxResult.error("未找到相关信息");
        }
        if (route.get("cd_code").toString().contains("YiDong")) {
            Query_YD quer = new Query_YD(route, "YiDong_EC");
            // 获取当前日期
            LocalDate today = LocalDate.now();
            // 计算昨天
            LocalDate yesterday = today.minusDays(1);
            // 创建一个列表来存储日期
            List<LocalDate> dateList = new ArrayList<>();
            // 从昨天开始，遍历7天
            for (int i = 0; i < 7; i++) {
                dateList.add(yesterday.minusDays(i));
            }
            // 打印日期列表
            for (LocalDate date : dateList) {
                Object replace = date.toString().replace("-", "");
                String dayUsage = quer.selDayUsage(object1.get("iccid").toString(), replace.toString());
                JSONObject day = JSONObject.parseObject(dayUsage.toString());
                if (day.get("status").toString().equals("0")) {
                    Map<String, Object> result = ((List<Map<String, Object>>) day.get("result")).get(0);
                    Map<String, Object> dataAmountList = ((List<Map<String, Object>>) result.get("dataAmountList")).get(0);
                    if (dataAmountList.get("dataAmount").toString().equals("")) {
                        Use = 0d;
                        usage.add(Use);
                        dates.add(date.toString().substring(5, 10));
                    } else {
                        double kb = Double.parseDouble(dataAmountList.get("dataAmount").toString());
                        Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                        usage.add(Use);
                        dates.add(date.toString().substring(5, 10));
                    }
                }
            }
            map.put("day", dates);
            map.put("usage", usage);
        } else if (route.get("cd_code").toString().contains("DianXin")) {
            Query_DX5G dx5G = new Query_DX5G(route);
            // 获取当前日期
            LocalDate today = LocalDate.now();
            // 计算昨天
            LocalDate yesterday = today.minusDays(1);
            // 创建一个列表来存储日期
            List<LocalDate> dateList = new ArrayList<>();
            // 从昨天开始，遍历7天
            for (int i = 0; i < 7; i++) {
                dateList.add(yesterday.minusDays(i));
            }
            Map<String, Object> stringObjectMap = new HashMap<>();
            for (LocalDate date : dateList) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String dateString = date.format(formatter);
                stringObjectMap = dx5G.queryTrafficByDate(object1.get("iccid").toString(), dateString, dateString);
                String total_bytes_cnt = stringObjectMap.get("total_bytes_cnt").toString();
                int len = total_bytes_cnt.length();
                total_bytes_cnt = total_bytes_cnt.substring(0, len - 2);
                Use = Double.parseDouble(total_bytes_cnt);
                usage.add(Use);
                dates.add(date.toString().substring(5, 10));
            }
            objects.add(route.get("card_no").toString());
            map.put("day", dates);
            map.put("usage", usage);
        }
        return AjaxResult.success(map);

    }

    @Override
    public AjaxResult cardMonthUsage(String query) {
        //区分运营商类型 移动、电信
        JSONObject object1 = JSON.parseObject(query);
        Map<String, List> map = Maps.newHashMap();
        ArrayList<String> dates = Lists.newArrayList();
        ArrayList<Object> usage = Lists.newArrayList();
        Double Use = 0d;//使用量  MB
        //获取卡信息
        Map<String, Object> route = yzCardMapper.findRoute(object1);
        if (StringUtils.isNull(route)) {
            return AjaxResult.error("未找到相关信息");
        }
        if (route.get("cd_code").toString().contains("YiDong")) {
            Query_YD quer = new Query_YD(route, "YiDong_EC");
            // 获取当前日期（但我们只关心月份和年份）
            LocalDate currentDate = LocalDate.now();
            // 设置日期格式器
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            // 打印从当前月份开始，包括当前月份，并往前推六个月的每个月份
            for (int i = 0; i <= 6; i++) {
                // 减去相应的月份数（从0开始，所以包括当前月份）
                LocalDate previousOrCurrentMonth = currentDate.minusMonths(i);
                dates.add(previousOrCurrentMonth.format(formatter).toString());
                // 打印格式化的月份和年份
                Object resDates = quer.newDesignatedMonth(object1.get("iccid").toString(), previousOrCurrentMonth.format(formatter).toString().replace("-", ""));
                JSONObject resDate = JSONObject.parseObject(resDates.toString());
                Map<String, Object> dataAmountList = ((List<Map<String, Object>>) resDate.get("result")).get(0);
                Map<String, Object> Obj = ((List<Map<String, Object>>) dataAmountList.get("dataAmountList")).get(0);
                double kb = Double.parseDouble(Obj.get("dataAmount").toString());
                Use = Arith.formatToTwo(Arith.div(kb, 1024));//KB 转 MB
                usage.add(Use);
            }
            map.put("day", dates);
            map.put("usage", usage);
        }else if (route.get("cd_code").toString().contains("DianXin")) {
            return AjaxResult.error("暂未开放 !");
//            Query_DX5G dx5G = new Query_DX5G(route);
//            // 获取当前日期（但我们只关心月份和年份）
//            LocalDate currentDate = LocalDate.now();
//            // 设置日期格式器
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
//            String stringObjectMap = dx5G.batchQryFlowByMonth(route.get("card_no").toString(), "202406");
//            System.err.println(stringObjectMap);
//            // 打印从当前月份开始，包括当前月份，并往前推六个月的每个月份
//            for (int i = 0; i <= 6; i++) {
//                // 减去相应的月份数（从0开始，所以包括当前月份）
//                LocalDate previousOrCurrentMonth = currentDate.minusMonths(i);
//                Object format = previousOrCurrentMonth.format(formatter);
//                dates.add(format.toString());
//            }
//            map.put("day", dates);
//            map.put("usage", usage);
        }

        return AjaxResult.success(map);
    }


    private  void sendGetOpenDate(Map map){
        //1.创建路由 绑定 生产队列 发送消息
        String addOrder_exchangeName = "admin_exchange", addOrder_queueName = "admin_CardGetOpenDate_queue", addOrder_routingKey = "admin.CardGetOpenDate.queue";
        try {
            Map<String, Object> start_type = new HashMap<>();
            start_type.putAll(map);//参数
            rabbitTemplate.convertAndSend(addOrder_exchangeName, addOrder_routingKey, JSON.toJSONString(map), message -> {
                // 设置消息过期时间 60 分钟 过期
                message.getMessageProperties().setExpiration("" + (60 * 1000 * 60));
                return message;
            });
        } catch (Exception e) {
            System.out.println("开放接口发送 ‘获取开卡日期’ 指令 发送  失败 " + e.getMessage().toString());
        }
    }


    public String dividCardOne(HashMap<String, Object> map) {
        String bcvalue = map.get("Bcvalue").toString();
        String[] split = bcvalue.split(",");
        List<String> valueList = Arrays.asList(split);
        map.put("valueList",valueList);
        String Message = "";
        String polling_queueName = "admin_DistributeCardOne_queue";
        String polling_routingKey = "admin.DistributeCardOne.queue";
        String polling_exchangeName = "admin_exchange";//路由
        try {
            rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(map), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
        } catch (AmqpException e) {
            System.out.println("划卡 失败 " + e.getMessage().toString());
            return ("物联卡管理 划卡 操作失败！");
        }
        return Message;
    }

    public AjaxResult updateOther(MultipartFile file, SysUser user) {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String newName = UUID.randomUUID().toString().replace("-", "") + "_otherImport";
        String flieUrlRx = "/mnt/file/";
        ReadName = flieUrlRx + ReadName;
        try {
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx + "1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            String path = filePath + ReadName;
            ExcelConfig excelConfig = new ExcelConfig();
            String columns[] = {"iccid", "changeOther"};
            String maxVid = yzCardMapper.findMaxVid();
            maxVid = maxVid != null ? maxVid : "16800000000";
            Long maxVidInt = Long.parseLong(maxVid);
            List<Map<String, String>> list = excelConfig.getExcelListMap(path, columns, 0L);
            if (list.size() > 0) {
                for (Map<String, String> stringMap : list) {
                    if (!stringMap.get("iccid").equals("")) {
                        yzCardMapper.updateOther(stringMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("添加失败 !");
        }
        return AjaxResult.error("添加成功 !");
    }

    public AjaxResult trafficImport(MultipartFile file, Map<String, Object> map) {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/mnt/file/";
        ReadName = flieUrlRx + ReadName;
        try {
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx + "1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            String path = filePath + ReadName;
            ExcelConfig excelConfig = new ExcelConfig();
            String columns[] = {"iccid", "changeOther"};
            List<Map<String, String>> list = excelConfig.getExcelListMap(path, columns, 0L);
            if (list.size() > 10000) {
                return AjaxResult.error("订购卡总数大于10000张,请调整后重试 ！");
            }
            if (list.size() > 0) {
                for (Map<String, String> stringMap : list) {
                    //判断该卡是否订购过当月套餐
                    int total = cardFlowMapper.selOneCardFlow(stringMap.get("iccid").toString());
                    if (total == 0) {
                        if (!stringMap.get("iccid").equals("")) {
                            //1、根据iccid获取卡信息、资费信息
                            Map<String, Object> cardInfoMap = yzCardMapper.selOneCardInfo(stringMap);
                            Map<String, Object> packageInfo = packetMapper.findPackageInfo(map);
                            //2、根据卡信息生成订单 变更执行类型
                            this.saveOrderInfo(cardInfoMap, packageInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("添加失败 ！");
        }
        return AjaxResult.success("添加成功 !");
    }

    public AjaxResult trafficImportText(Map<String, Object> map) {
        String iccid = map.get("iccid").toString();
        if (iccid.contains("\n")) {
            String iccidArr = iccid.replace("\n", ",");
            String idsArr[] = iccidArr.split(",");
            if (idsArr.length > 500) {
                return AjaxResult.error("订购卡总数大于500张,请调整后重试 ！");
            }
            for (String s : idsArr) {
                Map<String, String> stringMap = new HashMap<>();
                stringMap.put("iccid", s);
                //判断该卡是否订购过当月套餐
                int total = cardFlowMapper.selOneCardFlow(stringMap.get("iccid").toString());
                if (total == 0) {
                    //获取卡信息
                    Map<String, Object> cardInfoMap = yzCardMapper.selOneCardInfo(stringMap);
                    Map<String, Object> packageInfo = packetMapper.findPackageInfo(map);
                    //2、根据卡信息生成订单 变更执行类型
                    this.saveOrderInfo(cardInfoMap, packageInfo);
                }
            }
        } else {
            //判断该卡是否订购过当月套餐
            int total = cardFlowMapper.selOneCardFlow(iccid);
            if (total == 0) {
                Map<String, String> stringMap = new HashMap<>();
                stringMap.put("iccid", iccid);
                Map<String, Object> cardInfoMap = yzCardMapper.selOneCardInfo(stringMap);
                Map<String, Object> packageInfo = packetMapper.findPackageInfo(map);
                //2、根据卡信息生成订单 变更执行类型
                this.saveOrderInfo(cardInfoMap, packageInfo);
            }
        }
        return AjaxResult.success();
    }

    private void saveOrderInfo(Map<String, Object> cardInfoMap, Map<String, Object> packageInfo) {
        HashMap<String, Object> insertMap = new HashMap<>();
        String ord_no = VeDate.getNo(8);
        insertMap.put("ord_no", ord_no);
        String ord_type = "2";
        insertMap.put("ord_type", ord_type);
        String ord_name = "平台-批量充值";
        insertMap.put("ord_name", ord_name);
        String iccid = cardInfoMap.get("iccid").toString();
        insertMap.put("iccid", iccid);
        String wx_ord_no = null;
        insertMap.put("wx_ord_no", wx_ord_no);
        String status = "1";
        insertMap.put("status", status);
        String price = packageInfo.get("packet_price").toString();
        insertMap.put("price", price);
//                        String account = map.get("num").toString();
//                        insertMap.put("account", account);
        String packet_id = packageInfo.get("packet_id").toString();
        insertMap.put("packet_id", packet_id);
        String pay_type = "s";
        insertMap.put("pay_type", pay_type);
        String cre_type = "sys";
        insertMap.put("cre_type", cre_type);
        String is_profit = "0";
        insertMap.put("is_profit", is_profit);
        String add_package = "1";
        insertMap.put("add_package", add_package);
        String show_status = "0";
        insertMap.put("show_status", show_status);
        String open_id = null;
        insertMap.put("open_id", open_id);
        String agent_id = cardInfoMap.get("agent_id").toString();
        insertMap.put("agent_id", agent_id);
        String profit_type = "0";
        insertMap.put("profit_type", profit_type);
        insertMap.put("validate_type", "1");
        insertMap.put("add_parameter", null);
        yzOrderMapper.save(insertMap);
        //3、添加信息至card——flow
        packageInfo.put("ord_no", ord_no);
        this.saveFlowInfo(cardInfoMap, packageInfo);
    }

    private void saveFlowInfo(Map<String, Object> cardInfoMap, Map<String, Object> packageInfo) {
        //获取当月时间 2024-06-25

        //判断是年还是月 packet_valid_name   packet_valid_time
        if (packageInfo.get("packet_valid_name").toString().equals("月")) {
            LocalDate currentDate = LocalDate.now();
            int month = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
            LocalDate dateAfter12Months = currentDate.plusMonths(month);
            LocalDate lastDayOfPreviousMonth = dateAfter12Months.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            this.insertFlow(lastDayOfPreviousMonth, cardInfoMap, packageInfo);
            //修改到期日期
            this.updateCardEndTime(lastDayOfPreviousMonth, cardInfoMap, packageInfo);
        } else if (packageInfo.get("packet_valid_name").toString().equals("年")) {
            LocalDate currentDate = LocalDate.now();
            // 加上一年
            int year = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
            LocalDate dateAfterOneYear = currentDate.plusYears(year);
            // 获取前一个月的最后一天
            LocalDate lastDayOfPreviousMonth = dateAfterOneYear.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            this.insertFlow(lastDayOfPreviousMonth, cardInfoMap, packageInfo);
            this.updateCardEndTime(lastDayOfPreviousMonth, cardInfoMap, packageInfo);
        }
    }
    private void updateCardEndTime(LocalDate lastDayOfPreviousMonth, Map<String, Object> cardInfoMap, Map<String, Object> packageInfo) {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("endTime", lastDayOfPreviousMonth);
        cardMap.put("iccid", cardInfoMap.get("iccid"));
        cardMap.put("remaining", packageInfo.get("error_flow"));
        yzCardMapper.updateEndTime(cardMap);
    }

    private void insertFlow(LocalDate lastDayOfPreviousMonth, Map<String, Object> cardInfoMap, Map<String, Object> packageInfo) {
        Map<String, Object> insertMap = new HashMap<>();
        insertMap.put("package_id", packageInfo.get("package_id").toString());
        insertMap.put("packet_id", packageInfo.get("packet_id"));
        insertMap.put("ord_no", packageInfo.get("ord_no"));
        insertMap.put("true_flow", packageInfo.get("packet_flow"));
        insertMap.put("error_flow", packageInfo.get("error_flow"));
        insertMap.put("start_time", VeDate.getStringDate());
        insertMap.put("end_time", lastDayOfPreviousMonth + " " + "23:59:59");
        insertMap.put("ord_type", "1");
        insertMap.put("status", "1");
        insertMap.put("packet_type", "0");
        insertMap.put("use_flow", 0);
        insertMap.put("iccid", cardInfoMap.get("iccid").toString());
        insertMap.put("error_time", packageInfo.get("error_so"));
        insertMap.put("validate_type", "0");
        insertMap.put("validate_time", "");
        cardFlowMapper.insertFlow(insertMap);
    }

    public String uploadCardInfo(MultipartFile file) throws IOException {

        // 匿名内部类 不用额外写一个DataListener
        // 这里需要指定读用哪个class去读，读取第一个sheet 文件流会自动关闭
        EasyExcel.read(file.getInputStream(), YzCard.class, new ReadListener<YzCard>() {
                    //单次缓存的数据量
                    //批量插入数据 单次最大值为2万条
                    public static final int BATCH_COUNT = 20000;
                    //临时存储

                    private List<YzCard> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

                    @Override
                    @Transactional(rollbackFor = Exception.class)
                    public void invoke(YzCard data, AnalysisContext context) {
                        cachedDataList.add(data);
                        if (cachedDataList.size() >= BATCH_COUNT) {
                            saveData();
                            // 存储完成清理 list
                            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                        }
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        saveData();
                    }

                    //存储数据库
                    private void saveData() {
                        //插入集合到数据库
                        yzCardMapper.importCardInfoList(cachedDataList);
                        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
                        log.info("存储数据库成功！");
                    }
                })
                .sheet()
                .doRead();

        return "Excel数据导入完成";

    }



    public void exportCardInfo(String Pstr, HttpServletResponse response) throws Exception {

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("卡板信息导出", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xls");
            // 这里需要设置不关闭流
            HashMap<String, Object> Parammap = new HashMap<>();
            if (Pstr != null) {
                //转义 /
                Pstr = Pstr.replace("%2F", "/");
            }
            try {
                Pstr = AesEncryptUtil.desEncrypt(Pstr);
                Parammap.putAll(JSON.parseObject(Pstr));
                //这两个值前端可能并不会传 防止空指针异常
                String startValue;
                String endValue;
                if (Parammap.get("StartValue") == null) {
                    startValue = "";
                } else {
                    startValue = Parammap.get("StartValue").toString();
                }
                if (Parammap.get("EndValue") == null) {
                    endValue = "";
                } else {
                    endValue = Parammap.get("EndValue").toString();
                }
                //设置内容样式
                //内容样式策略
                WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                // 字体策略
                WriteFont contentWriteFont = new WriteFont();
                // 字体大小
                contentWriteFont.setFontHeightInPoints((short) 11);
                //字体格式
                contentWriteFont.setFontName("宋体");
                contentWriteCellStyle.setWriteFont(contentWriteFont);
                //头策略使用默认 设置字体大小
                WriteCellStyle headWriteCellStyle = new WriteCellStyle();
                WriteFont headWriteFont = new WriteFont();
                headWriteFont.setFontHeightInPoints((short) 11);
                headWriteFont.setFontName("宋体");
                //关闭加粗
                headWriteFont.setBold(false);
                headWriteCellStyle.setWriteFont(headWriteFont);
                //设置边框格式
                headWriteCellStyle.setBorderBottom(BorderStyle.NONE);
                headWriteCellStyle.setBorderLeft(BorderStyle.NONE);
                headWriteCellStyle.setBorderRight(BorderStyle.NONE);
                //写入
                EasyExcel.write(response.getOutputStream(), YzCard.class)
                        //设置工作表名称
                        .sheet("卡板信息表")
                        .registerWriteHandler(new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle))
                        .doWrite(() -> {
                            // 从数据库查询数据
                            return dataList(Parammap);
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }
    private List<YzCard> dataList(Map<String, Object> map) {
        String startValue = "";
        String endValue = "";
        List<YzCard> yzCardList = yzCardMapper.getCardInfoList(map);
        map.put("cardList", yzCardList);
        List<YzCard> cardInfoList = cardFlowMapper.outCardFlowByList(map);
        // 字段映射
        for (YzCard card : cardInfoList) {
            Integer Status_ShowId = card.getStatus_ShowId();
            char cdOperatorType = card.getCd_operator_type();
            String networkType = card.getNetwork_type();
            //状态映射
            if (Status_ShowId == 1) {
                card.setCard_status("库存");
            } else if (Status_ShowId == 2) {
                card.setCard_status("可测试");
            } else if (Status_ShowId == 3) {
                card.setCard_status("待激活");
            } else if (Status_ShowId == 4) {
                card.setCard_status("正常");
            } else if (Status_ShowId == 5) {
                card.setCard_status("停机");
            } else if (Status_ShowId == 6) {
                card.setCard_status("预销号");
            } else if (Status_ShowId == 7) {
                card.setCard_status("销号");
            } else if (Status_ShowId == 8) {
                card.setCard_status("未知");
            }
            //网络类型映射
            if ("1".equals(networkType)) {
                card.setNetworkDes("NB");
            } else if ("2".equals(networkType)) {
                card.setNetworkDes("4G SIM");
            } else if ("3".equals(networkType)) {
                card.setNetworkDes("5G SIM");
            }
            //运营商类型映射
            if (cdOperatorType == '1') {
                card.setOperatorType("移动");
            } else if (cdOperatorType == '2') {
                card.setOperatorType("联通");
            } else if (cdOperatorType == '3') {
                card.setOperatorType("电信");
            } else if (cdOperatorType == '4') {
                card.setOperatorType("其他");
            }
        }
        return cardInfoList;
    }

    @Override
    public int updBalance(HashMap<String, Object> paramMap) {
        return yzCardMapper.updBalance(paramMap);
    }
}




























