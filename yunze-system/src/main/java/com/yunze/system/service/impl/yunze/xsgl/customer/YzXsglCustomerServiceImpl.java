package com.yunze.system.service.impl.yunze.xsgl.customer;

import com.alibaba.fastjson.JSON;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.xsgl.customer.YzXsglCustomerAccountMapper;
import com.yunze.common.mapper.yunze.xsgl.customer.YzXsglCustomerChangeMapper;
import com.yunze.common.mapper.yunze.xsgl.customer.YzXsglCustomerMapper;
import com.yunze.common.mapper.yunze.xsgl.customer.YzXsglCustomerPeopleMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.system.service.yunze.xsgl.customer.IYzXsglCustomerService;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;


@Service
public class YzXsglCustomerServiceImpl implements IYzXsglCustomerService {
    @Resource
    private YzXsglCustomerMapper yzXsglCustomerMapper;
    @Resource
    private YzXsglCustomerAccountMapper yzXsglCustomerAccountMapper;
    @Resource
    private YzXsglCustomerPeopleMapper yzXsglCustomerPeopleMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzXsglCustomerChangeMapper yzXsglCustomerChangeMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitMQConfig rabbitMQConfig;


    @Override
    public Map<String, Object> selMap(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        Integer rowCount = yzXsglCustomerMapper.selMapCount(map);
        rowCount = rowCount != null ? rowCount : 0;
        PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzXsglCustomerMapper.selMap(map));
        return omp;
    }

    @Override
    public Map<String, Object> find(Map map) {
        Map<String, Object> Rmap = new HashMap<>();
        if (map.get("IsMaster") != null) {
            boolean bool = (boolean) map.get("IsMaster"); //是否查询主表信息
            if (bool) {
                Rmap.put("CMap", yzXsglCustomerMapper.find(map));
            }
        }
        Rmap.put("CatArrs", yzXsglCustomerAccountMapper.findArr(map));
        Rmap.put("CpeopleArrs", yzXsglCustomerPeopleMapper.findArr(map));
        return Rmap;
    }

    @Override
    @Transactional
    public Map<String, Object> upd(Map map) {
        Map<String, Object> Rmap = new HashMap<>();

        Map<String, Object> CMap = (Map<String, Object>) map.get("CMap");
        String CoID = map.get("CoID").toString();
        List<Map<String, Object>> CatArrs_Add = null;
        List<Map<String, Object>> CatArrs_Upd = null;
        List<Map<String, Object>> CpeopleArrs_Add = null;
        List<Map<String, Object>> CpeopleArrs_Upd = null;
        int CatArrs_UpdCount = 0;//统计 修改 账户条数
        int CpeopleArrs_UpdCount = 0;//统计 修改 联系人 条数
        int CatArrs_DelCount = 0;//统计 修改删除状态 账户条数
        int CpeopleArrs_DelCount = 0;//统计 修改删除状态 联系人 条数

        int CatArrs_AddCount = 0;//统计 新增 账户条数
        int CpeopleArrs_AddCount = 0;//统计 新增 联系人 条数

        boolean bool = false;
        String Msg = "";
        //账户
        if (map.get("CatArrs") != null) {
            List<Map<String, Object>> CatArrs = (List<Map<String, Object>>) map.get("CatArrs");
            if (CatArrs.size() > 0) {
                CatArrs_Add = new ArrayList<>();
                CatArrs_Upd = new ArrayList<>();
                for (int i = 0; i < CatArrs.size(); i++) {
                    Map<String, Object> CatMap = CatArrs.get(i);
                    //有ID 的 进修改数组 没有的 进新增数组
                    if (CatMap.get("Cat_ID") != null && CatMap.get("Cat_ID").toString().length() > 0) {
                        CatArrs_Upd.add(CatMap);
                    } else {
                        CatArrs_Add.add(CatMap);
                    }
                }
            }
        }
        //联系人
        if (map.get("CpeopleArrs") != null) {
            List<Map<String, Object>> CpeopleArrs = (List<Map<String, Object>>) map.get("CpeopleArrs");
            if (CpeopleArrs.size() > 0) {
                CpeopleArrs_Add = new ArrayList<>();
                CpeopleArrs_Upd = new ArrayList<>();
                for (int i = 0; i < CpeopleArrs.size(); i++) {
                    Map<String, Object> CatMap = CpeopleArrs.get(i);
                    //有ID 的 进修改数组 没有的 进新增数组
                    if (CatMap.get("id") != null && CatMap.get("id").toString().length() > 0) {
                        CpeopleArrs_Upd.add(CatMap);
                    } else {
                        CpeopleArrs_Add.add(CatMap);
                    }
                }
            }
        }
        //1.新增本次 新增
        if (CatArrs_Add != null && CatArrs_Add.size() > 0) {
            Map<String, Object> CatArrs_AddMap = new HashMap<>();
            CatArrs_AddMap.put("CoID", CoID);
            CatArrs_AddMap.put("CatArrs", CatArrs_Add);
            CatArrs_AddCount = yzXsglCustomerAccountMapper.save(CatArrs_AddMap);
        }
        if (CpeopleArrs_Add != null && CpeopleArrs_Add.size() > 0) {
            Map<String, Object> CpeopleArrs_AddMap = new HashMap<>();
            CpeopleArrs_AddMap.put("CoID", CoID);
            CpeopleArrs_AddMap.put("CpeopleArrs", CpeopleArrs_Add);
            CpeopleArrs_AddCount = yzXsglCustomerPeopleMapper.save(CpeopleArrs_AddMap);
        }
        //2.修改 原本数据
        if (CatArrs_Upd != null && CatArrs_Upd.size() > 0) {
            for (int i = 0; i < CatArrs_Upd.size(); i++) {
                CatArrs_UpdCount += yzXsglCustomerAccountMapper.upd(CatArrs_Upd.get(i));
            }
        }
        if (CpeopleArrs_Upd != null && CpeopleArrs_Upd.size() > 0) {
            for (int i = 0; i < CpeopleArrs_Upd.size(); i++) {
                CpeopleArrs_UpdCount += yzXsglCustomerPeopleMapper.upd(CpeopleArrs_Upd.get(i));
            }
        }
        //3.修改 删除 状态 数据
        if (map.get("CatArrs_delArr") != null) {
            if (((List<String>) map.get("CatArrs_delArr")).size() > 0) {
                CatArrs_DelCount = yzXsglCustomerAccountMapper.delId(map);
            }
        }
        if (map.get("CpeopleArrs_delArr") != null) {
            if (((List<String>) map.get("CpeopleArrs_delArr")).size() > 0) {
                CpeopleArrs_DelCount = yzXsglCustomerPeopleMapper.delId(map);
            }
        }
        // 新增主表
        bool = yzXsglCustomerMapper.upd(CMap) > 0;
        if (bool) {
            Msg = "更新数据成功！";
        } else {
            Msg = "操作失败……";
        }
        Rmap.put("bool", bool);
        Rmap.put("Msg", Msg);
        System.out.println(" 修改 账户条数 [" + CatArrs_UpdCount + "]" + " 修改 联系人 条数 [" + CpeopleArrs_UpdCount + "]" +
                " 修改删除状态 账户条数 [" + CatArrs_DelCount + "]" + " 修改删除状态 联系人 条数 [" + CpeopleArrs_DelCount + "]" +
                " 新增 账户条数 [" + CatArrs_AddCount + "]" + " 新增 联系人 条数 [" + CpeopleArrs_AddCount + "]");
        return Rmap;
    }

    @Override
    @Transactional
    public Map<String, Object> updInfo(Map map) {

        return null;
    }

    @Override
    @Transactional
    public Map<String, Object> save(Map map) {
        Map<String, Object> CMap = (Map<String, Object>) map.get("CMap");
        Map<String, Object> Rmap = new HashMap<>();
        String Message = "";
        boolean bool = false;

        String Str = CMap.get("dept_id").toString();
        if (Str != null && !"".equals(Str)) {
            Rmap = saveCustomer(bool,Message,Rmap,CMap,map);//新增时携带了 部门id 新增
        }

        String c_name = CMap.get("c_name").toString();//获取客户名称
        // 不存在客户表中 时 查询‘客户名称’是否与系统表 sys_dept dept_name 匹对 如果不批对 创建 企业 和用户
        Map<String, Object> getDeptName = yzXsglCustomerMapper.getDeptName(CMap);//查询 sys_dept

        boolean isAddDept = false;
        if (getDeptName != null) {
            String getDept = getDeptName.get("dept_name").toString();
            if (getDept.equals(c_name)) {
                //如果匹对上了 获取dept_id 再来客户表中 找 是不是已经有了
                //该dept_id的企业如果有了 新增客户取消 并提示
                Map<String, Object> deptID = yzXsglCustomerMapper.deptID(getDeptName);//查询 sys_dept
                if (deptID != null) {
                    Message = "客户【" + c_name + "】已存在无需重复添加！";
                }else{
                    CMap.put("dept_id",getDeptName.get("dept_id"));
                    // 新增主表
                    bool = yzXsglCustomerMapper.save(CMap) > 0;
                    map.put("CoID", CMap.get("id"));
                    List<Map<String, Object>> CatArrs = (List<Map<String, Object>>) map.get("CatArrs");
                    List<Map<String, Object>> CpeopleArrs = (List<Map<String, Object>>) map.get("CpeopleArrs");
                    if (CatArrs != null && CatArrs.size() > 0) {
                        yzXsglCustomerAccountMapper.save(map);
                    }
                    if (CpeopleArrs != null && CpeopleArrs.size() > 0) {
                        yzXsglCustomerPeopleMapper.save(map);
                    }
                    if (bool) {
                        Message = "操作成功！";
                    }
                }
            }
        } else {
            isAddDept = true;
        }
        if(isAddDept){
            Rmap = saveCustomerAndDept(c_name,CMap,bool,Message,Rmap,map);
        }else{
            Rmap.put("bool", bool);
            Rmap.put("Msg", Message);
        }
        return Rmap;
    }


    public Map<String, Object>  saveCustomer(boolean bool,String Message,Map<String, Object> Rmap,Map<String, Object> CMap,Map<String, Object> map)  {
        //检查 CMap 新增 客户信息 是否已经存在
        Integer IsExist = yzXsglCustomerMapper.IsExist(CMap);
        if (IsExist != null && IsExist > 0) {
            Message = "已存在客户信息无需重复添加！";
        } else {
            // 新增主表
            bool = yzXsglCustomerMapper.save(CMap) > 0;
            map.put("CoID", CMap.get("id"));
            List<Map<String, Object>> CatArrs = (List<Map<String, Object>>) map.get("CatArrs");
            List<Map<String, Object>> CpeopleArrs = (List<Map<String, Object>>) map.get("CpeopleArrs");
            if (CatArrs != null && CatArrs.size() > 0) {
                yzXsglCustomerAccountMapper.save(map);
            }
            if (CpeopleArrs != null && CpeopleArrs.size() > 0) {
                yzXsglCustomerPeopleMapper.save(map);
            }
            if (bool) {
                Message = "操作成功！";
            }
        }
        Rmap.put("bool", bool);
        Rmap.put("Msg", Message);
        return Rmap;
    }



    public Map<String, Object> saveCustomerAndDept(String c_name,Map<String, Object> CMap,boolean bool,String Message,Map<String, Object> Rmap,Map<String, Object> map){

            Integer MaxOrderNum = yzXsglCustomerMapper.getMaxOrderNum();
            MaxOrderNum = MaxOrderNum != null ? MaxOrderNum : 0;
            MaxOrderNum = MaxOrderNum + 1;
            List<Map<String, String>> list = new ArrayList<>();
            Map<String, String> HaMap = new HashMap<>();

            HaMap.put("order_num", MaxOrderNum.toString());
            HaMap.put("dept_name", c_name);
            HaMap.put("dname", CMap.get("alias").toString());

            list.add(HaMap);
            int saveCount = yzXsglCustomerMapper.saveSysDept(list);
            String dept_id = "";
            for (Map<String, String> mapArr : list) {
                dept_id = String.valueOf(mapArr.get("dept_id"));//把 int 转换成 字符串
            }
            int userAdd = 0;
            if (saveCount > 0) {
                try {
                    //生成账户 密码
                    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
                    //拼音小写
                    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
                    //不带声调
                    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

                    String password = VeDate.getNowDateShortNuber();//获取密码
                    String user_name = PinyinHelper.toHanYuPinyinString(c_name, format, ",", true);//中文转拼音
                    Map<String, String> mapUser = new HashMap<>();

                    mapUser.put("dept_id", dept_id);
                    mapUser.put("nick_name", c_name);
                    mapUser.put("user_name", user_name.replaceAll(",", ""));
                    mapUser.put("password", password);
                    userAdd = yzXsglCustomerMapper.userCustomer(mapUser);//新增 账户 密码

                    List<Map<String, String>> rooleList = new ArrayList<>();//用户
                    String user_id = String.valueOf(mapUser.get("user_id"));//把 int 转换成 字符串

                    Map<String, String> map_2 = new HashMap<>();

                    map_2.put("user_id", user_id);
                    map_2.put("role_id", "2");

                    Map<String, String> map_3 = new HashMap<>();
                    map_3.put("user_id", user_id);
                    map_3.put("role_id", "4");

                    rooleList.add(map_2);
                    rooleList.add(map_3);
                    bool = yzXsglCustomerMapper.userDept(rooleList) > 0;// 新增 用户和角色
                    if (bool) {
                        Message = "操作成功！";
                    }
                } catch (Exception e) {
                    System.out.println("生成账户密码--->失败");
                }
            }


            if (userAdd > 0) {
                CMap.put("dept_id",dept_id);
                Rmap = saveCustomer(bool,Message,Rmap,CMap,map);
            }else{
                Rmap.put("bool", bool);
                Rmap.put("Msg", Message);
            }
        return Rmap;
    }






    @Override
    public List<Map<String, Object>> findSalesPartner(Map<String, Object> map) {
        return yzCardMapper.findSalesPartner(map);
    }

    @Override
    public List<Map<String, Object>> IsAdmin(Map map) {
        return yzXsglCustomerMapper.IsAdmin(map);
    }

    @Override
    public Map<String, Object> Divide(Map map) {
        Map<String, Object> Rmap = new HashMap<>();
        map.put("IsSel_sales_id", true);
       /* map.put("IsSel_grade_id",false);
        map.put("IsSel_province",false);
        map.put("IsSel_city",false);
        map.put("IsSel_district",false);
        map.put("IsSel_address",false);
        map.put("IsSel_one_category_id",false);
        map.put("IsSel_two_category_id",false);
        map.put("IsSel_remarks",false);
        map.put("IsSel_source_id",false);
        map.put("IsSel_is_Recycle",false);
        map.put("IsSel_affiliation_id",false);
        map.put("IsSel_tax_number",false);
        map.put("IsSel_Co_Discount",false);
        map.put("IsSel_state_id",false);
        map.put("IsSel_affiliation_rate",false);
        map.put("IsSel_sales_rate",false);
        map.put("IsSel_afterSale_rate",false);
        map.put("IsSel_credit_code",false);
        map.put("IsSel_id_card",false);
        map.put("IsSel_sort_id",false);*/


        List<Map<String, Object>> salesArr = yzXsglCustomerMapper.selInfo(map);
        String sales_id = map.get("sales_id").toString();
        String request_args = JSON.toJSONString(map);
        SysUser User = (SysUser) map.get("User");
        Object user_id = User.getUserId();
        Object user_name = User.getUserName();
        List<Map<String, Object>> CustomerInfoMapList = new ArrayList<>();
        for (int i = 0; i < salesArr.size(); i++) {
            Map<String, Object> Amap = salesArr.get(i);
            Amap.put("ctype", "6");
            Amap.put("cbefore", Amap.get("sales_id"));
            Amap.put("cafterward", sales_id);
            Amap.put("remark", "");
            Amap.put("execution_status", "1");
            Amap.put("user_id", user_id);
            Amap.put("user_name", user_name);
            Amap.put("request_args", request_args);
            CustomerInfoMapList.add(Amap);
        }
        int UpdCount = yzXsglCustomerMapper.updDivide(map);
        Map<String, Object> addinfoMap = new HashMap<>();
        addinfoMap.put("CustomerInfoMapList", CustomerInfoMapList);
        yzXsglCustomerChangeMapper.addinfo(addinfoMap);
        boolean bool = UpdCount > 0;
        String Msg = "";
        if (bool) {
            Msg = "操作成功！";
        } else {
            Msg = "操作失败！";
        }
        Rmap.put("bool", bool);
        Rmap.put("Msg", Msg);
        return Rmap;
    }

    @Override
    public Map<String, Object> Rate(Map map) {
        Map<String, Object> Rmap = new HashMap<>();
        map.put("IsSel_affiliation_rate", true);
        map.put("IsSel_afterSale_rate", true);
        map.put("IsSel_sales_rate", true);


        List<Map<String, Object>> salesArr = yzXsglCustomerMapper.selInfo(map);
        String affiliation_rate = map.get("affiliation_rate").toString();
        String sales_rate = map.get("sales_rate").toString();
        String afterSale_rate = map.get("afterSale_rate").toString();
        String request_args = JSON.toJSONString(map);
        SysUser User = (SysUser) map.get("User");
        Object user_id = User.getUserId();
        Object user_name = User.getUserName();
        List<Map<String, Object>> CustomerInfoMapList = new ArrayList<>();
        int UpdCount = 0;
        for (int i = 0; i < salesArr.size(); i++) {
            Map<String, Object> Amap = salesArr.get(i);
            Amap.put("ctype", "2");
            Amap.put("cbefore", Amap.get("affiliation_rate"));
            Amap.put("cafterward", affiliation_rate);
            Amap.put("remark", "");
            Amap.put("execution_status", "1");
            Amap.put("user_id", user_id);
            Amap.put("user_name", user_name);
            Amap.put("request_args", request_args);
            CustomerInfoMapList.add(Amap);

            Map<String, Object> Bmap = new HashMap<>();
            Bmap.putAll(Amap);
            Bmap.put("ctype", "3");
            Bmap.put("cbefore", Amap.get("sales_rate"));
            Bmap.put("cafterward", sales_rate);
            CustomerInfoMapList.add(Bmap);
            Map<String, Object> Cmap = new HashMap<>();
            Cmap.putAll(Amap);
            Cmap.put("ctype", "4");
            Cmap.put("cbefore", Amap.get("afterSale_rate"));
            Cmap.put("cafterward", afterSale_rate);
            CustomerInfoMapList.add(Cmap);
            //修改联客户 费率
            Map<String, Object> Updmap = new HashMap<>();
            Updmap.put("sales_rate", Double.parseDouble(sales_rate));
            Updmap.put("afterSale_rate", Double.parseDouble(afterSale_rate));
            Updmap.put("affiliation_rate", Double.parseDouble(affiliation_rate));
            Updmap.put("id", Amap.get("id"));
            UpdCount += yzXsglCustomerMapper.updInfo(Updmap);
        }
        Map<String, Object> addinfoMap = new HashMap<>();
        addinfoMap.put("CustomerInfoMapList", CustomerInfoMapList);
        yzXsglCustomerChangeMapper.addinfo(addinfoMap);
        boolean bool = UpdCount > 0;
        String Msg = "";
        if (bool) {
            Msg = "操作成功！";
        } else {
            Msg = "操作失败！";
        }
        Rmap.put("bool", bool);
        Rmap.put("Msg", Msg);
        return Rmap;
    }

    @Override
    public List<Map<String, Object>> findCustomerArr(Map map) {
        return yzXsglCustomerMapper.findCustomerArr(map);
    }

    @Override
    public List<Map<String, Object>> fnidPeopleArr(Map map) {
        return yzXsglCustomerPeopleMapper.fnidPeopleArr(map);
    }

    @Override
    public List<Map<String, Object>> fnidAccountArr(Map map) {
        return yzXsglCustomerAccountMapper.fnidAccountArr(map);
    }

    @Override
    public String uploadCustomer(MultipartFile file, boolean updateSupport, SysUser User) throws IOException {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String newName = UUID.randomUUID().toString().replace("-", "") + "_CustomerImport";
        String flieUrlRx = "/upload/uploadCustomer/";
        ReadName = flieUrlRx + ReadName;
        try {
            /**
             * 获取当前项目的工作路径
             */
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath +  ReadName);
            File Url = new File(filePath + flieUrlRx +"1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);

            //1.创建路由 绑定 生产队列 发送消息
            //导卡 路由队列
            String polling_queueName = "admin_uploadCustomer_queue";
            String polling_routingKey = "admin.uploadCustomer.queue";
            String polling_exchangeName = "admin_exchange";//路由
            try {
                //rabbitMQConfig.creatExchangeQueue(polling_exchangeName, polling_queueName, polling_routingKey, null, null, null, BuiltinExchangeType.DIRECT);
                Map<String, Object> start_type = new HashMap<>();
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
                System.out.println("导入 客户信息 失败 " + e.getMessage().toString());
                return "客户管理 导入 操作失败！";
            }
        } catch (Exception e) {
            System.out.println(e);
            return "上传excel异常";
        }

        return "客户管理 导入指令 已发送，详细信息请在 【执行日志管理】查询！";
    }


}

















