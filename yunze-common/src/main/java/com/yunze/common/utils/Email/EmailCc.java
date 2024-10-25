package com.yunze.common.utils.Email;

import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.automationCC.YzAutomationCcMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.utils.yunze.VeDate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 邮件抄送
 *
 * @Auther: zhang feng
 * @Date: 2021/09/26/14:16
 * @Description:
 */
@Component
public class EmailCc {
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;
    @Resource
    private RedisCache redisCache;
    //findConfig


    /**
     * 下单发货通知 抄送 默认模板
     *
     * @param CCMap
     * @return
     */
    public Map<String, Object> ShoppingDelivery_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        String info = CCMap.get("info") != null ? CCMap.get("info").toString() : "";
        Map<String, Object> Text = EmailSend.Shopping_text(CCMap.get("ord_no").toString(), CCMap.get("create_time").toString(), CCMap.get("shipping_Phone").toString(),
                CCMap.get("shipping_user").toString(), CCMap.get("province").toString(), CCMap.get("city").toString(), CCMap.get("district").toString(),
                CCMap.get("address").toString(), CCMap.get("by_count").toString(), CCMap.get("product_name").toString(), info);

        try {
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("您有新的发货订单");
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("ShoppingDelivery_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }

    /**
     * 2022年8月16日15:30:26
     * 邮件抄送给运维
     */
    public Map<String, Object> ServiceOperation(Map<String, Object> Msg, String emailDept) {
        String userName = "", password = "";
        Boolean bool = false;
        Map<String, String> configMap = getConfig();
        HashMap<String, Object> rmap = new HashMap<>();
        userName = configMap.get("userName");
        password = configMap.get("password");
        //调用emailsend获取邮件模板 参数 :msg 里面的数据为个数
        Map<String, Object> text = EmailSend.ServiceOperation_text(Msg);
        MimeMessageDTO mimeMessageDTO = new MimeMessageDTO();
        mimeMessageDTO.setSentDate(new Date());
        mimeMessageDTO.setText(text.get("template").toString());
        mimeMessageDTO.setSubject("服务快到期通知");
        try {
            bool = MailUtil.sendEmail(userName, password, emailDept, mimeMessageDTO);
            System.out.println("===>>>邮件发送成功");
            rmap.put("bool", bool);
        } catch (Exception e) {
            System.out.println("===>>>邮件发送失败");
            rmap.put("bool", bool);
        }


        return rmap;
    }


    /**
     * 物联网卡 已订购有用量状态异常 抄送 默认模板
     *
     * @param CCMap
     * @return
     */
    public Map<String, Object> CardStatusAbnormal_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        String Count = CCMap.get("Count").toString();
        Map<String, Object> Text = EmailSend.CardUsefulAmountDowntime_text(CCMap.get("taskId").toString(), VeDate.getStringDate(), Count);
        try {
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("已订购用量状态已停机数量[" + Count + "]");
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("CardStatusAbnormal_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }


    /**
     * 物联网卡 未划分资费组状态异常 抄送 默认模板
     *
     * @param CCMap
     * @return
     */
    public Map<String, Object> NotDividedIntoTariffGroups_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        String Count = CCMap.get("Count").toString();

        Map<String, Object> emailMap = (Map<String, Object>) CCMap.get("emailMap");
        Map<String, Object> Text = EmailSend.NotDividedIntoTariffGroups_text(emailMap, VeDate.getStringDate(), Count);
        try {
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("未划分资费组数量[" + Count + "]");
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("NotDividedIntoTariffGroups_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }

    /**
     * 物联网卡 未划分通道 抄送 默认模板
     *
     * @param CCMap
     * @return
     */
    public Map<String, Object> UndividedChannel_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        String Count = CCMap.get("Count").toString();

        Map<String, Object> emailMap = (Map<String, Object>) CCMap.get("emailMap");
        Map<String, Object> Text = EmailSend.UndividedChannel_text(emailMap, VeDate.getStringDate(), Count);
        try {
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("未划分通道[" + Count + "]");
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("UndividedChannel_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }

    /**
     * 自动化任务邮件抄送  上游套餐超过百分比 抄送 默认模板
     *
     * @param
     * @return
     */
    public Map<String, Object> usageMailReminder_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        String Count = CCMap.get("Count").toString();
        Map<String, Object> Text = EmailSend.usageMailReminder_text(CCMap.get("taskId").toString(), VeDate.getStringDate(), Count);
        try {
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("上游套餐超过百分比数量[" + Count + "]");
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("usageMailReminder_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }


    /**
     * 物联网卡 归属为总平台 抄送 默认模板
     *
     * @param CCMap
     * @return
     */
    public Map<String, Object> CardConsumption_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        String Count = CCMap.get("Count").toString();
        Map<String, Object> findCardMap = (Map<String, Object>) CCMap.get("findCardMap");
        Map<String, Object> Text = EmailSend.CardConsumption_text(findCardMap, VeDate.getStringDate(), Count);
        try {
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("归属为总平台有用量卡总数[" + Count + "]");
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("CardConsumption_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }

    /**
     * 2022年8月10日15:56:40
     * 根据服务是否快到期进行发送邮件提醒
     *
     * @return
     */
    public Map<String, Object> sendEmailForDueSoon(Map map, Map pMap) throws Exception {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");


        Map<String, Object> textMap = EmailSend.CardDueSoonEmailSend(map.get("dept_name").toString(), map.get("count").toString(), pMap, com.yunze.apiCommon.utils.VeDate.getStringDate());

        MimeMessageDTO mimeMessageDTO = new MimeMessageDTO();
        mimeMessageDTO.setText(textMap.get("template").toString());
        mimeMessageDTO.setSentDate(new Date());
        mimeMessageDTO.setSubject("服务即将到期通知-" + map.get("dept_name").toString() + "-" + map.get("count").toString());
        Map<String, Object> returnMap = new HashMap<>();

        boolean b = MailUtil.sendEmail(userName, password, map.get("email").toString(), mimeMessageDTO);
        returnMap.put("bool", b);


        return returnMap;
    }


    /**
     * 获取配置的邮箱 账号 密码
     *
     * @return
     */
    public Map<String, String> getConfig() {
        String userName = "", password = "";
        Map<String, String> configMap = new HashMap<String, String>();
        Map<String, Object> userNameMap = new HashMap<>();
        Map<String, Object> passwordMap = new HashMap<>();
        Object Email = redisCache.getCacheObject("ccConfig.Email");
        Object EmailPwd = redisCache.getCacheObject("ccConfig.EmailPwd");
        if (Email != null && Email.toString().length() > 0) {
            userName = Email.toString();
        } else {
            userNameMap.put("config_key", "yunze.ccConfig.Email");
            userName = yzWxByProductAgentMapper.findConfig(userNameMap);
            redisCache.setCacheObject("ccConfig.Email", userName, 6, TimeUnit.HOURS);//6 小时 缓存
        }
        if (EmailPwd != null && EmailPwd.toString().length() > 0) {
            password = EmailPwd.toString();
        } else {
            passwordMap.put("config_key", "yunze.ccConfig.EmailPwd");
            password = yzWxByProductAgentMapper.findConfig(passwordMap);
            redisCache.setCacheObject("ccConfig.EmailPwd", password, 6, TimeUnit.HOURS);//6 小时 缓存
        }
        configMap.put("userName", userName);
        configMap.put("password", password);
        return configMap;
    }


    /**
     * 物联网卡 API同步数据错误抄送 抄送 默认模板
     *
     * @param CCMap
     * @param SendEmail
     * @return
     */
    public Map<String, Object> CardApiSynError_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        String describe = CCMap.get("describe").toString();
        String heartext = CCMap.get("heartext").toString();
        String createTime = VeDate.getStringDate();
        Map<String, Object> Text = EmailSend.CardApiSynError_text(heartext, CCMap.get("taskId").toString(), createTime, describe);
        try {
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("[告警]-[" + EmailSend.url + "]-[" + createTime + "] " + heartext);
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("CardApiSynError_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }


    /**
     * 自动化任务邮件抄送  续费申请通知 抄送 默认模板
     *
     * @param
     * @return
     */
    public Map<String, Object> ApplicationForRenewal_default(Map<String, Object> CCMap, String SendEmail) {
        String userName = "", password = "";
        Map<String, String> configMap = getConfig();
        userName = configMap.get("userName");
        password = configMap.get("password");

        Map<String, Object> rmap = new HashMap<String, Object>();
        boolean bool = true;
        Map<String, Object> parMap = (Map<String, Object>) CCMap.get("parMap");
        String create_time = CCMap.get("create_time").toString();
        String dept_name = CCMap.get("dept_name").toString();
        String card_sumCount = CCMap.get("card_sumCount").toString();
        String amount = CCMap.get("amount").toString();
        String info = CCMap.get("info").toString();
        String title = CCMap.get("title").toString();

        try {
            Map<String, Object> Text = EmailSend.ApplicationForRenewal_text(parMap, create_time, dept_name, card_sumCount, amount, info, title);
            MimeMessageDTO mimeDTO = new MimeMessageDTO();
            mimeDTO.setSentDate(new Date());
            mimeDTO.setSubject("[" + dept_name + "] - 续费申请待处理[" + dept_name + "]");
            mimeDTO.setText(Text.get("template").toString());
            bool = MailUtil.sendEmail(userName, password, SendEmail, mimeDTO);
        } catch (Exception e) {
            System.out.println("usageMailReminder_default 异常 " + e.getMessage());
            bool = false;
        }
        rmap.put("bool", bool);
        return rmap;
    }


}
