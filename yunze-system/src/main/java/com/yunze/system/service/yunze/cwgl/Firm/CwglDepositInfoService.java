
package com.yunze.system.service.yunze.cwgl.Firm;

import com.yunze.common.core.domain.entity.SysUser;

import java.util.List;
import java.util.Map;

public interface CwglDepositInfoService {


     public Map<String,Object> sel_CT_data_DS(Map map);

     /**
      * 修改商品信息
      * @param map
      * @return
      * @throws Exception
      */
     public boolean add_CDIO(Map<String, Object> map);

     /**
      * 修改状态
      * */
     public boolean updateID(Map<String,Object> map);

     /**
      * 查询 入款详情表
      * */
     public List<Map<String,Object>> find_CDIDS (Map map);




     /**
      * 查询本年入款总计和为入款
      * */
     public List<Map<String,Object>> sel_Year_data_CT(Map map);


     /**
      * 入款详情查看
      * */
     public Map<String,Object> sleDetails (Map map);

     /**
      * 入款 月 月入款 月未到款 金额合计
      * */
     public List<Map<String,Object>> sel_Month_data_CT(Map map);

     /**导出*/
     public String ExportRemittance(Map<String, Object> map, SysUser User);

     /**导出*/
     public String ExportIncome(Map<String, Object> map, SysUser User);


     /**
      * 新增 入款详情表
      * */
     public boolean add_DIDS(Map<String,Object> map);

     /**
      * 删除 入款详情表
      * */
     public boolean del_DIDS_all(Map<String,Object> map);

}
