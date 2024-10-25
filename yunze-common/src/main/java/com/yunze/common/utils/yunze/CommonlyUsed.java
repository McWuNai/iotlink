package com.yunze.common.utils.yunze;

import java.util.List;

/**
 * 常用工具类
 * 20201218 ~ 至今
 */
public class CommonlyUsed {

    /**
     * 判断 val 是否在 StrArr
     * @param StrArr
     * @param val
     * @return
     */
    public static boolean Val_Is_Arr (List<String> StrArr,String val){
        boolean bool = false;
        for (int i = 0; i < StrArr.size(); i++) {
            if (val.equals(StrArr.get(i))){
                bool = true;
                break;
            }
        }
        return bool;
    }

}
