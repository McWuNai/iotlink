package com.yunze.common.utils.yunze;



import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * List<Map> 帮助类 主要用户 TDengine 数据转换 别名与 格式化时间 等
 */
public class ListMapUtils {

    /**
     * 分组
     * @param list
     * @param byStr
     * @return
     */
    public static List<Map<String,Object>> GruoupByKey (List<Map<String,Object>>  list,String byStr){
        if(list!=null){
            Map<String, Object> iMap = new HashMap<>();
            List<Map<String,Object>>  rList = new ArrayList<>();
            if(list!=null && list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    Map<String,Object> obj = list.get(i);
                    String byKey = obj.get(""+byStr)!=null?obj.get(""+byStr).toString():null;
                    if(iMap.get(""+byKey)==null){
                        iMap.put(""+byKey,obj);
                        rList.add(obj);
                    }
                }
            }
            return  rList;
        }else {
            return null;
        }
    }

    /**
     * 多条件分组
     * @param list
     * @param byStr
     * @return
     */
    public static List<Map<String,Object>> GruoupByKey (List<Map<String,Object>>  list,String ...byStr){
        if(list!=null){
            List<Map<String,Object>>  rList = new ArrayList<>();
            for (int i = 0; i < byStr.length; i++) {
                rList = GruoupByKey(list,byStr[i]);
                list = rList;
            }
            return  rList;
        }else {
            return null;
        }
    }



    /**
     * 数据库别名替换
     * @param pList
     * @param cMap
     * @return
     */
    public static List<Map<String,Object>> DbAs (List<Map<String,Object>> pList,Map<String,String> cMap){
        if(pList!=null && pList.size()>0){
            List<Map<String,Object>> rList = new ArrayList<>();
            for (int i = 0; i < pList.size(); i++) {
                Map<String,Object> obj = pList.get(i);
                Map<String,Object> newMap = new HashMap<>();
                //循环替换 列明 给列明取个别名
                for(Map.Entry<String,Object > entry:obj.entrySet()){
                    String cName = entry.getKey();
                    for(Map.Entry<String,String > entry1:cMap.entrySet()){
                        if(entry.getKey().equals(entry1.getKey())){
                            cName = entry1.getValue();
                        }
                    }
                    newMap.put(cName,entry.getValue());
                }
                rList.add(newMap);
            }
            return rList;
        }else {
            return null;
        }
    }


    /**
     * 数据库 别名替换 并 格式化指定字段类型
     * @param pList
     * @param cMap {cName:"字段名称",type:"字符类型",format:"格式类型"}
     * @return
     */
    public static List<Map<String,Object>> DbAsFormat (List<Map<String,Object>> pList,Map<String,Map<String,String>> cMap){
        if(pList!=null && pList.size()>0){
            List<Map<String,Object>> rList = new ArrayList<>();
            for (int i = 0; i < pList.size(); i++) {
                Map<String,Object> obj = pList.get(i);
                Map<String,Object> newMap = new HashMap<>();
                //循环替换 列明 给列明取个别名
                for(Map.Entry<String,Object > entry:obj.entrySet()){
                    String cName = entry.getKey();
                    Object Value = entry.getValue();
                    for(Map.Entry<String,Map<String,String> > entry1:cMap.entrySet()){
                        if(entry.getKey().equals(entry1.getKey())){
                            Map<String, String> fMap = entry1.getValue();
                            cName = fMap.get("cName");
                            String type = fMap.get("type");
                            String format = fMap.get("format")!=null?fMap.get("format"):null;
                            Value = ObjConvert(type,Value,format);
                        }
                    }
                    newMap.put(cName,Value);
                }
                rList.add(newMap);
            }
            return rList;
        }else {
            return null;
        }
    }

    /** 将TS转化为string类型至前段再做操作
     * 确保pList中有ts字段
     * 2022年11月30日13:58:37
     * @param pList
     * @return
     */
    public static List<Map<String,Object>> CZCUpdTsToString (List<Map<String,Object>> pList){

        List<Map<String,Object>> rList = new ArrayList<>();
        if(pList!=null && pList.size()>0){
            for (int i = 0; i < pList.size(); i++) {
                Map<String, Object> MapOne = pList.get(i);
                if (MapOne.get("ts") != null){
//                    Object ts = MapOne.get("ts");
//                    String format = sdf.format(ts);
//                    String format1 = sdfutc.format(ts);
                    MapOne.put("ts",MapOne.get("ts").toString());
                    rList.add(MapOne);
                }else{
//                    出错 不做修改
                    rList.add(MapOne);
                }
            }
        }

        return rList;
    }
    public static List<Map<String,Object>> CZCUpdTimeStampToString (List<Map<String,Object>> pList,Map<String,Object> Kmap){
        Object key = Kmap.get("key");

        List<Map<String,Object>> rList = new ArrayList<>();
        if(pList!=null && pList.size()>0){
            for (int i = 0; i < pList.size(); i++) {
                Map<String, Object> MapOne = pList.get(i);
                if (MapOne.get(key) != null){
                    MapOne.put(key.toString(),MapOne.get(key).toString());
                    rList.add(MapOne);
                }else{
//                    出错 不做修改
                    rList.add(MapOne);
                }
            }
        }

        return rList;
    }

    /**
     * 格式化字段
     * @param type
     * @param obj
     * @param format
     * @return
     */
    public static Object ObjConvert(String type,Object obj,String format){
        Object rObj = null;
        try {
            switch (type){
                case "String":
                    rObj = obj;
                    break;
                case "Double":
                    rObj = Double.parseDouble(obj.toString());
                    break;
                case "Integer":
                    rObj = Integer.parseInt(obj.toString());
                    break;
                case "Date":
                    String str = obj.toString();
                    rObj = VeDate.strToDate(str,format);
                    break;
            }
        }catch (Exception e){
            System.out.println("ObjConvert 异常 type "+type+" , obj "+obj+" , format "+format+" "+e.getMessage());
        }
        return rObj;
    }


    /**
     * 切割成 多个 list
     * @param list
     * @param subNum
     * @return
     * @param <T>
     */
    public static <T> List<List<T>> splistList(List<T> list,int subNum) {
        List<List<T>> tNewList = new ArrayList<List<T>>();
        int priIndex = 0;
        int lastPriIndex = 0;
        int insertTimes = list.size()/subNum;
        List<T> subList = new ArrayList<>();
        for (int i = 0;i <= insertTimes;i++) {
            priIndex = subNum*i;
            lastPriIndex = priIndex + subNum;
            if (i == insertTimes) {
                subList = list.subList(priIndex,list.size());
            } else {
                subList = list.subList(priIndex,lastPriIndex);
            }
            if (subList.size() > 0) {
                tNewList.add(subList);
            }
        }
        return tNewList;
    }




    public static void main(String[] args) {
//
//        System.out.println(ListMapUtils.ObjConvert("String","123123123",null));
//        System.out.println(ListMapUtils.ObjConvert("Double","13123.85",null));
//        System.out.println(ListMapUtils.ObjConvert("Integer","123123123",null));
//        System.out.println(ListMapUtils.ObjConvert("Date","2022-11-01 14:52:00.000","yyyy-MM-dd HH:mm:ss"));


        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> car_1 = new HashMap<>();
        Map<String,Object> car_2 = new HashMap<>();
        Map<String,Object> car_3 = new HashMap<>();
        Map<String,Object> car_4 = new HashMap<>();
        Map<String,Object> car_5 = new HashMap<>();
        car_1.put("iccid","8986032246200928593");
        car_1.put("total_show_flow",52.3);
        car_1.put("create_time","2022-11-04 09:05:46");


        car_2.put("iccid","8986032246200928593");
        car_2.put("total_show_flow",12.3);
        car_2.put("create_time","2022-11-07 06:53:39");

        car_3.put("iccid","8986112028901278312");
        car_3.put("total_show_flow","1.32");
        car_3.put("create_time","2022-11-07 07:01:21");

        car_4.put("iccid","8986112028901288121");
        car_4.put("total_show_flow","156");
        car_4.put("create_time","2022-11-04 08:19:57");

        car_5.put("iccid","8986032246200926666");
        car_5.put("total_show_flow",52.3);
        car_5.put("create_time","2022-11-07 09:19:40");

        list.add(car_1);
        list.add(car_2);
        list.add(car_3);
        list.add(car_4);
        list.add(car_5);

//        Map<String,String> cMap = new HashMap<>();
//        cMap.put("total_show_flow","used");
//
//        System.out.println(ListMapUtils.DbAs(list,cMap));



        Map<String,Map<String,String>> cFMap = new HashMap<>();
        Map<String,String> map1 = new HashMap<>();
        map1.put("cName","used");
        map1.put("type","Double");

        Map<String,String> map2 = new HashMap<>();
        map2.put("cName","cTime");
        map2.put("type","Date");
        map2.put("format","yyyy-MM");


        cFMap.put("total_show_flow",map1);
        cFMap.put("create_time",map2);

        System.out.println(ListMapUtils.DbAsFormat(list,cFMap));

        //System.out.println(list);
        //System.out.println(ListMapUtils.GruoupByKey(list,"iccid"));

        // System.out.println(ListMapUtils.GruoupByKey(list,"iccid","total_show_flow"));

    }

