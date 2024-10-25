package com.yunze.system.service.yunze.xsgl.customer;

import com.yunze.common.core.domain.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IYzXsglCustomerService {


    /**
     * 查询
     * @param map
     * @return
     */
    public Map<String,Object> selMap(Map map);
    /**
     * 查询单条数据
     * @param map
     * @return
     */
    public Map <String,Object> find(Map map);


    /**
     * 基础修改
     * @param map
     * @return
     */
    public Map<String,Object> upd(Map map);

    /**
     * 修改 客户状态  是否回收 优惠折扣 客户所属人
     * @param map
     * @return
     */
    public Map<String,Object> updInfo(Map map);

    /**
     * 新增
     * @param map
     * @return
     */
    public Map<String,Object> save(Map map);



    /**
     * 查询 销售 合伙人
     * @param map
     * @return
     */
    public List<Map<String,Object>> findSalesPartner(Map<String, Object> map);



    /**
     * 查询 总管理 或 非总管理 下  客户
     * @param map
     * @return
     */
    public List<Map <String,Object>> IsAdmin(Map map);


    /**
     * 客户划分
     * @param map
     * @return
     */
    public Map<String,Object> Divide(Map map);



    /**
     * 费率设置
     * @param map
     * @return
     */
    public Map<String,Object> Rate(Map map);


    /**
     * 查询客户信息 id 名称
     * @param map
     * @return
     */
    public List<Map <String,Object>> findCustomerArr(Map map);

    /**
     * 查询客户下 简要联系人信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> fnidPeopleArr(Map map);


    /**
     * 查询 客户地址简要信息
     * @param map
     * @return
     */
    public List<Map <String,Object>> fnidAccountArr(Map map);


    /**
     * 批量导入 客户信息
     * @param file
     * @param updateSupport
     * @param User
     * @return
     * @throws IOException
     */
    public String uploadCustomer(MultipartFile file, boolean updateSupport, SysUser User) throws IOException;





}
