package com.yunze.system.service.impl.yunze.xsgl.Contract;

import com.yunze.common.mapper.yunze.cwgl.CwglDepositInfoMapper;
import com.yunze.common.mapper.yunze.cwgl.CwglInvoiceRecordMapper;
import com.yunze.common.mapper.yunze.fhgl.Ship.FhglOrderDeliveryMapper;
import com.yunze.common.mapper.yunze.xsgl.Contract.YzXsglContractMapper;
import com.yunze.common.mapper.yunze.xsgl.Contract.YzXsglContractStandardMapper;
import com.yunze.common.mapper.yunze.xsgl.Contract.YzXsglContractTariffMapper;
import com.yunze.common.utils.yunze.ID_Generate;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.system.service.yunze.xsgl.Contract.IYzXsglContractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class YzXsglContractServiceImpl implements IYzXsglContractService {
    @Resource
    private YzXsglContractMapper yzXsglContractMapper;
    @Resource
    private YzXsglContractStandardMapper yzXsglContractStandardMapper;
    @Resource
    private CwglDepositInfoMapper cwglDepositInfoMapper;
    @Resource
    private FhglOrderDeliveryMapper fhglOrderDeliveryMapper;
    @Resource
    private CwglInvoiceRecordMapper cwglInvoiceRecordMapper;
    @Resource
    private YzXsglContractTariffMapper yzXsglContractTariffMapper;



    @Override
    public Map<String, Object> selMap(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzXsglContractMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzXsglContractMapper.selMap(map));
        return  omp;
    }

    @Override
    public Map<String, Object> find(Map map) {
        Map<String, Object> Rmap = new HashMap<>();
        Rmap.put("CMap",yzXsglContractMapper.find(map));
        Rmap.put("standardList",yzXsglContractStandardMapper.find(map));
        Rmap.put("tariffList",yzXsglContractTariffMapper.find(map));
        return Rmap;
    }

    @Override
    @Transactional
    public Map<String, Object> upd(Map map) {
        Map<String, Object> Rmap = new HashMap<>();

        Map<String, Object> CMap = (Map<String, Object>) map.get("CMap");
        List<Map<String, Object>> standardList_Add = null;
        List<Map<String, Object>> standardList_Upd = null;
        int standardList_UpdCount = 0;//统计 修改 合同信息标配表 条数
        int standardList_DelCount = 0;//统计 修改删除状态 合同信息标配表 条数
        int standardList_AddCount = 0;//统计 新增 合同信息标配表 条数

        List<Map<String, Object>> tariffList_Add = null;
        List<Map<String, Object>> tariffList_Upd = null;
        int tariffList_UpdCount = 0;//统计 修改 资费表 条数
        int tariffList_DelCount = 0;//统计 修改删除状态 资费表 条数
        int tariffList_AddCount = 0;//统计 新增 资费表 条数



        boolean bool = false,CDIO_bool=false;
        String Msg = "";
        //商品
        if(map.get("standardList")!=null){
            List<Map<String, Object>> standardList = (List<Map<String, Object>>) map.get("standardList");
            if(standardList.size()>0){
                 standardList_Add = new ArrayList<>();
                 standardList_Upd = new ArrayList<>();
                for (int i = 0; i < standardList.size(); i++) {
                    Map<String, Object> CatMap =  standardList.get(i);
                    //有ID 的 进修改数组 没有的 进新增数组
                    if(CatMap.get("Csd_ID")!=null && CatMap.get("Csd_ID").toString().length()>0){
                        standardList_Upd.add(CatMap);
                    }else{
                        standardList_Add.add(CatMap);
                    }
                }
            }
        }
        //1.新增本次 新增
        if(standardList_Add!=null && standardList_Add.size()>0){
            Map<String, Object> standardList_AddMap= new HashMap<>();
            standardList_AddMap.put("code",CMap.get("code"));
            standardList_AddMap.put("standardList",standardList_Add);
            standardList_AddCount =  yzXsglContractStandardMapper.save(standardList_AddMap);
        }
        //2.修改 原本数据
        if(standardList_Upd!=null && standardList_Upd.size()>0){
            for (int i = 0; i < standardList_Upd.size(); i++) {
                standardList_UpdCount += yzXsglContractStandardMapper.upd(standardList_Upd.get(i));
            }
        }
        //3.修改 删除 状态 数据
        if(map.get("CommodityList_delArr")!=null){
            if(((List<String>) map.get("CommodityList_delArr")).size()>0){
                standardList_DelCount = yzXsglContractStandardMapper.delId(map);
            }
        }






        //资费
        if(map.get("tariffList")!=null){
            List<Map<String, Object>> tariffList = (List<Map<String, Object>>) map.get("tariffList");
            if(tariffList.size()>0){
                tariffList_Add = new ArrayList<>();
                tariffList_Upd = new ArrayList<>();
                for (int i = 0; i < tariffList.size(); i++) {
                    Map<String, Object> CatMap =  tariffList.get(i);
                    //有ID 的 进修改数组 没有的 进新增数组
                    if(CatMap.get("ctf_id")!=null && CatMap.get("ctf_id").toString().length()>0){
                        tariffList_Upd.add(CatMap);
                    }else{
                        tariffList_Add.add(CatMap);
                    }
                }
            }
        }
        //1.新增本次 新增
        if(tariffList_Add!=null && tariffList_Add.size()>0){
            Map<String, Object> tariffList_AddMap= new HashMap<>();
            tariffList_AddMap.put("code",CMap.get("code"));
            tariffList_AddMap.put("tariffList",tariffList_Add);
            tariffList_AddCount =  yzXsglContractTariffMapper.save(tariffList_AddMap);
        }
        //2.修改 原本数据
        if(tariffList_Upd!=null && tariffList_Upd.size()>0){
            for (int i = 0; i < tariffList_Upd.size(); i++) {
                tariffList_UpdCount += yzXsglContractTariffMapper.upd(tariffList_Upd.get(i));
            }
        }
        //3.修改 删除 状态 数据
        if(map.get("PacketList_delArr")!=null){
            if(((List<String>) map.get("PacketList_delArr")).size()>0){
                tariffList_DelCount = yzXsglContractTariffMapper.delId(map);
            }
        }

        // 修改主表
        bool = yzXsglContractMapper.upd(CMap)>0;

        //修改 入款信息
        Map<String, Object> CDIO_Map = new HashMap<>();
        CDIO_Map.put("Contract_ID", CMap.get("code"));// 赋值 合同ID
        CDIO_Map.put("Dio_Unpaid", CMap.get("c_contract_amount"));// 赋值 未到款金额
        //入款状态 判断
        Map<String, Object> U_Map = cwglDepositInfoMapper.sel_Dio_DsID(CDIO_Map);//未到款
        int Dio_DsID=(U_Map.get("da_yu_Unpaid").toString().equals("1"))==true?3//3.已到款
                :(U_Map.get("da_yu_0").toString().equals("1"))==true?2://2.有尾款
                1;//1.未到款
        CDIO_Map.put("Dio_DsID", Dio_DsID);//入款状态 赋值
        CDIO_bool=((cwglDepositInfoMapper.upd_Unpaid(CDIO_Map))>0);


        //开票要求 不为 不开票时


        //-------------------[销售提成信息更改]--------------------------


        if(bool){
            Msg ="更新数据成功！";
        }else{
            Msg ="操作失败……";
        }
        Rmap.put("bool",bool);
        Rmap.put("Msg",Msg);
        System.out.println(" 修改 合同信息标配表 条数 ["+standardList_UpdCount+"]"+
                " 修改删除状态 合同信息标配表 条数 ["+standardList_DelCount+"]"+
                " 新增 合同信息标配表 条数 ["+standardList_AddCount+"] |"+
                " 修改 合同信息资费表 条数 ["+tariffList_UpdCount+"]"+
                " 修改删除状态 合同信息资费表 条数 ["+tariffList_DelCount+"]"+
                " 新增 合同信息资费表 条数 ["+tariffList_AddCount+"]");
        return Rmap;
    }


    @Override
    @Transactional
    public Map<String, Object> save(Map map) {
        Map<String, Object> CMap = (Map<String, Object>) map.get("CMap");
        Map<String, Object> Rmap = new HashMap<>();
        String Message = "";
        boolean bool = false,CDIO_bool=false,OrderDelivery_bool=false,invoice_bool=false;

            //独立到MQ 队列
            // 1.新增主表
            Integer code_CT = yzXsglContractMapper.sel_data_count();//查询今日注册供应商数量
            code_CT=code_CT!=null?code_CT:0;//如果今日添加为0会反回null,给个默认值0
            String Ct_ID = ID_Generate.G_id("XCO", code_CT, 4);//生成ID
            map.put("code", Ct_ID);//赋值id
            CMap.put("code", Ct_ID);

            try {
                bool = yzXsglContractMapper.save(CMap)>0;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            //2.新增合同标配信息
            map.put("Csd_CtID",CMap.get("id"));
            List<Map<String,Object>> standardList = (List<Map<String, Object>>) map.get("standardList");
            if(standardList!=null && standardList.size()>0){
                yzXsglContractStandardMapper.save(map);
            }

            //2.1 新增合同 资费 信息
            map.put("ctf_ctId",CMap.get("id"));
            List<Map<String,Object>> tariffList = (List<Map<String, Object>>) map.get("tariffList");
            if(tariffList!=null && tariffList.size()>0){
                yzXsglContractTariffMapper.save(map);
            }




            //3.新增入款信息
            Map<String, Object> CDIO_Map= new HashMap<>();
            CDIO_Map.put("Contract_ID",Ct_ID);//赋值合同iD
            Integer Dio_ID_CT = cwglDepositInfoMapper.sel_data_count();//查询今日注册数量
            Dio_ID_CT=Dio_ID_CT!=null?Dio_ID_CT:0;//如果今日添加为0会反回null,给个默认值0
            String Dio_ID=ID_Generate.G_id("CDIO", Dio_ID_CT, 4);//生成ID
            CDIO_Map.put("Dio_ID",Dio_ID);//赋值新增的ID
            CDIO_Map.put("Dio_Unpaid",CMap.get("c_the_actual_amount"));//未到款 = 实际金额
            CDIO_Map.put("Dio_Source_Type", "1");//入款信息来源类别 1 销售合同  【DepositSource_type】

            CDIO_bool=cwglDepositInfoMapper.add_CDIO(CDIO_Map)>0;
            //4.新增 订单合同发货
            Map<String, Object>  ODY_Map = new HashMap<String, Object>();

            ODY_Map.put("ODY_Contract_ID", Ct_ID);//合同ID
            ODY_Map.put("ODY_dsID", "1");//发货状态 1 未发
            ODY_Map.put("ODY_nocID", "1");//合同性质 1 销售订单【contract_nature】
            Integer ODY_ID_CT = fhglOrderDeliveryMapper.sel_data_count();//查询今日注册供应商数量
            ODY_ID_CT=ODY_ID_CT!=null?ODY_ID_CT:0;//如果今日添加为0会反回null,给个默认值0
            String ODY_ID=ID_Generate.G_id("FODY", ODY_ID_CT, 4);//生成ID
            ODY_Map.put("ODY_ID", ODY_ID);//赋值id
            OrderDelivery_bool = fhglOrderDeliveryMapper.add_ODY(ODY_Map)>0;
            //5.开票要求 不为 不开票时 新增  发票记录信息
            if(CMap.get("c_brId").toString().equals("0")==false) {
                Integer Ird_ID_CT = cwglInvoiceRecordMapper.sel_data_count();//查询今日注册数量
                Ird_ID_CT=Ird_ID_CT!=null?Ird_ID_CT:0;//如果今日添加为0会反回null,给个默认值0
                String Ird_ID=ID_Generate.G_id("CIRD", Ird_ID_CT, 4);//生成ID
                CMap.put("Ird_ID",Ird_ID);//赋值新增的ID
                CMap.put("Ird_Nature","1");//发票性质 1 合同开票
                CMap.put("Ird_ContractAmount",CMap.get("c_the_actual_amount"));//	合同金额 = 实际金额
                CMap.put("Ird_UnopenedAmount",CMap.get("c_the_actual_amount"));//	未开金额 = 实际金额
                invoice_bool=cwglInvoiceRecordMapper.add_IRD(CMap)>0;
            }
            System.out.println(" 新增入款 "+CDIO_bool+" 订单合同发货 "+OrderDelivery_bool+" c_brId "+CMap.get("c_brId")+" 发票记录信息 "+invoice_bool);
            if(bool){
                Message = "操作成功！";
            }else{
                Message = "操作成功！";
            }
        Rmap.put("bool",bool);
        Rmap.put("Msg",Message);
        return Rmap;
    }

    @Override
    public Map<String, Object> delId(Map map) {



        return null;
    }

    @Override
    public List<Map<String, Object>> find_FH_data(Map map) {
        return yzXsglContractStandardMapper.find_FH_data(map);
    }


}
















