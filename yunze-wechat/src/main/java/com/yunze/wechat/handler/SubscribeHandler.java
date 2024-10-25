package com.yunze.wechat.handler;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yunze.apiCommon.config.RedisUtil;
import com.yunze.business.entity.WxUser;
import com.yunze.business.service.impl.WxUserServiceImpl;
import com.yunze.wechat.builder.TextBuilder;
import com.yunze.business.config.WxConfigInit;
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
 * @Return: 关注用户事件
 */
@Component
public class SubscribeHandler extends AbstractHandler {

    @Resource
	private WxUserServiceImpl wechatUserService;
    @Resource
	private RedisUtil redisUtil;
    @Resource
	private WxConfigInit wxConfigInit;

	@Override
	public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService weixinService,
			WxSessionManager sessionManager) throws WxErrorException {

		this.logger.info("新关注用户 OPENID: " + wxMessage.getFromUser());

		String appId=(String)redisUtil.get(wxMessage.getFromUser());
		// 获取微信用户基本信息
		WxMpService wxMpService=wxConfigInit.wxMpService().switchoverTo(appId);
		WxMpUser userWxInfo = wxMpService.getUserService().userInfo(wxMessage.getFromUser(), null);
		boolean flag=false;
		String openId=null;
		if (userWxInfo != null) {
			openId =userWxInfo.getOpenId();

			int count=wechatUserService.getCountOfWechatUser(openId);
			if(count!=0){
				UpdateWrapper<WxUser> updateWrapper =new UpdateWrapper<>();
				updateWrapper.set("status",1);
				updateWrapper.set("sex",userWxInfo.getSex());
				updateWrapper.eq("openid",openId);
				flag=wechatUserService.update(updateWrapper);
			}else{
				String userName=userWxInfo.getNickname();
				int sex=userWxInfo.getSex();
				String headImage=userWxInfo.getHeadImgUrl();
				String city=userWxInfo.getCity();
				String province=userWxInfo.getProvince();
				String country=userWxInfo.getCountry();

				WxUser wxUser =new WxUser();
				wxUser.setCity(city);
				wxUser.setCountry(country);
				wxUser.setHeadImage(headImage);
				wxUser.setIccid(null);
				wxUser.setOpenid(openId);
				wxUser.setProvince(province);
				wxUser.setSex(sex);
				wxUser.setUserName(userName);
				wxUser.setStatus(1);
				wxUser.setAppId(appId);
				flag=wechatUserService.save(wxUser);

			}
			if(flag){
				WxMpXmlOutMessage message =new TextBuilder().build("感谢关注", wxMessage, weixinService);
				return message;
			}



		}
		return null;



	}

	/**
	 * auther:
	 * return: 获取当前的appid
	 * 描述：
	 * 时间： 2021/11/28 22:13
	 */
	public String getAppid(String appid){
		return appid;
	}

	/**
	 * 处理特殊请求，比如如果是扫码进来的，可以做相应处理
	 */
	private WxMpXmlOutMessage handleSpecial(WxMpXmlMessage wxMessage) throws Exception {
		System.out.println("我是通过扫码进来的");
		// TODO
		return null;
	}

}
