package com.yunze.cn.util;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.utils.Arith;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.cn.config.RabbitMQConfig;
import com.yunze.cn.mapper.YzCardFlowHisMapper;
import com.yunze.cn.mapper.YzCardFlowMapper;
import com.yunze.cn.mapper.YzCardMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡用量同步
 * @Auther: zhang feng
 * @Date: 2021/07/20/15:19
 * @Description:
 */
@Component
public class CardFlowSyn {

    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    @Resource
    private YzCardFlowHisMapper yzCardFlowHisMapper;
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 用量计算 [直接同步 yzCardMapper ]
     * @param iccid
     * @param ApiUsed
     * @return
     */
    public Map<String,Object> CalculationFlow(String iccid,Double ApiUsed){
        Map<String,Object> Rmap =  CalculationFlowCommon(iccid, ApiUsed);
        Double SumFlow = Double.parseDouble(Rmap.get("SumFlow").toString());
        Double total_show_flow = Double.parseDouble(Rmap.get("total_show_flow").toString());
        boolean bool_info = false;
        Double remaining = Arith.sub(SumFlow,total_show_flow);
        Map<String,Object> updUsedMap = new HashMap<>();
        updUsedMap.put("iccid",iccid);
        updUsedMap.put("used",total_show_flow);
        updUsedMap.put("remaining",remaining);
        bool_info = yzCardMapper.updUsed(updUsedMap)>0;
        //返回数据
        Rmap.put("used",total_show_flow);
        Rmap.put("remaining",remaining);
        Rmap.put("bool_info",bool_info);
        return  Rmap;
    }



    /**
     * 用量计算 [queue 队列同步 yzCardMapper ]
     * @param iccid
     * @param ApiUsed
     * @return
     */
    public Map<String,Object> CalculationFlowQueue(String iccid,Double ApiUsed,boolean bool){
        Map<String,Object> Rmap =  CalculationFlowCommon(iccid, ApiUsed);
        Double SumFlow = Double.parseDouble(Rmap.get("SumFlow").toString());
        Double total_show_flow = Double.parseDouble(Rmap.get("total_show_flow").toString());
        String bool_info = "";
        Double remaining = Arith.sub(SumFlow,total_show_flow);
        Map<String,Object> updUsedMap = new HashMap<>();
        updUsedMap.put("iccid",iccid);
        updUsedMap.put("used",total_show_flow);
        updUsedMap.put("remaining",remaining);
        //1.创建路由 绑定 生产队列 发送消息
        String card_exchangeName = "admin_card_exchange", card_queueName = "admin_CardUpd_queue", card_routingKey = "admin.CardUpd.queue",
                card_del_exchangeName = "dlx_"+card_exchangeName,card_del_queueName = "dlx_"+card_queueName, card_del_routingKey = "dlx_"+card_routingKey;
        try {
            if(bool){
                try {
                   // rabbitMQConfig.creatExchangeQueue(card_exchangeName, card_queueName, card_routingKey, card_del_exchangeName, card_del_queueName, card_del_routingKey,null);
                }catch (Exception e){
                    System.out.println("CalculationFlowQueue   》creatExchangeQueue "+e.getMessage());
                }
            }
            updUsedMap.put("queueTypeName","admin_CardUpdUsed_queue");
            rabbitTemplate.convertAndSend(card_exchangeName, card_routingKey, JSON.toJSONString(updUsedMap), message -> {
                // 设置消息过期时间 30 分钟 过期
                message.getMessageProperties().setExpiration("" + (30 * 1000 * 60));
                return message;
            });
            bool_info = "发送 主表修改 指令成功 ！";
        } catch (Exception e) {
            System.out.println("用量计算 队列同步 创建 失败 " + e.getMessage().toString());
            bool_info = e.getMessage().toString();
            bool_info = bool_info.length()>500?bool_info.substring(0,500):bool_info;
        }



        //返回数据
        Rmap.put("used",total_show_flow);
        Rmap.put("remaining",remaining);
        Rmap.put("bool_info",bool_info);
        return  Rmap;
    }


