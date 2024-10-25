package com.yunze.common.mapper.yunze;

import org.yaml.snakeyaml.events.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface YzSysLogsMapper {

   /**
    * 查询所有
    * */
   public List<Map<String,Object>> Listwhole();

   /**
    * 备份删除
    * */
   public int deleteById(Map<String,Object> map);

   /**
    *查询
    * @param map
    * @return
    */
   public List<Map<String,Object>> selMap(Map map);

   /**
    * 查询总数
    * @param map
    * @return
    */
   public int selMapCount(Map map);

   /**
    * 通过id查询
    * request_args
    * response_args
    *
    * @param map*/
   public Map<String,Object> getById(Map<String, Object> map);


   /**
    * 删除指定日期之前的数据
    * @param map
    * @return
    */
   public int del(Map map);


   /**
    * 删除定时任务日志
    * @param map
    * @return
    */
   public int delJobLog(Map map);

   /**
    * 删除 操作日志记录
    * @param map
    * @return
    */
   public int delOperLog(Map map);



}
















