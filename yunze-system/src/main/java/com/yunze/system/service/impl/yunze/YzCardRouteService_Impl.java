package com.yunze.system.service.impl.yunze;


import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.common.mapper.yunze.YzCardPackageMapper;
import com.yunze.common.mapper.yunze.YzExecutionTaskMapper;
import com.yunze.common.utils.SecurityUtils;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.WriteCSV;
import com.yunze.system.service.yunze.IYzCardRouteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 上游通道 业务实现类
 */
@Service
public class YzCardRouteService_Impl implements IYzCardRouteService {


    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzExecutionTaskMapper yzExecutionTaskMapper;
    @Resource
    private WriteCSV writeCSV;
    @Resource
    private YzCardPackageMapper cardPackageMapper;

    @Override
    public Map<String, Object> sel_Map(Map map) {
        Map<String, Object> omp = new HashMap<String, Object>();
        Integer currenPage = map.get("pageNum") != null ? Integer.parseInt(map.get("pageNum").toString()) : 0;
        Integer pageSize = map.get("pageSize") != null ? Integer.parseInt(map.get("pageSize").toString()) : 10;
        try {
            Integer rowCount = yzCardRouteMapper.sel_Map_Count(map);
            System.err.println(rowCount);
            rowCount = rowCount != null ? rowCount : 0;
            PageUtil pu = new PageUtil(rowCount, currenPage, pageSize);//初始化分页工具类
            //数据打包
            map.put("StarRow", pu.getStarRow());
            map.put("PageSize", pu.getPageSize());
            omp.put("Pu", pu);
            omp.put("Data", yzCardRouteMapper.sel_Map(map));
        }catch (Exception e){
         e.printStackTrace();
        }

        return omp;
    }

    @Override
    public boolean delete(Map<String, Object> map) {
        return yzCardRouteMapper.delete(map) > 0;
    }

    @Override
    public Map<String, Object> find_route(Map map) {
        // return yzCardRouteMapper.find_route(map);
        return null;
    }

