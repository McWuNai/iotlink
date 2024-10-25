package com.yunze.system.service.yunze;


import com.yunze.common.core.domain.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 卡板信息 业务层
 * @author root
 */
public interface IYzOrderService
{
    /**
     *查询
     * @param map
     * @return
     */
    public Map<String,Object> selMap(Map<String, Object> map);


    /**
     * 获取单卡详情
     * @param map
     * @return
     */
    public Map<String,Object> find(Map<String, Object> map);




    /**
     *按当前赛选条件获取导出数据 iccid
     * @param map
     * @return
     */
    public List<String> outOrder(Map<String, Object> map);




    /**
     * 查询订单加包参数
     * @param map
     * @return
     */
    public Map<String,Object> findOrder(Map<String, Object> map);



    /**
     * 平台导入充值
     * @param file
     * @return
     * @throws IOException
     */
    public String importRecharge(MultipartFile file, Map<String, Object> map) throws IOException ;


    /**导出*/
    public String exportallorders(Map<String, Object> map,SysUser User);


    /**
     * 卡号导入查询
     * @param file
     * @return
     * @throws IOException
     */
    public Map<String, Object> CardNumberImport(MultipartFile file, Map<String, Object> map) throws IOException ;


    /**
     * 退订 不操作 下面的内容
     *
     * @return*/
    public String NoTbscribe(Map map, SysUser User);

     /**
      * 勾选执行加包
      * */
     public String getPackage (Map map);



    /**
     * 平台充值 【文本域】
     * @return
     * @throws IOException
     */
    public String TextRecharge(Map<String, Object> map) throws IOException ;



    /**
     * 企业充值 【文本域】
     * @return
     * @throws IOException
     */
    public Map<String, Object> DeptTextRecharge(Map<String, Object> map, SysUser operateMap) throws IOException ;


    /**
     * 查询已充值的卡号数据
     * @param map
     * @return
     */
    public List<String> findRecharged(Map<String, Object> map);
    /**
     * API续费订单审核状态 触发回调通知
     * @param map
     * @return
     */
    public Map<String, Object> orderAudit(Map<String, Object> map);

}
