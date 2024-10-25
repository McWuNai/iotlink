package com.yunze.common.constant;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.yunze.common.core.redis.FindSysConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 阿里云 短信发送配置
 * */
@Component
public class ALiYunSendMessage {


    private  String accessKeyId = "";//账号 阿里云短信服务账号
    private  String secret = "";//密码 阿里云短信服务密码
    private  String SignName = "";//签名


    @Resource
    private FindSysConfig findSysConfig;

    public void initConfig(){
        String accessKeyId_key = "aliyun.sdenMessage.accessKeyId";
        String secret_key = "aliyun.sdenMessage.secret";
        String SignName_key = "aliyun.sdenMessage.SignName";
        accessKeyId = findSysConfig.getSysConfig(accessKeyId_key,1);
        secret = findSysConfig.getSysConfig(secret_key,1);
        SignName = findSysConfig.getSysConfig(SignName_key,1);

    }


    /**
     *
     * @param phoneNum 手机号
     * @param templateCode 模板CODE
     * @param code 发送内容主题
     * @return
     */
    public boolean send(String phoneNum, String templateCode, Map<String, Object> code) {
        initConfig();


        // 链接阿里云  这个不需要动 cn-hangzhou  accessKeyId : 账号  secret: 密码
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",accessKeyId,secret);
        IAcsClient client = new DefaultAcsClient(profile);

        // 构建请求
        CommonRequest request = new CommonRequest();

        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");//官方说不要动
        request.setVersion("2017-05-25");//官方说不要动
        request.setAction("SendSms");//事件名称

        // 自定义的参数 （手机号,验证码,签名,模板!)
        request.putQueryParameter("PhoneNumbers",phoneNum);//手机号
        request.putQueryParameter("SignName",SignName);//签名
        request.putQueryParameter("TemplateCode",templateCode);//模板CODE

        // 构建一个短信的验证码
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(code));

        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();//成功
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }
}


















