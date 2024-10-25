package com.yunze.web.controller.common;

import com.yunze.common.constant.ALiYunSendMessage;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin // 跨域支持
@RequestMapping
public class SmsApiController {



    @Resource
    private RedisCache redisCache;
    @Resource
    private ALiYunSendMessage aLiYunSendMessage;


    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;

    @GetMapping("/send/{phone}")
    public String code(@PathVariable String phone){
        int H = 24;
        // 调用发送方法
        Object code = redisCache.getCacheObject("login_"+phone);
        if (code!=null && code.toString().length()>0) { //不等于 空
            return "五分钟内不能重复获取！";
        }

        Object count = redisCache.getCacheObject("login_SendMessageCount_"+phone);
        Integer sendCount = count!=null && count.toString().length()>0?Integer.parseInt(count.toString()):0;
        Map<String,Object> map = new HashMap<>();
        map.put("config_key","login.SendMessageCount");
        int maxSendCount = yzWxByProductAgentMapper.SendCount(map);//数据库查询配置参数
        if(sendCount >= maxSendCount){
            return "["+H+"]小时内不能超过["+maxSendCount+"]次短信验证！";
        }

        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            builder.append(random.nextInt(10));//实现验证码 6 位数字
        }
        // 生成验证码并存储到redis 中
        code = builder.toString();

        HashMap<String,Object> param = new HashMap<>();
        param.put("code",code);

        boolean isSend = aLiYunSendMessage.send(phone,"SMS_242220249",param);

        if(isSend){
            sendCount +=1;
            redisCache.setCacheObject("login_SendMessageCount_"+phone, sendCount, H, TimeUnit.HOURS);//设置X小时过期

            //设置 5 分钟过期
            redisCache.setCacheObject("login_"+phone, code, 5, TimeUnit.MINUTES);//设置5分钟过期
            return "验证成功 请查看手机验证码";

        }else {
            return "发送失败";
        }
    }
}
