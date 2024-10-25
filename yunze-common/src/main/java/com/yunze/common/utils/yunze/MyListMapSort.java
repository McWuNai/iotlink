package com.yunze.common.utils.yunze;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * list Map 排序工具
 * @Auther: zhang feng
 * @Date: 2022/03/16/16:07
 * @Description:
 */
public class MyListMapSort {


    /**
     * 排序  List<Map<String, Object>> 版
     * @param sList
     * @param sortType
     * @param conditions
     * @return
     */
    public static List<Map<String, String>> listMapObjectSort(List<Map<String, Object>> sList,String sortType,String... conditions){
        List<Map<String, String>> StrArr = new ArrayList<>();
        for (int i = 0; i < sList.size(); i++) {
            Map<String, Object> map = sList.get(i);
            Map<String, String> sMap = new HashMap<>();
            for(String key:map.keySet()){
                System.out.println("key="+key+"and value=" +map.get(key));
                sMap.put(key,""+map.get(key));
            }
            StrArr.add(sMap);
        }
        return  listMapSort(StrArr,sortType,conditions);
    }




    /**
     * 排序
     * @param sList 需要排序的 List<Map<String, String>>
     * @param sortType 排序类型 asc【升序】 desc【降序】
     * @param conditions 排序条件
     * @return
     */
    public static List<Map<String, String>> listMapSort(List<Map<String, String>> sList,String sortType,String... conditions){
        //先根据 conditions 传入参数 进行排序
        Collections.sort(sList, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String sort1 = "";//从list中拿出来第一个的 conditions 拼接到一起
                String sort2 = "";//从list中拿出来第二个的 conditions 拼接到一起
                for (int i = 0; i < conditions.length; i++) {
                    sort1 += o1.get(conditions[i]);
                    sort2 += o2.get(conditions[i]);
                }
                if(sortType.equalsIgnoreCase("asc")){
                    return sort1.compareTo(sort2);//利用String类的compareTo方法
                }else{
                    return sort2.compareTo(sort1);//利用String类的compareTo方法
                }
            }
        });
        return  sList;
    }





    public static void main(String[] args) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("iccid", "C0191971");
        map1.put("msisdn", "F31971");
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("iccid", "C0191972");
        map2.put("msisdn", "F31972");
        Map<String, String> map3 = new HashMap<String, String>();
        map3.put("iccid", "C0191973");
        map3.put("msisdn", "F31973");
        Map<String, String> map4 = new HashMap<String, String>();
        map4.put("iccid", "C0191974");
        map4.put("msisdn", "F31974");
        Map<String, String> map5 = new HashMap<String, String>();
        map5.put("iccid", "C0191981");
        map5.put("msisdn", "F31981");
        list.add(map1);
        list.add(map3);
        list.add(map2);
        list.add(map5);
        list.add(map4);
        //排序前
        System.out.println("---------------排序前------------------");
        for (Map<String, String> map : list) {
            System.out.println(map);
        }
        list = listMapSort(list,"asc","iccid","msisdn");
        //排序后
        System.out.println("---------------排序后 asc ------------------");
        for (Map<String, String> map : list) {
            System.out.println(map);
        }
        list = listMapSort(list,"desc","iccid","msisdn");
        //排序后
        System.out.println("---------------排序后 desc ------------------");
        for (Map<String, String> map : list) {
            System.out.println(map);
        }

    }



}
