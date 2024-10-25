package com.yunze.web.service;

import java.util.Map;
//白名单接口
public interface White {
    //查询 语音
    public Map<String,Object> selVoice (Map<String,Object> Pmap);
    //查询通道 账号密码新细
    public Map<String,Object> selRouteCreate (Map<String,Object> Pmap);
    //设置 白名单
    public Map<String,Object> setWhite (Map<String,Object> Pmap);
    //修改 白名单
    public Map<String,Object> updWhite (Map<String,Object> Pmap);
    //删除 白名单
    public Map<String,Object> delWhite (Map<String,Object> Pmap);
    //获取 信息列表
    public Map<String,Object> List (Map<String,Object> Pmap);
    //查询 资费组是否允许访问 微信端
    public Map<String,Object> getPre (Map<String,Object> Pmap);

}