    @Override
    @Transactional
    public boolean update(Map<String, Object> map) {
        boolean bool = false, bool_upd = false, bool_upd_index = false;
        if(map.get("sync_field")!=null && map.get("sync_field").toString().length()>1){
            List<String> sync_field = (List<String>) map.get("sync_field");
            StringBuffer fieldStr = new StringBuffer();
            if (sync_field.size()>=1){
                fieldStr.append(sync_field.get(0));
                for (int i = 1; i < sync_field.size(); i++) {
                    fieldStr.append(","+sync_field.get(i));
                }
            }else {
                if(sync_field.size()!=0){
                    fieldStr.append(sync_field.get(0));
                }
            }
            map.put("sync_field",fieldStr.toString());
        }
        try {
            bool_upd = yzCardRouteMapper.update(map) > 0;
            //bool_upd_index = updateCardIndex(map);
        } catch (Exception e) {
            System.out.println("通道更改 操作回滚 》 setRollbackOnly ");
        }
        Map<String, String> Ram = new HashMap<>();
        String channel_id = map.get("cd_id").toString();
        Ram.put("channel_id", channel_id);
        yzCardRouteMapper.deleteID(Ram);

        List<Map<String, String>> ruepkeArr = new ArrayList<>();
        List<String> list = (List<String>) map.get("package_id");
        String cd_id = map.get("cd_id").toString();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, String> Hap = new HashMap<>();
                Hap.put("channel_id", cd_id);//生成ID
                Hap.put("package_id", list.get(i).toString());
                ruepkeArr.add(Hap);
            }
            cardPackageMapper.ruepkeAdd(ruepkeArr);
        }
        return bool_upd;
    }

    @Override
    public boolean update_cd_status(Map<String, Object> map) {
        return yzCardRouteMapper.update_cd_status(map) > 0;
    }

    @Override
    @Transactional
    public boolean add(Map<String, Object> map) {
        //默认值失败
        boolean add = false;

        String syncFieldJsonArrayToString = map.get("sync_field").toString();
        map.put("sync_field", syncFieldJsonArrayToString);

        //当不为空再进入下一步
        add = yzCardRouteMapper.add(map) > 0;
        //新建数组
        List<Map<String, String>> ruepkeArr = new ArrayList<>();
        //填入包ID
        List<String> list = (List<String>) map.get("package_id");
        //cd_id转为string
        String cd_id = map.get("cd_id").toString();
        //循环填入频道ID与包ID
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, String> Hap = new HashMap<>();
                Hap.put("channel_id", cd_id);//生成ID
                Hap.put("package_id", list.get(i).toString());
                ruepkeArr.add(Hap);
            }
            cardPackageMapper.ruepkeAdd(ruepkeArr);
        }
        return add;
    }

    @Override
    public Map<String, Object> updateControlType(Map<String, Object> map) {
        Map<String, Object> Rmap = new HashMap<String, Object>();
        boolean bool = false;
        //System.out.println(map.toString());
        List<String> iccids = (List<String>) map.get("iccids");

        /*List<String> iccidArr= carChangeMapper.findInAgentIccid(map);
        if(iccids.size()==iccidArr.size()){
            map.put("iccidArr",iccidArr);
            bool = carChangeMapper.updateControlType(map)>0;
        }else{
            Rmap.put("msg","更改号码总数： [ "+iccids.size()+" ] 其中您所属号码 总数 ：[ "+iccidArr.size()+" ] ; 请核实您的号码信息后重试！");
        }*/
        Rmap.put("bool", bool);
        return Rmap;
    }

    @Override
    public List<Map<String, Object>> find_route_list(Map<String, Object> map) {
        return yzCardRouteMapper.find_route_list(map);
    }

    @Override
    public List<Map<String, Object>> find_sp() {
        return yzCardRouteMapper.find_sp();
    }

    @Override
    public List<Map<String, Object>> find_cr() {
        return yzCardRouteMapper.find_cr();
    }

    @Override
    public Map<String, Object> find(Map<String, Object> map) {
        return yzCardRouteMapper.find(map);
    }

    @Override
    public String exportData(Map<String, Object> map, SysUser User) {

        map.remove("pageNum");
        map.remove("pageSize");

        String create_by = " [ " + User.getDept().getDeptName() + " ] - " + " [ " + User.getUserName() + " ] ";
        String newName = UUID.randomUUID().toString().replace("-", "") + "_ChannelOut";
        String agent_id = User.getDept().getDeptId().toString();

        String task_name = "上游通道 [导出] ";
        String SaveUrl = "/getcsv/" + newName + ".csv";

        Map<String, Object> task_map = new HashMap<String, Object>();
        task_map.put("auth", create_by);
        task_map.put("task_name", task_name);
        task_map.put("url", SaveUrl);
        task_map.put("agent_id", agent_id);
        task_map.put("type", "4");
        yzExecutionTaskMapper.add(task_map);//添加执行 任务表
        List<Map<String, Object>> outChannelArr = yzCardRouteMapper.outChannel(map);

        //1.判断 是否查询到数据
        if (outChannelArr.size() > 0) {
            String Outcolumns[] = {"id", "通道编号", "通道名称", "通道注册用户名", "通道用户登录密码", "通道key码", "通道url", "运营商类型1移动2联通3电信4全网通", "线程数", "是否轮询:0否1是", "控制类型", "达量断网阈值", "创建时间", "修改时间", "删除时间", "代理商id", "通道状态 1 启用 2 停用 3 已删除", "通道别名"};
            String keys[] = {"cd_id", "cd_code", "cd_name", "cd_username", "cd_pwd", "cd_key", "cd_url", "cd_operator_type", "cd_concurrency", "cd_lunxun", "cd_control_type", "cd_max_use", "createTime", "updateTime", "cd_delete", "cd_agent_id", "cd_status", "cd_alias"};
            writeCSV.Write(newName, outChannelArr, Outcolumns, null, keys);
            yzExecutionTaskMapper.set_end_time(task_map);//任务结束
            return "已下发执行日志可在【系统管理】》【日志管理】》【执行日志】查看";
        } else {
            return "您当前的筛选的查询条件 未找到数据！导出任务取消！";
        }
    }

    @Override
    public List<Map<String, Object>> RouteList(Map map) {
        return  yzCardRouteMapper.ruepkeFz(map);
    }


    @Override
    public List<Map<String, Object>> operatorName(Map map) {
        return yzCardRouteMapper.operatorName(map);
    }



    /*public boolean updateCardIndex(Map<String, Object> map){
        Map<String, Object> ParMap = new HashMap<String, Object>();
        int cd_control_type = Integer.parseInt(map.get("cd_control_type").toString());//控制类型
        String is_max_use = map.get("is_max_use")!=null?map.get("is_max_use").toString():"";//达量断网阈值
        String  is_limit_rules = map.get("is_limit_rules")!=null?map.get("is_limit_rules").toString():"";//限速规则

        String is_speed_limit = "0";//是否限速  0 无 1 是
        String is_disconnected = "0";//是否断网 0 无 1 是

        if(cd_control_type ==1){// 限速
            is_speed_limit = "1";
        }else if(cd_control_type ==2) {// 达量断网
            is_disconnected = "1";
        }
        ParMap.put("is_max_use",is_max_use);
        ParMap.put("is_limit_rules",is_limit_rules);
        ParMap.put("is_speed_limit",is_speed_limit);
        ParMap.put("is_disconnected",is_disconnected);

        ParMap.put("related_cd_id",map.get("cd_id"));
        ParMap.put("agent_id",map.get("cd_agent_id"));
        //System.out.println(map.toString());
        //System.out.println(ParMap.toString());
        //return  carChangeMapper.update(ParMap)>0;
        return false;
    }*/

}
