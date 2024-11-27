package com.yunze.apiCommon.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 卡状态 转换类
 * @Auther: zhang feng
 * @Date: 2022/03/03/18:23
 * @Description:
 */
@Component
public class CardStatusReplacementUtil {



    /**
     * 接口返回状态装换成系统状态
     * 获取 IoTLink 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getIoTLinkCardStatus(Integer StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        Integer statusCode = 0;
        String statusMessage = "";
        //库存	1
        //可测试	2
        //待激活	3
        //已激活	4
        //已停机	5
        //预销户	6
        //已销户	7
        //未知	8
        if (StatusCd == 1 ) {
            statusCode = 19;
            statusMessage = "库存";

        }else if (StatusCd == 2) {
            statusCode = 17;
            statusMessage = "可测试";
        }else if (StatusCd == 3) {
            statusCode = 7;
            statusMessage = "待激活";
        } else if (StatusCd == 4) {
            statusCode = 1;
            statusMessage = "正常";
        } else if (StatusCd == 5 ) {
            statusCode = 2;
            statusMessage = "停机";
        }else if (StatusCd == 6) {
            statusCode = 4;
            statusMessage = "预销号";
        }else if (StatusCd == 7) {
            statusCode = 18;
            statusMessage = "销号";
        }else if (StatusCd == 8) {
            statusCode = 8;
            statusMessage = "未知";
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }




    /**
     * 获取卡状态
     * @param StatusCd
     * @return
     */
    public Map<String,Object> getStates(String StatusCd){
        Map<String,Object> map = new HashMap<>();
        int statusCode = 0;
        String statusMessage ="";
        // 正常• 单项停机• 停机• 预销号• 销号• 过户• 休眠• 待激活• 未知
        switch (StatusCd){
            case "正常":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "单向停机":
                statusCode = 3;
                statusMessage = "单向停机";
                break;
            case "停机":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "预销号":
                statusCode = 4;
                statusMessage = "预销号";
                break;
            case "预注销":
                statusCode = 4;
                statusMessage = "预销号";
                break;
            case "销号":
                statusCode = 18;
                statusMessage = "销号";
                break;
            case "过户":
                statusCode = 5;
                statusMessage = "过户";
                break;
            case "休眠":
                statusCode = 6;
                statusMessage = "休眠";
                break;
            case "待激活":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "号码不存在":

                break;
            case "已激活":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "激活":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "已停用":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "已失效":
                statusCode = 18;
                statusMessage = "销号";
                break;
            case "可测试":
                statusCode = 17;
                statusMessage = "可测试";
                break;
            case "库存":
                statusCode = 19;
                statusMessage = "库存";
                break;
            case "可激活":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "去激活":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "测试期正常":
                statusCode = 9;
                statusMessage = "测试期正常";
                break;
            case "测试去激活":
                statusCode = 9;
                statusMessage = "测试期正常";
                break;
            case "在用":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "运营商管理状态":
                statusCode = 14;
                statusMessage = "运营商管理状态";
                break;
            case "拆机":
                statusCode = 16;
                statusMessage = "拆机";
                break;
            case "销号/拆机":
                statusCode = 16;
                statusMessage = "拆机";
                break;
            case "违章停机":
                statusCode = 20;
                statusMessage = "违章停机";
                break;
            case "测试激活":
                statusCode = 9;
                statusMessage = "测试期正常";
                break;
            case "断网":
                statusCode = 21;
                statusMessage = "断网";
                break;
            case "已断网":
                statusCode = 21;
                statusMessage = "断网";
                break;
            case "运营商注销":
                statusCode = 18;
                statusMessage = "销号";
                break;
            case "欠停":
                statusCode = 27;
                statusMessage = "欠停";
                break;
            case "挂失":
                statusCode = 28;
                statusMessage = "挂失";
                break;
            case "故障卡":
                statusCode = 29;
                statusMessage = "故障卡";
                break;
            case "未实名停机":
                statusCode = 30;
                statusMessage = "未实名停机";
                break;
            case "超量停机":
                statusCode = 31;
                statusMessage = "超量停机";
                break;
            case "停机保号":
                statusCode = 32;
                statusMessage = "停机保号";
                break;
            case "已锁定":
                statusCode = 33;
                statusMessage = "已锁定";
                break;
        }
        map.put("statusCode",statusCode);
        map.put("statusMessage",statusMessage);
        return map;
    }




