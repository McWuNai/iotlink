package com.yunze.web.controller.yunze.card;

import com.yunze.system.service.yunze.IYzAgentPacketService;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 *  代理代理资费计划 管理
 *  2021-06-24
 * @author root
 */
@Api("代理资费计划管理")
@RestController
@RequestMapping("/yunze/agentTariffPlan")
public class YzAgentPacketController extends MyBaseController
{

    @Autowired
    private IYzAgentPacketService agentPacketService;




  /*  *//**
     * 代理资费计划 新增
     *//*
    @Log(title = "代理资费计划", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('yunze:agentTariffPlan:add')")
    @PostMapping(value = "/add", produces = { "application/json;charset=UTF-8" })
    public String add(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Double error_so = Double.parseDouble(Parammap.get("error_so").toString());
            Double packet_flow = Double.parseDouble(Parammap.get("packet_flow").toString());
            Double error_flow = Double.parseDouble(Parammap.get("error_flow").toString());

            if(error_so>=1){
                if(packet_flow>=error_flow) {
                    //校验计算 用量
                    Parammap.put("error_flow",Arith.div(packet_flow,error_so,0));
                    return MyRetunSuccess(agentPacketService.add(Parammap),null);
                }else{
                    return Myerr("资费计划 流量 不能小于 资费计划 用量  ！");
                }
            }else{
                return Myerr("资费计划系数不能 小于 1 ！");
            }

        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:agentTariffPlan:add  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("代理资费计划 新增 操作失败！");
    }



    *//**
     * 代理资费计划 详情
     *//*
    @PreAuthorize("@ss.hasPermi('yunze:agentTariffPlan:find')")
    @PostMapping(value = "/find", produces = { "application/json;charset=UTF-8" })
    public String find(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            //校验计算 用量
            return MyRetunSuccess(agentPacketService.find(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:agentTariffPlan:find  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("代理资费计划 详情 操作失败！");
    }


    *//**
     * 代理资费计划 编辑
     *//*
    @Log(title = "代理资费计划", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:agentTariffPlan:edit')")
    @PostMapping(value = "/edit", produces = { "application/json;charset=UTF-8" })
    public String edit(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Double error_so = Double.parseDouble(Parammap.get("error_so").toString());
            Double packet_flow = Double.parseDouble(Parammap.get("packet_flow").toString());
            Double error_flow = Double.parseDouble(Parammap.get("error_flow").toString());

            Double packet_price = Double.parseDouble(Parammap.get("packet_price").toString());
            Double packet_cost = Double.parseDouble(Parammap.get("packet_cost").toString());

            if(error_so>=1){
                if(packet_flow>=error_flow) {
                    //校验计算 用量
                    if(packet_price>=packet_cost){
                        //售价大等于 成本
                        Parammap.put("error_flow",Arith.div(packet_flow,error_so,0));
                        return MyRetunSuccess(agentPacketService.update(Parammap),null);
                    }else{
                        return Myerr("售价  不能小于 成本 ！");
                    }
                }else{
                    return Myerr("资费计划 流量 不能小于 资费计划 用量  ！");
                }
            }else{
                return Myerr("资费计划系数不能 小于 1 ！");
            }

        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:agentTariffPlan:edit  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.toString());
        }
        return Myerr("代理资费计划 编辑 操作失败！");
    }
*/





}
