package com.yunze.wechat.handler;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.business.config.WxConfigInit;
import com.yunze.business.entity.WxUser;
import com.yunze.business.service.impl.WxUserServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Component
public class UnsubscribeHandler extends AbstractHandler {

	@Resource
	private WxUserServiceImpl wechatUserService;
	@Resource
	private RedisUtil redisUtil;
	@Resource
	private WxConfigInit wxConfigInit;

	@Override
	public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,
			WxSessionManager sessionManager) throws WxErrorException {
		String openId = wxMessage.getFromUser();
		this.logger.info("取消关注用户 OPENID: " + openId);

		String appId=(String)redisUtil.get(wxMessage.getFromUser());
		// 获取微信用户基本信息
		wxMpService=wxConfigInit.wxMpService().switchoverTo(appId);
		// 获取微信用户基本信息
		WxMpUser userWxInfo = wxMpService.getUserService().userInfo(wxMessage.getFromUser(), null);
		String openid=userWxInfo.getOpenId();

		int count=wechatUserService.getCountOfWechatUser(openid);
		if(count==0){
			return null;
		}else{
			UpdateWrapper<WxUser> updateWrapper=new UpdateWrapper<>();
			updateWrapper.set("status",0);
			updateWrapper.eq("openid",openId);
			wechatUserService.update(updateWrapper);
		}
		return null;
	}

}
