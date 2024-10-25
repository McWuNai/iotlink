package com.yunze.cn.util;

import org.springframework.stereotype.Component;

@Component
public class WxGetShowStatIdArr {


    public  String GetShowStatId (String status_id){
        //卡状态描述 转换 卡状态
        String status_ShowId = "8";
        if(status_id.equals("13") || status_id.equals("14") || status_id.equals("22") || status_id.equals("19")){
            status_ShowId = "1";
        }if(status_id.equals("17") ){
            status_ShowId = "2";
        }if(status_id.equals("9") || status_id.equals("7") || status_id.equals("15")){
            status_ShowId = "3";
        }if(status_id.equals("1")){
            status_ShowId = "4";
        }if(status_id.equals("3") || status_id.equals("2") || status_id.equals("10") || status_id.equals("11") ||
                status_id.equals("20")|| status_id.equals("30")|| status_id.equals("31") || status_id.equals("32") ||
                status_id.equals("21")|| status_id.equals("27")|| status_id.equals("33") || status_id.equals("12")){
            status_ShowId = "5";
        }if(status_id.equals("4")){
            status_ShowId = "6";
        }if(status_id.equals("16")|| status_id.equals("23")|| status_id.equals("24") || status_id.equals("5") ||
                status_id.equals("6") || status_id.equals("28")|| status_id.equals("26") || status_id.equals("18")||
                status_id.equals("25")){
            status_ShowId = "7";
        }if(status_id.equals("8") || status_id.equals("29")){
            status_ShowId = "8";
        }
        return  status_ShowId;
    }

}