    /**
     * 接口返回状态装换成系统状态
     * 获取移动Ec 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getEcV5CardStatus(Integer StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        Integer statusCode = 0;
        String statusMessage = "";
        //   1：待激活2：已激活4：停机6：可测试7：库存 8：预销户
        if (StatusCd == 1 ) {
            statusCode = 7;
            statusMessage = "待激活";
        }else if (StatusCd == 6) {
            statusCode = 17;
            statusMessage = "可测试";
        }else if (StatusCd == 7) {
            statusCode = 19;
            statusMessage = "库存";
        } else if (StatusCd == 2) {
            statusCode = 1;
            statusMessage = "正常";
        } else if (StatusCd == 4 ) {
            statusCode = 2;
            statusMessage = "停机";
        }else if (StatusCd == 8) {
            statusCode = 4;
            statusMessage = "预销号";
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }




    /**
     * 接口返回状态装换成系统状态
     * DongXin_ECV2 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getDongXin_ECV2_CardStatus(String StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        Integer statusCode = 0;
        String statusMessage = "";
        // ⚫ 00 -正常；⚫  01-单向停机；⚫  02-停机；⚫  03-预销号；	04-销号；⚫  05-过户；⚫  06-休眠；⚫  07-待激活；⚫  99-号码不存在
        if (StatusCd.equals("07")) {
            statusCode = 7;
            statusMessage = "待激活";
        } else if (StatusCd.equals("00")) {
            statusCode = 1;
            statusMessage = "正常";
        } else if (StatusCd.equals("01")) {
            statusCode = 3;
            statusMessage = "单向停机";
        } else if (StatusCd.equals("03")) {
            statusCode = 4;
            statusMessage = "预销号";
        } else if (StatusCd.equals("04")) {
            statusCode = 18;
            statusMessage = "销号";
        }  else if (StatusCd.equals("05")) {
            statusCode = 5;
            statusMessage = "过户";
        } else if (StatusCd.equals("06")) {
            statusCode = 6;
            statusMessage = "休眠";
        }else if ( StatusCd.equals("02")) {
            statusCode = 2;
            statusMessage = "停机";
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }






    /**
     * 接口返回状态装换成系统状态
     * 获取 联通_CMP 卡状态
     * @param StatusCd
     * @return
     */
    public  Integer getLianTong_CMP_CardReturnStatus(Integer StatusCd){
        int statusCode = 7;
        // "0": 可测试,"1": 可激活, "2": 已激活, "3": 已停用, "4": 已失效, "5"": 已清除, "6": 已更换, "7": 库存, "8": 开始
        if (StatusCd == 7 ) {
            statusCode = 0;
        } else if (StatusCd == 9) {
            statusCode = 1;
        } else if (StatusCd == 19) {
            statusCode = 7;
        } else if (StatusCd == 22) {
            statusCode = 8;
        }else if (StatusCd == 1) {
            statusCode = 2;
        } else if (StatusCd == 2 ) {
            statusCode = 3;
        }else if (StatusCd == 23) {
            statusCode = 4;
        }else if (StatusCd == 24) {
            statusCode = 5;
        }else if (StatusCd == 25) {
            statusCode = 6;
        }
        return statusCode;
    }








