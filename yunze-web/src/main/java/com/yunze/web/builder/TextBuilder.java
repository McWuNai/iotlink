package com.yunze.web.builder;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTransferKefuMessage;

/**
 * @author
 */
public class TextBuilder extends AbstractBuilder {

	@Override
	public WxMpXmlOutMessage build(String content, WxMpXmlMessage wxMessage, WxMpService service) {
		WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(content).fromUser(wxMessage.getToUser())
				.toUser(wxMessage.getFromUser()).build();
		return m;
	}

	/**
	 * @Author:
	 * @Return: 客服接入
	 * @Description:  
	 * @Date: 2021/11/19 0019 11:11
	 */ 
	public WxMpXmlOutTransferKefuMessage buildKf(String content, WxMpXmlMessage wxMessage, WxMpService service) {
		WxMpXmlOutTransferKefuMessage m = WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
				.toUser(wxMessage.getFromUser()).build();
		return m;
	}

}
