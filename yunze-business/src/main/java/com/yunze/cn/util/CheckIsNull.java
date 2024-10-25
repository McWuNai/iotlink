package com.yunze.cn.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

public class CheckIsNull {

    /**
     * 描述： 检验非空方法
     */
    public static boolean  isNull(Object object){
        if(null==object){
            return object==null;
        }else if(object instanceof String){
            return object.toString().length()==0;
        }else if(object instanceof Collection<?>){
            return ((Collection<?>) object).isEmpty();
        }else if(object instanceof Map<?,?>){
            return ((Map<?, ?>) object).isEmpty();
        }
        return false;
    }

    /**
     * 获取指定月份的最后一天
     * 传 1：表示本月月底
     * @param num
     * @return
     */
    public static String getAssignMonths(int num){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, num-1);
        int day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd ");
        return sim.format(cal.getTime())+"23:59:59";
    }

    /**
     * 判断 object是不是空     不是返回自己，是空 返回参数二
     * @param obj
     * @param ifNull
     * @return
     */
    public static Object nvl(Object obj, Object ifNull) {
        if ( obj == null ) return ifNull;
        String str = String.valueOf(obj);
        if ( str == null ) return ifNull;
        str = str.trim();
        if (  str.equals("") || "null".equals(str) || " ".equals(str.trim()) || "undefined".equals(str)) {
            return ifNull;
        } else {
            return obj;
        }
    }


}
