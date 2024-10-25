package com.yunze.iotapi.service;


import java.util.Map;

public interface IOpenApi {


    public Map<String,Object> simDataUsage(Map<String, Object> Param);

    public Map<String,Object> simStatus(Map<String, Object> Param);


    public Map<String,Object> changeSimStatus(Map<String, Object> Param,String Is_Stop);


    public Map<String,Object> simApnFunction(Map<String, Object> Param,String Str_StatusBreak);


    public Map<String,Object> subscribe(Map<String, Object> ParamMap, Map<String, Object> agentAccount);






}