    /**
     * 接口返回状态装换成系统状态
     * 获取 联通_CMP 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getLianTong_CMP_CardStatus(Integer StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        Integer statusCode = 0;
        String statusMessage = "";
        // "0": 可测试,"1": 可激活, "2": 已激活, "3": 已停用, "4": 已失效, "5"": 已清除, "6": 已更换, "7": 库存, "8": 开始
        if (StatusCd == 0 ) {
            statusCode = 7;
            statusMessage = "待激活";
        } else if (StatusCd == 1) {
            statusCode = 9;
            statusMessage = "可激活";
        } else if (StatusCd == 7) {
            statusCode = 19;
            statusMessage = "库存";
        } else if (StatusCd == 8) {
            statusCode = 22;
            statusMessage = "开始";
        }else if (StatusCd == 2) {
            statusCode = 1;
            statusMessage = "正常";
        } else if (StatusCd == 3 ) {
            statusCode = 2;
            statusMessage = "停机";
        }else if (StatusCd == 4) {
            statusCode = 23;
            statusMessage = "已失效";
        }else if (StatusCd == 5) {
            statusCode = 24;
            statusMessage = "已清除";
        }else if (StatusCd == 6) {
            statusCode = 25;
            statusMessage = "已更换";
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }


    /**
     * 接口返回状态装换成系统状态
     * 获取 电信 CMP 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getDianXin_CMP_CardStatus(Integer StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        Integer statusCode = 0;
        String statusMessage = "";
        // 1：可激活 2：测试激活 3：测试去激活 4：在用 5：停机 6：运营商管理状态 7：拆机
        switch (StatusCd){
            case 1:
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case 2:
                statusCode = 9;
                statusMessage = "测试期正常";
                break;
            case 3:
                statusCode = 15;
                statusMessage = "测试去激活";
                break;
            case 4:
                statusCode = 1;
                statusMessage = "正常";
                break;
            case 5:
                statusCode = 2;
                statusMessage = "停机";
                break;
            case 6:
                statusCode = 14;
                statusMessage = "运营商管理状态";
                break;
            case 7:
                statusCode = 16;
                statusMessage = "拆机";
                break;
            default :
                statusCode = 8;
                statusMessage = "未知";
                break;
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }




    /**
     * 接口返回状态装换成系统状态
     * 获取 电信 DCP  卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getDianXin_DCP_CardStatus(String StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        Integer statusCode = 0;
        String statusMessage = "";
        // Active （ 已 激 活 ）Deactivated （去激活） Paused （ 停 机 保 号 ）Terminated （拆机）
        switch (StatusCd) {
            case "Active":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "Pause":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "Deactivated":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "Terminated":
                statusCode = 16;
                statusMessage = "拆机";
                break;
            default:
                break;
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }



    /**
     * 接口返回状态装换成系统状态
     * 获取 TenngYu 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getTenngYu_CardStatus(String StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        Integer statusCode = 0;
        String statusMessage = "";
        switch (StatusCd){
            case "1":
                statusCode = 17;
                statusMessage = "可测试";
                break;
            case "2":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "3":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "4":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "5":
                statusCode = 23;
                statusMessage = "已失效";
                break;
            case "6":
                statusCode = 24;
                statusMessage = "已清除";
                break;
            case "7":
                statusCode = 25;
                statusMessage = "已清除";
                break;
            case "8":
                statusCode = 19;
                statusMessage = "库存";
                break;
            case "9":
                statusCode = 22;
                statusMessage = "开始";
                break;
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }







    /**
     * 接口返回状态装换成系统状态
     * 获取 ChenZe 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getChenZe_CardStatus(String StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        //卡生命周期状态(1:待激活；2:已激活；3:---；4:停机；5:停机保号；8:预销户；9:已销户)
        Integer statusCode = 0;
        String statusMessage = "";
        switch (StatusCd){
            case "1":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "2":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "3":
                statusCode = 8;
                statusMessage = "未知";
                break;
            case "4":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "5":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "8":
                statusCode = 4;
                statusMessage = "预销号";
                break;
            case "9":
                statusCode = 18;
                statusMessage = "销号";
                break;
        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }


    /**
     * 接口返回状态装换成系统状态
     * 获取 DianXin_CMP_5G 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getDianXin_CMP_5G_CardStatus(String StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        //卡生命周期状态(1：可激活 2：测试激活 3：测试去激活 4：在用 5：停机6：运营商管理状态)
        Integer statusCode = 0;
        String statusMessage = "";
        switch (StatusCd){
            case "1":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "2":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "3":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "4":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "5":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "6":
                statusCode = 14;
                statusMessage = "运营商管理状态";
                break;

        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }



    /**
     * 接口返回状态装换成系统状态
     * 获取 MwFengZuShou 卡状态
     * @param StatusCd
     * @return
     */
    public  Map<String,Object> getMwFengZuShou_CardStatus(String StatusCd){
        Map<String,Object> Rmap = new HashMap<>();
        //卡生命周期状态(0 测试 1 沉默 2 使用 3 当月停用 4 预约销户 5 人工停用 6 到期停用 7 渠道停用 8 运营商停用)
        Integer statusCode = 0;
        String statusMessage = "";
        switch (StatusCd){
            case "0":
                statusCode = 17;
                statusMessage = "可测试";
                break;
            case "1":
                statusCode = 7;
                statusMessage = "待激活";
                break;
            case "2":
                statusCode = 1;
                statusMessage = "正常";
                break;
            case "3":
                statusCode = 2;
                statusMessage = "停机";
                break;
            case "4":
                statusCode = 4;
                statusMessage = "预销号";
                break;
            case "5":
                statusCode = 3;
                statusMessage = "单项停机";
                break;
            case "6":
                statusCode = 31;
                statusMessage = "超量停机";
            case "7":
                statusCode = 3;
                statusMessage = "单项停机";
                break;
            case "8":
                statusCode = 20;
                statusMessage = "违章停机";
                break;

        }
        Rmap.put("statusCode",statusCode);
        Rmap.put("statusMessage",statusMessage);
        return Rmap;
    }




}