//    public static void main(String[] args) {
////       List<Map<String,Object>> list = new ArrayList<>();
////        HashMap< String,Object> map = new HashMap<>();
////        map.put("cd_code","YiDOng_Ec");
////        map.put("codeon","12007");
////        map.put("message","MSISDN号不是所查询的集团下的用户");
////        map.put("count(1)","5");
////        HashMap< String,Object> map1 = new HashMap<>();
////        map1.put("cd_code","YiDOng_Ec");
////        map1.put("codeon","12008");
////        map1.put("message","MSISDN号不是所查询的集团下的用户");
////        map1.put("count(1)","1");
////        list.add(map);
////        list.add(map1);
////        System.out.println(list);
////        System.out.println("↑List数据");
////        HashMap<String, String> map2 = new HashMap<>();
////        map2.put("count(1)","ct_num");
////
////        List<Map<String, Object>> maps = DbAs(list, map2);
////        System.out.println("最终数据maps↓");
////        System.out.println(maps);
//        Date now = VeDate.getNow();
//        for (int i = 0; i < 100000; i++) {
//
//        }
//        for (int j = 0; j < 100000; j++) {
//
//        }
//        for (int k = 0; k < 100000; k++) {
//
//        }
//        Date now1 = VeDate.getNow();
//        System.out.println(now);
//        System.out.println(now1);
//
//    }

}
