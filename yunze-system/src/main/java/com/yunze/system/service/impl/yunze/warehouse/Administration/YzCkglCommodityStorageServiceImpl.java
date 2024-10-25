package com.yunze.system.service.impl.yunze.warehouse.Administration;
import com.alibaba.fastjson.JSON;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStockMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStorageDetailsMapper;
import com.yunze.common.mapper.yunze.warehouse.Administration.YzCkglCommodityStorageMapper;
import com.yunze.common.utils.yunze.ID_Generate;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.system.service.yunze.warehouse.Administration.YzCkglCommodityStorageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class YzCkglCommodityStorageServiceImpl implements YzCkglCommodityStorageService {

    @Resource
    private YzCkglCommodityStorageMapper yzCkglCommodityStorageMapper;
    @Resource
    private YzCkglCommodityStorageDetailsMapper yzCkglCommodityStorageDetailsMapper;
    @Resource
    private YzCkglCommodityMapper yzCkglCommodityMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private YzCkglCommodityStockMapper yzCkglCommodityStockMapper;

    @Override
    @Transactional
    public Map<String, Object> warehousingadd(Map map) {
        Map<String, Object> CMap = (Map<String, Object>) map.get("CMap");
        Map<String, Object> Rmap = new HashMap<>();
        String Message = "";
        boolean bool = false;
        //检查 CMap 新增 商品信息 是否已经存在

        Integer CSE_ID_CT = yzCkglCommodityStorageMapper.sel_data_count();//查询今日注册供应商数量
        CSE_ID_CT = CSE_ID_CT != null ? CSE_ID_CT : 0;//如果今日添加为0会反回null,给个默认值0
        String CSE_ID = ID_Generate.G_id("CCSE", CSE_ID_CT, 4);//生成ID
        CMap.put("CSE_ID", CSE_ID);//赋值id
        map.put("CSDS_CSEID", CSE_ID);//赋值id

        // 新增主表
        bool = yzCkglCommodityStorageMapper.warehousingadd(CMap) > 0;
        List<Map<String, Object>> CatArrs = (List<Map<String, Object>>) map.get("CatArrs");
        if (CatArrs != null && CatArrs.size() > 0) {
            yzCkglCommodityStorageDetailsMapper.quantity(map);

            //遍历 list
            for (int i = 0; i < CatArrs.size(); i++) {
                Map<String, Object> O_map = CatArrs.get(i);
                //计算商品 库存 = 商品入库总数 - 商品出库总数
                Integer Imp_Count = yzCkglCommodityStorageDetailsMapper.sle_quantity(O_map);//查询 商品入库总数
                Imp_Count = Imp_Count != null ? Imp_Count : 0;
                Integer Out_Count = yzCkglCommodityStorageMapper.sle_warehouse(O_map);//查询 商品出库总数
                Out_Count = Out_Count != null ? Out_Count : 0;
                Integer Quantity = Imp_Count - Out_Count;
                O_map.put("Quantity", Quantity);//当前数量 刷新赋值
                Integer sel_Ex = yzCkglCommodityStockMapper.sle_stock(O_map);//查询 商品 是否存在

                //存在进行 修改
                if (sel_Ex != null && sel_Ex > 0) {
                    bool = yzCkglCommodityStockMapper.update_quantity(O_map) > 0;
                } else {
                    bool = yzCkglCommodityStockMapper.add_quantity(O_map) > 0;
                }
            }
        }
        if (bool) {
            Message = "操作成功！";
        }
        Rmap.put("bool", bool);
        Rmap.put("Msg", Message);
        return Rmap;
    }

    @Override
    public Map<String, Object> slestorage(Map map) {
        //System.out.println("CardRouteServiceImpl > sel_Map = ");
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzCkglCommodityStorageMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzCkglCommodityStorageMapper.slestorage(map));
        return omp;
    }

    @Override
    public List<Map<String, Object>> detailedId(Map map) {
        return yzCkglCommodityMapper.selId(map);
    }

    @Override
    public String ImportStock(MultipartFile file,Map<String,String> Pmap, SysUser User,HashMap<String, Object> parammap) {
        String filename=file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-","")+filename;
        String newName = UUID.randomUUID().toString().replace("-","")+"_uploadDetails";
        String flieUrlRx = "/upload/uploadDetails/";
        ReadName = flieUrlRx + ReadName;
        try {
            /**
             * 获取当前项目的工作路径
             */
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath +ReadName);
            Map<String,Object> map = new HashMap();
            map.put("CSDS_ID",parammap.get("CSDS_ID"));
            map.put("Cy_ID",parammap.get("Cy_ID"));
            String CSDS_fliieUrl = yzCkglCommodityStorageDetailsMapper.sleFile(map);
            if(CSDS_fliieUrl != null && CSDS_fliieUrl.toString().length()>0){
                CSDS_fliieUrl += ","+ReadName;
            }else {
                CSDS_fliieUrl = ReadName;
            }
            map.put("CSDS_URL",CSDS_fliieUrl);
            yzCkglCommodityStorageDetailsMapper.uploadFile(map);
            File Url = new File(filePath + flieUrlRx +"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_uploadDetails_queue";
            String polling_routingKey = "admin.uploadDetails.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
               // rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
                start_type.put("type", "importCardData");//启动类型
                start_type.put("filePath", filePath);//项目根目录
                start_type.put("ReadName", ReadName);//上传新文件名
                start_type.put("newName", newName);//输出文件名
                start_type.put("User", User);//登录用户信息
                start_type.put("Pmap", Pmap);//
                start_type.put("parammap", parammap);//
                rabbitTemplate.convertAndSend(polling_exchangeName, polling_routingKey, JSON.toJSONString(start_type), message -> {
                    // 设置消息过期时间 30 分钟 过期
                    message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                    return message;
                });
            }catch (Exception e){
                System.out.println("导入 卡号 失败 " + e.getMessage().toString());
                return ("仓库管理明细 导入 操作失败 !");
            }

        }catch (Exception e){
            System.out.println(e);
            return "上传excel异常";
        }
        return "仓库管理明细 导入指令 已发送，详细信息请在 【执行日志管理】查询！";

    }


}












