    /**
     * 用量计算
     * @param iccid
     * @param ApiUsed
     * @return
     */
    public Map<String,Object> CalculationFlowCommon(String iccid,Double ApiUsed){
        Map<String,Object> Rmap = new HashMap<>();
        boolean bool_info=false,bool_flow=false,bool_flowHis=false;
        Map<String,Object> findMap = new HashMap<>();
        findMap.put("iccid",iccid);

        String  []YyyyAndMm = VeDate.getYyyyAndMm();
        String nowMonthSta = YyyyAndMm[0]+"-"+YyyyAndMm[1]+"-01";
        String nowMonthEnd = VeDate.getLastDayOfMonth(Integer.parseInt(YyyyAndMm[0]),Integer.parseInt(YyyyAndMm[1]));
        findMap.put("nowMonthSta",nowMonthSta+" 00:00:00");
        findMap.put("nowMonthEnd",nowMonthEnd+" 23:59:59");
        //0.修改 时间到期资费计划
        yzCardFlowMapper.updStatus(findMap);
        //1.查询过期用量
        String SumErrorFlow = yzCardFlowMapper.findInvalidationSumErrorFlow(findMap);
        Double SumErrorFlow_D = SumErrorFlow!=null && SumErrorFlow.length()>0?Double.parseDouble(SumErrorFlow):0.0;

        Double DseFlow = SumErrorFlow!=null && SumErrorFlow.length()>0?Double.parseDouble(SumErrorFlow):0.0;

/*


        SumErrorFlow_D = Arith.add(SumErrorFlow_Year_D,SumErrorFlow_D);
        DseFlow = Arith.add(SumErrorFlow_Year_D,DseFlow);*/

        //当前计算 = 接口用量 - 已到期 用量
        Double cl_Used = Arith.sub(ApiUsed,DseFlow);
        Double used = DseFlow;//主表 账期 已用

        //2.获取 有效资费计划 计算
        Double xiShu = 1.0;//默认

        double sumTrue_flow = 0.0;
        double sumUse_so_flow = 0.0;

        List<Map<String,Object>> InEffectArr =  yzCardFlowMapper.findInEffect(findMap);
        if(InEffectArr!=null && InEffectArr.size()>0){
            Double UdF = cl_Used+0;
            for (int i = 0; i < InEffectArr.size(); i++) {
                Map<String,Object> Pobj = InEffectArr.get(i);
                Double true_flow = Double.parseDouble(Pobj.get("true_flow").toString());
                Double error_flow = Double.parseDouble(Pobj.get("error_flow").toString());
                Double error_time = Double.parseDouble(Pobj.get("error_time").toString());
                String id = Pobj.get("id").toString();
                xiShu = error_time;//同步包系数
                //   当前计算 用量 - 资费计划 用量 作比较 小等 0 用完了 否则未用完继续 作比较
                UdF = Arith.sub(cl_Used,error_flow);
                if(UdF<0){
                    Double use_so_flow = Arith.mul(cl_Used,error_time);
                    DseFlow = Arith.add(DseFlow,use_so_flow);//累加 主表用量 = 已用 * 系数
                    Map<String,Object> UpdMap = new HashMap<>();
                    UpdMap.put("use_true_flow",cl_Used);
                    UpdMap.put("use_so_flow",use_so_flow);
                    UpdMap.put("status","1");// 生效中
                    UpdMap.put("id",id);
                    bool_flow =  yzCardFlowMapper.updFlow(UpdMap)>0;
                    break;
                }else{
                    cl_Used = Arith.sub(cl_Used,error_flow);
                    DseFlow = Arith.add(Arith.add(DseFlow,error_flow),Math.abs(cl_Used));//累加 主表用量 = 包容量 + 超出部分绝对值
                    Map<String,Object> UpdMap = new HashMap<>();
                    UpdMap.put("use_true_flow",error_flow);
                    UpdMap.put("use_so_flow",true_flow);
                    UpdMap.put("status","0");// 已失效
                    UpdMap.put("id",id);
                    //bool_flow = true;
                    bool_flow =  yzCardFlowMapper.updFlow(UpdMap)>0;
                }
            }
        }else{
            //获取最近过期的资费计划 系数
            String Error_time =  yzCardFlowMapper.FindError_time(findMap);
            xiShu = Error_time!=null && Error_time.length()>0 && Double.parseDouble(Error_time)>=1?Double.parseDouble(Error_time):xiShu;



        }

        //3.同步用量历史表
        String now[] = VeDate.getStringDateShort().split("-");
        findMap.put("year",now[0]);
        findMap.put("month",now[1]);
        findMap.put("day",now[2]);

        String total_flow = yzCardFlowHisMapper.total_flow(findMap);//历史用量 月用量记录
        Double DtFlow = total_flow!=null&&total_flow.length()>0?Double.parseDouble(total_flow):0.0;
        //同步
        Double total_flow_now = Arith.sub(ApiUsed,DtFlow);
        Double total_show_flow = Arith.mul(ApiUsed,xiShu);
        Double total_show_flow_now = Arith.mul(total_flow_now,xiShu);
        Integer isExist = yzCardFlowHisMapper.isExist(findMap);//历史用量 月用量记录
        if(isExist!=null && isExist>0){
            Map<String,Object> editMap = new HashMap<>();
            editMap.putAll(findMap);
            editMap.put("total_flow",ApiUsed);
            editMap.put("total_flow_now",total_flow_now);
            editMap.put("total_show_flow",total_show_flow);
            editMap.put("total_show_flow_now",total_show_flow_now);
            bool_flowHis = yzCardFlowHisMapper.edit(editMap)>0;
        }else{
            Map<String,Object> saveMap = new HashMap<>();
            saveMap.putAll(findMap);
            saveMap.put("total_flow",ApiUsed);
            saveMap.put("total_flow_now",total_flow_now);
            saveMap.put("total_show_flow",total_show_flow);
            saveMap.put("total_show_flow_now",total_show_flow_now);
            bool_flowHis = yzCardFlowHisMapper.save(saveMap)>0;
        }

        //同步主表用量
        Double SumFlow = 0.0;
        SumFlow = Arith.add(SumFlow,SumErrorFlow_D);
        if(InEffectArr!=null && InEffectArr.size()>0){
            for (int i = 0; i < InEffectArr.size(); i++) {
                Map<String,Object> Pobj = InEffectArr.get(i);
                Double true_flow = Double.parseDouble(Pobj.get("true_flow").toString());
                SumFlow = Arith.add(SumFlow,true_flow);
            }

        }else{

            //查询该卡是否已经订购过资费计划 且  时间有效状态失效

            //获取时间有效状态失效的资费计划 总量 和 已使用总量
            Map<String,Object> iMap =  yzCardFlowMapper.findInvalidationSum(findMap);
            if(iMap!=null){
                sumTrue_flow = iMap.get("sumTrue_flow")!=null?Double.parseDouble(iMap.get("sumTrue_flow").toString()):sumTrue_flow;
                sumUse_so_flow = iMap.get("sumUse_so_flow")!=null?Double.parseDouble(iMap.get("sumUse_so_flow").toString()):sumUse_so_flow;
                DseFlow = Arith.add(sumUse_so_flow,Arith.mul(cl_Used,xiShu));//目前用量 = 已记录已用加上最近一个 资费计划 系数 * 接口减去已订购失效 真实用量 剩余用量
                SumFlow = sumTrue_flow;//总用量 =  时间有效状态失效 sum（True_flow）
            }else {
                //查询 累计包类型 年包 有年包类型时不做累加
                String SumErrorFlow_Year = yzCardFlowMapper.findInvalidationSumErrorFlow_Year(findMap);
                Double SumErrorFlow_Year_D = SumErrorFlow_Year!=null && SumErrorFlow_Year.length()>0?Double.parseDouble(SumErrorFlow_Year):0.0;
                if(SumErrorFlow_Year_D == 0.0){
                    //未订购过资费计划  但是接口有用量 返回时   主表 账期 已用 += 接口用量 * 系数
                    if(cl_Used>0.0){
                        DseFlow = Arith.add(DseFlow,Arith.mul(cl_Used,xiShu));
                    }
                }
            }
        }



        //返回数据
        Rmap.put("SumFlow",SumFlow);
        Rmap.put("total_show_flow",DseFlow);
        Rmap.put("used",DseFlow);

        Rmap.put("bool_info",bool_info);
        Rmap.put("bool_flowHis",bool_flowHis);
        Rmap.put("bool_flow",bool_flow);
        return  Rmap;
    }




}
