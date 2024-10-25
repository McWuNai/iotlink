package com.yunze.cn.entity;


import java.util.Map;

public class WxConfig
{

    public WxConfig(Map<String,Object> map ) {
        this.id = map.get("id")!=null?map.get("id").toString():"";
        this.appId = map.get("appId")!=null?map.get("appId").toString():"";
        this.appSecret = map.get("appSecret")!=null?map.get("appSecret").toString():"";
        this.appToken = map.get("appToken")!=null?map.get("appToken").toString():"";
        this.originId = map.get("originId")!=null?map.get("originId").toString():"";
        this.mchId = map.get("mchId")!=null?map.get("mchId").toString():"";
        this.paternerKey = map.get("paternerKey")!=null?map.get("paternerKey").toString():"";
        this.indexUrl = map.get("indexUrl")!=null?map.get("indexUrl").toString():"";
        this.backUrl = map.get("backUrl")!=null?map.get("backUrl").toString():"";
        this.status = map.get("status")!=null?map.get("status").toString():"";
        this.createTime = map.get("createTime")!=null?map.get("createTime").toString():"";
        this.updateTime = map.get("updateTime")!=null?map.get("updateTime").toString():"";
        this.createUser = map.get("createUser")!=null?map.get("createUser").toString():"";
        this.companyId = map.get("companyId")!=null?map.get("companyId").toString():"";
        this.agentId = map.get("agentId")!=null?map.get("agentId").toString():"";
        this.nativeUrl = map.get("nativeUrl")!=null?map.get("nativeUrl").toString():"";
        this.authTxt = map.get("authTxt")!=null?map.get("authTxt").toString():"";
    }




    public WxConfig(String id, String appId, String appSecret, String appToken, String originId, String mchId, String paternerKey, String indexUrl, String backUrl, String status, String createTime, String updateTime, String createUser, String companyId, String agentId, String nativeUrl, String authTxt) {
        this.id = id;
        this.appId = appId;
        this.appSecret = appSecret;
        this.appToken = appToken;
        this.originId = originId;
        this.mchId = mchId;
        this.paternerKey = paternerKey;
        this.indexUrl = indexUrl;
        this.backUrl = backUrl;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createUser = createUser;
        this.companyId = companyId;
        this.agentId = agentId;
        this.nativeUrl = nativeUrl;
        this.authTxt = authTxt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getPaternerKey() {
        return paternerKey;
    }

    public void setPaternerKey(String paternerKey) {
        this.paternerKey = paternerKey;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

    public String getBackUrl() {
        return backUrl;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getNativeUrl() {
        return nativeUrl;
    }

    public void setNativeUrl(String nativeUrl) {
        this.nativeUrl = nativeUrl;
    }

    public String getAuthTxt() {
        return authTxt;
    }

    public void setAuthTxt(String authTxt) {
        this.authTxt = authTxt;
    }

    private String id;
    private String appId;
    private String appSecret;
    private String appToken;
    private String originId;
    private String mchId;
    private String paternerKey;
    private String indexUrl;
    private String backUrl;
    private String status;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String companyId;
    private String agentId;
    private String nativeUrl;
    private String authTxt;



}
